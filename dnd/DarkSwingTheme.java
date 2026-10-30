package dnd;

import java.awt.Color;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * Complete light/dark Swing palette used by the operator interface.
 *
 * <p>Both modes explicitly replace every color previously customized by the
 * other mode. This is important because UIManager overrides survive a simple
 * Look & Feel switch.</p>
 */
final class DarkSwingTheme {
    private static final ColorUIResource DARK_BACKGROUND = color(0, 0, 0);
    private static final ColorUIResource DARK_PANEL = color(8, 8, 8);
    private static final ColorUIResource DARK_CONTROL = color(28, 28, 28);
    private static final ColorUIResource DARK_BORDER = color(45, 45, 45);
    private static final ColorUIResource DARK_TEXT = color(245, 245, 245);
    private static final ColorUIResource DARK_DISABLED = color(135, 135, 135);

    private static final ColorUIResource LIGHT_BACKGROUND = color(255, 255, 255);
    private static final ColorUIResource LIGHT_PANEL = color(242, 242, 242);
    private static final ColorUIResource LIGHT_CONTROL = color(225, 225, 225);
    private static final ColorUIResource LIGHT_BORDER = color(170, 170, 170);
    private static final ColorUIResource LIGHT_TEXT = color(25, 25, 25);
    private static final ColorUIResource LIGHT_DISABLED = color(120, 120, 120);

    private static final ColorUIResource SELECTION = color(0, 82, 150);
    private static final ColorUIResource SELECTION_TEXT = color(255, 255, 255);
    private static boolean dark = true;

    private DarkSwingTheme() {}

    static void install() {
        install(true);
    }

    static void install(boolean useDarkTheme) {
        setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        dark = useDarkTheme;
        applyPalette(
            useDarkTheme ? DARK_BACKGROUND : LIGHT_BACKGROUND,
            useDarkTheme ? DARK_PANEL : LIGHT_PANEL,
            useDarkTheme ? DARK_CONTROL : LIGHT_CONTROL,
            useDarkTheme ? DARK_BORDER : LIGHT_BORDER,
            useDarkTheme ? DARK_TEXT : LIGHT_TEXT,
            useDarkTheme ? DARK_DISABLED : LIGHT_DISABLED);
    }

    private static void applyPalette(
            ColorUIResource background,
            ColorUIResource panel,
            ColorUIResource control,
            ColorUIResource border,
            ColorUIResource text,
            ColorUIResource disabled) {
        UIManager.put("control", panel);
        UIManager.put("info", control);
        UIManager.put("text", text);
        UIManager.put("nimbusBase", background);
        UIManager.put("nimbusBlueGrey", control);
        UIManager.put("nimbusLightBackground", background);

        colors("Panel", panel, text);
        colors("Label", panel, text);
        colors("Button", control, text);
        colors("ToggleButton", control, text);
        colors("CheckBox", panel, text);
        colors("RadioButton", panel, text);
        colors("TextField", background, text);
        colors("FormattedTextField", background, text);
        colors("TextArea", background, text);
        colors("EditorPane", background, text);
        colors("TextPane", background, text);
        colors("List", background, text);
        colors("Tree", background, text);
        colors("Table", background, text);
        colors("TableHeader", control, text);
        colors("ComboBox", control, text);
        colors("ScrollPane", background, text);
        colors("ScrollBar", background, text);
        colors("Viewport", background, text);
        colors("Slider", panel, text);
        colors("SplitPane", panel, text);
        colors("TabbedPane", panel, text);
        colors("OptionPane", panel, text);
        colors("FileChooser", panel, text);
        colors("ToolTip", control, text);
        colors("Menu", control, text);
        colors("MenuBar", control, text);
        colors("MenuItem", control, text);
        colors("PopupMenu", control, text);
        colors("Spinner", control, text);

        UIManager.put("TextField.caretForeground", text);
        UIManager.put("TextArea.caretForeground", text);
        UIManager.put("TextPane.caretForeground", text);
        UIManager.put("EditorPane.caretForeground", text);
        UIManager.put("TextField.inactiveForeground", disabled);
        UIManager.put("Button.disabledText", disabled);
        UIManager.put("Label.disabledForeground", disabled);

        selection("Tree");
        selection("Table");
        selection("List");
        selection("ComboBox");
        selection("Menu");
        selection("MenuItem");
        UIManager.put("Tree.textBackground", background);
        UIManager.put("Tree.textForeground", text);
        UIManager.put("Table.gridColor", border);
        UIManager.put("ScrollBar.track", background);
        UIManager.put("ScrollBar.thumb", border);
        UIManager.put("ScrollBar.thumbDarkShadow", control);
        UIManager.put("ScrollBar.thumbHighlight", disabled);
        UIManager.put("ScrollBar.thumbShadow", control);
        UIManager.put("Button.select", border);
        UIManager.put("ToggleButton.select", border);

        UIManager.put("TitledBorder.titleColor", text);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(border));
        UIManager.put("Separator.foreground", border);
        UIManager.put("Separator.background", background);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(border));
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPane.dividerSize", 5);
        UIManager.put("Button.border", BorderFactory.createLineBorder(border));
        UIManager.put("ToggleButton.border", BorderFactory.createLineBorder(border));
        UIManager.put("TextField.border", BorderFactory.createLineBorder(border));
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(border));
    }

    /** Switches every existing Swing window between the operator themes. */
    static void setDark(boolean useDarkTheme) {
        install(useDarkTheme);
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }

    static boolean isDark() {
        return dark;
    }

    private static void selection(String component) {
        UIManager.put(component + ".selectionBackground", SELECTION);
        UIManager.put(component + ".selectionForeground", SELECTION_TEXT);
    }

    private static void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
        } catch (Exception exception) {
            System.err.println("Cannot apply Swing theme: " + exception.getMessage());
        }
    }

    private static void colors(String component, Color background, Color foreground) {
        UIManager.put(component + ".background", background);
        UIManager.put(component + ".foreground", foreground);
    }

    private static ColorUIResource color(int red, int green, int blue) {
        return new ColorUIResource(red, green, blue);
    }
}
