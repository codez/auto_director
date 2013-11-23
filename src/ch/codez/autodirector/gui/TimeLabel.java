package ch.codez.autodirector.gui;

import java.awt.Font;
import java.io.File;

import javax.swing.JLabel;

import ch.codez.autodirector.model.TimeListener;

public class TimeLabel extends JLabel implements TimeListener {

    public TimeLabel() {
        init();
    }

    public void updateTime(long millis) {
        if (millis >= 0L) {
            setTime(millis);
        }
    }

    public void setTime(long millis) {
        setText(formatTime(millis));
    }

    public void started() {
    }

    public void stopped(File file) {
        reset();
    }

    public void reset() {
        setTime(0L);
    }

    private String formatTime(long millis) {
        StringBuilder sb = new StringBuilder();
        long minutes = millis / (1000 * 60);
        long seconds = (millis / 1000 - minutes * 60);
        long tenths = millis / 100 - minutes * 600 - seconds * 10;
        sb.append(formatTwoDigits(minutes));
        sb.append(':');
        sb.append(formatTwoDigits(seconds));
        sb.append(':');
        sb.append(tenths);
        return sb.toString();
    }

    private String formatTwoDigits(long val) {
        return String.format("%02d", val);
    }

    private void init() {
        setHorizontalAlignment(JLabel.CENTER);
        setFont(new Font(getFont().getName(), getFont().getStyle(), getFont().getSize() + 28));
        reset();
    }

}
