package dnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * UTF-8 reader/writer for legacy and current playlist line formats.
 *
 * <p>Playback fields remain comma-separated. Duration and an operator comment
 * are stored inline as {@code # duration=<seconds> # comment=<text>}.</p>
 */
public final class PlaylistCodec {
    private PlaylistCodec() {}

    public static List<PlaylistItem> read(Path path) throws IOException {
        List<PlaylistItem> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                PlaylistItem item = parse(trimmed);
                if (item == null) {
                    System.err.println("Skipping invalid playlist line " + lineNumber + " in " + path);
                } else {
                    result.add(item);
                }
            }
        }
        return result;
    }

    public static PlaylistItem parse(String line) {
        Metadata metadata = parseMetadata(line);
        String content = stripComment(line);
        String[] fields = content.split(",", 6);
        if (fields.length == 5) {
            return new PlaylistItem(fields[0], fields[1], fields[2], fields[3], "",
                fields[4], metadata.duration, metadata.comment);
        }
        if (fields.length == 6) {
            return new PlaylistItem(fields[0], fields[1], fields[2], fields[3],
                fields[4], fields[5], metadata.duration, metadata.comment);
        }
        return null;
    }

    public static void write(Path path, List<PlaylistItem> items) throws IOException {
        List<String> lines = new ArrayList<>();
        for (PlaylistItem item : items) {
            if (!item.path.isEmpty()) lines.add(item.serialize());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private static String stripComment(String line) {
        int marker = line.indexOf('#');
        return (marker >= 0 ? line.substring(0, marker) : line).trim();
    }

    private static Metadata parseMetadata(String line) {
        int marker = line.indexOf('#');
        if (marker < 0) return new Metadata("", "");

        String duration = "";
        List<String> comments = new ArrayList<>();
        for (String part : splitMetadata(line.substring(marker + 1))) {
            String value = part.trim();
            if (value.isEmpty()) continue;
            int equals = value.indexOf('=');
            if (equals < 0) {
                comments.add(unescapeMetadata(value));
                continue;
            }
            String key = value.substring(0, equals).trim();
            String fieldValue = value.substring(equals + 1).trim();
            if ("duration".equalsIgnoreCase(key)) duration = fieldValue;
            else if ("comment".equalsIgnoreCase(key)) comments.add(unescapeMetadata(fieldValue));
        }
        return new Metadata(duration, String.join(" # ", comments));
    }

    private static List<String> splitMetadata(String metadata) {
        List<String> result = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < metadata.length(); i++) {
            char ch = metadata.charAt(i);
            if (ch == '#' && !escaped) {
                result.add(part.toString());
                part.setLength(0);
                continue;
            }
            part.append(ch);
            escaped = ch == '\\' && !escaped;
            if (ch != '\\') escaped = false;
        }
        result.add(part.toString());
        return result;
    }

    private static String unescapeMetadata(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaped) {
                result.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else {
                result.append(ch);
            }
        }
        if (escaped) result.append('\\');
        return result.toString();
    }

    private static final class Metadata {
        final String duration;
        final String comment;

        Metadata(String duration, String comment) {
            this.duration = duration;
            this.comment = comment;
        }
    }
}
