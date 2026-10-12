package dnd;

/**
 * Parsed command received from the web renderer through JCEF.
 */
public final class BrowserCommand {
    public final String name;
    public final String value;

    private BrowserCommand(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static BrowserCommand parse(String message) {
        if (message == null || !message.startsWith("cmd:")) return null;
        String payload = message.substring(4);
        int separator = payload.indexOf(';');
        String name = separator < 0 ? payload : payload.substring(0, separator);
        if (name.isBlank()) return null;
        return new BrowserCommand(name, separator < 0 ? "" : payload.substring(separator + 1));
    }
}
