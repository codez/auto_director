/*
 * Created on 08.04.2004
 *
 * $Id: ChicaneFrame.java,v 1.6 2004/04/14 23:12:47 pascal Exp $
 */
package ch.codez.autodirector.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import org.apache.log4j.Logger;

import ch.codez.autodirector.model.DirectorOptions;
import ch.codez.autodirector.model.FileReleaser;
import ch.codez.autodirector.model.TimeListener;
import ch.codez.autodirector.model.VideoDirector;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class DirectorFrame extends JFrame implements TimeListener, DialogCloseListener {

    private final static int GAP = 50;

    private final static int BORDER_WIDTH = 5;

    private final static String CONTROL_BG_IMAGE = "/images/metal.jpg";

    private static Logger log = Logger.getLogger(DirectorFrame.class);

    VideoDirector videoDirector;

    private File currentFile = null;

    private JButton recordButton;

    private SemiTransparentDialog saveDialog = new SemiTransparentDialog(
            "Willst Du den aufgenommenen Film behalten?", "Behalten", "Verwerfen");

    public DirectorFrame() {
        super("Auto Director");
        this.init();
    }

    public void runFullScreen() {
        if (DirectorOptions.getInstance().getIsKioskMode()) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            gs.setFullScreenWindow(this);
        }
        this.videoDirector.go();
        this.validate();
    }

    public void updateTime(long millis) {
    }

    public void started() {
    }

    public void stopped(File file) {
        if (file != null) {
            this.currentFile = file;
            this.saveDialog.showDialog();
            this.recordButton.setEnabled(false);
        }
    }

    public void dialogClosed(boolean ok) {
        this.recordButton.setEnabled(true);
        if (ok && currentFile != null) {
            FileReleaser releaser = new FileReleaser(currentFile);
            releaser.start();
        }
        this.currentFile = null;
    }

    @SuppressWarnings("unused")
    private boolean shouldReleaseMovie() {
        Object[] options = { "Behalten", "Verwerfen" };
        int answer = JOptionPane
                .showOptionDialog(this, "Willst Du den aufgenommenen Film behalten?",
                        "Film behalten?", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

        return answer == 0;
    }

    protected void init() {
        this.videoDirector = new VideoDirector();
        this.videoDirector.addTimeListener(this);

        this.initWindow();

        this.initMainPanel();
        this.initControls();

        this.validate();
    }

    private void initWindow() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBackground(Color.BLACK);
        if (DirectorOptions.getInstance().getIsKioskMode()) {
            this.setUndecorated(true);
            this.setResizable(false);
            this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        } else {
            this.setSize(700, 500);
        }
        this.setVisible(true);
        this.addWindowListener(new FrameListener());

        Application app = new Application();
        app.addApplicationListener(new MacOsMenuHandler());
        app.addPreferencesMenuItem();
        app.setEnabledPreferencesMenu(true);
        app.setEnabledAboutMenu(false);
    }

    private JPanel initVideoPanel() {
        VideoPane videoPane = new VideoPane();
        videoPane.setLayout(new BorderLayout());
        videoDirector.getVideoThread().setVideoListener(videoPane);

        CountdownLabel countdownLabel = new CountdownLabel();
        videoDirector.addTimeListener(countdownLabel);
        videoPane.add(countdownLabel, BorderLayout.CENTER);

        return videoPane;
    }

    private void initMainPanel() {
        JLayeredPane center = new JLayeredPane();
        center.setLayout(new OverlayLayout(center));
        this.saveDialog.addCloseListener(this);
        center.add(this.saveDialog, JLayeredPane.POPUP_LAYER);
        center.add(this.initVideoPanel(), JLayeredPane.DEFAULT_LAYER);
        this.getContentPane().add(center, BorderLayout.CENTER);
    }

    private void initControls() {
        ImageIcon bgImage = this.loadIcon(CONTROL_BG_IMAGE);
        JPanel bottomPane = new BackgroundPane(bgImage.getImage());
        bottomPane.setLayout(new BorderLayout());
        bottomPane.setBorder(BorderFactory.createCompoundBorder(
        // BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE), BorderFactory
                        .createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH * 4, BORDER_WIDTH, 100)));

        bottomPane.add(getDescriptionLabel(), BorderLayout.CENTER);

        bottomPane.add(initRecordButton(), BorderLayout.EAST);
        this.getContentPane().add(bottomPane, BorderLayout.SOUTH);
    }

    private JLabel getDescriptionLabel() {
        DirectorOptions options = DirectorOptions.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>Zum Aufnehmen und Stoppen drücke den roten Knopf.<br>");
        sb.append("Danach hast du ");
        sb.append(options.getDirectorCountdown());
        sb.append(" Sekunden Zeit, bevor die Aufnahme beginnt.<br>");
        sb.append("Die maximale Redezeit beträgt ");
        sb.append(options.getMaxVideoLength() / 60);
        sb.append(" Minuten.</html>");

        JLabel description = new JLabel(sb.toString());
        description.setFont(new Font(getFont().getName(), Font.PLAIN, 22));

        return description;
    }

    private JComponent initRecordButton() {
        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(false);
        buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT, GAP, 0));

        TimeLabel timeLabel = new TimeLabel();
        videoDirector.addTimeListener(timeLabel);
        recordButton = new RecordButton(this.videoDirector);

        buttonPane.add(recordButton);
        buttonPane.add(timeLabel);
        return buttonPane;
    }

    private ImageIcon loadIcon(String file) {
        ImageIcon icon = new ImageIcon(this.getClass().getResource(file));
        log.debug("Image " + file + " loaded with status " + icon.getImageLoadStatus());
        return icon;
    }

    final class FrameListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            videoDirector.shutdown();
        }

    }

    class MacOsMenuHandler extends ApplicationAdapter {
        public void handlePreferences(ApplicationEvent event) {
            videoDirector.showSettings();
        }

        public void handleQuit(ApplicationEvent event) {
            videoDirector.shutdown();
            DirectorFrame.this.setVisible(false);
            // wait for video thread to clean up
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }
}
