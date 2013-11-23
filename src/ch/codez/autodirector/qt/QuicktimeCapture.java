package ch.codez.autodirector.qt;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import org.apache.log4j.Logger;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.io.QTFile;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.image.Compressor;
import quicktime.std.sg.SGSoundChannel;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;
import ch.codez.autodirector.model.DirectorOptions;

/**
 * Previews and captures a single video frame using QuickTime for Java. Press
 * the space bar to stop previewing. Press the alt key to capture a frame and
 * continue previewing. While previewing, type "+" to zoom in, "-" to zoom out,
 * and "h" to display a histogram. Captures and displays a single frame if
 * called with the argument "grab". Based on the LiveCam example posted to the
 * QuickTime for Java mailing list by Jochen Broz.
 * http://lists.apple.com/archives/quicktime-java/2005/Feb/msg00062.html
 */
public class QuicktimeCapture {

    private static final Logger LOG = Logger.getLogger(QuicktimeCapture.class);

    private static final int COMPRESSOR_TYPE = 1836070006; // MPEG compressor

    // private static final int COMPRESSOR_TYPE = 1635148593; // H.264 compressor

    private SequenceGrabber grabber;

    private QDRect cameraSize;

    private QDGraphics gWorld;

    private SGVideoChannel videoChannel;

    private int[] pixelData;

    private int intsPerRow;

    /**
     * Initializes the SequenceGrabber. Gets it's source video bounds, creates a
     * gWorld with that size. Configures the video channel for grabbing,
     * previewing and playing during recording.
     */
    public void init() throws QTException {
        QTSession.open();
        grabber = new SequenceGrabber();
        videoChannel = new SGVideoChannel(grabber);
        SGSoundChannel soundChannel = new SGSoundChannel(grabber);
        cameraSize = videoChannel.getSrcVideoBounds();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (cameraSize.getHeight() > screen.height - 40) {
            // iSight camera claims to 1600x1200!
            cameraSize.resize(640, 480);
        }
        gWorld = new QDGraphics(cameraSize);
        grabber.setGWorld(gWorld, null);

        DirectorOptions options = DirectorOptions.getInstance();
        videoChannel.setBounds(cameraSize);
        videoChannel.setUsage(StdQTConstants.seqGrabRecord | StdQTConstants.seqGrabPreview
                | StdQTConstants.seqGrabPlayDuringRecord);
        videoChannel.setFrameRate(options.getVideoFrameRate());
        videoChannel.setCompressor(24, Compressor.findCodec(COMPRESSOR_TYPE,
                Compressor.bestCompressionCodec), options.getVideoCompressionQuality(), 512,
                options.getVideoKeyFrameRate());

        logSettings();

        soundChannel.setUsage(StdQTConstants.seqGrabPreview | StdQTConstants.seqGrabRecord
                | StdQTConstants.seqGrabPlayDuringRecord);
        soundChannel.setVolume(0.0f);
        intsPerRow = gWorld.getPixMap().getPixelData().getRowBytes() / 4;
        pixelData = new int[intsPerRow * cameraSize.getHeight()];
    }

    public void showSettings() throws QTException {
        try {
            videoChannel.settingsDialog();

            DirectorOptions options = DirectorOptions.getInstance();
            options.setVideoCompressionQuality(videoChannel.getCompressor().mSpatialQuality);
            options.setVideoKeyFrameRate(videoChannel.getCompressor().mKeyFrameRate);
            options.save();
        } catch (StdQTException e) {
            // canceled dialog
        }
        logSettings();
    }

    private void logSettings() throws QTException {
        LOG.debug(videoChannel.getCompressorType());
        LOG.debug(videoChannel.getCompressor().mDepth);
        LOG.debug(videoChannel.getCompressor().mKeyFrameRate);
        LOG.debug(videoChannel.getCompressor().mSpatialQuality);
        LOG.debug(videoChannel.getCompressor().mTemporalQuality);
        LOG.debug(videoChannel.getFrameRate());
    }

    public void preview() throws QTException {
        start(null, StdQTConstants.seqGrabDontMakeMovie);
    }

    public void record(File file) throws QTException {
        start(new QTFile(file), StdQTConstants.seqGrabToDisk);
    }

    private void start(QTFile file, int args) throws QTException {
        grabber.setDataOutput(file, args);
        grabber.prepare(true, true);
        grabber.startRecord();
    }

    public int[] getFrame(int[] pixels) throws QTException {
        grabber.idle();
        grabber.update(null);

        gWorld.getPixMap().getPixelData().copyToArray(0, pixelData, 0, pixelData.length);

        if (pixels != null && intsPerRow != cameraSize.getWidth()) {
            for (int i = 0; i < cameraSize.getHeight(); i++) {
                System.arraycopy(pixelData, i * intsPerRow, pixels, i * cameraSize.getWidth(),
                        cameraSize.getWidth());
            }
            return pixels;
        } else {
            return pixelData;
        }
    }

    public void stop() throws QTException {
        if (grabber != null) {
            grabber.stop();
        }
        LOG.debug("Stopped capturing");
    }

    public void release() throws QTException {
        if (grabber != null) {
            grabber.release();
        }
        QTSession.close();
    }

    public int getWidth() {
        return cameraSize.getWidth();
    }

    public int getHeight() {
        return cameraSize.getHeight();
    }

}
