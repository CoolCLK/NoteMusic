package github.coolclk.notemusic;

public class debugControl {
    public static float musicTimer = -1;

    public static int synchronousMusicTimer(float time) {
        if (main.config.getBoolean("debug")) {
            if (main.playingMusic == null) musicTimer = -1;
            else musicTimer = time;
        }
        return 0;
    }

    public static String[] getPlayerDebugMessage() {
        String[] message = new String[2];
        message[0] = "§eMusic runnable status: ";
        if (main.playingMusic == null) message[0] += "§cUn-working";
        else {
            message[0] += "§aWorking";
            message[1] = "§eMusic runnable timer: " + musicTimer;
        }
        return message;
    }
}
