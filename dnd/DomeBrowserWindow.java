// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

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

import dnd.BrowserQueryHandler;

import org.cef.callback.CefQueryCallback;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsConfiguration;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

import java.io.File;
import javax.imageio.ImageIO;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;



/**
 * JCEF window displayed on the planetarium dome screen.
 *
 * <p>It owns the embedded browser and translates messages from the web
 * renderer into calls on the desktop controller.</p>
 */
public class DomeBrowserWindow extends JFrame {
    private JComponent parent;
    private static final long serialVersionUID = -5570653778104813836L;
    private final JTextField address_;
    private final CefClient client_;
    public final CefBrowser browser_;
    private final Component browerUI_;
    private boolean browserFocus_ = true;
    public int currentSlide = 0;
//    public static ScreenShotThread screenshot;
    public int domeScreen = 0;

    public void setParent(JComponent parent_) {parent=parent_; System.out.println("parent: "+parent);}

    public DomeBrowserWindow(CefApp cefApp_, String startURL, boolean useOSR, boolean isTransparent, boolean dome) {
        PlayerConfig playerConfig = PlayerConfig.load();
        client_ = cefApp_.createClient();

        browser_ = client_.createBrowser(startURL, useOSR, isTransparent);
        browerUI_ = browser_.getUIComponent();

        address_ = new JTextField(startURL, 100);

        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new BrowserQueryHandler(this), true);
        client_.addMessageRouter(msgRouter);


        // Переключить фокус ввода на браузер. 
        client_.addFocusHandler(new CefFocusHandlerAdapter() {
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (browserFocus_) return;
                browserFocus_ = true;
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                browser.setFocus(true);
            }

            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                browserFocus_ = false;
            }
        });

        getContentPane().add(browerUI_, BorderLayout.CENTER);
        pack();

if(dome) {  // на купол, на полный экран
   GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
   GraphicsDevice[] gs = ge.getScreenDevices();
   for (int j = 0; j < gs.length; j++) {
      GraphicsDevice gd = gs[j];
      DisplayMode dm=gd.getDisplayMode();
	System.out.println("Display "+j+" "+dm.getHeight()+"x"+dm.getWidth());
	if((playerConfig.domeScreenIndex() >= 0 && j == playerConfig.domeScreenIndex())
            || (playerConfig.domeScreenIndex() < 0 && dm.getWidth() >= playerConfig.domeMinimumWidth())) {
		domeScreen = j;
//		gd.setFullScreenWindow( this ); // при переключении на другое окно, исчезает.

		dispose();
	        setUndecorated(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		int width = 0, height = 0;
		width = gd.getDefaultConfiguration().getBounds().width;
	        height = gd.getDefaultConfiguration().getBounds().height;

	        setLocation(
        	    ((width / 2) - (getSize().width / 2)) + gd.getDefaultConfiguration().getBounds().x, 
	            ((height / 2) - (getSize().height / 2)) + gd.getDefaultConfiguration().getBounds().y
        	);

	}
   }

} else {
        setSize(800, 600);
}
//   setVisible(true);

        // (6) To take care of shutting down CEF accordingly, it's important to call
        //     the method "dispose()" of the CefApp instance if the Java
        //     application will be closed. Otherwise you'll get asserts from CEF.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CefApp.getInstance().dispose();
                dispose();
            }

        });


    }

	public void setfocus(boolean f) {
		browser_.setFocus(f);
	}



/*
	public void setChildFrame(DomeBrowserWindow mf) {
		childFrame=mf;
	}
	public DomeBrowserWindow getChildFrame() {return childFrame;}
*/
//@Override
	public boolean onQuery(CefBrowser browser, long query_id, String request, boolean persistent, CefQueryCallback callback) {
		System.out.println("DomeBrowserWindow query: "+request);
		if (request.equals("CLOSING")) {
			callback.success("I have no idea y this wont work");
			return true;
		}
		callback.failure(1, "Still not working");
		return false;
	}
