package dnd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class PlaylistCodecTest {
    public static void main(String[] args) throws Exception {
        PlaylistItem oldFormat = require(PlaylistCodec.parse("0,10,20,30,/video/a.webm"));
        check(oldFormat.flags.isEmpty(), "old format flags");
        check(oldFormat.path.equals("/video/a.webm"), "old format path");

        PlaylistItem current = require(PlaylistCodec.parse("1.5,20,-10,5,fm,/video/a,b.webm # duration=11.25"));
        check(current.path.equals("/video/a,b.webm"), "comma in path");
        check(current.duration.equals("11.25"), "duration metadata");
        check(current.serialize().equals("1.5,20,-10,5,fm,/video/a,b.webm # duration=11.25"), "round trip");

        PlaylistItem commented = require(PlaylistCodec.parse(
            "0,0,0,0,f,/video/show.mp4 # duration=615.24 # comment=Проверить звук \\#1"));
        check(commented.comment.equals("Проверить звук #1"), "inline comment metadata");
        check(commented.serialize().equals(
            "0,0,0,0,f,/video/show.mp4 # duration=615.24 # comment=Проверить звук \\#1"),
            "comment round trip");

        PlaylistItem freeComment = require(PlaylistCodec.parse(
            "0,0,0,0,f,/video/show.mp4 # обычный комментарий"));
        check(freeComment.comment.equals("обычный комментарий"), "free inline comment");

        Path temporary = Files.createTempFile("player-playlist-", ".js");
        try {
            PlaylistCodec.write(temporary, List.of(oldFormat, current));
            check(PlaylistCodec.read(temporary).size() == 2, "UTF-8 file round trip");
        } finally {
            Files.deleteIfExists(temporary);
        }

        BrowserCommand command = BrowserCommand.parse("cmd:currenttime;12.34");
        check(command != null && command.name.equals("currenttime") && command.value.equals("12.34"),
            "browser command");
        check(BrowserCommand.parse("bad") == null, "invalid browser command");
        System.out.println("PlaylistCodecTest: OK");
    }

    private static PlaylistItem require(PlaylistItem item) {
        if (item == null) throw new AssertionError("Expected playlist item");
        return item;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
