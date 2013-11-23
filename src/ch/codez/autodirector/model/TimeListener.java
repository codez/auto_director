package ch.codez.autodirector.model;

import java.io.File;

public interface TimeListener {
    public void updateTime(long millis);

    public void started();

    public void stopped(File file);

}
