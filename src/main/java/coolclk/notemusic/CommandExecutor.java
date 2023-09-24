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
        Boolean quiet = Arrays.asList(args).contains("--quiet");
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help": {
                    if (args.length > 1) {
                        if (Main.message.isSet("command-help-" + args[1])) for (String message : Main.message.getStringList("command-help-" + args[1])) sender.sendMessage(message);
                        else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-enough-arg"));
                    }
                    else {
                        if (!quiet) for (String message : Main.message.getStringList("command-help")) sender.sendMessage(message);
                    }
                    return true;
                }
                case "playmusic": {
                    if (sender.hasPermission("notemusic.playmusic")) {
                        if (args.length > 1) {
                            if (Main.music.contains(args[1])) {
                                final AtomicReference<World> world = new AtomicReference<>();
                                final AtomicReference<Location> location = new AtomicReference<>();
                                if (args.length == 3) {
                                    Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(args[2])).findAny().orElse(null);
                                    if (player != null) {
                                        world.set(player.getWorld());
                                        location.set(player.getLocation());
                                    } else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-unknown-player"));
                                } else if (args.length >= 3) {
                                    if (StringUtil.isDouble(args[2]) && StringUtil.isDouble(args[3]) && StringUtil.isDouble(args[4])) {
                                        if (args.length >= 6) {
                                            World pWorld = Bukkit.getWorlds().stream().filter(w -> w.getName().equals(args[5])).findAny().orElse(null);
                                            if (pWorld != null) world.set(pWorld);
                                            else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-unknown-world"));
                                        } else {
                                            if (sender instanceof Player) {
                                                world.set(((Player) sender).getWorld());
                                            } else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-console"));
                                        }
                                    } else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-enough-arg"));
                                } else {
                                    if (sender instanceof Player) {
                                        world.set(((Player) sender).getWorld());
                                        location.set(((Player) sender).getLocation());
                                    } else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-console"));
                                }
                                if (world.get() != null && location.get() != null) {
                                    if (!quiet) sender.sendMessage(Main.config.getString("prefix") + String.format(Main.message.getString("command-play-music"), args[1]));
                                    Main.playMusic(world.get(), location.get(), args[1]);
                                }
                            }
                            else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-unknown-music"));
                        } else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-enough-arg"));
                    }
                    else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-permission"));
                    return true;
                }
                case "stopmusic": {
                    if (sender.hasPermission("notemusic.stopmusic")) {
                        if (args.length > 1) {
                            if (Main.music.contains(args[1])) {
                                if (!quiet) sender.sendMessage(Main.config.getString("prefix") + String.format(Main.message.getString("command-stop-music"), args[1]));
                                Main.stopMusic(args[1]);
                            }
                            else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-unknown-music"));
                        }
                        else {
                            if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-stop-all-music"));
                            Main.stopMusicAll();
                        }
                    }
                    else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-permission"));
                    return true;
                }
                case "removemusic": {
                    if (sender.hasPermission("notemusic.removemusic")) {
                        if (args.length > 1) {
                            if (Main.music.contains(args[1])) {
                                try {
                                    Main.removeMusic(args[1]);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-unknown-music"));
                        }
                    }
                    else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-permission"));
                    return true;
                }
                case "importmusic": {
                    if (sender.hasPermission("notemusic.importmusic")) {
                        if (args.length > 1) {
                            Plugin mainPlugin = JavaPlugin.getProvidingPlugin(Main.class);
                            StringBuilder fileName = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                fileName.append(args[i]);
                                if (i + 1 < args.length) fileName.append(" ");
                            }
                            if (Main.config.getBoolean("debug")) Bukkit.getConsoleSender().sendMessage(Main.config.getString("prefix") + "[Debug] Try import file \"" + fileName + "\".");
                            if (Main.getImportMusic().contains(fileName.toString())) {
                                String fileSuffix = (Main.getFilenameSuffix(fileName.toString())).toUpperCase();
                                if (fileSuffix.equals("MID") || fileSuffix.equals("MIDI")) {
                                    if (!quiet) sender.sendMessage(Main.config.getString("prefix") + String.format(Main.message.getString("command-import-music"), fileName));
                                    try {
                                        MidiImporter.importMusic(fileName.toString());
                                        if (Arrays.asList(args).contains("--quiet")) if (!quiet) sender.sendMessage(Main.config.getString("prefix") + String.format(Main.message.getString("command-import-success-music"), fileName));
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-error-music-suffix"));
                            }
                            else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-unknown-music"));
                            try {
                                Main.music.save(new File(mainPlugin.getDataFolder(), "music.yml"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-enough-arg"));
                    }
                    else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-permission"));
                    return true;
                }
                case "list": {
                    if (sender.hasPermission("notemusic.list")) {
                        if (!quiet) sender.sendMessage(Main.config.getString("prefix") + String.format(Main.message.getString("command-list-counts"), Main.music.getKeys(false).size()));
                        for (String key : Main.music.getKeys(false)) {
                            if (sender instanceof Player) {
                                String rawMessage = "[\"\",{\"text\":\"" + Main.config.getString("prefix") + " " + key + " \"}";
                                if (sender.hasPermission("notemusic.playmusic")) rawMessage += ",{\"text\":\"" + Main.message.getString("command-list-play") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + Main.message.getString("command-list-hover") + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/notemusic:notemusic playmusic " + key + "\"}},\" \"";
                                if (sender.hasPermission("notemusic.stopmusic")) rawMessage += ",{\"text\":\"" + Main.message.getString("command-list-stop") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + Main.message.getString("command-list-hover") + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/notemusic:notemusic stopmusic " + key + "\"}},\" \"";
                                if (sender.hasPermission("notemusic.removemusic")) rawMessage += ",{\"text\":\"" + Main.message.getString("command-list-remove") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + Main.message.getString("command-list-hover") + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/notemusic:notemusic removemusic " + key + "\"}},\" \"";
                                rawMessage += "]";
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw " + sender.getName() + " " + rawMessage);
                            }
                            else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + " " + key);
                        }
                    }
                    else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-permission"));
                    return true;
                }
                case "reload": {
                    if (sender.hasPermission("notemusic.reload")) {
                        if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-reload"));
                        Main.loadPlugin(quiet);
                        if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-reloaded"));
                    }
                    else if (!quiet) sender.sendMessage(Main.config.getString("prefix") + Main.message.getString("command-permission"));
                    return true;
                }
            }
        }
        for (String message : Main.message.getStringList("command-help")) if (!quiet) sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1: {
                list.add("help");
                if (sender.hasPermission("notemusic.playmusic")) list.add("playmusic");
                if (sender.hasPermission("notemusic.stopmusic")) list.add("stopmusic");
                if (sender.hasPermission("notemusic.importmusic")) list.add("importmusic");
                if (sender.hasPermission("notemusic.removemusic")) list.add("removemusic");
                if (sender.hasPermission("notemusic.list")) list.add("list");
                if (sender.hasPermission("notemusic.relaod")) list.add("reload");
                break;
            }
            case 2: {
                switch (args[0]) {
                    case "help": {
                        if (sender.hasPermission("notemusic.playmusic")) list.add("playmusic");
                        if (sender.hasPermission("notemusic.stopmusic")) list.add("stopmusic");
                        if (sender.hasPermission("notemusic.importmusic")) list.add("importmusic");
                        if (sender.hasPermission("notemusic.removemusic")) list.add("removemusic");
                        if (sender.hasPermission("notemusic.list")) list.add("list");
                        if (sender.hasPermission("notemusic.relaod")) list.add("reload");
                        break;
                    }
                    case "playmusic":
                    case "stopmusic":
                    case "removemusic": {
                        if (sender.hasPermission("notemusic.playmusic")) list.addAll(Main.music.getKeys(false));
                        break;
                    }
                    case "importmusic": {
                        if (sender.hasPermission("notemusic.importmusic")) {
                            Main.getImportMusic();
                            list.addAll(Main.getImportMusic());
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
        return list;
    }
}