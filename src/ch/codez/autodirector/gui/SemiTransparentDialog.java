/*
 * Created on 09.12.2007
 *
 */
package ch.codez.autodirector.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SemiTransparentDialog extends JPanel {

    public final static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    public final static Dimension HALF_SCREEN_SIZE = new Dimension(
            (int) (SCREEN_SIZE.getWidth() * 0.66), (int) (SCREEN_SIZE.getHeight() * 0.66));

    public final static Dimension NOTIFICATION_SIZE = new Dimension(350, 250);

    protected final static int BORDER_WIDTH = 20;

    protected final static double ARC_SIZE = 0.1;

    // protected final static Color BG_COLOR = new Color(30, 30, 30, 230);
    protected final static Color BG_COLOR = new Color(50, 50, 50, 230);

    protected final static Color FONT_COLOR = new Color(255, 255, 255);

    private final static int BUTTON_FONT_SIZE = 40;

    private final static int MESSAGE_FONT_SIZE = 90;

    private JLabel messageLabel = new JLabel();

    private JLabel okLabel = new JLabel();

    private JLabel cancelLabel = new JLabel();

    private String okText;

    private String cancelText;

    private Set<DialogCloseListener> listeners = new HashSet<DialogCloseListener>();

    public SemiTransparentDialog(String message, String okText, String cancelText) {
        this.okText = okText;
        this.cancelText = cancelText;

        this.messageLabel.setText("<html><p>" + message + "</p></html>");

        this.init();
    }

    public void showDialog() {
        this.requestFocus();
        this.setVisible(true);
        this.setSize(HALF_SCREEN_SIZE);
        this.validate();
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.setMaximumSize(new Dimension(width, height));
        this.setLocation((int) (SCREEN_SIZE.getWidth() - width) / 2,
                (int) (SCREEN_SIZE.getHeight() - height) / 2);
        this.validate();
        this.getParent().validate();
    }

    public void setSize(Dimension dim) {
        this.setSize((int) dim.getWidth(), (int) dim.getHeight());
    }

    public void addCloseListener(DialogCloseListener l) {
        listeners.add(l);
    }

    public void removeCloseListener(DialogCloseListener l) {
        listeners.remove(l);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.paintBackground(g);
    }

    protected void paintBackground(Graphics g) {
        int w = this.getWidth();
        int h = this.getHeight();
        g.setColor(BG_COLOR);
        g.fillRoundRect(0, 0, w, h, (int) (w * ARC_SIZE), (int) (h * ARC_SIZE));
    }

    private void init() {
        this.setVisible(false);
        this.setOpaque(false);
        this.setFocusable(true);
        this.setLayout(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));
        this.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH,
                BORDER_WIDTH));
        this.initMessage();
        this.initButtons();
    }

    private void initMessage() {
        this.messageLabel.setForeground(FONT_COLOR);
        this.messageLabel.setFont(getFont(MESSAGE_FONT_SIZE));
        this.add(this.messageLabel, BorderLayout.CENTER);
    }

    private Font getFont(int size) {
        return new Font(this.getFont().getName(), Font.PLAIN, size);
    }

    private void initButtons() {
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        buttonPane.setOpaque(false);
        this.okLabel.setHorizontalAlignment(JLabel.CENTER);
        this.okLabel.setForeground(FONT_COLOR);
        this.okLabel.setFont(getFont(BUTTON_FONT_SIZE));
        this.okLabel.addMouseListener(new CloseClickListener(true, this.okLabel, this.okText));
        buttonPane.add(this.okLabel);

        this.cancelLabel.setHorizontalAlignment(JLabel.CENTER);
        this.cancelLabel.setForeground(FONT_COLOR);
        this.cancelLabel.setFont(getFont(BUTTON_FONT_SIZE));
        this.cancelLabel.addMouseListener(new CloseClickListener(false, this.cancelLabel,
                this.cancelText));
        buttonPane.add(this.cancelLabel);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    protected void close(boolean ok) {
        this.setVisible(false);
        for (DialogCloseListener l : this.listeners) {
            l.dialogClosed(ok);
        }
    }

    private class CloseClickListener extends MouseAdapter {
        private boolean ok;

        private JLabel label;

        private String text;

        public CloseClickListener(boolean ok, JLabel label, String text) {
            this.ok = ok;
            this.label = label;
            this.text = text;
            label.setText("[" + text + "]");
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                SemiTransparentDialog.this.close(ok);
            }
        }

        public void mouseEntered(MouseEvent e) {
            label.setText("<html>[<u>" + text + "</u>]</html>");
        }

        public void mouseExited(MouseEvent e) {
            label.setText("[" + text + "]");
        }

    }

}
