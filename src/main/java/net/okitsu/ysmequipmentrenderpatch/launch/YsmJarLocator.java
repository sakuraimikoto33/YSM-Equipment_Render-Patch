package net.okitsu.ysmequipmentrenderpatch.launch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class YsmJarLocator {
    private static final String MODS_TOML = "META-INF/neoforge.mods.toml";
    private static final String YSM_MOD_ID = "yes_steve_model";
    private static final Pattern STRING_ASSIGNMENT_PATTERN =
            Pattern.compile("^([A-Za-z0-9_.-]+)\\s*=\\s*([\"'])(.*?)\\2\\s*$");

    private YsmJarLocator() {
    }

    static Optional<Path> locate(Path gameDirectory) {
        for (Path candidate : candidates(gameDirectory)) {
            if (isYsmJar(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    static Optional<String> readYsmVersion(Path jarPath) {
        return readYsmModInfo(jarPath).flatMap(ModInfo::version);
    }

    private static Set<Path> candidates(Path gameDirectory) {
        Set<Path> candidates = new LinkedHashSet<>();
        addJarDirectory(candidates, gameDirectory.resolve("mods"));
        addJarDirectory(candidates, Path.of(".").toAbsolutePath().normalize().resolve("libs"));
        addJarDirectory(candidates, Path.of(".").toAbsolutePath().normalize().resolve("libs").resolve("ExternalProjects"));

        String classPath = System.getProperty("java.class.path", "");
        for (String entry : classPath.split(Pattern.quote(System.getProperty("path.separator")))) {
            if (!entry.isBlank()) {
                addJar(candidates, Path.of(entry));
            }
        }
        return candidates;
    }

    private static void addJarDirectory(Set<Path> candidates, Path directory) {
        if (!Files.isDirectory(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory, 2)) {
            paths.filter(Files::isRegularFile).forEach(path -> addJar(candidates, path));
        } catch (IOException exception) {
            // Ignore unreadable candidate directories. Missing YSM will disable the patch safely.
        }
    }

    private static void addJar(Set<Path> candidates, Path path) {
        if (path.getFileName() != null && path.getFileName().toString().endsWith(".jar")) {
            candidates.add(path.toAbsolutePath().normalize());
        }
    }

    private static boolean isYsmJar(Path jarPath) {
        return readYsmModInfo(jarPath).isPresent();
    }

    private static Optional<ModInfo> readYsmModInfo(Path jarPath) {
        return readModsToml(jarPath).flatMap(toml -> findModInfo(toml, YSM_MOD_ID));
    }

    private static Optional<ModInfo> findModInfo(String toml, String targetModId) {
        boolean inModsBlock = false;
        String modId = null;
        String version = null;

        for (String rawLine : toml.split("\\R")) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }

            Optional<String> arrayTable = readArrayTableName(line);
            if (arrayTable.isPresent()) {
                Optional<ModInfo> current = finishModBlock(inModsBlock, modId, version, targetModId);
                if (current.isPresent()) {
                    return current;
                }

                inModsBlock = "mods".equals(arrayTable.get());
                modId = null;
                version = null;
                continue;
            }

            Optional<String> table = readTableName(line);
            if (table.isPresent()) {
                Optional<ModInfo> current = finishModBlock(inModsBlock, modId, version, targetModId);
                if (current.isPresent()) {
                    return current;
                }

                inModsBlock = false;
                modId = null;
                version = null;
                continue;
            }

            if (!inModsBlock) {
                continue;
            }

            java.util.regex.Matcher matcher = STRING_ASSIGNMENT_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            String key = matcher.group(1);
            String value = matcher.group(3);
            if ("modId".equals(key)) {
                modId = value;
            } else if ("version".equals(key)) {
                version = value;
            }
        }

        return finishModBlock(inModsBlock, modId, version, targetModId);
    }

    private static Optional<ModInfo> finishModBlock(
            boolean inModsBlock,
            String modId,
            String version,
            String targetModId
    ) {
        if (inModsBlock && targetModId.equals(modId)) {
            return Optional.of(new ModInfo(modId, Optional.ofNullable(version)));
        }
        return Optional.empty();
    }

    private static Optional<String> readArrayTableName(String line) {
        if (line.startsWith("[[") && line.endsWith("]]")) {
            return Optional.of(line.substring(2, line.length() - 2).trim());
        }
        return Optional.empty();
    }

    private static Optional<String> readTableName(String line) {
        if (line.startsWith("[") && line.endsWith("]") && !line.startsWith("[[")) {
            return Optional.of(line.substring(1, line.length() - 1).trim());
        }
        return Optional.empty();
    }

    private static String stripComment(String line) {
        char quote = 0;
        boolean escaped = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (quote != 0) {
                if (quote == '"' && character == '\\' && !escaped) {
                    escaped = true;
                    continue;
                }
                if (character == quote && !escaped) {
                    quote = 0;
                }
                escaped = false;
            } else if (character == '"' || character == '\'') {
                quote = character;
            } else if (character == '#') {
                return line.substring(0, index);
            }
        }
        return line;
    }

    private static Optional<String> readModsToml(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(MODS_TOML);
            if (entry == null) {
                return Optional.empty();
            }
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                return Optional.of(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    private record ModInfo(String modId, Optional<String> version) {
    }
}
