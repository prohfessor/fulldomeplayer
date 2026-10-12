package dnd;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefCommandLine;

/**
 * Configures command-line switches and lifecycle handling for JCEF.
 */
public class JcefApplicationHandler extends CefAppHandlerAdapter {
    public JcefApplicationHandler(String [] args) {
        super(args);
    }

    @Override
    public void onBeforeCommandLineProcessing(String process_type, CefCommandLine command_line) {
        super.onBeforeCommandLineProcessing(process_type, command_line);
        if (process_type.isEmpty()) {
            command_line.appendSwitchWithValue("autoplay-policy","no-user-gesture-required");
        }
    }
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefAppState.TERMINATED) System.exit(0);
            }

}
