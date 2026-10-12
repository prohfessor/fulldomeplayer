/*
http://www.java2s.com/Tutorial/Java/0240__Swing/Dragginganddroppingtextbetweenatextareaalistandatable.htm
*/

package dnd;

import java.util.Locale;

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

import org.cef.callback.CefQueryCallback;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;


import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JViewport;
import java.awt.Button;
import java.awt.Component;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

    import java.io.Serializable;
    import java.awt.event.*;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

/**
 * Main Swing application for editing playlists and controlling dome playback.
 *
 * <p>The web renderer remains isolated behind {@link DomeBrowserWindow}; this
 * class owns only desktop UI state, playlist editing and playback commands.</p>
 */
public class PlanetariumPlayer extends JPanel {

static DomeBrowserWindow childFrame;
static JComponent newContentPane;
static final PlayerConfig CONFIG = PlayerConfig.load();
public float endVideo=0;

    /** Removes a reusable UI component from its current layout container. */
    private static void detachComponent(Component component) {
        if (component != null && component.getParent() != null) {
            component.getParent().remove(component);
        }
    }

    private static String jsString(String value) {
        if (value == null) return "null";
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\': escaped.append("\\\\"); break;
                case '"': escaped.append("\\\""); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                    else escaped.append(ch);
            }
        }
        return escaped.append('"').toString();
    }

    private void callBrowser(String function, Object... arguments) {
        if (childFrame == null || childFrame.browser_ == null) return;
        StringBuilder script = new StringBuilder(function).append('(');
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) script.append(',');
            Object value = arguments[i];
            if (value instanceof Number || value instanceof Boolean) script.append(value);
            else script.append(jsString(String.valueOf(value)));
        }
        script.append(')');
        childFrame.browser_.executeJavaScript(script.toString(), "", 1);
    }

    private boolean hasCurrentEditorRow() {
        return table_playlist_editor != null
            && playlist_currentrow >= 0
            && playlist_currentrow < table_playlist_editor.getRowCount();
    }

   	public int playCurVideo() {    
		int row = playlist_currentrow;
                if (!hasCurrentEditorRow()) return -1;

		TableModel model = table_playlist_editor.getModel();
		table_playlist_editor.setRowSelectionInterval(row, row);

		String srow="";
		srow+=model.getValueAt(row, 0)+",";
		srow+=model.getValueAt(row, 1)+",";
		srow+=model.getValueAt(row, 3)+",";
		srow+=model.getValueAt(row, 4)+",";
		if(model.getValueAt(row, 5)!=null) srow+=model.getValueAt(row, 5)+","; else srow+=",";
		srow+=model.getValueAt(row, 2);

		System.out.println("--------------------- srow: "+srow);
		  callBrowser("videoNext", srow);

		return row;
	}

	public int playNextVideo() {
                if (table_playlist_editor == null || table_playlist_editor.getRowCount() == 0) return -1;
                if (playlist_currentrow + 1 >= table_playlist_editor.getRowCount()) {
                    playlist_currentrow = table_playlist_editor.getRowCount() - 1;
                    return -1;
                }
		playlist_currentrow=playlist_currentrow+1;
		System.out.println("play row: "+playlist_currentrow);
		playCurVideo();
		updateControlsFromPlaylistRow(playlist_currentrow);
		return playlist_currentrow;
	}
	public int playPreviousVideo() {
                if (table_playlist_editor == null || table_playlist_editor.getRowCount() == 0) return -1;
                if (playlist_currentrow <= 0) {
                    playlist_currentrow = 0;
                    return -1;
                }
		playlist_currentrow--;
		System.out.println("play row: "+playlist_currentrow);
		playCurVideo();
		updateControlsFromPlaylistRow(playlist_currentrow);
		return playlist_currentrow;
	}
	public void handleVideoEnd() {
                if (playNextVideo() < 0 && slider != null) {
                    slider.playbackEnded();
                }
	}
	public void  playVideo() { callBrowser("play"); }
	public void  pauseVideo() { callBrowser("pause"); }
	public void  stopVideo() { callBrowser("stop"); }


	public void  setBeginTime(float time) {
//		System.out.println("ExtendedDndDemo:setBeginTime: "+time);
		callBrowser("setBeginTime", time);
		if (hasCurrentEditorRow()) table_playlist_editor.getModel().setValueAt(time, playlist_currentrow, 0);
	}
	public void setEndTime(float time)   {
		System.out.println("ExtendedDndDemo:setEndTime: "+time);

		callBrowser("setEndTime", time);
		if (hasCurrentEditorRow()) table_playlist_editor.getModel().setValueAt(time, playlist_currentrow, 1);
	}
	public void setCurTime(float time)   {
//		System.out.println("ExtendedDndDemo:setCurTime:163 "+time);
		callBrowser("setCurTime", time);
	}

public void setVE(float videoend) {
		endVideo=videoend;
//		slider.setSliderMaximum(videoend);
  //      	slider.rangeSlider.setMaximum(500);


                slider.setEndTime(videoend);
}
	public void setVideoEnd(float videoend)   {
                float effectiveEnd = endVideo > 0 ? Math.min(endVideo, videoend) : videoend;
		slider.setSliderMaximum(videoend);
		System.out.println("ExtendedDndDemo:setSliderMaximum: "+videoend);
                slider.setEndTime(effectiveEnd);
	}
	public void setDuration(float time)   {
                slider.setDuration(time);
	}

	public void setTime(String stime)   {
		float time; try {   time= Float.parseFloat(stime); } catch (NumberFormatException ne) {   time = 0; }
                final float currentTime = time;
                SwingUtilities.invokeLater(() -> slider.setCurTime(currentTime));
	}

	public void  setVideo360(boolean v360) {
		System.out.println("setVideo360: "+v360);
		 callBrowser("setVideo360", v360);
// не здесь	// ставим флаг в плейер (f - fulldome, 3 - 360).

		// проверяем. Если флаг видео 360 в файле не установлен, устанавливаем.
		// если уже установлен, ничего не делаем
	}


