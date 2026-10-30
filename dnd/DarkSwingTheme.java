package dnd;

import java.awt.Color;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * High-contrast dark palette for operation inside a dark planetarium.
 */
final class DarkSwingTheme {
    private static final ColorUIResource BLACK = color(0, 0, 0);
    private static final ColorUIResource PANEL = color(8, 8, 8);
    private static final ColorUIResource CONTROL = color(28, 28, 28);
    private static final ColorUIResource BORDER = color(45, 45, 45);
    private static final ColorUIResource WHITE = color(245, 245, 245);
    private static final ColorUIResource DISABLED = color(135, 135, 135);
    private static final ColorUIResource SELECTION = color(0, 82, 150);
    private static boolean dark = true;

    private DarkSwingTheme() {}

    static void install() {
        setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        dark = true;
        UIManager.put("control", PANEL);
        UIManager.put("info", CONTROL);
        UIManager.put("text", WHITE);
        UIManager.put("nimbusBase", BLACK);
        UIManager.put("nimbusBlueGrey", CONTROL);
        UIManager.put("nimbusLightBackground", BLACK);

        colors("Panel", PANEL, WHITE);
        colors("Label", PANEL, WHITE);
        colors("Button", CONTROL, WHITE);
        colors("ToggleButton", CONTROL, WHITE);
        colors("CheckBox", PANEL, WHITE);
        colors("RadioButton", PANEL, WHITE);
        colors("TextField", BLACK, WHITE);
        colors("FormattedTextField", BLACK, WHITE);
        colors("TextArea", BLACK, WHITE);
        colors("EditorPane", BLACK, WHITE);
        colors("TextPane", BLACK, WHITE);
        colors("List", BLACK, WHITE);
        colors("Tree", BLACK, WHITE);
        colors("Table", BLACK, WHITE);
        colors("TableHeader", CONTROL, WHITE);
        colors("ComboBox", CONTROL, WHITE);
        colors("ScrollPane", BLACK, WHITE);
        colors("ScrollBar", BLACK, WHITE);
        colors("Viewport", BLACK, WHITE);
        colors("Slider", PANEL, WHITE);
        colors("SplitPane", PANEL, WHITE);
        colors("TabbedPane", PANEL, WHITE);
        colors("OptionPane", PANEL, WHITE);
        colors("FileChooser", PANEL, WHITE);
        colors("ToolTip", CONTROL, WHITE);
        colors("Menu", CONTROL, WHITE);
        colors("MenuItem", CONTROL, WHITE);
        colors("PopupMenu", CONTROL, WHITE);
        colors("Spinner", CONTROL, WHITE);

        UIManager.put("TextField.caretForeground", WHITE);
        UIManager.put("TextArea.caretForeground", WHITE);
        UIManager.put("TextPane.caretForeground", WHITE);
        UIManager.put("EditorPane.caretForeground", WHITE);
        UIManager.put("TextField.inactiveForeground", DISABLED);
        UIManager.put("Button.disabledText", DISABLED);
        UIManager.put("Label.disabledForeground", DISABLED);

        UIManager.put("Tree.selectionBackground", SELECTION);
        UIManager.put("Tree.selectionForeground", WHITE);
        UIManager.put("Tree.textBackground", BLACK);
        UIManager.put("Tree.textForeground", WHITE);
        UIManager.put("Table.selectionBackground", SELECTION);
        UIManager.put("Table.selectionForeground", WHITE);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("List.selectionBackground", SELECTION);
        UIManager.put("List.selectionForeground", WHITE);
        UIManager.put("ComboBox.selectionBackground", SELECTION);
        UIManager.put("ComboBox.selectionForeground", WHITE);
        UIManager.put("ScrollBar.track", BLACK);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("ScrollBar.thumbDarkShadow", CONTROL);
        UIManager.put("ScrollBar.thumbHighlight", DISABLED);
        UIManager.put("ScrollBar.thumbShadow", CONTROL);
        UIManager.put("Menu.selectionBackground", SELECTION);
        UIManager.put("Menu.selectionForeground", WHITE);
        UIManager.put("MenuItem.selectionBackground", SELECTION);
        UIManager.put("MenuItem.selectionForeground", WHITE);

        UIManager.put("TitledBorder.titleColor", WHITE);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("Separator.foreground", BORDER);
        UIManager.put("Separator.background", BLACK);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPane.dividerSize", 5);
        UIManager.put("Button.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("ToggleButton.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("TextField.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER));
    }

    /** Switches every existing Swing window between the operator themes. */
    static void setDark(boolean useDarkTheme) {
        if (useDarkTheme) {
            install();
        } else {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            dark = false;
        }
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
