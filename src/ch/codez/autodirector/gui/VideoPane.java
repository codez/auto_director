package ch.codez.autodirector.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.codez.autodirector.qt.VideoListener;

public class VideoPane extends JPanel implements VideoListener {
    static final String WRONG_LENGTH = "width*height!=pixels.length";

    public static final int MIN_WIDTH = 128;

    public static final int MIN_HEIGHT = 32;

    private int[] pixels;

    private int width = 1;

    private int height = 1;

    private static final ColorModel CM = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);;

    private Image img;

    private MemoryImageSource source;

    private boolean imageUpdated = false;

    private Rectangle srcRect;

    private Rectangle destRect;

    private JLabel messageLabel;

    public VideoPane() {
        messageLabel = new JLabel();
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        Font font = this.messageLabel.getFont().deriveFont(Font.BOLD, 20);
        messageLabel.setFont(font);
        messageLabel.setForeground(Color.WHITE);
        add(messageLabel, BorderLayout.CENTER);
    }

    /** Creates a blank ColorProcessor of the specified dimensions. */
    public void init(int width, int height) {
        init(width, height, new int[width * height]);
    }

    /**
     * Opens a window to display this image and displays 'statusMessage' in the
     * status bar.
     */
    public void init(int width, int height, int[] pixels) {
        if (pixels != null && width * height != pixels.length) {
            throw new IllegalArgumentException(WRONG_LENGTH);
        }
        this.width = width;
        this.height = height;
        this.pixels = pixels;

        setDestRect();
        createSource();
        img = getImage();
        if ((img != null) && (width >= 0) && (height >= 0)) {
            srcRect = new Rectangle(0, 0, width, height);

            draw();
        }
    }

    /** Returns this image as a AWT image. */
    public Image getImage() {
        if (img == null) {
            createImage();
        }
        return img;
    }

    private void createImage() {
        if (img == null) {
            DataBuffer dataBuffer = new DataBufferInt(pixels, width * height, 0);
            WritableRaster rgbRaster = Raster.createWritableRaster(getRGBSampleModel(), dataBuffer,
                    null);

            img = new BufferedImage(CM, rgbRaster, false, null);
        }
    }

    private void createSource() {
        source = new MemoryImageSource(width, height, CM, pixels, 0, width);
        source.setAnimated(true);
        source.setFullBufferUpdates(true);
        img = Toolkit.getDefaultToolkit().createImage(source);
    }

    public void setException(Exception e) {
        if (e == null) {
            messageLabel.setText(null);
        } else if (e.getMessage() != null) {
            if (e.getMessage().indexOf("-9405") >= 0

            || e.getMessage().indexOf("kIOReturnNoDevice") >= 0) {
                messageLabel.setText("No QuickTime compatible camera found.");
                setPixels(null);
            } else {
                messageLabel.setText(e.getMessage());
                setPixels(null);
            }
        }
    }

    private SampleModel getRGBSampleModel() {
        WritableRaster wr = CM.createCompatibleWritableRaster(1, 1);
        SampleModel sampleModel = wr.getSampleModel();
        sampleModel = sampleModel.createCompatibleSampleModel(width, height);
        return sampleModel;
    }

    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
        resetPixels(pixels);
    }

    private void resetPixels(int[] pixels) {
        if (pixels == null) {
            if (img != null) {
                img.flush();
                img = null;
            }
            source = null;
        }
        source = null;
        img = null;
    }

    public void updateAndDraw() {
        imageUpdated = true;
        repaint();
    }

    public void draw() {
        repaint();
    }

    public void paintComponent(Graphics g) {
        if (imageUpdated) {
            imageUpdated = false;
            createImage();
        }
        setBilinearInterpolation(g, true);
        Image img = getImage();
        if (img != null && srcRect != null) {
            g.drawImage(img, destRect.x, destRect.y, destRect.x + destRect.width, destRect.y
                    + destRect.height, srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y
                    + srcRect.height, null);
        }
        paintChildren(g);
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        setDestRect();
    }

    private void setDestRect() {
        int paneWidth = getWidth();
        int paneHeight = getHeight();
        double srcRelation = width / (double) height;
        if (paneHeight * srcRelation > paneWidth) {
            int destHeight = (int) (paneWidth / srcRelation);
            destRect = new Rectangle(0, (paneHeight - destHeight) / 2, paneWidth, destHeight);
        } else {
            int destWidth = (int) (paneHeight * srcRelation);
            destRect = new Rectangle((paneWidth - destWidth) / 2, 0, destWidth, paneHeight);
        }
    }

    public static void setBilinearInterpolation(Graphics g, boolean bilinearInterpolation) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                bilinearInterpolation ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
                        : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

    }

}