Button buttonNext = new Button(">");

	private void nextSlide() {
		if(childFrame!=null) {
			System.out.println("in nextSlide ");
//			childFrame.currentSlide++;
//			childFrame.browser_.executeJavaScript("videoNext()", "", 1);
			callBrowser("videoNext", "0, 0, 0, 0, f,/video360/IrkCosmo/ИК6.webm");
//			setSlide(childFrame.currentSlide);
		}
	}


	public void refreshSelectedControls() {
		updateControlsFromPlaylistRow(playlist_currentrow);
	}
    private void updateControlsFromPlaylistRow(int row) {
                if (table_playlist_editor == null || row < 0 || row >= table_playlist_editor.getRowCount()) {
                    endVideo = 0;
                    return;
                }
		TableModel model_editor = table_playlist_editor.getModel();
                updatingControlsFromSelection = true;
                try {
		String srow=""+model_editor.getValueAt(row, 2);

		System.out.println("Selected media: "+srow+" end="+model_editor.getValueAt(row, 1));

		field_rotate_360.setText(""+model_editor.getValueAt(row, 3));
// добавляем toString(), т.к. в ячейке может быть int
		int foo; try {   foo = Integer.parseInt((String)model_editor.getValueAt(row, 3).toString()); } catch (NumberFormatException ne) {   foo = 0; }
//		int foo=0;
//		foo = (int)model.getValueAt(row, 3);
		slider_rotate.setValue(foo);
		field_rotate_tangage.setText(""+model_editor.getValueAt(row, 4));
		try {   foo = Integer.parseInt((String)model_editor.getValueAt(row, 4).toString()); } catch (NumberFormatException ne) {   foo = 0; }
//		foo = (int)model.getValueAt(row, 4);
		slider_rotate_tangage.setValue(foo);
		float beginTime=0, endTime=0, duration=0;
//		try {   dur = Float.parseFloat((String)slider.videoDuration.getText()); } catch (NumberFormatException ne) {   dur = 0; } // в этом месте еще рано. duration выставляется после вызова videoInfo ниже
		try {   beginTime=  Float.parseFloat(model_editor.getValueAt(row, 0).toString()); } catch (NumberFormatException ne) {   beginTime = 0; } 
		try {   endTime=  Float.parseFloat(model_editor.getValueAt(row, 1).toString()); } catch (NumberFormatException ne) {   endTime = 0; } 
                if (model_editor.getColumnCount() > 6 && model_editor.getValueAt(row, 6) != null) {
                    try { duration = Float.parseFloat(model_editor.getValueAt(row, 6).toString()); }
                    catch (NumberFormatException ignored) { duration = 0; }
                }

endVideo=endTime;

		System.out.println("Selection interval: begin="+beginTime+" end="+endTime);

                slider.setSelectionTimes(beginTime, endTime, duration);

                String flags = model_editor.getValueAt(row, 5) == null ? "" : model_editor.getValueAt(row, 5).toString();
                int up = flagNumber(flags, 'h');
                int forward = flagNumber(flags, 'n');
                slider_rotate_up.setValue(up);
                field_rotate_up.setText(String.valueOf(up));
                slider_rotate_fb.setValue(forward);
                field_rotate_fb.setText(String.valueOf(forward));
                boolean is360 = flags.indexOf('o') >= 0
                    || (flags.indexOf('f') < 0 && srow.toLowerCase().contains("video360"));
                checkVideo360.setSelected(is360);
                } finally {
                    updatingControlsFromSelection = false;
                }
	}

        private void playPreviewRow(int row) {
            if (model_show == null || row < 0 || row >= model_show.getRowCount()) return;
            table_playlist_show.setRowSelectionInterval(row, row);
            String srow = model_show.getValueAt(row, 0) + ","
                + model_show.getValueAt(row, 1) + ","
                + model_show.getValueAt(row, 3) + ","
                + model_show.getValueAt(row, 4) + ","
                + model_show.getValueAt(row, 5) + ","
                + model_show.getValueAt(row, 2);
            flag_isVideoFile = 2;
            callBrowser("videoNext", srow);
        }

        private static int flagNumber(String flags, char flag) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile(java.util.regex.Pattern.quote(String.valueOf(flag)) + "(-?\\d+)")
                .matcher(flags == null ? "" : flags);
            if (!matcher.find()) return 0;
            try { return Integer.parseInt(matcher.group(1)); }
            catch (NumberFormatException ex) { return 0; }
        }

        private void updateProjectionFlag(boolean video360) {
            if (!hasCurrentEditorRow()) return;
            Object currentValue = table_playlist_editor.getModel().getValueAt(playlist_currentrow, 5);
            String currentFlags = currentValue == null ? "" : currentValue.toString().trim();
            StringBuilder updated = new StringBuilder();
            for (int i = 0; i < currentFlags.length(); i++) {
                char flag = currentFlags.charAt(i);
                if (flag != 'o' && flag != 'f') updated.append(flag);
            }
            updated.insert(0, video360 ? 'o' : 'f');
            table_playlist_editor.getModel().setValueAt(updated.toString(), playlist_currentrow, 5);
            playlist_editor_change = true;
        }


DefaultTableModel model_editor;
DefaultTableModel model_show;
JTable table_playlist_editor;
boolean playlist_editor_change=false;
boolean updatingControlsFromSelection=false;
private final Set<String> durationRequests = java.util.Collections.synchronizedSet(new HashSet<>());
private final MediaDurationProbe durationProbe = new MediaDurationProbe(CONFIG);
int playlist_currentrow=-1;
JTable table_playlist_show;
JScrollPane leftPane2;
JScrollPane rightPane2;
PlaybackControlPanel slider;

BoundedRangeModel model_slider_360 = new DefaultBoundedRangeModel(0, 0, -180, 180);
JTextField field_rotate_360;
JSlider slider_rotate = new JSlider(model_slider_360);

