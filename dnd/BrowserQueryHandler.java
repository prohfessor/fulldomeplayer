// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package dnd;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

/**
 * Completes JCEF queries and forwards their payload to the browser window.
 */
public class BrowserQueryHandler extends CefMessageRouterHandlerAdapter {
    private final DomeBrowserWindow browserWindow;

    public BrowserQueryHandler(final DomeBrowserWindow browserWindow) {
        this.browserWindow = browserWindow;
    }


    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request, boolean persistent, CefQueryCallback callback) {
        try {
            browserWindow.message(request);
            callback.success("");
        } catch (RuntimeException ex) {
            System.err.println("Invalid browser message: " + request + " (" + ex.getMessage() + ")");
            callback.failure(400, ex.getMessage() == null ? "Invalid browser message" : ex.getMessage());
        }
        return true;
    }
}
