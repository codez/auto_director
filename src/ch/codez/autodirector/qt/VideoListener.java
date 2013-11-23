package ch.codez.autodirector.qt;

public interface VideoListener {
    void setPixels(int[] pixels);

    int[] getPixels();

    void updateAndDraw();

    void init(int width, int height);

    void setException(Exception e);
}