//	public void setGui(GUI gui_) {gui=gui_;}


	public void message(String mess) {
                if (!SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(() -> message(mess));
                    return;
                }
                BrowserCommand parsed = BrowserCommand.parse(mess);
		if (parsed == null) return;
                String command = parsed.name;
                String value = parsed.value;
			if("onload".equals(command)) {
//				browser_.executeJavaScript("getSlidesNames()", "", 1);
			}
			else if("times".equals(command)) {
				String stimes=value;
				int itimes=0;
				try{  
					itimes = Integer.valueOf(stimes);  
		        	} catch (Exception ex){  }
				System.out.println("---- in times: "+itimes);
			}
			else if("count".equals(command)) {
				String scount=value;
				try{  
					currentSlide = Integer.valueOf(scount);  
//					if(currentSlide!=0) gui.setSlide(currentSlide);
		        	} catch (Exception ex){  }
				System.out.println("---- in count: "+scount);
			}
			else if("slides".equals(command)) {
				System.out.println("---- in slides: ");
				String[] slides=value.split("~");
//				gui.setSlides(slides);
			}
			else if("videoend".equals(command)) {
//				String stimes=mess.substring(startPos + 10);
//				System.out.println("---- video end: ");
				PlanetariumPlayer p=(PlanetariumPlayer)parent;
				p.handleVideoEnd();
			}
			else if("videoinfo".equals(command)) {
				String stimes=value;
				System.out.println("---- video info: "+stimes);
				PlanetariumPlayer p=(PlanetariumPlayer)parent;
				float endVideo=Float.valueOf(stimes);
				if(p!=null) {
					p.setDuration(endVideo);
					p.refreshSelectedControls();
					p.setVideoEnd(endVideo);
				}
//				if(p!=null) p.endVideo=endVideo;
			}
			else if("currenttime".equals(command)) {
				String stimes=value;
//				System.out.println("---- currenttime: "+stimes);
				PlanetariumPlayer p=(PlanetariumPlayer)parent;
				p.setTime(stimes);

			}
			else if("error".equals(command)) {
                                System.err.println("Browser player error: " + value);
                        }
			else if("slideNames".equals(command)) {
//				System.out.println("in slide names: "+mess.substring(startPos + 12));
				String[] names=value.split("\n");
/*
				String[subts.length] t;
				for(i=0; i<subts.length(); i++) {
					t[0]=mess;
				}
*/				
//				gui.setPageNames(names);
			}
			else if("slideSubts".equals(command)) {
				System.out.println("in slide subts: ");
				String[] subts=value.split("\n");
//				gui.setPageSubts(subts);
			}

			
	}

    public void screenShot() throws Exception
    {
        Calendar now = Calendar.getInstance();
        Robot robot = new Robot();

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();
	GraphicsDevice gd = gs[domeScreen];


	Rectangle screenRect = new Rectangle(0, 0, 0, 0);
	screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());

        BufferedImage screenShot = robot.createScreenCapture(screenRect.union(gd.getDefaultConfiguration().getBounds()));

//        ImageIO.write(screenShot, "JPG", new File("d:\\tmp\\ttt.jpg"));
//	gui.setScreenShot(scaleImage(screenShot, 0.1));

    }


private static BufferedImage scaleImage(BufferedImage before, double scale) {
    int w = before.getWidth();
    int h = before.getHeight();
    // Create a new image of the proper size
    int w2 = (int) (w * scale);
    int h2 = (int) (h * scale);
    BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
    AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
    AffineTransformOp scaleOp 
        = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);

    scaleOp.filter(before, after);
    return after;
}

//--------------------------- MAIN
/*
    public static void main(String[] args) {
        // Perform startup initialization on platforms that require it.
        if (!CefApp.startup(args)) {
            System.out.println("Startup initialization failed!");
            return;
        }

    JcefApplicationHandler appHandler = new JcefApplicationHandler(args);
    CefApp.addAppHandler(appHandler);

        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = false;
	settings.remote_debugging_port = 8081; // localhost:8081
        CefApp cefApp_ = CefApp.getInstance(settings);

        boolean useOsr = false;
        DomeBrowserWindow t1 = new DomeBrowserWindow(cefApp_, args[0], useOsr, false, true);

        GUI gui = new GUI("Презентации");
        gui.setVisible(true);
	gui.setChildFrame(t1);
	t1.setVisible(true);
	t1.setfocus(true);


	screenshot=new ScreenShotThread(t1);
     
	screenshot.start();    


    }
*/
}
