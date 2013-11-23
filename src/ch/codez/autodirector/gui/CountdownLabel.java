package ch.codez.autodirector.gui;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.JLabel;

import ch.codez.autodirector.model.DirectorOptions;
import ch.codez.autodirector.model.TimeListener;

public class CountdownLabel extends JLabel implements TimeListener {

    private static final long DELAY_COUNTDOWN = 750L;

    private final static Color END_COLOR = Color.RED;

    private final static Color BEFORE_COLOR = Color.WHITE;

    private long endCountdownStart;

    private int endOfVideoCountdown;

    public CountdownLabel() {
        super("", JLabel.CENTER);
        DirectorOptions options = DirectorOptions.getInstance();
        endOfVideoCountdown = options.getEndOfVideoCountdown() * 1000;
        endCountdownStart = options.getMaxVideoLength() * 1000 - endOfVideoCountdown;

        setFont(new Font(getFont().getName(), Font.BOLD, 260));
    }

    public void updateTime(long millis) {
        if (millis > endCountdownStart) {
            setForeground(END_COLOR);
            long timeLeft = endOfVideoCountdown - (millis - endCountdownStart);
            setTime(timeLeft);
        } else if (millis < 0) {
            setForeground(BEFORE_COLOR);
            setTime(DELAY_COUNTDOWN - millis);
        }
    }

    private void setTime(long millis) {
        setText(String.valueOf((int) (millis / 1000)));
    }

    public void started() {
        reset();

    }

    public void stopped(File file) {
        reset();
    }

    public void reset() {
        setText("");
    }
}
