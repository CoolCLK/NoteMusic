package coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CommandExecutor implements org.bukkit.command.CommandExecutor, TabCompleter {

    public final static CommandExecutor INSTANCE = new CommandExecutor();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean unknownCommand = false;
        boolean quiet = Arrays.asList(args).contains("--quiet");
        List<String> commandMessage = new ArrayList<>();
        if (args.length > 0) {
            if (sender.hasPermission("notemusic.command." + args[0])) {
                switch (args[0].toLowerCase()) {
                    case "gui": {
                        if (sender instanceof Player) NoteMusicGui.open((Player) sender);
                        else commandMessage.add(Main.message.getString("command-console"));
                        break;
                    }
                    case "help": {
                        if (args.length > 1) {
                            if (Main.message.isSet("command-help-" + args[1])) commandMessage.addAll(Main.message.getStringList("command-help-" + args[1]));
                            else commandMessage.add(Main.message.getString("command-enough-arg"));
                        } else commandMessage.addAll(Main.message.getStringList("command-help"));
                        break;
                    }
                    case "playmusic": {
                        if (args.length > 1) {
                            if (Main.music.contains(args[1])) {
                                final AtomicReference<World> world = new AtomicReference<>();
                                final AtomicReference<Location> location = new AtomicReference<>();
                                if (args.length == 3) {
                                    Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(args[2])).findAny().orElse(null);
                                    if (player != null) {
                                        world.set(player.getWorld());
                                        location.set(player.getLocation());
                                    } else commandMessage.add(Main.message.getString("command-unknown-player"));
                                } else if (args.length >= 3) {
                                    if (StringUtil.isDouble(args[2]) && StringUtil.isDouble(args[3]) && StringUtil.isDouble(args[4])) {
                                        if (args.length >= 6) {
                                            World pWorld = Bukkit.getWorlds().stream().filter(w -> w.getName().equals(args[5])).findAny().orElse(null);
                                            if (pWorld != null) world.set(pWorld);
                                            else commandMessage.add(Main.message.getString("command-unknown-world"));
                                        } else {
                                            if (sender instanceof Player) world.set(((Player) sender).getWorld());
                                            else commandMessage.add(Main.message.getString("command-console"));
                                        }
                                    } else commandMessage.add(Main.message.getString("command-enough-arg"));
                                } else {
                                    if (sender instanceof Player) {
                                        world.set(((Player) sender).getWorld());
                                        location.set(((Player) sender).getLocation());
                                    } else commandMessage.add(Main.message.getString("command-console"));
                                }
                                if (world.get() != null && location.get() != null) {
                                    commandMessage.add(String.format(Main.message.getString("command-play-music"), args[1]));
                                    Main.playMusic(world.get(), location.get(), args[1]);
                                }
                            } else commandMessage.add(Main.message.getString("command-unknown-music"));
                        } else commandMessage.add(Main.message.getString("command-enough-arg"));
                        break;
                    }
                    case "stopmusic": {
                        if (args.length > 1) {
                            /* Stop by music name
                            if (Main.music.contains(args[1])) {
                                if (!quiet) commandMessage.add(Main.config.getString("prefix") + String.format(Main.message.getString("command-stop-musicByName"), args[1]));
                                Main.stopMusic(args[1]);
                            }
                            else if (!quiet) commandMessage.add(Main.config.getString("prefix") + Main.message.getString("command-unknown-music"));
                             */
                            if (StringUtil.isInteger(args[1]) && Main.stopMusic(Integer.parseInt(args[1]))) commandMessage.add(String.format(Main.message.getString("command-stop-musicByPlayingId"), args[1]));
                            else commandMessage.add(Main.message.getString("command-unknown-playingId"));

                        } else {
                            commandMessage.add(Main.message.getString("command-stop-all-music"));
                            Main.stopMusicAll();
                        }
                        break;
                    }
                    case "removemusic": {
                        if (args.length > 1) {
                            if (Main.music.contains(args[1])) {
                                try {
                                    Main.removeMusic(args[1]);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else commandMessage.add(Main.message.getString("command-unknown-music"));
                        }
                        break;
                    }
                    case "importmusic": {
                        if (args.length > 1) {
                            Plugin mainPlugin = JavaPlugin.getProvidingPlugin(Main.class);
                            StringBuilder fileName = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                fileName.append(args[i]);
                                if (i + 1 < args.length) fileName.append(" ");
                            }
                            if (Main.config.getBoolean("debug")) Bukkit.getConsoleSender().sendMessage(Main.config.getString("prefix") + "[Debug] Try import file \"" + fileName + "\".");
                            if (Main.getUnreportedMusic().contains(fileName.toString())) {
                                String fileSuffix = (Main.getFilenameSuffix(fileName.toString())).toUpperCase();
                                if (fileSuffix.equals("MID") || fileSuffix.equals("MIDI")) {
                                    commandMessage.add(String.format(Main.message.getString("command-import-music"), fileName));
                                    try {
                                        MidiImporter.importMusic(fileName.toString());
                                        commandMessage.add(String.format(Main.message.getString("command-import-success-music"), fileName));
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                } else commandMessage.add(Main.message.getString("command-error-music-suffix"));
                            } else commandMessage.add(Main.message.getString("command-unknown-music"));
                            try {
                                Main.music.save(new File(mainPlugin.getDataFolder(), "music.yml"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else commandMessage.add(Main.message.getString("command-enough-arg"));
                        break;
                    }
                    case "playinglist": {
                        commandMessage.add(String.format(Main.message.getString("command-playinglist-counts"), Main.music.getKeys(false).size()));
                        for (MusicRunnable runnable : Main.playingMusic) {
                            commandMessage.add(String.format(Main.message.getString("command-playinglist-object"), runnable.musicName, runnable.musicPlayId));
                        }
                        break;
                    }
                    case "musiclist": {
                        commandMessage.add(String.format(Main.message.getString("command-musiclist-counts"), Main.music.getKeys(false).size()));
                        commandMessage.addAll(Main.music.getKeys(false));
                        break;
                    }
                    case "reload": {
                        commandMessage.add(Main.message.getString("command-reload"));
                        Main.loadPlugin();
                        commandMessage.add(Main.message.getString("command-reloaded"));
                        break;
                    }
                    default: {
                        unknownCommand = true;
                        break;
                    }
                }
            } else commandMessage.add(Main.message.getString("command-permission"));
        } else unknownCommand = true;
        if (!quiet) {
            if (unknownCommand) {
                commandMessage.addAll(Main.message.getStringList("command-default"));
                if (sender.getEffectivePermissions().stream().anyMatch(permissionAttachmentInfo -> permissionAttachmentInfo.getPermission().startsWith("notemusic."))) commandMessage.add(Main.message.getString("command-default-help"));
            }
            commandMessage.forEach(message -> sender.sendMessage(Main.config.getString("prefix") + message));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1: {
                list.add("help");
                if (sender.hasPermission("notemusic.gui")) list.add("gui");
                if (sender.hasPermission("notemusic.playmusic")) list.add("playmusic");
                if (sender.hasPermission("notemusic.stopmusic")) list.add("stopmusic");
                if (sender.hasPermission("notemusic.importmusic")) list.add("importmusic");
                if (sender.hasPermission("notemusic.removemusic")) list.add("removemusic");
                if (sender.hasPermission("notemusic.playinglist")) list.add("playinglist");
                if (sender.hasPermission("notemusic.musiclist")) list.add("musiclist");
                if (sender.hasPermission("notemusic.reload")) list.add("reload");
                break;
            }
            case 2: {
                switch (args[0]) {
                    case "help": {
                        if (sender.hasPermission("notemusic.gui")) list.add("gui");
                        if (sender.hasPermission("notemusic.playmusic")) list.add("playmusic");
                        if (sender.hasPermission("notemusic.stopmusic")) list.add("stopmusic");
                        if (sender.hasPermission("notemusic.importmusic")) list.add("importmusic");
                        if (sender.hasPermission("notemusic.removemusic")) list.add("removemusic");
                        if (sender.hasPermission("notemusic.playinglist")) list.add("playinglist");
                        if (sender.hasPermission("notemusic.musiclist")) list.add("musiclist");
                        if (sender.hasPermission("notemusic.reload")) list.add("reload");
                        break;
                    }
                    case "playmusic":
                    // case "stopmusic": Stop by name
                    case "removemusic": {
                        if (sender.hasPermission("notemusic." + args[0])) list.addAll(Main.music.getKeys(false));
                        break;
                    }
                    case "stopmusic": {
                        if (sender.hasPermission("notemusic.stopmusic")) Main.playingMusic.forEach(musicRunnable -> list.add(String.valueOf(musicRunnable.musicPlayId)));
                        break;
                    }
                    case "importmusic": {
                        if (sender.hasPermission("notemusic.importmusic")) {
                            list.addAll(Main.getUnreportedMusic());
                        }
                        break;
                    }
                }
                break;
            }
            case 3: {
                if (sender.hasPermission("notemusic.playmusic")) {
                    if (args[0].equals("playmusic")) {
                        Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
                        if (sender instanceof Player) list.add(String.valueOf(((Player) sender).getLocation().getX()));
                    }
                }
                break;
            }
            case 4:
            case 5: {
                if (sender.hasPermission("notemusic.playmusic")) {
                    if (args[0].equals("playmusic") && StringUtil.isDouble(args[2])) {
                        if (sender instanceof Player) list.add(String.valueOf(args.length == 4 ? ((Player) sender).getLocation().getY() : ((Player) sender).getLocation().getZ()));
                    }
                }
                break;
            }
            case 6: {
                if (sender.hasPermission("notemusic.playmusic")) {
                    if (args[0].equals("playmusic") && StringUtil.isDouble(args[2])) {
                        Bukkit.getWorlds().forEach(world -> list.add(world.getName()));
                    }
                }
                break;
            }
        }
        new ArrayList<>(list).forEach(tab -> {
            if (!tab.startsWith(args[args.length - 1])) list.remove(tab);
        });
        return list;
    }
}