package net.okitsu.ysmequipmentrenderpatch.runtime;

import net.okitsu.ysmequipmentrenderpatch.YsmEquipmentRenderPatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public final class YsmRuntimeSymbolCache {
    private static final String CACHE_DIRECTORY = YsmEquipmentRenderPatch.MOD_ID;
    private static final String CACHE_FILE = "ysm-symbols-cache.json";
    private static final String CACHE_WARNING =
            "This file is generated automatically by YSM Equipment Render Patch. Do not edit it manually; changes may be overwritten.";

    private YsmRuntimeSymbolCache() {
    }

    public static Path cachePath(Path gameDirectory) {
        return gameDirectory.resolve("config").resolve(CACHE_DIRECTORY).resolve(CACHE_FILE);
    }

    public static Optional<YsmRuntimeSymbols> read(Path path) {
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(parse(Files.readString(path, StandardCharsets.UTF_8)))
                    .filter(YsmRuntimeSymbols::isComplete);
        } catch (IOException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    public static void write(Path path, YsmRuntimeSymbols symbols) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, toJson(symbols), StandardCharsets.UTF_8);
    }

    public static String sha256(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is not available", exception);
        }
        digest.update(Files.readAllBytes(path));
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String toJson(YsmRuntimeSymbols symbols) {
        StringBuilder builder = new StringBuilder(1024);
        builder.append("{\n");
        appendField(builder, "_warning", CACHE_WARNING).append(",\n");
        appendField(builder, "schemaVersion", symbols.schemaVersion).append(",\n");
        appendField(builder, "analyzerVersion", symbols.analyzerVersion).append(",\n");
        appendField(builder, "ysmVersion", symbols.ysmVersion).append(",\n");
        appendField(builder, "ysmJarSha256", symbols.ysmJarSha256).append(",\n");
        appendMethod(builder, "elytraLookup", symbols.elytraLookup).append(",\n");
        appendMethod(builder, "elytraRender", symbols.elytraRender).append(",\n");
        appendMethod(builder, "customPlayerGetEntity", symbols.customPlayerGetEntity).append(",\n");
        appendMethod(builder, "customPlayerGetCurrentModel", symbols.customPlayerGetCurrentModel).append(",\n");
        appendMethod(builder, "animatedModelRightWaistBones", symbols.animatedModelRightWaistBones).append(",\n");
        appendMethod(builder, "prepMatrixForLocator", symbols.prepMatrixForLocator).append(",\n");
        appendOptionalMethod(builder, "animatedModelHeadBones", symbols.animatedModelHeadBones).append(",\n");
        appendOptionalMethod(builder, "animatedModelAllHeadBone", symbols.animatedModelAllHeadBone).append(",\n");
        appendOptionalMethod(builder, "prepMatrixForBone", symbols.prepMatrixForBone).append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private static StringBuilder appendField(StringBuilder builder, String name, int value) {
        return builder.append("  \"").append(name).append("\": ").append(value);
    }

    private static StringBuilder appendField(StringBuilder builder, String name, String value) {
        return builder.append("  \"").append(name).append("\": \"").append(escape(value)).append('"');
    }

    private static StringBuilder appendMethod(StringBuilder builder, String name, YsmRuntimeSymbols.MethodRef methodRef) {
        builder.append("  \"").append(name).append("\": {\n");
        builder.append("    \"owner\": \"").append(escape(methodRef.owner)).append("\",\n");
        builder.append("    \"name\": \"").append(escape(methodRef.name)).append("\",\n");
        builder.append("    \"descriptor\": \"").append(escape(methodRef.descriptor)).append("\"\n");
        return builder.append("  }");
    }

    private static StringBuilder appendOptionalMethod(
            StringBuilder builder,
            String name,
            YsmRuntimeSymbols.MethodRef methodRef
    ) {
        if (methodRef == null) {
            return builder.append("  \"").append(name).append("\": null");
        }
        return appendMethod(builder, name, methodRef);
    }

    private static YsmRuntimeSymbols parse(String json) {
        YsmRuntimeSymbols symbols = new YsmRuntimeSymbols();
        symbols.schemaVersion = readInt(json, "schemaVersion");
        symbols.analyzerVersion = readInt(json, "analyzerVersion");
        symbols.ysmVersion = readString(json, "ysmVersion");
        symbols.ysmJarSha256 = readString(json, "ysmJarSha256");
        symbols.elytraLookup = readMethod(json, "elytraLookup");
        symbols.elytraRender = readMethod(json, "elytraRender");
        symbols.customPlayerGetEntity = readMethod(json, "customPlayerGetEntity");
        symbols.customPlayerGetCurrentModel = readMethod(json, "customPlayerGetCurrentModel");
        symbols.animatedModelRightWaistBones = readMethod(json, "animatedModelRightWaistBones");
        symbols.prepMatrixForLocator = readMethod(json, "prepMatrixForLocator");
        symbols.animatedModelHeadBones = readOptionalMethod(json, "animatedModelHeadBones");
        symbols.animatedModelAllHeadBone = readOptionalMethod(json, "animatedModelAllHeadBone");
        symbols.prepMatrixForBone = readOptionalMethod(json, "prepMatrixForBone");
        return symbols;
    }

    private static int readInt(String json, String field) {
        String rawValue = readRawValue(json, field);
        return Integer.parseInt(rawValue);
    }

    private static String readString(String json, String field) {
        String rawValue = readRawValue(json, field).trim();
        if (rawValue.length() < 2 || rawValue.charAt(0) != '"' || rawValue.charAt(rawValue.length() - 1) != '"') {
            throw new IllegalArgumentException("Expected string field: " + field);
        }
        return unescape(rawValue.substring(1, rawValue.length() - 1));
    }

    private static YsmRuntimeSymbols.MethodRef readMethod(String json, String field) {
        String body = readObject(json, field);
        return new YsmRuntimeSymbols.MethodRef(
                readString(body, "owner"),
                readString(body, "name"),
                readString(body, "descriptor")
        );
    }

    private static YsmRuntimeSymbols.MethodRef readOptionalMethod(String json, String field) {
        int fieldStart = json.indexOf('"' + field + '"');
        if (fieldStart < 0) {
            return null;
        }

        int colon = json.indexOf(':', fieldStart);
        if (colon < 0) {
            throw new IllegalArgumentException("Missing field separator: " + field);
        }

        int valueStart = skipWhitespace(json, colon + 1);
        if (json.startsWith("null", valueStart)) {
            return null;
        }
        return readMethod(json, field);
    }

    private static String readRawValue(String json, String field) {
        int fieldStart = json.indexOf('"' + field + '"');
        if (fieldStart < 0) {
            throw new IllegalArgumentException("Missing field: " + field);
        }
        int colon = json.indexOf(':', fieldStart);
        if (colon < 0) {
            throw new IllegalArgumentException("Missing field separator: " + field);
        }
        int valueStart = skipWhitespace(json, colon + 1);
        if (valueStart >= json.length()) {
            throw new IllegalArgumentException("Missing field value: " + field);
        }
        if (json.charAt(valueStart) == '"') {
            int valueEnd = findStringEnd(json, valueStart);
            return json.substring(valueStart, valueEnd + 1);
        }
        int valueEnd = valueStart;
        while (valueEnd < json.length() && ",\r\n}".indexOf(json.charAt(valueEnd)) < 0) {
            valueEnd++;
        }
        return json.substring(valueStart, valueEnd).trim();
    }

    private static String readObject(String json, String field) {
        int fieldStart = json.indexOf('"' + field + '"');
        if (fieldStart < 0) {
            throw new IllegalArgumentException("Missing object: " + field);
        }
        int objectStart = json.indexOf('{', fieldStart);
        if (objectStart < 0) {
            throw new IllegalArgumentException("Missing object body: " + field);
        }
        int depth = 0;
        for (int index = objectStart; index < json.length(); index++) {
            char character = json.charAt(index);
            if (character == '"') {
                index = findStringEnd(json, index);
            } else if (character == '{') {
                depth++;
            } else if (character == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(objectStart, index + 1);
                }
            }
        }
        throw new IllegalArgumentException("Unclosed object: " + field);
    }

    private static int skipWhitespace(String text, int start) {
        int index = start;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int findStringEnd(String text, int start) {
        for (int index = start + 1; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '\\') {
                index++;
            } else if (character == '"') {
                return index;
            }
        }
        throw new IllegalArgumentException("Unclosed string");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\\' && index + 1 < value.length()) {
                builder.append(value.charAt(++index));
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
