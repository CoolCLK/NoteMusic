package github.coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class commandExecutor implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "help": {
                    if (args.length > 1) {
                        if (main.message.isSet("command-help-" + args[1])) for (String message : main.message.getStringList("command-help-" + args[1])) sender.sendMessage(message);
                        else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-enough-arg"));
                        return true;
                    }
                    else {
                        for (String message : main.message.getStringList("command-help")) sender.sendMessage(message);
                        return true;
                    }
                }
                case "playmusic": {
                    if (sender.hasPermission("notemusic.playmusic")) {
                        if (args.length > 1) {
                            if (sender instanceof Player) {
                                if (main.music.contains(args[1])) {
                                    sender.sendMessage(main.config.getString("prefix") + String.format(main.message.getString("command-play-music"), args[1]));
                                    main.playMusic((Player) sender, args[1]);
                                }
                                else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-unknown-music"));
                            } else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-console"));
                        } else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-enough-arg"));
                    }
                    else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-permission"));
                    return true;
                }
                case "stopmusic": {
                    if (sender.hasPermission("notemusic.stopmusic")) {
                        if (args.length > 1) {
                            if (main.music.contains(args[1])) {
                                sender.sendMessage(main.config.getString("prefix") + String.format(main.message.getString("command-stop-music"), args[1]));
                                main.stopMusic(args[1]);
                            }
                            else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-unknown-music"));
                        }
                        else {
                            sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-stop-all-music"));
                            main.stopMusicAll();
                        }
                    }
                    else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-permission"));
                    return true;
                }
                case "removemusic": {
                    if (sender.hasPermission("notemusic.removemusic")) {
                        if (args.length > 1) {
                            if (main.music.contains(args[1])) {
                                try {
                                    main.removeMusic(args[1]);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-unknown-music"));
                        }
                    }
                    else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-permission"));
                    return true;
                }
                case "importmusic": {
                    if (sender.hasPermission("notemusic.importmusic")) {
                        if (args.length > 1) {
                            Plugin mainPlugin = JavaPlugin.getProvidingPlugin(main.class);
                            String fileName = "";
                            for (int i = 1; i < args.length; i++) {
                                fileName += args[i];
                                if (i + 1 < args.length) fileName += " ";
                            }
                            if (main.config.getBoolean("debug")) Bukkit.getConsoleSender().sendMessage(main.config.getString("prefix") + "[Debug] Try import file \"" + fileName + "\".");
                            if (main.getImportMusic().contains(fileName)) {
                                String fileSuffix = (main.getFilenameSuffix(fileName)).toUpperCase();
                                if (fileSuffix.equals("MID") || fileSuffix.equals("MIDI")) {
                                    sender.sendMessage(main.config.getString("prefix") + String.format(main.message.getString("command-import-music"), fileName));
                                    try {
                                        midiImporter.importMusic(fileName);
                                        sender.sendMessage(main.config.getString("prefix") + String.format(main.message.getString("command-import-success-music"), fileName));
                                    } catch (InvalidMidiDataException e) {
                                        throw new RuntimeException(e);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    } catch (InstantiationException e) {
                                        throw new RuntimeException(e);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-error-music-suffix"));
                            }
                            else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-unknown-music"));
                            try {
                                main.music.save(new File(mainPlugin.getDataFolder(), "music.yml"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-enough-arg"));
                    }
                    else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-permission"));
                    return true;
                }
                case "list": {
                    if (sender.hasPermission("notemusic.list")) {
                        sender.sendMessage(main.config.getString("prefix") + String.format(main.message.getString("command-list-counts"), main.music.getKeys(false).size()));
                        for (String key : main.music.getKeys(false)) {
                            if (sender instanceof Player) {
                                String rawMessage = "[\"\",{\"text\":\"" + main.config.getString("prefix") + " " + key + " \"}";
                                if (sender.hasPermission("notemusic.playmusic")) rawMessage += ",{\"text\":\"" + main.message.getString("command-list-play") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + main.message.getString("command-list-hover") + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/notemusic:notemusic playmusic " + key + "\"}},\" \"";
                                if (sender.hasPermission("notemusic.stopmusic")) rawMessage += ",{\"text\":\"" + main.message.getString("command-list-stop") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + main.message.getString("command-list-hover") + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/notemusic:notemusic stopmusic " + key + "\"}},\" \"";
                                if (sender.hasPermission("notemusic.removemusic")) rawMessage += ",{\"text\":\"" + main.message.getString("command-list-remove") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + main.message.getString("command-list-hover") + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/notemusic:notemusic removemusic " + key + "\"}},\" \"";
                                rawMessage += "]";
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw " + ((Player) sender).getName() + " " + rawMessage);
                            }
                            else sender.sendMessage(main.config.getString("prefix") + " " + key);
                        }
                    }
                    else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-permission"));
                    return true;
                }
                case "reload": {
                    if (sender.hasPermission("notemusic.reload")) {
                        sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-reload"));
                        main.loadPlugin();
                        sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-reloaded"));
                    }
                    else sender.sendMessage(main.config.getString("prefix") + main.message.getString("command-permission"));
                    return true;
                }
            }
        }
        for (String message : main.message.getStringList("command-help")) sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
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
                        if (sender.hasPermission("notemusic.playmusic")) list = Arrays.asList(main.music.getKeys(false).toArray(new String[0]));
                        break;
                    }
                    case "importmusic": {
                        if (sender.hasPermission("notemusic.importmusic")) if (main.getImportMusic() != null) list = main.getImportMusic();
                        break;
                    }
                }
                break;
            }
            case 3: {
                if (sender.hasPermission("notemusic.playmusic")) {
                    if (args[0] == "playmusic") {
                        List<String> finalList = list;
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            finalList.add(player.getName());
                        });
                        list = finalList;
                    }
                }
                break;
            }
        }
        return list;
    }
}