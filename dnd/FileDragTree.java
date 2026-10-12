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
import javax.swing.JButton;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

    import java.io.Serializable;
    import java.awt.event.*;


/**
 * File-system tree with drag support used by media and playlist browsers.
 */
class FileDragTree extends JTree implements Autoscroll {

  private Insets insets;

  private int top = 0, bottom = 0, topRow = 0, bottomRow = 0;

  public FileDragTree() {
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, new TreeDragGestureListener());
    DropTarget dropTarget = new DropTarget(this,new TreeDropTargetListener());
  }

  public FileDragTree(TreeModel model) {
    super(model);
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, new TreeDragGestureListener());
    DropTarget dropTarget = new DropTarget(this, new TreeDropTargetListener());
  }

  public Insets getAutoscrollInsets() {
    return insets;
  }

  public void autoscroll(Point p) {
    // Only support up/down scrolling
    top = Math.abs(getLocation().y) + 10;
    bottom = top + getParent().getHeight() - 20;
    int next;
    if (p.y < top) {
      next = topRow--;
      bottomRow++;
      scrollRowToVisible(next);
    } else if (p.y > bottom) {
      next = bottomRow++;
      topRow--;
      scrollRowToVisible(next);
    }
  }

  private static class TreeDragGestureListener implements DragGestureListener {
    public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
      // Can only drag leafs
      JTree tree = (JTree) dragGestureEvent.getComponent();
        System.out.println("-------------- 88 "+tree);

      TreePath path = tree.getSelectionPath(); //TreePath представляет массив объектов, которые однозначно идентифицируют путь к узлу в дереве.
      if (path == null) {
        // Nothing selected, nothing to drag
        System.out.println("Nothing selected - beep");
        tree.getToolkit().beep();
      } else {


/*
String jTreeVarSelectedPath = "";
    Object[] paths = path.getPath();
    for (int i=0; i<paths.length; i++) {
        jTreeVarSelectedPath += paths[i];
        if (i+1 <paths.length ) {
            jTreeVarSelectedPath += File.separator;
        }
    }
System.out.println("path: "+jTreeVarSelectedPath);
*/
System.out.println("path in TreeDragGestureListener: "+path);

        DefaultMutableTreeNode selection = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (selection.isLeaf()) {
System.out.println("selection: "+selection.getLevel());
          TransferableTreeNod node = new TransferableTreeNod( path);
          dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, node, new MyDragSourceListener());
        } else {
          System.out.println("Not a leaf - beep");
          tree.getToolkit().beep();
        }
      }
    }
  }



  private class TreeDropTargetListener implements DropTargetListener {

    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
      // Setup positioning info for auto-scrolling
      top = Math.abs(getLocation().y);
      bottom = top + getParent().getHeight();
      topRow = getClosestRowForLocation(0, top);
      bottomRow = getClosestRowForLocation(0, bottom);
      insets = new Insets(top + 10, 0, bottom - 10, getWidth());
    }

    public void dragExit(DropTargetEvent dropTargetEvent) {
    }

    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
    }

    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
    }

    public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
      // Only support dropping over nodes that aren't leafs

        System.out.println("----------------- 3");
      Point location = dropTargetDropEvent.getLocation();
      TreePath path = getPathForLocation(location.x, location.y);
      Object node = path.getLastPathComponent();
      if ((node != null) && (node instanceof TreeNode)
          && (!((TreeNode) node).isLeaf())) {
        try {
        System.out.println("----------------- 4");

          Transferable tr = dropTargetDropEvent.getTransferable();
          if (tr.isDataFlavorSupported(TransferableTreeNode.DEFAULT_MUTABLE_TREENODE_FLAVOR)) {
            dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Object userObject = tr.getTransferData(TransferableTreeNode.DEFAULT_MUTABLE_TREENODE_FLAVOR);
            addElement(path, userObject);
            dropTargetDropEvent.dropComplete(true);
          } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            String string = (String) tr.getTransferData(DataFlavor.stringFlavor);
            addElement(path, string);
            dropTargetDropEvent.dropComplete(true);
          } else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dropTargetDropEvent
                .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            List fileList = (List) tr
                .getTransferData(DataFlavor.javaFileListFlavor);
            Iterator iterator = fileList.iterator();
            while (iterator.hasNext()) {
              File file = (File) iterator.next();
              addElement(path, file.toURI().toURL());
        System.out.println("-----------------5 "+file.toURI().toURL());

            }
            dropTargetDropEvent.dropComplete(true);
          } else {
            System.err.println("Rejected");
            dropTargetDropEvent.rejectDrop();
          }
        } catch (IOException io) {
          io.printStackTrace();
          dropTargetDropEvent.rejectDrop();
        } catch (UnsupportedFlavorException ufe) {
          ufe.printStackTrace();
          dropTargetDropEvent.rejectDrop();
        }
      } else {
        System.out.println("Can't drop on a leaf");
        dropTargetDropEvent.rejectDrop();
      }
    }

    private void addElement(TreePath path, Object element) {
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);
      System.out.println("Added: " + node + " to " + parent);
      DefaultTreeModel model = (DefaultTreeModel) (FileDragTree.this.getModel());
      model.insertNodeInto(node, parent, parent.getChildCount());
    }
  }

  private static class MyDragSourceListener implements DragSourceListener {
    public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) {
      if (dragSourceDropEvent.getDropSuccess()) {
        int dropAction = dragSourceDropEvent.getDropAction();
        if (dropAction == DnDConstants.ACTION_MOVE) {
          System.out.println("MOVE: remove node"+dragSourceDropEvent);

        }
      }
    }

    public void dragEnter(DragSourceDragEvent dragSourceDragEvent) {
      DragSourceContext context = dragSourceDragEvent
          .getDragSourceContext();
      int dropAction = dragSourceDragEvent.getDropAction();
      if ((dropAction & DnDConstants.ACTION_COPY) != 0) {
        context.setCursor(DragSource.DefaultCopyDrop);
      } else if ((dropAction & DnDConstants.ACTION_MOVE) != 0) {
        context.setCursor(DragSource.DefaultMoveDrop);
      } else {
        context.setCursor(DragSource.DefaultCopyNoDrop);
      }
    }

    public void dragExit(DragSourceEvent dragSourceEvent) {
    }

    public void dragOver(DragSourceDragEvent dragSourceDragEvent) {
    }

    public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) {
    }
  }
}