BoundedRangeModel model_slider_tangage = new DefaultBoundedRangeModel(0, 0, -180, 180);
JTextField field_rotate_tangage;
JSlider slider_rotate_tangage = new JSlider(model_slider_tangage);
BoundedRangeModel model_slider_up = new DefaultBoundedRangeModel(0, 0, -500, 500);
JTextField field_rotate_up;
JSlider slider_rotate_up = new JSlider(model_slider_up);
BoundedRangeModel model_slider_fb = new DefaultBoundedRangeModel(0, 0, -500, 500);
JTextField field_rotate_fb;
JSlider slider_rotate_fb = new JSlider(model_slider_fb);
JCheckBox checkVideo360;
int flag_isVideoFile; // 0 - работаем с файлом напрямую, 1 - из редактора плейлистов, 2 - из плейлистов

File cur_script_name=null;

// filechooser
private  JFileChooser fileChooser = null;
private final String[][] FILTERS = {{"js", "Файлы презентаций"}};
private  JButton  btnSaveFile   = null;
private  JButton  btnGetScript   = null;
private  JButton  btnNewScript   = null;
private  JButton  btnDelete   = null;


public void loadPlaylistIntoEditor(JTable table_playlist_editor, File cur_script_name) {
        if (cur_script_name == null || !cur_script_name.isFile()) {
            JOptionPane.showMessageDialog(this, "Выберите существующий файл плейлиста.");
            return;
        }
        JViewport viewport = leftPane2.getViewport();
        Component[] components = viewport.getComponents();
        ((JPanel)components[0]).setBorder(BorderFactory.createTitledBorder(cur_script_name.toString()));
        DefaultTableModel model = (DefaultTableModel) table_playlist_editor.getModel();
        model.setRowCount(0);
        configurePlaylistColumns(table_playlist_editor);
        try {
            for (PlaylistItem item : PlaylistCodec.read(cur_script_name.toPath())) {
                model.addRow(item.toTableRow());
            }
            playlist_currentrow = model.getRowCount() > 0 ? 0 : -1;
            scanUnknownDurations();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Не удалось прочитать плейлист:\n" + ex.getMessage());
        }
}

private void scanUnknownDurations() {
        for (int row = 0; row < model_editor.getRowCount(); row++) {
            requestDurationForRow(row);
        }
}

private void requestDurationForRow(int row) {
        if (row < 0 || row >= model_editor.getRowCount()) return;
        Object pathValue = model_editor.getValueAt(row, 2);
        Object durationValue = model_editor.getValueAt(row, 6);
        String mediaPath = pathValue == null ? "" : pathValue.toString().trim();
        String duration = durationValue == null ? "" : durationValue.toString().trim();
        if (mediaPath.isEmpty() || !duration.isEmpty() || !durationRequests.add(mediaPath)) return;

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return durationProbe.probe(mediaPath);
            }

            @Override
            protected void done() {
                durationRequests.remove(mediaPath);
                try {
                    String detected = get();
                    if (detected == null || detected.isEmpty()) return;
                    boolean selectedRowUpdated = false;
                    for (int currentRow = 0; currentRow < model_editor.getRowCount(); currentRow++) {
                        Object currentPath = model_editor.getValueAt(currentRow, 2);
                        Object currentDuration = model_editor.getValueAt(currentRow, 6);
                        if (currentPath != null && mediaPath.equals(currentPath.toString().trim())
                            && (currentDuration == null || currentDuration.toString().trim().isEmpty())) {
                            model_editor.setValueAt(detected, currentRow, 6);
                            playlist_editor_change = true;
                            selectedRowUpdated |= currentRow == playlist_currentrow;
                        }
                    }
                    if (selectedRowUpdated) updateControlsFromPlaylistRow(playlist_currentrow);
                } catch (Exception ex) {
                    System.err.println("Не удалось получить длительность " + mediaPath + ": " + ex.getMessage());
                }
            }
        }.execute();
}

public void clearPlaylistEditor(JTable table_playlist_editor) {
        JViewport viewport = leftPane2.getViewport();
        Component[] components = viewport.getComponents();
        ((JPanel)components[0]).setBorder(BorderFactory.createTitledBorder("Новый плейлист"));
        DefaultTableModel model = (DefaultTableModel) table_playlist_editor.getModel();
        model.setRowCount(0);
        configurePlaylistColumns(table_playlist_editor);
        cur_script_name = null;
        playlist_currentrow = -1;
}

private static void configurePlaylistColumns(JTable table) {
        int[] widths = {20, 20, 420, 20, 20, 30, 45, 260};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
}



