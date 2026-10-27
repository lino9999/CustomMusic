package com.lino.custommusic.listener;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.gui.ControlGui;
import com.lino.custommusic.model.MusicBox;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class BoxInteractionListener implements Listener {
    private final CustomMusicPlugin plugin;

    public BoxInteractionListener(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!plugin.getConfigManager().isMusicBoxItem(item)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("custommusic.place")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
            event.setCancelled(true);
            return;
        }

        int currentBoxes = plugin.getBoxManager().getPlayerBoxCount(player.getUniqueId());
        int limit = plugin.getBoxManager().getPlayerBoxLimit(player);
        if (currentBoxes >= limit) {
            String msg = plugin.getConfigManager().getMessage("box.limit_reached")
                    .replace("%limit%", String.valueOf(limit));
            player.sendMessage(msg);
            event.setCancelled(true);
            return;
        }

        boolean isLobby = plugin.getConfigManager().isLobbyBoxItem(item);
        Location loc = event.getBlockPlaced().getLocation();
        loc.setYaw(player.getLocation().getYaw());

        MusicBox box = new MusicBox(UUID.randomUUID(), player.getUniqueId(), loc, isLobby);
        plugin.getBoxManager().registerBox(box);
        plugin.getDatabaseService().saveBox(box);

        player.sendMessage(plugin.getConfigManager().getMessage("box.placed"));
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            MusicBox box = plugin.getBoxManager().getBoxAt(block.getLocation());

            if (box != null) {
                event.setCancelled(true);

                if (!player.hasPermission("custommusic.use")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                    return;
                }

                if (box.isLocked() && !player.getUniqueId().equals(box.getOwnerUuid()) && !player.hasPermission("custommusic.modify.others")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("box.not_owner"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                if (plugin.getConfigManager().isCompanionEnabled() && plugin.getConfigManager().isCompanionSneakInteract() && player.isSneaking()) {
                    if (!player.hasPermission("custommusic.portable")) {
                        player.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
                        return;
                    }

                    if (box.getEngine() != null) {
                        box.getEngine().getFloorEffect().reset();
                        box.getEngine().getBallEffect().remove();
                    }
                    plugin.getBoxManager().unregisterBox(box);
                    block.setType(Material.AIR);
                    plugin.getCompanionManager().setCompanion(player, box);
                    player.sendMessage(plugin.getConfigManager().getMessage("box.portable_activated"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                    return;
                }

                if (box.isInUse()) {
                    player.sendMessage(plugin.getConfigManager().getMessage("box.already_open"));
                    return;
                }

                box.setInUse(true);
                ControlGui gui = new ControlGui(plugin, player, box);
                plugin.getGuiManager().registerWindow(player, gui);
                gui.open();
                return;
            }

            if (plugin.getCompanionManager().isCarrying(player) && player.isSneaking()) {
                event.setCancelled(true);
                Block targetBlock = block.getRelative(event.getBlockFace());
                if (isDockable(targetBlock)) {
                    MusicBox companionBox = plugin.getCompanionManager().getCompanionBox(player);
                    plugin.getCompanionManager().removeCompanion(player);

                    targetBlock.setType(Material.NOTE_BLOCK);
                    companionBox.setLocation(targetBlock.getLocation());
                    plugin.getBoxManager().registerBox(companionBox);
                    plugin.getDatabaseService().saveBox(companionBox);

                    player.sendMessage(plugin.getConfigManager().getMessage("box.portable_docked"));
                    player.playSound(targetBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        MusicBox box = plugin.getBoxManager().getBoxAt(block.getLocation());
        if (box == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("custommusic.place")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no_permission"));
            event.setCancelled(true);
            return;
        }

        if (box.isLocked() && !player.getUniqueId().equals(box.getOwnerUuid()) && !player.hasPermission("custommusic.break.others")) {
            player.sendMessage(plugin.getConfigManager().getMessage("box.not_owner"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);
        if (box.getEngine() != null) {
            box.getEngine().getFloorEffect().reset();
            box.getEngine().getBallEffect().remove();
        }
        plugin.getBoxManager().unregisterBox(box);

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack drop = plugin.getConfigManager().createBoxItem(box.isAutoPlay());
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }

        player.sendMessage(plugin.getConfigManager().getMessage("box.broken"));
        player.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplodedBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplodedBlocks(event.blockList());
    }

    private void handleExplodedBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            MusicBox box = plugin.getBoxManager().getBoxAt(block.getLocation());
            if (box != null) {
                if (box.getEngine() != null) {
                    box.getEngine().getFloorEffect().reset();
                    box.getEngine().getBallEffect().remove();
                }
                plugin.getBoxManager().unregisterBox(box);
                ItemStack drop = plugin.getConfigManager().createBoxItem(box.isAutoPlay());
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getBoxManager().getBoxAt(block.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getBoxManager().getBoxAt(block.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isDockable(Block block) {
        if (block == null) return false;
        Material mat = block.getType();
        return mat.isAir() || mat == Material.SHORT_GRASS || mat == Material.TALL_GRASS
                || mat == Material.FERN || mat == Material.LARGE_FERN
                || mat == Material.SNOW || mat == Material.DEAD_BUSH
                || mat == Material.VINE || mat == Material.GLOW_LICHEN;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (isPluginArmorStand(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (isPluginArmorStand(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (isPluginArmorStand(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    private boolean isPluginArmorStand(Entity entity) {
        if (!(entity instanceof ArmorStand stand)) return false;
        NamespacedKey companionKey = new NamespacedKey(plugin, "companion");
        NamespacedKey discoballKey = new NamespacedKey(plugin, "discoball");
        return stand.getPersistentDataContainer().has(companionKey, PersistentDataType.BYTE)
                || stand.getPersistentDataContainer().has(discoballKey, PersistentDataType.BYTE);
    }
}
