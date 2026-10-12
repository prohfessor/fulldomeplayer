package dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

abstract class StringTransferHandler extends TransferHandler {
    
    protected abstract String exportString(JComponent c);
    protected abstract void importString(JComponent c, String str);
    protected abstract void cleanup(JComponent c, boolean remove);
    
    protected Transferable createTransferable(JComponent c) {
        return new StringSelection(exportString(c));
    }
    
    public int getSourceActions(JComponent c) {
        return COPY;
    }
    
    public boolean importData(JComponent c, Transferable t) { // вызывается при drag&drop при бросании
System.out.println("importData");
        if (canImport(c, t.getTransferDataFlavors())) {
//System.out.println("canImport == true");

            try {
                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
System.out.println("вызываем importString");
                importString(c, str);
                return true;
            } catch (UnsupportedFlavorException ufe) {
            } catch (IOException ioe) {
            }
        }

        return false;
    }
     
    protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }
    
    public boolean canImport(JComponent c, DataFlavor[] flavors) { // показывает, можно ли бросить в это место
//System.out.println("canImport");

        for (int i = 0; i < flavors.length; i++) {
            if (DataFlavor.stringFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Transfers media paths and playlist rows into the editor table.
 *
 * <p>JTable's INSERT_ROWS drop mode supplies the exact insertion boundary,
 * allowing the UI to show and honor the blue line above or below a row.</p>
 */
class PlaylistTransferHandler extends StringTransferHandler {
    private static final String PLAYLIST_ITEM_PREFIX = "planetarium-playlist-item:";
    private int[] rows = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.
    public int curIndex = -1;  // в какую строку ткнули
    boolean flagChangeTable=false;
    private boolean internalTableDrag = false;
    private final IntConsumer changeListener;

    PlaylistTransferHandler(IntConsumer changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable)c;
        curIndex = table.getSelectedRow();
        internalTableDrag = curIndex >= 0;
        return super.createTransferable(c);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop() || !(support.getComponent() instanceof JTable)) return false;
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) return false;
        support.setDropAction(COPY);
        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;
        JTable target = (JTable)support.getComponent();
        JTable.DropLocation location = (JTable.DropLocation)support.getDropLocation();
        int insertionIndex = Math.max(0, Math.min(location.getRow(), target.getRowCount()));
        try {
            String value = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            importAt(target, value, insertionIndex);
            target.repaint();
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            System.err.println("Cannot drop playlist row: " + ex.getMessage());
            return false;
        }
    }

    private void importAt(JTable target, String value, int insertionIndex) {
        DefaultTableModel model = (DefaultTableModel)target.getModel();
        int selectedIndex;

        if (internalTableDrag && curIndex >= 0 && curIndex < model.getRowCount()) {
            selectedIndex = insertionIndex > curIndex ? insertionIndex - 1 : insertionIndex;
            selectedIndex = Math.max(0, Math.min(selectedIndex, model.getRowCount() - 1));
            if (selectedIndex != curIndex) model.moveRow(curIndex, curIndex, selectedIndex);
        } else {
            Object[] normalized;
            if (value.startsWith(PLAYLIST_ITEM_PREFIX)) {
                PlaylistItem item = PlaylistCodec.parse(
                    value.substring(PLAYLIST_ITEM_PREFIX.length()));
                if (item == null) return;
                normalized = item.toTableRow();
            } else {
                String row = value;
                if (row.indexOf(',') < 0) row = "0,0,/" + row + ",0,0,";
                String[] fields = row.split(",", -1);
                normalized = new Object[8];
                for (int i = 0; i < normalized.length; i++) {
                    normalized[i] = i < fields.length ? fields[i].trim() : "";
                }
            }
            model.insertRow(insertionIndex, normalized);
            selectedIndex = insertionIndex;
        }

        target.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
        curIndex = selectedIndex;
        flagChangeTable = true;
        if (changeListener != null) changeListener.accept(selectedIndex);
    }

    protected String exportString(JComponent c) {
        JTable table = (JTable)c;
        rows = table.getSelectedRows();
        if (rows.length == 1) {
            return encodePlaylistItem(table.getModel(), rows[0]);
        }
        int colCount = table.getColumnCount();
        
        StringBuffer buff = new StringBuffer();
        
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < colCount; j++) {
                Object val = table.getValueAt(rows[i], j);
                buff.append(val == null ? "" : val.toString());
                if (j != colCount - 1) {
                    buff.append(",");
                }
            }
            if (i != rows.length - 1) {
                buff.append("\n");
            }
        }
        
        return buff.toString();
    }

    static String encodePlaylistItem(javax.swing.table.TableModel model, int row) {
        return PLAYLIST_ITEM_PREFIX + PlaylistItem.fromTableRow(model, row).serialize();
    }

    protected void importString(JComponent c, String str) { // в этом месте вставляем строку в таблицу при бросании при drag&drop
        System.out.println("PlaylistTransferHandler import: "+str);
        JTable target = (JTable)c;
        DefaultTableModel model = (DefaultTableModel)target.getModel();
        int index = target.getSelectedRow(); // номер строки, в которую перенесли данные
        System.out.println("index: "+index);
/*
        if (rows != null && index >= rows[0] - 1 && index <= rows[rows.length - 1]) {
            rows = null;
            return;
        }
*/
        int max = model.getRowCount();
        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
//                index = max;
            }
        }

/*
        String[] values = new String[6]; 
	String[] fields = new String[6];
	fields[0]="0"; fields[1]="0"; fields[2]=selPath.getPath()[1].toString(); fields[3]="0";  fields[4]="0"; fields[5]=""; model_editor.addRow(fields); 
System.out.println("asdf: "+model_editor.getRowCount());
*/
/* добавляет в таблицу несколько строк. Нам это не надо
        addIndex = index;
        String[] values = str.split("\n");
        addCount = values.length;
        int colCount = target.getColumnCount();
        for (int i = 0; i < values.length && i < colCount; i++) {
            model.insertRow(index++, values[i].split(","));
        }
*/

	if(str.indexOf(",")<0) str="0, 0,/"+str.toString()+", 0, 0, ";
        System.out.println("index newx: "+index);
        System.out.println("curIndex: "+curIndex);
//        System.out.println("row: "+model.getValueAt(index, 2));

        if(curIndex==-1) model.insertRow(index-1, str.split(",")); // строка перемещается из правого плейлиста (curIndex назначается при клике на левую таблицу)
        model.moveRow(curIndex, curIndex, index-1);           // перемещаем строки внутри таблицы
	curIndex=-1;
	flagChangeTable=true;
    }

    protected void cleanup(JComponent c, boolean remove) {
        JTable source = (JTable)c;
        if (remove && rows != null) {
            DefaultTableModel model =
                 (DefaultTableModel)source.getModel();

            //If we are moving items around in the same table, we
            //need to adjust the rows accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < rows.length; i++) {
                    if (rows[i] > addIndex) {
                        rows[i] += addCount;
                    }
                }
            }
            for (int i = rows.length - 1; i >= 0; i--) {
                model.removeRow(rows[i]);
            }
        }
        rows = null;
        addCount = 0;
        addIndex = -1;
        curIndex = -1;
        internalTableDrag = false;
    }
}

/**
 * Lazy Swing tree model backed by directories on disk.
 */
