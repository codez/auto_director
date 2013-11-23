package ch.codez.autodirector.model;

public class Blinker {

    private long lastChangeTime;

    private boolean on;

    public void start() {
        this.on = true;
        this.lastChangeTime = System.currentTimeMillis();
    }

    public void stop() {
        this.lastChangeTime = 0;
    }

    public boolean isRunning() {
        return this.lastChangeTime > 0;
    }

    public Action getAction() {
        long millis = System.currentTimeMillis();
        long diff = millis - lastChangeTime;
        if (on && diff > 800) {
            toggle(millis);
            return Action.TURN_OFF;
        } else if (!on && diff > 400) {
            toggle(millis);
            return Action.TURN_ON;
        } else {
            return Action.STAY;
        }
    }

    private void toggle(long millis) {
        on = !on;
        lastChangeTime = millis;
    }

    public static enum Action {
        TURN_ON, TURN_OFF, STAY;
    }

}
