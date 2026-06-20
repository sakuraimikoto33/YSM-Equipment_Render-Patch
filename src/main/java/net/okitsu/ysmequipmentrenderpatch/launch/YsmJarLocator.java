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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class YsmJarLocator {
    private static final String MODS_TOML = "META-INF/neoforge.mods.toml";
    private static final String YSM_MOD_ID = "yes_steve_model";
    private static final Pattern VERSION_PATTERN = Pattern.compile("version\\s*=\\s*\"([^\"]+)\"");

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
        return readModsToml(jarPath).flatMap(toml -> {
            Matcher matcher = VERSION_PATTERN.matcher(toml);
            return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
        });
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
        return readModsToml(jarPath)
                .map(toml -> toml.contains("modId=\"" + YSM_MOD_ID + "\""))
                .orElse(false);
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
}