private String readFromInputStream(InputStream inputStream)
  throws IOException {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br
      = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = br.readLine()) != null) {
            resultStringBuilder.append(line).append("\n");
        }
    }
  return resultStringBuilder.toString();
}
    
    public PlanetariumPlayer() {
        super(new BorderLayout(0, 4));
//        add(createArea());

// ---------------  левая-верхняя панель. Выбор видео файлов --------------------------------------------------------

    FileDragTree tree1 = new FileDragTree();

MouseListener ml1 = new MouseAdapter() {
     public void mousePressed(MouseEvent e) {
         int selRow = tree1.getRowForLocation(e.getX(), e.getY());
         TreePath selPath = tree1.getPathForLocation(e.getX(), e.getY());
		System.out.println("selPath: "+selPath);
         if(selRow != -1) {
             if(e.getClickCount() == 1) {
//                 mySingleClick(selRow, selPath);
childFrame.browser_.setZoomLevel(-1.18); // !!-- поднимаем купол
             }
         }
     }
     public void mouseClicked(MouseEvent e) {
         int selRow = tree1.getRowForLocation(e.getX(), e.getY());
         if(selRow != -1) {
	         TreePath selPath = tree1.getPathForLocation(e.getX(), e.getY());
		int arr=selPath.getPath().length;
		String p="";
		if(arr>2) for(int i=1; i<arr; i++) p+="/"+selPath.getPath()[i];
		else p+="/"+selPath.getPath()[1];

		if(e.getClickCount()==2) {
			p="0, 5, 0, 0,,"+p; 
			callBrowser("videoNext", p);
//			System.out.println(p);
			flag_isVideoFile=0;
		}

	}
    }
 };
 tree1.addMouseListener(ml1);

    MediaFileNode mroot = new MediaFileNode(CONFIG.mediaRoot().toFile());
    mroot.setFlagExt(true);
    mroot.setAllowedExt(new String[] {"webm"});
    FileSystemTreeModel filetree = new FileSystemTreeModel(mroot);
  tree1.setModel(filetree); 

    JComboBox<String> mediaFilter = new JComboBox<>(new String[] {
        "webm", "webm, mp4", "все файлы"
    });
    mediaFilter.setSelectedIndex(0);
    mediaFilter.setToolTipText("Фильтр файлов медиатеки");
    mediaFilter.addActionListener(event -> {
        switch (mediaFilter.getSelectedIndex()) {
            case 0:
                mroot.setFlagExt(true);
                mroot.setAllowedExt(new String[] {"webm"});
                break;
            case 1:
                mroot.setFlagExt(true);
                mroot.setAllowedExt(new String[] {"webm", "mp4"});
                break;
            default:
                mroot.setFlagExt(false);
                mroot.setAllowedExt(new String[0]);
                break;
        }
        filetree.reload();
    });

// ---------------- правая, верхняя панель. Выбор плейлистов из папки h:/player -------------------------------------------------------

    MediaFileNode mroot_scripts = new MediaFileNode(CONFIG.playlistRoot().toFile());
    mroot_scripts.setFlagExt(true);
    String allowext[]={"js"};
    mroot_scripts.setAllowedExt(allowext);

    FileSystemTreeModel scripts_tree = new FileSystemTreeModel(mroot_scripts);
    FileDragTree tree2 = new FileDragTree(scripts_tree);

    MouseListener ml = new MouseAdapter() {
     public void mousePressed(MouseEvent e) {
         int selRow = tree2.getRowForLocation(e.getX(), e.getY());
         TreePath selPath = tree2.getPathForLocation(e.getX(), e.getY());
         if(selRow != -1) {
             if(e.getClickCount() == 1) {
		int arr=selPath.getPath().length;
		String p=""; for(int i=0; i<arr; i++) p+="\\"+selPath.getPath()[i];
		cur_script_name = CONFIG.mediaRoot().resolve(p.replaceFirst("^[\\\\/]+", "")).normalize().toFile();
// меняем заголовок правой панели на имя файла. 
		JViewport viewport = rightPane2.getViewport();
		Component[] components = viewport.getComponents();
		((JPanel)components[0]).setBorder(BorderFactory.createTitledBorder(cur_script_name.toString()));
// выводим содержимое выбранного файла
                DefaultTableModel previewModel = (DefaultTableModel) table_playlist_show.getModel();
                previewModel.setRowCount(0);
                configurePlaylistColumns(table_playlist_show);
                try {
                    for (PlaylistItem item : PlaylistCodec.read(cur_script_name.toPath())) {
                        previewModel.addRow(item.toTableRow());
                    }
                } catch (IOException ex) {
                    System.err.println("Не удалось прочитать плейлист " + cur_script_name + ": " + ex.getMessage());
                }

             } else if(e.getClickCount() == 2) {
		int arr=selPath.getPath().length;
		String p=""; for(int i=0; i<arr; i++) p+="\\"+selPath.getPath()[i];
//		System.out.println(p);
             }
         }
     }
 };
 tree2.addMouseListener(ml);

    JScrollPane mediaTreeScroll = new JScrollPane(tree1);
    JPanel leftPane = new JPanel(new BorderLayout(0, 4));
    JPanel filterRow = new JPanel(new BorderLayout(4, 0));
    filterRow.add(new JLabel("Файлы:"), BorderLayout.WEST);
    filterRow.add(mediaFilter, BorderLayout.CENTER);
    leftPane.add(filterRow, BorderLayout.NORTH);
    leftPane.add(mediaTreeScroll, BorderLayout.CENTER);
    JScrollPane rightPane = new JScrollPane(tree2);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
    splitPane.setResizeWeight(0.5);

//    f.getContentPane().add(splitPane, BorderLayout.CENTER);

// ------------------------------------------------ редактор плейлиста (слева-снизу) ------------------------
    model_editor = new DefaultTableModel() {
	@Override
	public boolean isCellEditable(int row, int column) { // запрещаем редактировать плейлист
		return column == 7;
	}
    };
    model_editor.addTableModelListener(event -> {
        if (event.getType() == javax.swing.event.TableModelEvent.UPDATE
            && event.getColumn() == 7) {
            playlist_editor_change = true;
        }
    });
    table_playlist_editor= new JTable(model_editor);
    table_playlist_editor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // нельзя выделить несколько строк: https://stackoverflow.com/questions/23735247/not-selecting-a-row-in-a-jtable

  table_playlist_editor.addMouseListener(new MouseAdapter() {
    public void mousePressed(MouseEvent mouseEvent) {
        JTable table =(JTable) mouseEvent.getSource();
        Point point = mouseEvent.getPoint();
        int row = table.rowAtPoint(point);
        int column = table.columnAtPoint(point);
	TableModel model = table.getModel();
childFrame.browser_.setZoomLevel(-1.18); // !!-- 
        if (mouseEvent.getClickCount() == 1 && row >= 0) {
                // Keep the highlight on the playing row. Double click remains
                // available for an intentional, immediate track switch.
                if (slider != null && slider.isPlaying()) {
                    final int playingRow = playlist_currentrow;
                    SwingUtilities.invokeLater(() -> {
                        if (playingRow >= 0 && playingRow < table.getRowCount()) {
                            table.setRowSelectionInterval(playingRow, playingRow);
                        }
                    });
                    return;
                }
		playlist_currentrow=row;
		PlaylistTransferHandler th=(PlaylistTransferHandler)table.getTransferHandler();
		th.curIndex=row;		
//		System.out.println("th.curIndex: "+row);
                updateControlsFromPlaylistRow(row);
		
/*
try {
	Process process = Runtime.getRuntime().exec("ffprobe -v quiet -print_format json -show_format -show_streams \"h:/"+srow+"\"");
	BufferedReader reader = new BufferedReader(  new InputStreamReader(process.getInputStream()));
	StringBuilder output = new StringBuilder();
	String line;
	while ((line = reader.readLine()) != null) { output.append(line).append(System.lineSeparator()); }
	process.waitFor();
	reader.close();
	String result = output.toString();
	result.indexOf("duration");

	System.out.println("ffprobe: "+output);
} catch(Exception ex) {System.out.println("error in ffprobe: "+ex);}
*/

//		if(childFrame!=null) {  childFrame.browser_.executeJavaScript("videoInfo('"+srow+"')", "", 1); }
        }

flag_isVideoFile=1;

        if (mouseEvent.getClickCount() == 2 && row >= 0 && column != 7) {
                playlist_currentrow = row;
                updateControlsFromPlaylistRow(row);
		System.out.println("--------------------- PLAY NEW WIDEO-------------------- ");
		slider.playSelected();
        }
    }
  });

    leftPane2 = new JScrollPane(createTable(model_editor, table_playlist_editor)); // редактор плейлиста
    leftPane2.setMinimumSize(new Dimension(0, 0));

//JTable.addActionListener(new MyActionListener());
//        BigObjectThatShouldNotBeSerializedWithAButton bigOne;

// ------------------------------------------------ показываем содержимое плейлиста (справа-внизу) ------------------------
    model_show = new DefaultTableModel() {
	@Override
	public boolean isCellEditable(int row, int column) { // запрещаем редактировать плейлист
//		return column == 3; // можно редактировать только 3 колонку
		return false; // запрещаем редактировать все колонки и строки.
	}
    };
    table_playlist_show= new JTable(model_show);
    table_playlist_show.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // нельзя выделить несколько строк: https://stackoverflow.com/questions/23735247/not-selecting-a-row-in-a-jtable

table_playlist_show.addMouseListener(new MouseAdapter() {
    public void mousePressed(MouseEvent mouseEvent) {
childFrame.browser_.setZoomLevel(-1.18); //!!--
        JTable table =(JTable) mouseEvent.getSource();
        Point point = mouseEvent.getPoint();
        int row = table.rowAtPoint(point);
        int column = table.columnAtPoint(point);
        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
//         TreePath selPath = table.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
//		System.out.println("selPath: "+selPath);
		TableModel model = table.getModel();
		String srow="";
		srow+=model.getValueAt(row, 0)+",";
		srow+=model.getValueAt(row, 1)+",";
		srow+=model.getValueAt(row, 3)+",";
		srow+=model.getValueAt(row, 4)+",";
		srow+=model.getValueAt(row, 5)+",";
		srow+=model.getValueAt(row, 2);
//		for(int i=0; i<table.getColumnCount(); i++) {		
		System.out.println(srow);
		flag_isVideoFile=2;
		callBrowser("videoNext", srow);
        }
    }
});
    rightPane2 = new JScrollPane(createTable(model_show, table_playlist_show)); // просмотр текущего плейлиста
    rightPane2.setMinimumSize(new Dimension(0, 0));

    // DJ mode has its own view of the same model. Keeping a separate JTable
    // avoids reparenting the editor's preview component between layouts.
    JTable tablePlaylistDj = new JTable(model_show);
    tablePlaylistDj.setSelectionModel(table_playlist_show.getSelectionModel());
    tablePlaylistDj.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tablePlaylistDj.setFillsViewportHeight(true);
    tablePlaylistDj.getTableHeader().setReorderingAllowed(false);
    configurePlaylistColumns(tablePlaylistDj);
    tablePlaylistDj.setDragEnabled(true);
    tablePlaylistDj.setTransferHandler(new PlaylistTransferHandler(null));
    tablePlaylistDj.addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent event) {
            int row = tablePlaylistDj.rowAtPoint(event.getPoint());
            if (event.getClickCount() == 2 && row >= 0) playPreviewRow(row);
        }
    });
    JScrollPane djListPane = new JScrollPane(tablePlaylistDj);
    PlaylistPreviewPanel playlistPreview =
        new PlaylistPreviewPanel(tablePlaylistDj, CONFIG, this::playPreviewRow);
    java.awt.CardLayout djPlaylistCardsLayout = new java.awt.CardLayout();
    JPanel djPlaylistCards = new JPanel(djPlaylistCardsLayout);
    djPlaylistCards.add(djListPane, "list");
    djPlaylistCards.add(playlistPreview, "preview");
    JToggleButton djListMode = new JToggleButton("Список", true);
    JToggleButton djPreviewMode = new JToggleButton("Превью");
    ButtonGroup djViewModes = new ButtonGroup();
    djViewModes.add(djListMode);
    djViewModes.add(djPreviewMode);
    JPanel djViewButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 2));
    djViewButtons.add(djListMode);
    djViewButtons.add(djPreviewMode);
    JPanel djPlaylistView = new JPanel(new BorderLayout());
    djPlaylistView.add(djViewButtons, BorderLayout.NORTH);
    djPlaylistView.add(djPlaylistCards, BorderLayout.CENTER);
    djListMode.addActionListener(event -> djPlaylistCardsLayout.show(djPlaylistCards, "list"));
    djPreviewMode.addActionListener(event -> djPlaylistCardsLayout.show(djPlaylistCards, "preview"));



        btnSaveFile = new JButton("Сохранить плейлист");
        // Подключение слушателей к кнопкам

	btnSaveFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		JFrame parentFrame = new JFrame();
		String cur_dir=CONFIG.playlistRoot().toString();
 		if(cur_script_name!=null) cur_dir=cur_script_name.toString();
		JFileChooser fileChooser = new JFileChooser(cur_dir){ // указать директорию в настройках
			@Override
			public void approveSelection(){
			File f = getSelectedFile();
			if(f.exists() && getDialogType() == SAVE_DIALOG){
				int result = JOptionPane.showConfirmDialog(this,"Файл существует, переписать?","Существующий файл",JOptionPane.YES_NO_CANCEL_OPTION);
				switch(result){
					case JOptionPane.YES_OPTION:
					super.approveSelection();
					return;
					case JOptionPane.NO_OPTION:
					return;
					case JOptionPane.CLOSED_OPTION:
					return;
					case JOptionPane.CANCEL_OPTION:
					cancelSelection();
					return;
				}
			}
			super.approveSelection();
			}        
		};
		fileChooser.setSelectedFile(cur_script_name);
		fileChooser.setDialogTitle("Выберите файл для записи");   
 
		int userSelection = fileChooser.showSaveDialog(parentFrame);
 
