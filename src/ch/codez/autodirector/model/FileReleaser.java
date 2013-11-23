package ch.codez.autodirector.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class FileReleaser extends Thread {
    private final static Logger LOG = Logger.getLogger(FileReleaser.class);

    private File movie;

    public FileReleaser(File movie) {
        this.movie = movie;
    }

    public void run() {
        File released = getReleasedFile();

        if (released.exists()) {
            LOG.warn("File " + released.getName() + " already exists. Not released.");
        } else {
            try {
                copy(released);
            } catch (IOException e) {
                LOG.error("Could not release File " + released.getName(), e);
            }
        }
    }

    private File getReleasedFile() {
        File releaseFolder = new File(DirectorOptions.getInstance().getPathReleasedMovies());
        releaseFolder.mkdirs();

        return new File(releaseFolder, movie.getName());
    }

    private void copy(File released) throws IOException {
        FileChannel inChannel = new FileInputStream(movie).getChannel();
        FileChannel outChannel = new FileOutputStream(released).getChannel();

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
}
