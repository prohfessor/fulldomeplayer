package dnd;

import java.awt.Color;
import javax.swing.UIManager;

/** Regression test ensuring that switching theme replaces every palette value. */
public final class DarkSwingThemeTest {
    private DarkSwingThemeTest() {}

    public static void main(String[] args) {
        DarkSwingTheme.install(true);
        expect("dark panel", UIManager.getColor("Panel.background"), 8, 8, 8);
        expect("dark text", UIManager.getColor("Label.foreground"), 245, 245, 245);

        DarkSwingTheme.install(false);
        expect("light panel", UIManager.getColor("Panel.background"), 242, 242, 242);
        expect("light text", UIManager.getColor("Label.foreground"), 25, 25, 25);
        expect("light file chooser", UIManager.getColor("FileChooser.background"),
            242, 242, 242);
        if (DarkSwingTheme.isDark()) {
            throw new AssertionError("Theme state was not switched to light");
        }

        System.out.println("DarkSwingThemeTest: OK");
    }

    private static void expect(
            String label, Color actual, int red, int green, int blue) {
        Color expected = new Color(red, green, blue);
        if (!expected.equals(actual)) {
            throw new AssertionError(
                label + ": expected " + expected + ", got " + actual);
        }
    }
}