if (userSelection == JFileChooser.APPROVE_OPTION) {
    File fileToSave = fileChooser.getSelectedFile();
    System.out.println("Save as file: " + fileToSave.getAbsolutePath());

                List<PlaylistItem> items = new ArrayList<>();
                for (int row=0; row<model_editor.getRowCount(); row++) {
                    PlaylistItem item = PlaylistItem.fromTableRow(model_editor, row);
                    if (!item.path.isEmpty()) items.add(item);
                }
                try {
                    PlaylistCodec.write(fileToSave.toPath(), items);
                    cur_script_name = fileToSave;
                    playlist_editor_change = false;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(PlanetariumPlayer.this, "Не удалось сохранить плейлист:\n" + ex.getMessage());
                }
}

    }
});

        btnGetScript= new JButton("Редактировать плейлист");
	btnGetScript.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			if(playlist_editor_change) {
			JDialog checkdialog = new JDialog();
			checkdialog.add(new JLabel( "Плейлист не записан. Продолжить?"), BorderLayout.CENTER);
				JButton no = new JButton("Нет");
				no.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						checkdialog.setVisible(false);
					}
				});
				JButton ok = new JButton("Да");
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						checkdialog.setVisible(false);
						loadPlaylistIntoEditor(table_playlist_editor, cur_script_name);
						playlist_editor_change=false;
					}
				});
				JPanel panel = new JPanel();
				panel.add(no);
				panel.add(ok);
				checkdialog.add(panel, BorderLayout.SOUTH);
				checkdialog.setSize(260, 160);
				checkdialog.setVisible(true); // отобразить диалог
			} else {
						loadPlaylistIntoEditor(table_playlist_editor, cur_script_name);
						playlist_editor_change=false;
			}
		}
	});

        btnNewScript= new JButton("Новый");
	btnNewScript.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			if(playlist_editor_change) {
			JDialog checkdialog = new JDialog();
			checkdialog.add(new JLabel( "Плейлист не записан. Продолжить?"), BorderLayout.CENTER);
				JButton no = new JButton("Нет");
				no.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						checkdialog.setVisible(false);
					}
				});
				JButton ok = new JButton("Да");
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						checkdialog.setVisible(false);
						clearPlaylistEditor(table_playlist_editor);
						playlist_editor_change=false;
					}
				});
				JPanel panel = new JPanel();
				panel.add(no);
				panel.add(ok);
				checkdialog.add(panel, BorderLayout.SOUTH);
				checkdialog.setSize(260, 160);
				checkdialog.setVisible(true); // отобразить диалог
			} else {
						clearPlaylistEditor(table_playlist_editor);
						playlist_editor_change=false;
			}
		}
	});


        btnDelete= new JButton("X");
	btnDelete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			System.out.println("remove row: "+playlist_currentrow);
                        if (playlist_currentrow >= 0 && playlist_currentrow < model_editor.getRowCount()) {
			    model_editor.removeRow(playlist_currentrow);
                            playlist_currentrow = Math.min(playlist_currentrow, model_editor.getRowCount() - 1);
			    playlist_editor_change=true;
                        }
		}
	});



