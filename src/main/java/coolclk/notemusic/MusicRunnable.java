package coolclk.notemusic;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class MusicRunnable {
    public String musicName;
    public int musicPlayId;
    public BukkitRunnable musicBukkitRunnable = null;

    public void run(String musicNamed, final World musicWorld, final Location musicLocation) {
        this.musicName = musicNamed;
        musicBukkitRunnable = new BukkitRunnable() {
            float timer = 0;
            final float bpm = Float.parseFloat(Main.music.getString(musicName + ".speed")) * 0.08f; //获取刻经过了MID刻
            final List<String> noteList = Main.music.getStringList(musicName + ".note");

            @Override
            public void run() {
                for (String noteData : noteList.toArray(new String[0])) {
                    String[] noteInfo = noteData.split(":");
                    float noteTime = Float.parseFloat(noteInfo[3]);
                    if (timer >= noteTime) {
                        float noteKey = Float.parseFloat(noteInfo[1]);
                        int noteVolume = Integer.parseInt(noteInfo[2]);
                        Sound noteSound = Main.getSoundByChannel(Integer.parseInt(noteInfo[0]));
                        if (noteSound != null) {
                            float notePitch = (float) Math.pow(2, ((((noteKey - 54) + 1) - 12) / 12));
                            musicWorld.playSound(musicLocation, //位置
                                    noteSound, //乐器
                                    noteVolume, //音量
                                    notePitch); //音符
                        }
                        noteList.remove(noteData);
                        if (noteList.isEmpty()) {
                            Main.stopMusic(musicPlayId);
                        }
                    }
                }
                this.timer += bpm; //游戏刻
            }
        };
        musicBukkitRunnable.runTaskTimer(JavaPlugin.getProvidingPlugin(Main.class), 0, 0);
    }



    public void stop() {
        musicBukkitRunnable.cancel();
    }
}
