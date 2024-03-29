package coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends JavaPlugin {
    public static FileConfiguration config = null, instrument = null, music = null, message = null;
    public final static List<MusicRunnable> playingMusic = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(EventListener.INSTANCE, this);

        this.getCommand("notemusic").setExecutor(CommandExecutor.INSTANCE);
        this.getCommand("nm").setExecutor(CommandExecutor.INSTANCE);
        this.getCommand("notemusic").setTabCompleter(CommandExecutor.INSTANCE);
        this.getCommand("nm").setTabCompleter(CommandExecutor.INSTANCE);
        loadPlugin();
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
        if (channelKey == null) channelKey = "_DEFAULT_";
        try {
            return Sound.valueOf(Main.instrument.getConfigurationSection("sounds").getString(channelKey).toUpperCase().replaceAll(" ", "_"));
        } catch (Exception e) {
            System.err.println("Cannot found channel sound \"" + channelKey + "\": ");
            e.printStackTrace(System.err);
            return null;
        }
    }

    public static void loadPlugin() {
        Plugin mainPlugin = JavaPlugin.getProvidingPlugin(Main.class);
        Main.saveDefaultResource("config.yml", true);
        Main.saveDefaultResource("music.yml", true);
        Main.saveDefaultResource("instrument.yml", true);
        Main.saveDefaultResource("lang/message-en_US.yml", false);
        Main.saveDefaultResource("lang/message-zh_CN.yml", false);
        Main.saveDefaultFolder("mus");
        Main.config = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "config.yml"));
        Main.instrument = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "instrument.yml"));
        Main.message = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "lang/message-" + Main.config.getString("language") + ".yml"));
        loadMusic();
        Bukkit.getConsoleSender().sendMessage(config.getString("prefix") + String.format(message.getString("load-music"), music.getKeys(false).size()));
        for (String key : music.getKeys(false)) {
            Bukkit.getConsoleSender().sendMessage(config.getString("prefix") + " - " + key);
        }
    }

    public static void loadMusic() {
        Plugin mainPlugin = JavaPlugin.getProvidingPlugin(Main.class);
        Main.stopMusicAll();
        Main.music = YamlConfiguration.loadConfiguration(new File(mainPlugin.getDataFolder(), "music.yml"));
        Main.saveDefaultFolder("mus");
    }

    public static void playMusic(World playWorld, Location playLocation, String musicName) {
        MusicRunnable musicRun = new MusicRunnable();
        if (!Main.playingMusic.isEmpty()) {
            int tempMusicId = (int) (Math.random() * Main.playingMusic.size() * 10);
            for (MusicRunnable runnable : Main.playingMusic) {
                while (tempMusicId == runnable.musicPlayId) {
                    tempMusicId = new Random().nextInt(Main.playingMusic.size() * 10);
                }
            }
            musicRun.musicPlayId = tempMusicId;
            musicRun.run(musicName, playWorld, playLocation);
            Main.playingMusic.add(musicRun);
        }
    }

    /* Stop by name
    public static void stopMusic(String name) {
        for (MusicRunnable musicRunnable : Main.playingMusic.toArray(new MusicRunnable[0])) {
            if (Objects.equals(musicRunnable.musicName, name)) {
                musicRunnable.stop();
                Main.playingMusic.remove(musicRunnable);
            }
        }
    }
     */

    public static boolean stopMusic(int id) {
        int index = 0;
        int idIndex = -1;
        for (MusicRunnable mRunnable : Main.playingMusic) {
            if (mRunnable.musicPlayId == id) {
                idIndex = index;
            }
            index++;
        }
        if (idIndex < 0) return false;
        Main.playingMusic.get(idIndex).stop();
        Main.playingMusic.remove(idIndex);
        return true;
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

    public static List<String> getUnreportedMusic() {
        List<String> filesList = new ArrayList<>();
        File musicDir = new File(Main.getProvidingPlugin(Main.class).getDataFolder(), "mus");
        String[] files = musicDir.list();
        if (files != null) {
            for (String s : files) {
                File file = new File(musicDir, s);
                String expandName = getFilenameSuffix(file.getName()).toUpperCase();
                if (file.isFile() && (expandName.equals("MIDI") || expandName.equals("MID"))) filesList.add(file.getName());
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

    public static void saveDefaultResource(String resourcePath, boolean onlyWarning) {
        boolean replace = false;
        if (new File(Main.getProvidingPlugin(Main.class).getDataFolder(), resourcePath).exists()) {
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Main.getProvidingPlugin(Main.class).getResource(resourcePath))), oldConfig = YamlConfiguration.loadConfiguration(new File(Main.getProvidingPlugin(Main.class).getDataFolder(), resourcePath));
            if (!oldConfig.contains("version") || !newConfig.getString("version").equals(oldConfig.getString("version"))) {
                replace = !onlyWarning;
                Bukkit.getConsoleSender().sendMessage("§7[§eNote§bMusic§7] §eWarning! The config file " + resourcePath + " version was different from the plugin version" + (onlyWarning ? "" : ", plugin will replace it."));
            }
        }
        Main.getProvidingPlugin(Main.class).saveResource(resourcePath, replace);
    }

    public static void saveDefaultFolder(String folderPath) {
        new File(Main.getProvidingPlugin(Main.class).getDataFolder(), folderPath).mkdirs();
    }
}
