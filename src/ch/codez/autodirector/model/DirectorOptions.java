/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autodirector.model;

import java.awt.Color;
import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

public class DirectorOptions {

    public final static String CONFIG_FILE = "autodirector.properties";

    private static DirectorOptions INSTANCE = new DirectorOptions();

    private static Logger log = Logger.getLogger(DirectorOptions.class);

    private PropertiesConfiguration config;

    public static DirectorOptions getInstance() {
        return INSTANCE;
    }

    private DirectorOptions() {
        this.initConfig();
    }

    public boolean getIsKioskMode() {
        return this.config.getBoolean("kiosk.mode", true);
    }

    public boolean getIsFakeFullscreen() {
        return this.config.getBoolean("fake.fullscreen", true);
    }
    
    public int getMaxVideoLength() {
        return this.config.getInt("director.video.length", 5 * 60);
    }

    public int getEndOfVideoCountdown() {
        return this.config.getInt("director.video.endCountdown", 30);
    }

    public int getDirectorCountdown() {
        return this.config.getInt("director.countdown", 10);
    }

    public int getVideoFrameRate() {
        return this.config.getInt("video.frame.rate", 24);
    }

    public void setVideoFrameRate(int rate) {
        this.config.setProperty("video.data.rate", rate);
    }

    public int getVideoKeyFrameRate() {
        return this.config.getInt("video.keyframe.rate", 24);
    }

    public void setVideoKeyFrameRate(int rate) {
        this.config.setProperty("video.keyframe.rate", rate);
    }

    public int getVideoCompressionQuality() {
        return this.config.getInt("video.compression.quality", 768);
    }

    public void setVideoCompressionQuality(int q) {
        this.config.setProperty("video.compression.quality", q);
    }

    public String getPathTempMovies() {
        return this.getPath("path.movies.temp");
    }

    public String getPathReleasedMovies() {
        return this.getPath("path.movies.released");
    }

    public Color getLafColorHighlight() {
        int[] color = this.getColor("laf.color.highlight", "#ff70aa");
        return new Color(color[0], color[1], color[2]);
    }

    public void save() {
        try {
            this.config.save();
        } catch (ConfigurationException e) {
            log.warn("Could not save configuration.", e);
        }
    }

    private String getPath(String key) {
        String path = this.config.getString(key, ".");
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    private int[] getColor(String key, String def) {
        String bg = this.config.getString(key, def);
        int color[] = new int[3];
        int index = ('#' == bg.charAt(0)) ? 1 : 0;
        for (int i = 0; i < 3; i++) {
            color[i] = Integer.parseInt(bg.substring(index, index + 2), 16);
            index += 2;
        }
        return color;
    }

    private void initConfig() {
        PropertiesConfiguration config;
        try {
            config = new PropertiesConfiguration(CONFIG_FILE);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException e) {
            log.error("Configuration file " + CONFIG_FILE + " not found!", e);
            config = new PropertiesConfiguration();
        }
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = this.config.getKeys(); it.hasNext();) {
            String key = it.next();
            sb.append(key);
            sb.append('=');
            sb.append(this.config.getProperty(key));
            sb.append("\n");
        }
        return sb.toString();
    }

}
