package dnd;

import java.util.Objects;

/**
 * One playlist row in its domain order, independent of JTable column order.
 */
public final class PlaylistItem {
    public final String begin;
    public final String end;
    public final String rotation;
    public final String pitch;
    public final String flags;
    public final String path;
    public final String duration;
    public final String comment;

    public PlaylistItem(String begin, String end, String rotation, String pitch, String flags, String path) {
        this(begin, end, rotation, pitch, flags, path, "", "");
    }

    public PlaylistItem(String begin, String end, String rotation, String pitch, String flags, String path, String duration) {
        this(begin, end, rotation, pitch, flags, path, duration, "");
    }

    public PlaylistItem(String begin, String end, String rotation, String pitch, String flags,
                        String path, String duration, String comment) {
        this.begin = normalizedNumber(begin);
        this.end = normalizedNumber(end);
        this.rotation = normalizedNumber(rotation);
        this.pitch = normalizedNumber(pitch);
        this.flags = value(flags);
        this.path = value(path);
        this.duration = value(duration);
        this.comment = value(comment);
    }

    public Object[] toTableRow() {
        return new Object[] {begin, end, path, rotation, pitch, flags, duration, comment};
    }

    public String serialize() {
        String line = begin + "," + end + "," + rotation + "," + pitch + "," + flags + "," + path;
        if (!duration.isEmpty()) line += " # duration=" + duration;
        if (!comment.isEmpty()) line += " # comment=" + escapeMetadata(comment);
        return line;
    }

    public static PlaylistItem fromTableRow(javax.swing.table.TableModel model, int row) {
        return new PlaylistItem(
            cell(model, row, 0), cell(model, row, 1), cell(model, row, 3),
            cell(model, row, 4), cell(model, row, 5), cell(model, row, 2),
            model.getColumnCount() > 6 ? cell(model, row, 6) : "",
            model.getColumnCount() > 7 ? cell(model, row, 7) : ""
        );
    }

    private static String escapeMetadata(String input) {
        return input.replace("\\", "\\\\").replace("#", "\\#");
    }

    private static String cell(javax.swing.table.TableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private static String value(String input) {
        return Objects.toString(input, "").trim();
    }

    private static String normalizedNumber(String input) {
        String value = value(input);
        return value.isEmpty() ? "0" : value;
    }
}
