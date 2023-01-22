package github.coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class main extends JavaPlugin {

    public static FileConfiguration config = null;
    public static FileConfiguration music = null;
    public static FileConfiguration message = null;
    public static BukkitRunnable playingMusic = null;
    @Override
    public void onEnable() {
        this.getCommand("notemusic").setExecutor(new commandExecutor());
        this.getCommand("nm").setExecutor(new commandExecutor());
        this.getCommand("notemusic").setTabCompleter(new commandExecutor());
        this.getCommand("nm").setTabCompleter(new commandExecutor());
        loadPlugin();
    }

    @Override
    public void onDisable() {

    }

    public static int loadPlugin() {
        Plugin mainPlugin = JavaPlugin.getProvidingPlugin(main.class);
        main.saveDefaultResource("config.yml");
        main.saveDefaultResource("music.yml");
        main.saveDefaultResource("lang/message-en.yml");
        main.saveDefaultResource("lang/message-cn.yml");
        main.config = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "config.yml"));
        main.message = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "lang/message-" + main.config.getString("language") + ".yml"));
        loadMusic();
        Bukkit.getConsoleSender().sendMessage(config.getString("prefix") + String.format(message.getString("load-music"), music.getKeys(false).size()));
        for (String key : music.getKeys(false)) {
            Bukkit.getConsoleSender().sendMessage(config.getString("prefix") + " - " + key);
        }
        return 0;
    }

    public static int loadMusic() {
        Plugin mainPlugin = JavaPlugin.getProvidingPlugin(main.class);
        main.stopMusic();
        main.music = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "music.yml"));
        saveDefaultResource("mus/");
        return 0;
    }

    public static int playMusic(Player player, String musicName) {
        player.sendMessage(main.config.getString("prefix") + String.format(main.message.getString("command-play-music"), musicName));
        if (main.playingMusic != null) main.playingMusic.cancel();
        main.playingMusic = new BukkitRunnable() {
            int timer = 0;
            int speed = main.music.getInt(musicName + ".speed");
            final List<String> noteList = main.music.getStringList(musicName + ".note");
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                    onlinePlayer.getWorld();
                });
                for (int i = 0; i < noteList.size(); i++) {
                    String noteString = noteList.get(i);
                    String[] noteArray = noteString.split(":");
                    Location noteLocation = player.getLocation();
                    Sound noteSound;
                    try {
                        noteSound = Sound.valueOf(noteArray[0].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        noteSound = Sound.valueOf(noteArray[0].toUpperCase().replaceAll("NOTE", "NOTE_BLOCK"));
                    }
                    Sound finalNoteSound = noteSound;
                    float notePitch = Float.parseFloat(noteArray[1]);
                    int noteVolume = Integer.parseInt(noteArray[2]);
                    float noteTime = Float.parseFloat(noteArray[3]);
                    if (timer >= noteTime) {
                        player.getWorld().playSound(noteLocation, //位置
                                finalNoteSound, //乐器
                                noteVolume, //音量
                                notePitch); //音符
                        noteList.remove(i);
                        if (noteList.size() <= 0) main.stopMusic();
                    }
                }
                debugControl.synchronousMusicTimer(timer);
                timer += speed * 0.083; //游戏刻
            }
        };
        main.playingMusic.runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(main.class), 0, 0);
        return 0;
    }

    public static int stopMusic() {
        if (main.playingMusic != null) {
            main.playingMusic.cancel();
            main.playingMusic = null;
            return 1;
        }
        return 0;
    }

    public static List<String> getImportMusic() {
        List<String> filesList = new ArrayList<>();
        File musicDir = new File(main.getProvidingPlugin(main.class).getDataFolder(), "mus");
        String[] files = musicDir.list();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = new File(musicDir, files[i]);
                if (file.isFile()) filesList.add(file.getName());
            }
        }
        return filesList;
    }

    public static String getFilenameSuffix(String fileName) {
        String suffix = "";
        String[] split = fileName.split("\\.");
        if (split.length > 0) {
            suffix = split[split.length - 1];
        }
        return suffix;
    }


    public static String removeFilenameSuffix(String fileName) {
        return fileName.replace(getFilenameSuffix(fileName), "");
    }

    public static int saveDefaultResource(String resourcePath) {
        if (!(new File(github.coolclk.notemusic.main.getProvidingPlugin(github.coolclk.notemusic.main.class).getDataFolder(), resourcePath).exists())) {
            github.coolclk.notemusic.main.getProvidingPlugin(github.coolclk.notemusic.main.class).saveResource(resourcePath, false);
            return 1;
        }
        return 0;
    }
}
