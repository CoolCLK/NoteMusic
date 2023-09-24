package coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {
    public static FileConfiguration config = null, instrument = null, music = null, message = null;
    public final static List<MusicRunnable> playingMusic = new ArrayList<>();

    @Override
    public void onEnable() {
        this.getCommand("notemusic").setExecutor(CommandExecutor.INSTANCE);
        this.getCommand("nm").setExecutor(CommandExecutor.INSTANCE);
        this.getCommand("notemusic").setTabCompleter(CommandExecutor.INSTANCE);
        this.getCommand("nm").setTabCompleter(CommandExecutor.INSTANCE);
        loadPlugin(false);
    }

    public static Sound getSoundByChannel(int channel) {
        String channelKey = null;
        for (String channelName : Main.instrument.getConfigurationSection("channels").getKeys(true)) {
            String index = Main.instrument.getConfigurationSection("channels").getString(channelName);
            if (index.contains("..")) {
                int channelIndex0 = Integer.parseInt(index.split("\\.\\.")[0]), channelIndex1 = Integer.parseInt(index.split("\\.\\.")[1]);
                if (channel >= channelIndex0 && channel <= channelIndex1) channelKey = channelName;
            } else if (Integer.parseInt(index) == channel) channelKey = channelName;
        }
        if (channelKey == null) channelKey = Main.instrument.getConfigurationSection("sounds").getString("_DEFAULT_");
        try {
            return Sound.valueOf(channelKey.toUpperCase().replaceAll(" ", "_"));
        }
        catch (Exception e) {
            return Sound.BLOCK_NOTE_PLING;
        }
    }

    public static void loadPlugin(Boolean quiet) {
        Plugin mainPlugin = JavaPlugin.getProvidingPlugin(Main.class);
        Main.saveDefaultResource("config.yml");
        Main.saveDefaultResource("music.yml");
        Main.saveDefaultResource("instrument.yml");
        Main.saveDefaultResource("lang/message-en_US.yml");
        Main.saveDefaultResource("lang/message-zh_CN.yml");
        Main.saveDefaultResource("mus/Drop any midi file here");
        Main.config = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "config.yml"));
        Main.instrument = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "instrument.yml"));
        Main.message = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "lang/message-" + Main.config.getString("language") + ".yml"));
        loadMusic();
        if (!quiet) {
            Bukkit.getConsoleSender().sendMessage(config.getString("prefix") + String.format(message.getString("load-music"), music.getKeys(false).size()));
            for (String key : music.getKeys(false)) {
                Bukkit.getConsoleSender().sendMessage(config.getString("prefix") + " - " + key);
            }
        }
    }

    public static void loadMusic() {
        Plugin mainPlugin = JavaPlugin.getProvidingPlugin(Main.class);
        Main.stopMusicAll();
        Main.music = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "music.yml"));
        saveDefaultResource("mus/Drop any midi file here");
    }

    public static void playMusic(World playWorld, Location playLocation, String musicName) {
        MusicRunnable musicRun = new MusicRunnable();
        int tempMusicId = 0;
        boolean tempMusicFindId = false;
        int tempCheckMusicIndex = 0;
        if (!Main.playingMusic.isEmpty()) {
            while (!tempMusicFindId) {
                if (Main.playingMusic.get(tempCheckMusicIndex).musicPlayId == tempMusicId) {
                    tempMusicFindId = true;
                }
                else {
                    if (tempCheckMusicIndex + 1 >= Main.playingMusic.size()) {
                        while (Main.playingMusic.get(tempCheckMusicIndex).musicPlayId == tempMusicId) tempMusicId++;
                        tempMusicFindId = true;
                    }
                }
                tempCheckMusicIndex++;
            }
        }
        //设定Id
        musicRun.musicPlayId = tempMusicId;
        musicRun.run(musicName, playWorld, playLocation);
        Main.playingMusic.add(musicRun);
    }

    public static void stopMusic(String name) {
        for (MusicRunnable musicRunnable : Main.playingMusic.toArray(new MusicRunnable[0])) {
            if (Objects.equals(musicRunnable.musicName, name)) {
                musicRunnable.stop();
                Main.playingMusic.remove(musicRunnable);
            }
        }
    }

    public static void stopMusic(int id) {
        int index = 0;
        int idIndex = -1;
        for (MusicRunnable mRunnable : Main.playingMusic) {
            if (mRunnable.musicPlayId == id) {
                idIndex = index;
            }
            index++;
        }
        Main.playingMusic.get(idIndex).stop();
        Main.playingMusic.remove(idIndex);
    }

    public static void stopMusicAll() {
        for (MusicRunnable mRunnable : Main.playingMusic) {
            mRunnable.stop();
        }
        Main.playingMusic.clear();
    }

    public static void removeMusic(String musicName) throws IOException {
        Main.music.set(musicName, null);
        Main.music.save(new File(Main.getProvidingPlugin(Main.class).getDataFolder(), "music.yml"));
        Main.loadMusic();
    }

    public static List<String> getImportMusic() {
        List<String> filesList = new ArrayList<>();
        File musicDir = new File(Main.getProvidingPlugin(Main.class).getDataFolder(), "mus");
        String[] files = musicDir.list();
        if (files != null) {
            for (String s : files) {
                File file = new File(musicDir, s);
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

    public static void saveDefaultResource(String resourcePath) {
        if (!(new File(Main.getProvidingPlugin(Main.class).getDataFolder(), resourcePath).exists())) {
            Main.getProvidingPlugin(Main.class).saveResource(resourcePath, false);
        }
    }
}