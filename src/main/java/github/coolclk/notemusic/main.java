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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class main extends JavaPlugin {

    public static FileConfiguration config = null;
    public static FileConfiguration music = null;
    public static FileConfiguration message = null;
    public static List<musicRunnable> playingMusic = new ArrayList();
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
        main.stopMusicAll();
        main.music = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "music.yml"));
        saveDefaultResource("mus/");
        return 0;
    }

    public static int playMusic(Player player, String musicName) {
        musicRunnable musicRun = new musicRunnable();
        //获取运行Id
        int tempMusicId = 0;
        boolean tempMusicFindId = false;
        int tempCheckMusicIndex = 0;
        if (main.playingMusic.size() > 0) {
            while (!tempMusicFindId) {
                if (main.playingMusic.get(tempCheckMusicIndex).musicPlayId == tempMusicId) {
                    tempMusicFindId = true;
                }
                else {
                    if (tempCheckMusicIndex + 1 >= main.playingMusic.size()) {
                        while (main.playingMusic.get(tempCheckMusicIndex).musicPlayId == tempMusicId) tempMusicId++;
                        tempMusicFindId = true;
                    }
                }
                tempCheckMusicIndex++;
            }
        }
        //设定Id
        musicRun.musicPlayId = tempMusicId;
        musicRun.run(musicName, player.getWorld(), player.getLocation());
        main.playingMusic.add(musicRun);
        return 0;
    }

    public static int stopMusic(String name) {
        if (main.playingMusic != null) {
            for (int i = 0; i < main.playingMusic.size(); i++) {
                musicRunnable musicRunnable = main.playingMusic.get(i);
                if (Objects.equals(musicRunnable.musicName, name)) {
                    musicRunnable.stop();
                    main.playingMusic.remove(i);
                }
            }
            return 1;
        }
        return 0;
    }

    public static int stopMusic(int id) {
        if (main.playingMusic != null) {
            int index = 0;
            int idIndex = -1;
            for (musicRunnable mRunnable : main.playingMusic) {
                if (mRunnable.musicPlayId == id) {
                    idIndex = index;
                }
                index++;
            }
            main.playingMusic.get(idIndex).stop();
            main.playingMusic.remove(idIndex);
            return 1;
        }
        return 0;
    }

    public static int stopMusicAll() {
        if (main.playingMusic != null) {
            for (musicRunnable mRunnable : main.playingMusic) {
                mRunnable.stop();
            }
            main.playingMusic = new ArrayList<>();
            return 1;
        }
        return 0;
    }

    public static int removeMusic(String musicName) throws IOException {
        main.music.set(musicName, null);
        main.music.save(new File(main.getProvidingPlugin(main.class).getDataFolder(), "music.yml"));
        main.loadMusic();
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
