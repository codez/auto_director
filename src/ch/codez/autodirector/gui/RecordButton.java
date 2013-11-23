package ch.codez.autodirector.gui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import ch.codez.autodirector.model.Blinker;
import ch.codez.autodirector.model.DirectorOptions;
import ch.codez.autodirector.model.TimeListener;
import ch.codez.autodirector.model.VideoDirector;

public class RecordButton extends RoundButton implements TimeListener {

    private final static String RECORD_IMAGE = "/images/record.png";

    private final static String STOP_RED_IMAGE = "/images/stop-red.png";

    private final static String STOP_GRAY_IMAGE = "/images/stop-gray.png";

    private final static int BUTTON_SIZE = 60;

    private final ImageIcon recordIcon;

    private final ImageIcon stopRedIcon;

    private final ImageIcon stopGrayIcon;

    private Blinker blinker = new Blinker();

    public RecordButton(VideoDirector director) {
        super();
        director.addTimeListener(this);
        this.setAction(new RecordAction(director));
        this.recordIcon = loadIcon(RECORD_IMAGE);
        this.stopRedIcon = loadIcon(STOP_RED_IMAGE);
        this.stopGrayIcon = loadIcon(STOP_GRAY_IMAGE);
        this.setIcon(recordIcon);
        this.setForeground(DirectorOptions.getInstance().getLafColorHighlight());
    }

    private ImageIcon loadIcon(String file) {
        ImageIcon icon = new ImageIcon(this.getClass().getResource(file));
        return new ImageIcon(icon.getImage().getScaledInstance(BUTTON_SIZE, BUTTON_SIZE,
                Image.SCALE_SMOOTH));
    }

    public void updateTime(long millis) {
        if (millis > 0) {
            blink();
        } else {
            if (getIcon() != stopRedIcon) {
                setIcon(stopRedIcon);
            }
        }
    }

    private void blink() {
        switch (blinker.getAction()) {
        case TURN_ON:
            setIcon(stopGrayIcon);
            return;
        case TURN_OFF:
            setIcon(stopRedIcon);
            return;
        }
    }

    public void started() {
        blinker.start();
        setIcon(stopRedIcon);
    }

    public void stopped(File file) {
        blinker.stop();
        setIcon(recordIcon);
    }

    boolean isRecording() {
        return getIcon() != recordIcon;
    }

    class RecordAction extends AbstractAction {

        private VideoDirector director;

        public RecordAction(VideoDirector director) {
            this.director = director;
        }

        public void actionPerformed(ActionEvent e) {
            if (isRecording()) {
                director.stop();
            } else {
                director.record();
            }
        }
    }

}
