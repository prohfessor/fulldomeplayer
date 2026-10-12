package dnd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Immutable view of runtime settings loaded from {@code player.properties}.
 */
public final class PlayerConfig {
    private static final String DEFAULT_CONFIG = "player.properties";
    private final Properties properties = new Properties();

    public static PlayerConfig load() {
        PlayerConfig config = new PlayerConfig();
        String configuredPath = System.getProperty("player.config", DEFAULT_CONFIG);
        Path path = Paths.get(configuredPath).toAbsolutePath().normalize();
        if (Files.isRegularFile(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                config.properties.load(input);
            } catch (IOException ex) {
                System.err.println("Cannot read configuration " + path + ": " + ex.getMessage());
            }
        }
        return config;
    }

    public Path mediaRoot() {
        return path("media.root", "E:\\www");
    }

    public Path playlistRoot() {
        return path("playlist.root", "E:\\www\\player");
    }

    public String playerUrl() {
        return properties.getProperty("player.url", "http://localhost/player/index.html?playlist=test.js").trim();
    }

    public int remoteDebuggingPort() {
        return integer("chromium.remoteDebuggingPort", 8081);
    }

    public int domeMinimumWidth() {
        return integer("dome.minimumWidth", 3000);
    }

    public int domeScreenIndex() {
        return integer("dome.screenIndex", -1);
    }

    public String ffprobePath() {
        return properties.getProperty("ffprobe.path", "ffprobe").trim();
    }

    public String ffmpegPath() {
        return properties.getProperty("ffmpeg.path", "ffmpeg").trim();
    }

    private Path path(String key, String fallback) {
        return Paths.get(properties.getProperty(key, fallback)).toAbsolutePath().normalize();
    }

    private int integer(String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException ex) {
            System.err.println("Invalid integer property " + key + "; using " + fallback);
            return fallback;
        }
    }
}
