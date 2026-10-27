package com.lino.custommusic;

import com.lino.custommusic.api.CustomMusicAPI;
import com.lino.custommusic.audio.RadioStation;
import com.lino.custommusic.audio.SongRegistry;
import com.lino.custommusic.command.CustomMusicCommand;
import com.lino.custommusic.config.ConfigManager;
import com.lino.custommusic.database.DatabaseService;
import com.lino.custommusic.gui.GuiManager;
import com.lino.custommusic.hook.EconomyHook;
import com.lino.custommusic.hook.PlaceholderHook;
import com.lino.custommusic.listener.BoxInteractionListener;
import com.lino.custommusic.listener.InventoryClickListener;
import com.lino.custommusic.listener.PlayerSessionListener;
import com.lino.custommusic.model.BoxManager;
import com.lino.custommusic.portable.CompanionTracker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomMusicPlugin extends JavaPlugin {
    private static CustomMusicPlugin instance;

    private ConfigManager configManager;
    private DatabaseService databaseService;
    private EconomyHook economyHook;
    private SongRegistry songRegistry;
    private BoxManager boxManager;
    private RadioStation radioStation;
    private CompanionTracker companionManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.reload();

        databaseService = new DatabaseService(this);
        databaseService.initialize();

        economyHook = new EconomyHook();
        economyHook.init();

        songRegistry = new SongRegistry(this);
        if (configManager.isAutoScanOnStartup()) {
            songRegistry.loadAll();
        }

        boxManager = new BoxManager(this);
        boxManager.loadBoxes();

        radioStation = new RadioStation(this);
        radioStation.start();

        companionManager = new CompanionTracker(this);
        guiManager = new GuiManager(this);

        CustomMusicAPI.init(this);

        getServer().getPluginManager().registerEvents(new BoxInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);

        CustomMusicCommand commandHandler = new CustomMusicCommand(this);
        if (getCommand("custommusic") != null) {
            getCommand("custommusic").setExecutor(commandHandler);
            getCommand("custommusic").setTabCompleter(commandHandler);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (companionManager != null) {
                companionManager.tick();
            }
        }, 1L, 1L);

        getLogger().info("CustomMusic enabled successfully! Designed by Lino.");
    }

    @Override
    public void onDisable() {
        if (guiManager != null) {
            guiManager.closeAll();
        }

        if (companionManager != null) {
            companionManager.clearAll();
        }

        if (radioStation != null) {
            radioStation.stop();
        }

        if (boxManager != null) {
            boxManager.stopAll();
            boxManager.saveAll();
        }

        if (databaseService != null) {
            databaseService.close();
        }

        getLogger().info("CustomMusic disabled cleanly.");
    }

    public static CustomMusicPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public SongRegistry getSongRegistry() {
        return songRegistry;
    }

    public BoxManager getBoxManager() {
        return boxManager;
    }

    public RadioStation getRadioStation() {
        return radioStation;
    }

    public CompanionTracker getCompanionManager() {
        return companionManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