class TransferableTreeNod extends TreePath implements
    Transferable {
  final static int TREE = 0;
  final static int FILE = 1;
  final static int STRING = 2;
  final static int PLAIN_TEXT = 2;

  final public static DataFlavor DEFAULT_MUTABLE_TREENODE_FLAVOR = new DataFlavor( DefaultMutableTreeNode.class, "Default Mutable Tree Node");

  final public static DataFlavor FILE_FLAVOR = new DataFlavor( File.class, "Default Mutable File Node");

  static DataFlavor flavors[] = { DEFAULT_MUTABLE_TREENODE_FLAVOR, FILE_FLAVOR,
      DataFlavor.stringFlavor };

  private TreePath data;

  public TransferableTreeNod(TreePath data) {
    this.data = data;

  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {


String jTreeVarSelectedPath = "";
    Object[] paths = data.getPath();
    for (int i=0; i<paths.length; i++) {
        jTreeVarSelectedPath += paths[i];
        if (i>0 && (i+1 <paths.length) ) {
            jTreeVarSelectedPath += "/";//File.separator;
        }
    }
System.out.println("path: "+jTreeVarSelectedPath);

    Object returnObject = jTreeVarSelectedPath;
/*
    if (flavor.equals(flavors[TREE])) {
      Object userObject = data.getUserObject();
      if (userObject == null) {
        returnObject = data;
      } else {
        returnObject = userObject;
      }
    } else if (flavor.equals(flavors[FILE])) {
      Object userObject = data.getUserObject();
      if (userObject == null) {
        returnObject = data.toString();
      } else {
        returnObject = userObject.toString();
      }
    } else if (flavor.equals(flavors[STRING])) {
        System.out.println("data: "+data.getLevel());


Object elements[] = data.getPath();
		for (int i = 0, n = elements.length; i < n; i++) {
                System.out.print("->" + elements[i]);
///	        value+=elements[i]+"\\";
        }

      Object userObject = data.getUserObject();
      if (userObject == null) {
        returnObject = data.toString();
      } else {
        returnObject = userObject.toString();
      }
    } else if (flavor.equals(flavors[PLAIN_TEXT])) {
      Object userObject = data.getUserObject();
      String string;
      if (userObject == null) {
        string = data.toString();
      } else {
        string = userObject.toString();
      }
      returnObject = new ByteArrayInputStream(string.getBytes("Unicode"));
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
        System.out.println("----------------- 6"+returnObject);
*/
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    boolean returnValue = false;
    for (int i = 0, n = flavors.length; i < n; i++) {
      if (flavor.equals(flavors[i])) {
        returnValue = true;
        break;
      }
    }
    return returnValue;
  }
}



class TransferableTreeNode extends DefaultMutableTreeNode implements
    Transferable {
  final static int TREE = 0;
  final static int FILE = 1;
  final static int STRING = 2;
  final static int PLAIN_TEXT = 2;

  final public static DataFlavor DEFAULT_MUTABLE_TREENODE_FLAVOR = new DataFlavor( DefaultMutableTreeNode.class, "Default Mutable Tree Node");

  final public static DataFlavor FILE_FLAVOR = new DataFlavor( File.class, "Default Mutable File Node");

  static DataFlavor flavors[] = { DEFAULT_MUTABLE_TREENODE_FLAVOR, FILE_FLAVOR,
      DataFlavor.stringFlavor };

  private DefaultMutableTreeNode data;

  public TransferableTreeNode(DefaultMutableTreeNode data) {
    this.data = data;

  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    Object returnObject;
    if (flavor.equals(flavors[TREE])) {
      Object userObject = data.getUserObject();
      if (userObject == null) {
        returnObject = data;
      } else {
        returnObject = userObject;
      }
    } else if (flavor.equals(flavors[FILE])) {
      Object userObject = data.getUserObject();
      if (userObject == null) {
        returnObject = data.toString();
      } else {
        returnObject = userObject.toString();
      }
    } else if (flavor.equals(flavors[STRING])) {
        System.out.println("data: "+data.getLevel());


Object elements[] = data.getPath();
		for (int i = 0, n = elements.length; i < n; i++) {
                System.out.print("->" + elements[i]);
///	        value+=elements[i]+"\\";
        }

      Object userObject = data.getUserObject();
      if (userObject == null) {
        returnObject = data.toString();
      } else {
        returnObject = userObject.toString();
      }
    } else if (flavor.equals(flavors[PLAIN_TEXT])) {
      Object userObject = data.getUserObject();
      String string;
      if (userObject == null) {
        string = data.toString();
      } else {
        string = userObject.toString();
      }
      returnObject = new ByteArrayInputStream(string.getBytes("Unicode"));
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
        System.out.println("----------------- 6"+returnObject);
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    boolean returnValue = false;
    for (int i = 0, n = flavors.length; i < n; i++) {
      if (flavor.equals(flavors[i])) {
        returnValue = true;
        break;
      }
    }
    return returnValue;
  }

}