// fulldome или 360

checkVideo360 = new JCheckBox("360");
checkVideo360.addItemListener(new ItemListener() {
    public void itemStateChanged(ItemEvent e) {
        if (updatingControlsFromSelection) return;
        boolean video360 = checkVideo360.isSelected();
	updateProjectionFlag(video360);
	setVideo360(video360);
    }
});


// размещаем панель редактирования и кнопки записи
        JPanel contents = new JPanel();
        JToggleButton modeEditor = new JToggleButton("Редактор", true);
        JToggleButton modePlayer = new JToggleButton("Плеер");
        JToggleButton modeDj = new JToggleButton("Диджей");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(modeEditor);
        modeGroup.add(modePlayer);
        modeGroup.add(modeDj);
        JPanel modeButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
        modeButtons.add(modeEditor);
        modeButtons.add(modePlayer);
        modeButtons.add(modeDj);

	GridBagLayout gbl = new GridBagLayout();
	contents.setLayout(gbl);
	GridBagConstraints c = new GridBagConstraints();    

	c.anchor = GridBagConstraints.NORTHWEST; 
	c.fill   = GridBagConstraints.NONE;  
	c.ipadx = 32;
	gbl.setConstraints(btnSaveFile , c);
        contents.add(btnSaveFile  );
	gbl.setConstraints(btnGetScript , c);
        contents.add(btnGetScript  );
	gbl.setConstraints(btnNewScript , c);
        contents.add(btnNewScript  );
	c.gridwidth  = 1;
	gbl.setConstraints(btnDelete, c);
        contents.add(btnDelete);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        Component modeSpacer = Box.createHorizontalGlue();
        gbl.setConstraints(modeSpacer, c);
        contents.add(modeSpacer);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(modeButtons, c);
        contents.add(modeButtons);

	c.fill   = GridBagConstraints.BOTH;  
	c.weighty = 1.0; // занимаем все по высоте
	c.weightx = 1.0; // занимаем все по ширине
	c.gridheight = 1;
	c.gridwidth  = GridBagConstraints.REMAINDER; 
	gbl.setConstraints(leftPane2, c);
        contents.add(leftPane2 );
        // The wide toolbar must not become the minimum width of the editor.
        // Otherwise JSplitPane cannot reveal or resize the playlist preview.
        contents.setMinimumSize(new Dimension(0, 0));

