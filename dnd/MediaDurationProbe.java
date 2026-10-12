package dnd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Resolves playlist URLs to local media files and reads their duration.
 *
 * <p>The caller is responsible for running {@link #probe(String)} outside the
 * Swing event thread. Failures are reported to stderr and return an empty
 * string so playlist editing can continue.</p>
 */
final class MediaDurationProbe {
    private final PlayerConfig config;

    MediaDurationProbe(PlayerConfig config) {
        this.config = config;
    }

    String probe(String mediaPath) {
        Path file = resolve(mediaPath);
        if (file == null || !Files.isRegularFile(file)) {
            System.err.println("Файл видео не найден: " + mediaPath);
            return "";
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(
                config.ffprobePath(), "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                file.toString()
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                System.err.println("ffprobe timeout: " + file);
                return "";
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String output = reader.readLine();
                if (process.exitValue() != 0 || output == null) return "";
                return new BigDecimal(output.trim()).stripTrailingZeros().toPlainString();
            }
        } catch (Exception ex) {
            System.err.println("ffprobe error for " + file + ": " + ex.getMessage());
            return "";
        }
    }

    private Path resolve(String mediaPath) {
        try {
            String decoded = URLDecoder.decode(mediaPath, StandardCharsets.UTF_8);
            if (decoded.startsWith("http://") || decoded.startsWith("https://")) {
                decoded = URI.create(decoded).getPath();
            }
            decoded = decoded.replace('/', File.separatorChar);
            if (decoded.matches("^[A-Za-z]:.*")) return Paths.get(decoded).normalize();
            while (decoded.startsWith("\\") || decoded.startsWith("/")) decoded = decoded.substring(1);
            Path resolved = config.mediaRoot().resolve(decoded).normalize();
            return resolved.startsWith(config.mediaRoot()) ? resolved : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
