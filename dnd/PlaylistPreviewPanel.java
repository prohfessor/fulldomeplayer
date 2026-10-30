package dnd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

/**
 * Scrollable interactive storyboard cards for the selected playlist.
 */
final class PlaylistPreviewPanel extends JScrollPane {
    private static final int CARD_WIDTH = 250;
    private final JPanel cards = new JPanel();
    private final JTable sourceTable;
    private final PreviewStoryboardCache cache;
    private final IntConsumer playRow;
    private final Timer rebuildTimer;

    PlaylistPreviewPanel(JTable sourceTable, PlayerConfig config, IntConsumer playRow) {
        this.sourceTable = sourceTable;
        this.cache = new PreviewStoryboardCache(config);
        this.playRow = playRow;
        cards.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setViewportView(cards);
        getVerticalScrollBar().setUnitIncrement(24);

        rebuildTimer = new Timer(80, event -> rebuild());
        rebuildTimer.setRepeats(false);
        sourceTable.getModel().addTableModelListener(event -> rebuildTimer.restart());
        sourceTable.getSelectionModel().addListSelectionListener(event -> cards.repaint());
        getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) {
                updateColumns();
            }
        });
        rebuild();
    }

    private void rebuild() {
        cards.removeAll();
        TableModel model = sourceTable.getModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            cards.add(new PreviewCard(row, model));
        }
        updateColumns();
        cards.revalidate();
        cards.repaint();
    }

    private void updateColumns() {
        int width = Math.max(CARD_WIDTH, getViewport().getWidth() - 16);
        int columns = Math.max(1, width / CARD_WIDTH);
        cards.setLayout(new java.awt.GridLayout(0, columns, 8, 8));
        cards.revalidate();
    }

    private final class PreviewCard extends JComponent {
        private static final int IMAGE_X = 8;
        private static final int IMAGE_Y = 8;
        private static final int IMAGE_WIDTH = 224;
        private static final int IMAGE_HEIGHT = 126;

        private final int row;
        private final String title;
        private final String comment;
        private final double duration;
        private final Timer animation;
        private BufferedImage storyboard;
        private String previewError = "";
        private String previewCommand = "";
        private int frame = 4;
        private boolean loading = true;
        private Point dragOrigin;
        private boolean dragStarted;

        PreviewCard(int row, TableModel model) {
            this.row = row;
            String path = cell(model, row, 2);
            this.title = fileName(path);
            this.comment = model.getColumnCount() > 7 ? cell(model, row, 7) : "";
            this.duration = parse(model.getColumnCount() > 6 ? cell(model, row, 6) : "");
            setPreferredSize(new Dimension(240, comment.isEmpty() ? 174 : 192));
            setToolTipText(path);
            setTransferHandler(new TransferHandler() {
                @Override protected Transferable createTransferable(JComponent component) {
                    return new StringSelection(
                        PlaylistTransferHandler.encodePlaylistItem(
                            sourceTable.getModel(), PreviewCard.this.row));
                }

                @Override public int getSourceActions(JComponent component) {
                    return COPY;
                }
            });

            animation = new Timer(110, event -> {
                frame = (frame + 1) % PreviewStoryboardCache.FRAME_COUNT;
                repaint();
            });
            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent event) {
                    animation.start();
                }

                @Override public void mouseExited(MouseEvent event) {
                    animation.stop();
                    frame = 4;
                    repaint();
                }

                @Override public void mouseMoved(MouseEvent event) {
                    double fraction = Math.max(0, Math.min(1,
                        (event.getX() - IMAGE_X) / (double)IMAGE_WIDTH));
                    frame = Math.min(PreviewStoryboardCache.FRAME_COUNT - 1,
                        (int)Math.floor(fraction * PreviewStoryboardCache.FRAME_COUNT));
                    animation.restart();
                    repaint();
                }

                @Override public void mousePressed(MouseEvent event) {
                    dragOrigin = event.getPoint();
                    dragStarted = false;
                    sourceTable.setRowSelectionInterval(row, row);
                    if (SwingUtilities.isRightMouseButton(event)
                            && storyboard == null && !previewError.isEmpty()) {
                        showPreviewError();
                        return;
                    }
                    if (event.getClickCount() == 2) playRow.accept(row);
                    repaint();
                }

                @Override public void mouseReleased(MouseEvent event) {
                    dragOrigin = null;
                    dragStarted = false;
                }

                @Override public void mouseDragged(MouseEvent event) {
                    if (dragStarted || dragOrigin == null
                            || (event.getModifiersEx()
                                & MouseEvent.BUTTON1_DOWN_MASK) == 0) return;
                    if (dragOrigin.distance(event.getPoint()) < 6) return;
                    dragStarted = true;
                    animation.stop();
                    getTransferHandler().exportAsDrag(
                        PreviewCard.this, event, TransferHandler.COPY);
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);

            cache.load(path, duration).thenAccept(result -> SwingUtilities.invokeLater(() -> {
                storyboard = result.image;
                previewError = result.error;
                previewCommand = result.command;
                loading = false;
                setToolTipText(previewError.isEmpty()
                    ? path
                    : "<html><div width='520'>" + escapeHtml(path)
                        + "<br><b>Нет превью:</b> " + escapeHtml(previewError)
                        + "<br>Правый щелчок — подробности и команда ffmpeg</div></html>");
                repaint();
            }));
        }

        private void showPreviewError() {
            String command = previewCommand.isEmpty()
                ? "ffmpeg не запускался: ошибка возникла на подготовительном этапе."
                : previewCommand;
            JTextArea details = new JTextArea(
                "Причина:\n" + previewError + "\n\nКоманда запуска ffmpeg:\n" + command,
                14, 76);
            details.setEditable(false);
            details.setLineWrap(true);
            details.setWrapStyleWord(true);
            details.setCaretPosition(0);
            JScrollPane scroll = new JScrollPane(details);
            JOptionPane.showMessageDialog(
                PlaylistPreviewPanel.this,
                scroll,
                "Причина отсутствия превью",
                JOptionPane.WARNING_MESSAGE);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D)graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                boolean dark = DarkSwingTheme.isDark();
                g.setColor(sourceTable.getSelectedRow() == row
                    ? (dark ? new Color(0, 38, 70) : new Color(210, 232, 250))
                    : (dark ? Color.BLACK : new Color(245, 245, 245)));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g.setColor(sourceTable.getSelectedRow() == row
                    ? new Color(0, 120, 215)
                    : (dark ? new Color(75, 75, 75) : new Color(180, 180, 180)));
                g.setStroke(new BasicStroke(sourceTable.getSelectedRow() == row ? 2f : 1f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);

                g.setColor(Color.BLACK);
                g.fillRect(IMAGE_X, IMAGE_Y, IMAGE_WIDTH, IMAGE_HEIGHT);
                if (storyboard != null) {
                    int sourceX = (frame % PreviewStoryboardCache.COLUMNS)
                        * PreviewStoryboardCache.FRAME_WIDTH;
                    int sourceY = (frame / PreviewStoryboardCache.COLUMNS)
                        * PreviewStoryboardCache.FRAME_HEIGHT;
                    g.drawImage(storyboard,
                        IMAGE_X, IMAGE_Y, IMAGE_X + IMAGE_WIDTH, IMAGE_Y + IMAGE_HEIGHT,
                        sourceX, sourceY,
                        sourceX + PreviewStoryboardCache.FRAME_WIDTH,
                        sourceY + PreviewStoryboardCache.FRAME_HEIGHT, null);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    String status = loading ? "Создание превью…" : "Нет превью ⓘ";
                    drawCentered(g, status, IMAGE_Y + IMAGE_HEIGHT / 2);
                }

                double fraction = frame / (double)(PreviewStoryboardCache.FRAME_COUNT - 1);
                g.setColor(new Color(0, 120, 215));
                g.fillRect(IMAGE_X, IMAGE_Y + IMAGE_HEIGHT - 3,
                    (int)Math.round(IMAGE_WIDTH * fraction), 3);

                g.setColor(dark ? Color.WHITE : Color.BLACK);
                drawClipped(g, title, IMAGE_X, 151, IMAGE_WIDTH);
                String time = duration > 0
                    ? format(duration * fraction) + " / " + format(duration)
                    : "";
                drawClipped(g, time, IMAGE_X, 168, IMAGE_WIDTH);
                if (!comment.isEmpty()) {
                    g.setColor(dark ? new Color(185, 185, 185) : new Color(80, 80, 80));
                    drawClipped(g, comment, IMAGE_X, 185, IMAGE_WIDTH);
                }
            } finally {
                g.dispose();
            }
        }

        private void drawCentered(Graphics2D g, String text, int baseline) {
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, IMAGE_X + (IMAGE_WIDTH - metrics.stringWidth(text)) / 2, baseline);
        }
    }

    private static void drawClipped(Graphics2D g, String text, int x, int y, int width) {
        FontMetrics metrics = g.getFontMetrics();
        if (metrics.stringWidth(text) <= width) {
            g.drawString(text, x, y);
            return;
        }
        String suffix = "…";
        int length = text.length();
        while (length > 0 && metrics.stringWidth(text.substring(0, length) + suffix) > width) {
            length--;
        }
        g.drawString(text.substring(0, length) + suffix, x, y);
    }

    private static String cell(TableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private static String fileName(String path) {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private static double parse(String value) {
        try { return Double.parseDouble(value.trim().replace(',', '.')); }
        catch (Exception ignored) { return 0; }
    }

    private static String format(double seconds) {
        int whole = Math.max(0, (int)Math.round(seconds));
        return String.format("%d:%02d", whole / 60, whole % 60);
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
