package com.lino.custommusic.config;

import com.lino.custommusic.CustomMusicPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {
    public static final NamespacedKey BOX_ITEM_KEY = new NamespacedKey("custommusic", "music_box_item");
    public static final NamespacedKey LOBBY_ITEM_KEY = new NamespacedKey("custommusic", "lobby_box_item");

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})|<#([A-Fa-f0-9]{6})>");
    private final CustomMusicPlugin plugin;
    private FileConfiguration messagesConfig;

    public ConfigManager(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String msg = messagesConfig != null ? messagesConfig.getString(path) : null;
        if (msg == null) {
            return path;
        }
        String prefix = messagesConfig.getString("prefix", "");
        return format(prefix + msg);
    }

    public String getRawMessage(String path) {
        String msg = messagesConfig != null ? messagesConfig.getString(path) : null;
        return msg != null ? format(msg) : path;
    }

    public String format(String text) {
        if (text == null) return "";

        text = text.replace("<bold>", "&l").replace("</bold>", "&r")
                .replace("<italic>", "&o").replace("</italic>", "&r")
                .replace("<underline>", "&n").replace("</underline>", "&r")
                .replace("<red>", "&c").replace("</red>", "&r")
                .replace("<green>", "&a").replace("</green>", "&r")
                .replace("<yellow>", "&e").replace("</yellow>", "&r")
                .replace("<gold>", "&6").replace("</gold>", "&r")
                .replace("<aqua>", "&b").replace("</aqua>", "&r")
                .replace("<blue>", "&9").replace("</blue>", "&r")
                .replace("<purple>", "&d").replace("</purple>", "&r")
                .replace("<white>", "&f").replace("</white>", "&r")
                .replace("<gray>", "&7").replace("</gray>", "&r")
                .replace("<dark_gray>", "&8").replace("</dark_gray>", "&r");

        text = text.replaceAll("<gradient:[^>]+>", "").replace("</gradient>", "");

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            matcher.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public float getVolumeMultiplier() {
        return (float) plugin.getConfig().getDouble("general.volume_multiplier", 1.0);
    }

    public int getVolumeStep() {
        return plugin.getConfig().getInt("general.volume_step", 5);
    }

    public boolean isAutoScanOnStartup() {
        return plugin.getConfig().getBoolean("general.auto_scan_on_startup", true);
    }

    public boolean isRadioEnabled() {
        return plugin.getConfig().getBoolean("radio.enabled", true);
    }

    public int getRadioPauseSeconds() {
        return plugin.getConfig().getInt("radio.pause_between_tracks_seconds", 3);
    }

    public boolean isBossBarEnabled() {
        return plugin.getConfig().getBoolean("displays.boss_bar_enabled", true);
    }

    public boolean isActionBarEnabled() {
        return plugin.getConfig().getBoolean("displays.action_bar_enabled", false);
    }

    public String getBossBarFormat() {
        return plugin.getConfig().getString("displays.boss_bar_format", "♫ %song% (%time% / %total%)");
    }

    public String getActionBarFormat() {
        return plugin.getConfig().getString("displays.action_bar_format", "Playing: %song%");
    }

    public boolean isDiscoFloorEnabled() {
        return plugin.getConfig().getBoolean("disco_floor.enabled", true);
    }

    public List<Material> getDiscoFloorPalette() {
        List<Material> list = new ArrayList<>();
        for (String name : plugin.getConfig().getStringList("disco_floor.palette")) {
            try {
                list.add(Material.valueOf(name));
            } catch (Exception ignored) {}
        }
        if (list.isEmpty()) {
            list.add(Material.LIME_CONCRETE);
            list.add(Material.PINK_CONCRETE);
            list.add(Material.LIGHT_BLUE_CONCRETE);
        }
        return list;
    }

    public boolean isCompanionEnabled() {
        return plugin.getConfig().getBoolean("portable_companion.enabled", true);
    }

    public boolean isCompanionSneakInteract() {
        return plugin.getConfig().getBoolean("portable_companion.sneak_interact", true);
    }

    public ItemStack createBoxItem(boolean lobby) {
        Material mat = Material.NOTE_BLOCK;
        try {
            mat = Material.valueOf(plugin.getConfig().getString("box_appearance.material", "NOTE_BLOCK"));
        } catch (Exception ignored) {}

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String nameKey = lobby ? "box_appearance.lobby_item_name" : "box_appearance.item_name";
            meta.setDisplayName(format(plugin.getConfig().getString(nameKey, "&6&lCustom Music Box")));

            List<String> rawLore = plugin.getConfig().getStringList("box_appearance.item_lore");
            List<String> formattedLore = new ArrayList<>();
            for (String l : rawLore) {
                formattedLore.add(format(l));
            }
            meta.setLore(formattedLore);

            int cmd = plugin.getConfig().getInt("box_appearance.custom_model_data", 0);
            if (cmd > 0) {
                meta.setCustomModelData(cmd);
            }

            meta.getPersistentDataContainer().set(
                    lobby ? LOBBY_ITEM_KEY : BOX_ITEM_KEY,
                    PersistentDataType.BYTE,
                    (byte) 1
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isMusicBoxItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(BOX_ITEM_KEY, PersistentDataType.BYTE)
                || meta.getPersistentDataContainer().has(LOBBY_ITEM_KEY, PersistentDataType.BYTE);
    }

    public boolean isLobbyBoxItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(LOBBY_ITEM_KEY, PersistentDataType.BYTE);
    }
}
