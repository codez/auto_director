package ch.codez.autodirector.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ch.codez.autodirector.qt.VideoThread;

public class VideoDirector implements Runnable {

    private final static String FILE_PREFIX = "ad-";

    private final static String FILE_DATE_FORMAT = "HHmmss";

    private VideoThread videoThread;

    private volatile boolean recording = false;

    private File currentFile = null;

    private Set<TimeListener> timeListeners = new HashSet<TimeListener>();

    public VideoDirector() {
        this.videoThread = new VideoThread();
    }

    public void go() {
        videoThread.start();
    }

    public void record() {
        recording = true;
        new Thread(this).start();
    }

    public void stop() {
        recording = false;
    }

    public void shutdown() {
        videoThread.shutdown();
        recording = false;
    }

    public void showSettings() {
        videoThread.showSettings();
    }

    public VideoThread getVideoThread() {
        return videoThread;
    }

    public void run() {
        DirectorOptions options = DirectorOptions.getInstance();
        long startTime = System.currentTimeMillis() + options.getDirectorCountdown() * 1000;

        doCountdown(startTime);
        doStart();
        doRecord(startTime);
        doStop();
    }

    private void doCountdown(long startTime) {
        long time = System.currentTimeMillis() - startTime;
        while (recording && time < 0) {
            updateTime(time);
            sleep(200);
            time = System.currentTimeMillis() - startTime;
        }
    }

    private void doStart() {
        if (recording) {
            currentFile = createNewFile();
            videoThread.record(currentFile);
            started();
        }
    }

    private void doRecord(long startTime) {
        long maxLength = DirectorOptions.getInstance().getMaxVideoLength() * 1000;
        long time = 0;
        while (recording && time < maxLength) {
            updateTime(time);
            sleep(50);
            time = System.currentTimeMillis() - startTime;
        }
    }

    private void doStop() {
        videoThread.preview();

        stopped(currentFile);
        currentFile = null;
    }

    private File createNewFile() {
        File folder = new File(DirectorOptions.getInstance().getPathTempMovies());
        folder.mkdirs();
        String filename = new SimpleDateFormat(FILE_DATE_FORMAT).format(new Date());

        return new File(folder, FILE_PREFIX + filename + ".mov");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            // whatever
        }
    }

    public void addTimeListener(TimeListener listener) {
        timeListeners.add(listener);
    }

    protected void updateTime(long millis) {
        for (TimeListener listener : timeListeners) {
            listener.updateTime(millis);
        }
    }

    protected void started() {
        for (TimeListener listener : timeListeners) {
            listener.started();
        }
    }

    protected void stopped(File file) {
        for (TimeListener listener : timeListeners) {
            listener.stopped(file);
        }
    }

}