// Рабочая область перестраивается без пересоздания таблиц и деревьев.
    JPanel workspace = new JPanel(new BorderLayout());
    java.util.function.Consumer<String> applyMode = mode -> {
        workspace.removeAll();
        detachComponent(leftPane);
        detachComponent(rightPane);
        detachComponent(contents);
        detachComponent(rightPane2);
        detachComponent(djPlaylistView);

        if ("player".equals(mode)) {
            workspace.add(contents, BorderLayout.CENTER);
        } else {
            JPanel rows = new JPanel(new GridLayout(2, 1, 0, 4));
            if ("dj".equals(mode)) {
                djPlaylistCardsLayout.show(
                    djPlaylistCards, djPreviewMode.isSelected() ? "preview" : "list");
                JSplitPane djTop = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, rightPane, djPlaylistView);
                djTop.setResizeWeight(1.0 / 6.0);
                djTop.setDividerLocation(1.0 / 6.0);
                rows.add(djTop);
                rows.add(contents);
            } else {
                JSplitPane editorTop = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
                editorTop.setResizeWeight(0.5);
                editorTop.setDividerLocation(0.5);
                JSplitPane editorBottom = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, contents, rightPane2);
                editorBottom.setResizeWeight(0.5);
                editorBottom.setDividerLocation(0.5);
                rows.add(editorTop);
                rows.add(editorBottom);
                // At construction time the split panes have zero width.
                // Reapply the proportions after Swing has laid them out.
                SwingUtilities.invokeLater(() -> {
                    if (editorTop.isShowing()) editorTop.setDividerLocation(0.5);
                    if (editorBottom.isShowing()) editorBottom.setDividerLocation(0.5);
                });
            }
            workspace.add(rows, BorderLayout.CENTER);
        }
        workspace.revalidate();
        workspace.repaint();
    };
    modeEditor.addActionListener(event -> applyMode.accept("editor"));
    modePlayer.addActionListener(event -> applyMode.accept("player"));
    modeDj.addActionListener(event -> applyMode.accept("dj"));
    add(workspace, BorderLayout.CENTER);
    applyMode.accept("editor");

/*
// -------------------------------------  Кнопка Play. Запускает текущее (выбранное) видео из редактора
    buttonNext = new Button();
    buttonNext.addActionListener(e -> {
		playVideo();
        });
    add(buttonNext);
*/
// перемотка, play, pause, stop
    slider = new PlaybackControlPanel(this);

// поворот по горизонтали
    JPanel contents_rotate360 = new JPanel();
    contents_rotate360.add(slider_rotate);
    field_rotate_360 = new JTextField(2);
    field_rotate_360.setToolTipText("Поворот по горизонтали");
    field_rotate_360.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                        if (updatingControlsFromSelection) return;
			playlist_editor_change=true;
			int foo; try {   foo = Integer.parseInt(field_rotate_360.getText()); } catch (NumberFormatException ne) {   foo = 0; }
			slider_rotate.setValue(foo);
			callBrowser("setRotate360", foo);
			if (hasCurrentEditorRow()) table_playlist_editor.getModel().setValueAt(foo, playlist_currentrow, 3);
		}
	});

	slider_rotate.addChangeListener(
		new ChangeListener() { public void stateChanged(ChangeEvent e) { 
                                if (updatingControlsFromSelection) return;
				playlist_editor_change=true;
				int value = ((JSlider)e.getSource()).getValue(); 
				field_rotate_360.setText(""+value);
				callBrowser("setRotate360", value);
				if (hasCurrentEditorRow()) table_playlist_editor.getModel().setValueAt(value, playlist_currentrow, 3);
			}
		}
	);
	contents_rotate360.add(field_rotate_360);


// тангаж - наклон сферы
    JPanel contents_rotate_tangage = new JPanel();
    contents_rotate_tangage.add(slider_rotate_tangage);

    field_rotate_tangage = new JTextField(2);
    field_rotate_tangage.setToolTipText("Тангаж");

	field_rotate_tangage.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                        if (updatingControlsFromSelection) return;
			int foo; try {   foo = Integer.parseInt(field_rotate_tangage.getText()); } catch (NumberFormatException ne) {   foo = 0; }
			slider_rotate.setValue(foo);
			callBrowser("setRotateTangage", foo);
			if (hasCurrentEditorRow()) table_playlist_editor.getModel().setValueAt(foo, playlist_currentrow, 4);
		}
	});

	slider_rotate_tangage.addChangeListener(
		new ChangeListener() { public void stateChanged(ChangeEvent e) { 
                                if (updatingControlsFromSelection) return;
				playlist_editor_change=true;
				int value = ((JSlider)e.getSource()).getValue(); field_rotate_tangage.setText(""+value);
				callBrowser("setRotateTangage", value);
				if (hasCurrentEditorRow()) table_playlist_editor.getModel().setValueAt(value, playlist_currentrow, 4);
			}
		}
	);
	contents_rotate_tangage.add(field_rotate_tangage);

// подъем сферы вверх-вниз
    JPanel contents_rotate_up = new JPanel();
    contents_rotate_up.add(slider_rotate_up);

    field_rotate_up = new JTextField(2);
    field_rotate_up.setToolTipText("подъем");

	field_rotate_up.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                        if (updatingControlsFromSelection) return;
			int foo; try {   foo = Integer.parseInt(field_rotate_up.getText()); } catch (NumberFormatException ne) {   foo = 0; }
			slider_rotate.setValue(foo);
			callBrowser("setRotateUp", foo);
		}
	});

	slider_rotate_up.addChangeListener(
		new ChangeListener() { public void stateChanged(ChangeEvent e) { int value = ((JSlider)e.getSource()).getValue(); field_rotate_up.setText(""+value);
                                if (updatingControlsFromSelection) return;
				callBrowser("setRotateUp", value);
				playlist_editor_change=true;
			}
		}
	);
	contents_rotate_up.add(field_rotate_up);

// продвижение сферы вперед-назад
    JPanel contents_rotate_fb = new JPanel();
    contents_rotate_fb.add(slider_rotate_fb);

    field_rotate_fb = new JTextField(2);
    field_rotate_fb.setToolTipText("вперед-назад");

	field_rotate_fb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                        if (updatingControlsFromSelection) return;
			int foo; try {   foo = Integer.parseInt(field_rotate_fb.getText()); } catch (NumberFormatException ne) {   foo = 0; }
			slider_rotate.setValue(foo);
			callBrowser("setRotateFB", foo);
		}
	});

	slider_rotate_fb.addChangeListener(
		new ChangeListener() { public void stateChanged(ChangeEvent e) { int value = ((JSlider)e.getSource()).getValue(); field_rotate_fb.setText(""+value);
                                if (updatingControlsFromSelection) return;
				callBrowser("setRotateFB", value);
				playlist_editor_change=true;
			}
		}
	);
	contents_rotate_fb.add(field_rotate_fb);

