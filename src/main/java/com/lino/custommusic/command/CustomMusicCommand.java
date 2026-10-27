package com.lino.custommusic.command;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.gui.ControlGui;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomMusicCommand implements CommandExecutor, TabCompleter {
    private final CustomMusicPlugin plugin;

    public CustomMusicCommand(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                MusicBox companion = plugin.getCompanionManager().getCompanionBox(player);
                if (companion != null) {
                    ControlGui gui = new ControlGui(plugin, player, companion);
                    plugin.getGuiManager().registerWindow(player, gui);
                    gui.open();
                    return true;
                }
            }
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("custommusic.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getSongRegistry().loadAll();
                plugin.getRadioStation().refreshQueue();
                sender.sendMessage(plugin.getConfigManager().getMessage("general.reloaded"));
                return true;
            }
            case "give" -> {
                if (!sender.hasPermission("custommusic.admin") && !sender.hasPermission("custommusic.give")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return true;
                }

                Player target = null;
                boolean isLobby = false;

                if (args.length >= 2) {
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        if (args[1].equalsIgnoreCase("lobby") && sender instanceof Player p) {
                            target = p;
                            isLobby = true;
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getMessage("general.player_not_found"));
                            return true;
                        }
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                }

                if (args.length >= 3 && args[2].equalsIgnoreCase("lobby")) {
                    isLobby = true;
                }

                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.player_only"));
                    return true;
                }

                ItemStack boxItem = plugin.getConfigManager().createBoxItem(isLobby);
                target.getInventory().addItem(boxItem);

                target.sendMessage(plugin.getConfigManager().getMessage("box.received"));
                if (sender != target) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("box.given_to")
                            .replace("%player%", target.getName()));
                }
                return true;
            }
            case "scan" -> {
                if (!sender.hasPermission("custommusic.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return true;
                }
                plugin.getSongRegistry().loadAll();
                plugin.getRadioStation().refreshQueue();
                int count = plugin.getSongRegistry().getAllSongs().size();
                sender.sendMessage(plugin.getConfigManager().getMessage("audio.scanned")
                        .replace("%count%", String.valueOf(count)));
                return true;
            }
            case "stopall" -> {
                if (!sender.hasPermission("custommusic.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return true;
                }
                plugin.getBoxManager().stopAll();
                plugin.getRadioStation().stop();
                sender.sendMessage(plugin.getConfigManager().getMessage("box.all_stopped"));
                return true;
            }
            case "add" -> {
                if (!sender.hasPermission("custommusic.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(plugin.getConfigManager().format("&cUsage: /cm add <id> <price> <name...>"));
                    return true;
                }

                String id = args[1].toLowerCase();
                if (plugin.getSongRegistry().getSong(id) != null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("audio.song_already_registered")
                            .replace("%id%", id));
                    return true;
                }

                double price;
                try {
                    price = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfigManager().format("&cInvalid price format."));
                    return true;
                }

                String title = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                File songFile = new File(plugin.getDataFolder(), "songs/" + id + ".nbs");
                if (!songFile.exists()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("audio.file_not_found"));
                    return true;
                }

                SongEntry entry = com.lino.custommusic.audio.NbsParser.parse(songFile, id, price, "");
                if (entry != null) {
                    plugin.getSongRegistry().registerSong(entry);
                    sender.sendMessage(plugin.getConfigManager().getMessage("audio.song_added")
                            .replace("%name%", title));
                }
                return true;
            }
            case "delete" -> {
                if (!sender.hasPermission("custommusic.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().format("&cUsage: /cm delete <id>"));
                    return true;
                }
                String id = args[1].toLowerCase();
                plugin.getSongRegistry().unregisterSong(id);
                sender.sendMessage(plugin.getConfigManager().getMessage("audio.song_deleted"));
                return true;
            }
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            default -> {
                sender.sendMessage(plugin.getConfigManager().getMessage("general.unknown_command"));
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getRawMessage("help.header"));
        sender.sendMessage(plugin.getConfigManager().getRawMessage("help.use"));
        if (sender.hasPermission("custommusic.admin") || sender.hasPermission("custommusic.give")) {
            sender.sendMessage(plugin.getConfigManager().getRawMessage("help.give"));
        }
        if (sender.hasPermission("custommusic.admin")) {
            sender.sendMessage(plugin.getConfigManager().getRawMessage("help.scan"));
            sender.sendMessage(plugin.getConfigManager().getRawMessage("help.add"));
            sender.sendMessage(plugin.getConfigManager().getRawMessage("help.delete"));
            sender.sendMessage(plugin.getConfigManager().getRawMessage("help.stopall"));
            sender.sendMessage(plugin.getConfigManager().getRawMessage("help.reload"));
        }
        sender.sendMessage(plugin.getConfigManager().getRawMessage("help.footer"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            if (sender.hasPermission("custommusic.admin") || sender.hasPermission("custommusic.give")) {
                list.add("give");
            }
            if (sender.hasPermission("custommusic.admin")) {
                list.add("scan");
                list.add("add");
                list.add("delete");
                list.add("stopall");
                list.add("reload");
            }
            return filter(list, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                list.add("lobby");
            } else if (args[0].equalsIgnoreCase("delete")) {
                list.addAll(plugin.getSongRegistry().getAllSongs().stream().map(SongEntry::getId).toList());
            }
            return filter(list, args[1]);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            list.add("lobby");
            return filter(list, args[2]);
        }
        return list;
    }

    private List<String> filter(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .sorted()
                .toList();
    }
}
