/*
 * Created on 13.11.2007
 *
 */
package ch.codez.autodirector;

import ch.codez.autodirector.gui.DirectorFrame;
import ch.codez.autodirector.model.DirectorOptions;

public class Main {

    public static void main(String[] args) throws Exception {
        setOSXOptions();

        DirectorFrame frame = new DirectorFrame();
        frame.runFullScreen();
    }

    private static void setOSXOptions() {
        //fake full screen allows using webcam settings
        if (DirectorOptions.getInstance().getIsFakeFullscreen()) {
      	  	System.setProperty("apple.awt.fakefullscreen", "true");
    	}
        
        //System.setProperty("apple.awt.fullscreenusefade", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

}