// размещаем слайдеры друг под другом
        JPanel content_sliders = new JPanel();
	GridBagLayout gbl_sl = new GridBagLayout();
	content_sliders.setLayout(gbl_sl);
	GridBagConstraints c_sl = new GridBagConstraints();    

	c_sl.anchor = GridBagConstraints.NORTHWEST; 
	c_sl.fill   = GridBagConstraints.NONE;  
	c_sl.ipadx = 32;
	c_sl.gridheight = 1;
	c_sl.gridwidth  = GridBagConstraints.REMAINDER; 
	c_sl.weightx = 1.0; // занимаем все по ширине

	gbl_sl.setConstraints(slider , c_sl); // перемотка и начала и конец видео
        content_sliders.add(slider);

	c_sl.gridwidth  = GridBagConstraints.REMAINDER; 
	gbl_sl.setConstraints(checkVideo360, c_sl);
	content_sliders.add(checkVideo360);


	gbl_sl.setConstraints(contents_rotate360 , c_sl);
        content_sliders.add(contents_rotate360);


	c_sl.gridwidth  = GridBagConstraints.REMAINDER; 
	gbl_sl.setConstraints(contents_rotate_tangage, c_sl);
        content_sliders.add(contents_rotate_tangage);

	c_sl.gridwidth  = GridBagConstraints.REMAINDER; 
	gbl_sl.setConstraints(contents_rotate_up, c_sl);
        content_sliders.add(contents_rotate_up);

	c_sl.gridwidth  = GridBagConstraints.REMAINDER; 
	gbl_sl.setConstraints(contents_rotate_fb, c_sl);
        content_sliders.add(contents_rotate_fb);

        add(content_sliders, BorderLayout.SOUTH);

/*
	add(slider);
	add(contents_rotate360);
	add(contents_rotate_tangage);
	add(contents_rotate_up);
	add(contents_rotate_fb);
*/


////////  END ////////////////////////////////////////////
}



         class MyActionListener implements ActionListener
         {
             public void actionPerformed(ActionEvent e)
             {
                 System.out.println("Hello There");
JViewport viewport = rightPane2.getViewport();
Component[] components = viewport.getComponents();
((JPanel)components[0]).setBorder(BorderFactory.createTitledBorder("Hello There"));

             }
         }
    
   
    private JPanel createArea() {
        String text = "This is the text that I want to show.";
        
        JTextArea area = new JTextArea();
        area.setText(text);
        area.setDragEnabled(true);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(400,100));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Text Area"));
        return panel;
    }

    private JPanel createTable(DefaultTableModel model, JTable table) {
        
        model.addColumn("Начало");
        model.addColumn("Конец");
        model.addColumn("Видео");
        model.addColumn("поворот по горизонтали");
        model.addColumn("поворот по вертикали");
        model.addColumn("Флаги");
        model.addColumn("Длительность");
        model.addColumn("Комментарий");

        
        table.getTableHeader().setReorderingAllowed(false);
        // Keep an empty table as a full-size drop target. The old implementation
        // needed placeholder rows; INSERT_ROWS can insert directly at index 0.
        table.setFillsViewportHeight(true);
//        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	configurePlaylistColumns(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400,100));

        table.setDragEnabled(true);
        if (table == table_playlist_editor) {
            UIManager.put("Table.dropLineColor", new Color(0, 120, 215));
            UIManager.put("Table.dropLineShortColor", new Color(0, 120, 215));
            table.setDropMode(DropMode.INSERT_ROWS);
            table.setTransferHandler(new PlaylistTransferHandler(row -> {
                playlist_editor_change = true;
                requestDurationForRow(row);
            }));
        } else {
            table.setTransferHandler(new PlaylistTransferHandler(null));
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Table"));
        return panel;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Плеер планетария");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
//        newContentPane = new PlanetariumPlayer();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        if (!CefApp.startup(args)) {
            System.out.println("Startup initialization failed!");
            return;
        }
Locale.setDefault(Locale.US);
DarkSwingTheme.install();


/*
        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefAppState.TERMINATED) System.exit(0);
            }
        });
*/
        newContentPane = new PlanetariumPlayer();

    JcefApplicationHandler appHandler = new JcefApplicationHandler(args);
    CefApp.addAppHandler(appHandler);

        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = false;
	settings.remote_debugging_port = CONFIG.remoteDebuggingPort();
        CefApp cefApp_ = CefApp.getInstance(settings);

        // The simple example application is created as anonymous class and points
        // to Google as the very first loaded page. Windowed rendering mode is used by
        // default. If you want to test OSR mode set |useOsr| to true and recompile.
        boolean useOsr = false;
	try {
                String startUrl = CONFIG.playerUrl();
                if (args.length > 0 && !args[0].isBlank()) {
                    int queryStart = startUrl.indexOf('?');
                    String baseUrl = queryStart >= 0 ? startUrl.substring(0, queryStart) : startUrl;
                    startUrl = args[0].startsWith("http://") || args[0].startsWith("https://")
                        ? args[0]
                        : baseUrl + "?playlist=" + java.net.URLEncoder.encode(args[0], StandardCharsets.UTF_8);
                }
	        childFrame = new DomeBrowserWindow(cefApp_, startUrl, useOsr, false, true);
	} catch(Exception ex) {System.out.println("present url");}
	childFrame.setParent(newContentPane);

	childFrame.setfocus(true);

	childFrame.toFront();
	childFrame.requestFocus();
	childFrame.setAlwaysOnTop(true); 
	childFrame.setVisible(true);

	childFrame.toFront();
 	childFrame.setState(JFrame.NORMAL); 
	childFrame.requestFocusInWindow();





        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}

