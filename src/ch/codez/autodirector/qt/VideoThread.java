package ch.codez.autodirector.qt;

import java.io.File;

import org.apache.log4j.Logger;

import quicktime.QTException;

public class VideoThread extends Thread {

    private static final Logger LOG = Logger.getLogger(VideoThread.class);

    private VideoListener videoListener;

    private volatile File nextFile;

    private volatile State state = State.PREVIEW;

    private volatile boolean active = true;

    private volatile boolean showSettings = false;

    public void setVideoListener(VideoListener listener) {
        this.videoListener = listener;
    }

    public void record(File file) {
        nextFile = file;
        state = State.STOP;
    }

    public void preview() {
        nextFile = null;
        if (state == State.RECORD) {
            state = State.STOP;
        }
    }

    public void showSettings() {
        if (state == State.PREVIEW) {
            showSettings = true;
            nextFile = null;
            state = State.STOP;
        }
    }

    public void shutdown() {
        active = false;
        state = State.STOP;
    }

    public State getVideoState() {
        return state;
    }

    @Override
    public void run() {
        while (active) {
            QuicktimeCapture qtCapture = new QuicktimeCapture();

            try {
                init(qtCapture);
                while (active) {
                    if (showSettings) {
                        qtCapture.showSettings();
                        showSettings = false;
                    }
                    doCapturing(qtCapture);
                }
            } catch (QTException e) {
                LOG.error(e);
                if (videoListener != null) {
                    videoListener.setException(e);
                }
            } finally {
                stopAction(qtCapture);
            }

        }

    }

    private void doCapturing(QuicktimeCapture qtCapture) throws QTException {
        state = startAction(qtCapture);
        while (state != State.STOP) {
            if (videoListener != null) {
                int[] pixels = qtCapture.getFrame(videoListener.getPixels());
                videoListener.setPixels(pixels);
                videoListener.updateAndDraw();
            }
            sleep(10);
        }
        qtCapture.stop();
    }

    private void init(QuicktimeCapture qtCapture) throws QTException {
        LOG.debug("Initializing...");
        qtCapture.init();
        LOG.debug("Initialized");
        if (videoListener != null) {
            videoListener.setException(null);
            videoListener.init(qtCapture.getWidth(), qtCapture.getHeight());
        }
    }

    private State startAction(QuicktimeCapture qtCapture) throws QTException {
        if (shouldRecord()) {
            qtCapture.record(nextFile);
            LOG.debug("Started recording");
            return State.RECORD;
        } else {
            qtCapture.preview();
            LOG.debug("Started preview");
            return State.PREVIEW;
        }
    }

    private boolean shouldRecord() {
        return nextFile != null;
    }

    private void stopAction(QuicktimeCapture qtCapture) {
        try {
            qtCapture.stop();
            LOG.debug("Stopped capturing");
        } catch (QTException e) {
            LOG.fatal(e);
        }
        try {
            qtCapture.release();
        } catch (QTException e) {
            LOG.fatal(e);
        }
    }

    private void sleep(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException e) {
        }
    }

    public enum State {
        STOP, PREVIEW, RECORD;
    }
}
