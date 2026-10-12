package dnd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

/**
 * Creates and caches compact 8x5 storyboard images for playlist previews.
 */
final class PreviewStoryboardCache {
    static final class Result {
        final BufferedImage image;
        final String error;
        final String command;

        private Result(BufferedImage image, String error, String command) {
            this.image = image;
            this.error = error;
            this.command = command;
        }

        static Result success(BufferedImage image) {
            return new Result(image, "", "");
        }

        static Result failure(String error) {
            return failure(error, "");
        }

        static Result failure(String error, String command) {
            return new Result(null, error, command);
        }
    }

    static final int COLUMNS = 8;
    static final int ROWS = 5;
    static final int FRAME_COUNT = COLUMNS * ROWS;
    static final int FRAME_WIDTH = 240;
    // Both dimensions must be even for common YUV 4:2:0 video formats.
    static final int FRAME_HEIGHT = 136;
    private static final String CACHE_VERSION = "storyboard-v2-240x136";

    private final PlayerConfig config;
    private final Path cacheDirectory;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "preview-storyboard");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, CompletableFuture<Result>> requests = new ConcurrentHashMap<>();

    PreviewStoryboardCache(PlayerConfig config) {
        this.config = config;
        this.cacheDirectory = Paths.get("cache", "previews").toAbsolutePath().normalize();
    }

    CompletableFuture<Result> load(String mediaPath, double knownDuration) {
        Path source = resolve(mediaPath);
        if (source == null || !Files.isRegularFile(source)) {
            return CompletableFuture.completedFuture(
                Result.failure("Файл видео не найден: " + mediaPath));
        }
        String key = cacheKey(source);
        return requests.computeIfAbsent(key,
            ignored -> CompletableFuture.supplyAsync(
                () -> loadOrCreate(source, mediaPath, knownDuration, key), executor));
    }

    private Result loadOrCreate(
            Path source, String mediaPath, double knownDuration, String key) {
        try {
            Files.createDirectories(cacheDirectory);
            Path storyboard = cacheDirectory.resolve(key + ".jpg");
            if (Files.isRegularFile(storyboard)) {
                BufferedImage cached = ImageIO.read(storyboard.toFile());
                if (cached != null) return Result.success(cached);
                Files.deleteIfExists(storyboard);
            }

            double duration = knownDuration;
            if (duration <= 0) {
                String detected = new MediaDurationProbe(config).probe(mediaPath);
                try { duration = Double.parseDouble(detected); }
                catch (NumberFormatException ignored) { duration = 0; }
            }
            if (duration <= 0) {
                return Result.failure("Не удалось определить длительность видео.");
            }

            Path temporary = cacheDirectory.resolve(key + ".tmp.jpg");
            Path errorLog = cacheDirectory.resolve(key + ".ffmpeg-error.txt");
            double framesPerSecond = FRAME_COUNT / duration;
            String filter = String.format(Locale.ROOT,
                "fps=%.9f,scale=%d:%d:force_original_aspect_ratio=decrease,"
                    + "setsar=1,pad=%d:%d:(ow-iw)/2:(oh-ih)/2:black,tile=%dx%d",
                framesPerSecond, FRAME_WIDTH, FRAME_HEIGHT,
                FRAME_WIDTH, FRAME_HEIGHT, COLUMNS, ROWS);
            List<String> command = Arrays.asList(
                config.ffmpegPath(), "-v", "error", "-y", "-i", source.toString(),
                "-vf", filter, "-frames:v", "1", "-q:v", "4", temporary.toString());
            String displayedCommand = formatCommand(command);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectError(errorLog.toFile());
            Process process = processBuilder.start();
            if (!process.waitFor(600, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                Files.deleteIfExists(temporary);
                return Result.failure(
                    "ffmpeg не завершил создание превью за 600 секунд.",
                    displayedCommand);
            }
            if (process.exitValue() != 0 || !Files.isRegularFile(temporary)) {
                Files.deleteIfExists(temporary);
                String details = readError(errorLog);
                Files.deleteIfExists(errorLog);
                return Result.failure(details.isEmpty()
                    ? "ffmpeg завершился с кодом " + process.exitValue() + "."
                    : "ffmpeg: " + details,
                    displayedCommand);
            }
            Files.deleteIfExists(errorLog);
            Files.move(temporary, storyboard);
            BufferedImage image = ImageIO.read(storyboard.toFile());
            return image == null
                ? Result.failure("Созданный файл превью не удалось прочитать.")
                : Result.success(image);
        } catch (Exception ex) {
            System.err.println("Не удалось создать превью " + source + ": " + ex.getMessage());
            return Result.failure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private static String readError(Path errorLog) {
        try {
            if (!Files.isRegularFile(errorLog)) return "";
            String text = Files.readString(errorLog, StandardCharsets.UTF_8)
                .replace('\r', ' ').replace('\n', ' ').trim();
            return text.length() <= 800 ? text : text.substring(0, 800) + "…";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String formatCommand(List<String> arguments) {
        StringBuilder command = new StringBuilder();
        for (String argument : arguments) {
            if (command.length() > 0) command.append(' ');
            boolean quote = argument.isEmpty()
                || argument.chars().anyMatch(Character::isWhitespace)
                || argument.indexOf(',') >= 0
                || argument.indexOf('(') >= 0
                || argument.indexOf(')') >= 0;
            if (quote) command.append('"');
            command.append(argument.replace("\"", "\\\""));
            if (quote) command.append('"');
        }
        return command.toString();
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

    private static String cacheKey(Path source) {
        try {
            String identity = CACHE_VERSION + "|"
                + source.toAbsolutePath().normalize() + "|"
                + Files.size(source) + "|" + Files.getLastModifiedTime(source).toMillis();
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(identity.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (Exception ex) {
            return Integer.toHexString(source.toString().hashCode());
        }
    }
}
