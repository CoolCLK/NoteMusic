package github.coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

public class musicRunnable {
    public String musicName;
    public int musicPlayId;
    public BukkitRunnable musicBukkitRunnable = null;

    public int run(String musicNamed, World musicWorld, Location musicLocation) {
        this.musicName = musicNamed;
        musicBukkitRunnable = new BukkitRunnable() {
            float timer = 0;
            final float bpm = Float.parseFloat(main.music.getString(musicName + ".speed")) * 0.08f; //获取刻经过了MID刻
            List<String> noteList = main.music.getStringList(musicName + ".note");
            ScriptEngine se = new ScriptEngineManager().getEngineByName("js");
            @Override
            public void run() {
                for (int i = 0; i < noteList.size(); i++) {
                    String noteString = noteList.get(i);
                    String[] noteArray = noteString.split(":");
                    float noteTime = Float.parseFloat(noteArray[3]);
                    if (timer >= noteTime) {
                        Location noteLocation = musicLocation;
                        float noteKey = Float.parseFloat(noteArray[1]);
                        int noteVolume = Integer.parseInt(noteArray[2]);
                        Sound noteSound;
                        float notePitch = (float) Math.pow(2, ((((noteKey - 54) + 1) - 12) / 12)); //算法A
                        //float notePitch = (noteKey - (54 - 20)) * 0.05f; 算法B，已弃用
                        try {
                            noteSound = Sound.valueOf(noteArray[0].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            try {
                                noteSound = Sound.valueOf(noteArray[0].toUpperCase().replaceAll("NOTE", "NOTE_BLOCK"));
                            } catch (IllegalArgumentException ex) {
                                noteSound = Sound.BLOCK_NOTE_PLING;
                            }
                        }
                        musicWorld.playSound(noteLocation, //位置
                                noteSound, //乐器
                                noteVolume, //音量
                                notePitch); //音符
                        noteList.remove(i);
                        if (noteList.size() <= 0) {
                            main.stopMusic(musicPlayId);
                        }
                    }
                }
                this.timer += bpm; //游戏刻
            }
        };
        musicBukkitRunnable.runTaskTimer(JavaPlugin.getProvidingPlugin(main.class), 0, 0);
        return 0;
    }



    public int stop() {
        musicBukkitRunnable.cancel();
        return 0;
    }
}
