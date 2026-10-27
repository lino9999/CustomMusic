package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class GuiWindow implements InventoryHolder {
    protected final CustomMusicPlugin plugin;
    protected final Player player;
    protected final MusicBox musicBox;
    protected final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();

    public GuiWindow(CustomMusicPlugin plugin, Player player, MusicBox musicBox, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        this.musicBox = musicBox;
        this.inventory = Bukkit.createInventory(this, rows * 9, plugin.getConfigManager().format(title));
    }

    public abstract void build();

    public void open() {
        build();
        plugin.getGuiManager().registerWindow(player, this);
        player.openInventory(inventory);
    }

    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        inventory.setItem(slot, item);
        if (handler != null) {
            clickHandlers.put(slot, handler);
        } else {
            clickHandlers.remove(slot);
        }
    }

    public void fillBorders(Material material) {
        ItemStack filler = createButton(material, " ");
        int size = inventory.getSize();
        int rows = size / 9;

        for (int c = 0; c < 9; c++) {
            setItem(c, filler, null);
            setItem(size - 9 + c, filler, null);
        }
        for (int r = 1; r < rows - 1; r++) {
            setItem(r * 9, filler, null);
            setItem(r * 9 + 8, filler, null);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getRawSlot());
        if (handler != null) {
            handler.accept(event);
        }
        player.updateInventory();
    }

    protected ItemStack createButton(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().format(name));
            if (lore.length > 0) {
                meta.setLore(Arrays.stream(lore).map(plugin.getConfigManager()::format).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createButton(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().format(name));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(plugin.getConfigManager()::format).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public MusicBox getMusicBox() {
        return musicBox;
    }
}
