# CustomMusic - Agent Guidelines & Codebase Map

This document is designed for AI agents and LLM-assisted developers working on the **CustomMusic** codebase. Reading this file provides full context on the architecture, avoiding the need to scan large swathes of files or waste context window tokens.

---

## 1. Project Overview

- **Name**: CustomMusic
- **Author**: Lino
- **Platforms**: Paper / Spigot / Purpur / Folia-compatible Minecraft servers (1.21.x up to 26.x+)
- **Target JDK**: Java 21
- **Build System**: Maven (`pom.xml`)
- **Key Capabilities**:
  - Full `.nbs` (Note Block Studio v0-v5) music parsing and synchronized playback.
  - Custom `.ogg` sound playback via resource pack playsound commands.
  - Vanilla Minecraft music discs integration with dedicated jukebox collector GUI.
  - Synchronized server-wide continuous Radio station.
  - Interactive GUIs: Control Panel, Catalog (with Vault purchasing), Custom Playlists, Music Discs, Stats/Leaderboards, Visual Customizer.
  - Companion / Portable Player mode: A floating speaker follows player movement without fragile NMS.
  - Client-side Visuals: Particle rings/spirals, client-only Disco Floors (no block modification lag or griefing), Disco Ball head accessories, BossBar and ActionBar song progress indicators.
  - Pure Modern Bukkit API: Zero internal NMS version coupling; builds against 1.21+ API and runs across all current and future versions.
  - Asynchronous SQLite persistence for boombox states, playlists, unlocked tracks, and listen/purchase statistics.

---

## 2. Directory Structure

```
CustomMusic/
├── .gitignore
├── .ignore
├── pom.xml
├── README.md
├── docs/
│   ├── agents.md                  # This guide
│   ├── spigotmc_description.bbcode# SpigotMC BBCode overview
│   └── spigotmc_documentation.bbcode# SpigotMC BBCode documentation
├── assets/
│   └── icon.png                   # Pixel art plugin icon
└── src/
    └── main/
        ├── java/com/lino/custommusic/
        │   ├── CustomMusicPlugin.java
        │   ├── api/
        │   │   ├── CustomMusicAPI.java
        │   │   ├── MusicPlayer.java
        │   │   └── SongTrack.java
        │   ├── audio/
        │   │   ├── NbsParser.java
        │   │   ├── PlaybackEngine.java
        │   │   ├── RadioStation.java
        │   │   ├── SoundMapper.java
        │   │   └── DiscLibrary.java
        │   ├── command/
        │   │   ├── CustomMusicCommand.java
        │   │   └── CommandManager.java
        │   ├── config/
        │   │   ├── ConfigManager.java
        │   │   ├── ConfigKeys.java
        │   │   └── LocaleManager.java
        │   ├── database/
        │   │   ├── DatabaseService.java
        │   │   └── SQLiteBackend.java
        │   ├── gui/
        │   │   ├── GuiManager.java
        │   │   ├── GuiWindow.java
        │   │   ├── ControlGui.java
        │   │   ├── CatalogGui.java
        │   │   ├── PlaylistGui.java
        │   │   ├── DiscsGui.java
        │   │   ├── VisualsGui.java
        │   │   └── StatsGui.java
        │   ├── hook/
        │   │   ├── EconomyHook.java
        │   │   └── PlaceholderHook.java
        │   ├── listener/
        │   │   ├── BoxInteractionListener.java
        │   │   ├── InventoryClickListener.java
        │   │   └── PlayerSessionListener.java
        │   ├── model/
        │   │   ├── MusicBox.java
        │   │   ├── AudioNote.java
        │   │   ├── SongEntry.java
        │   │   ├── PlayerSession.java
        │   │   └── VisualSettings.java
        │   └── visual/
        │       ├── DiscoFloorEffect.java
        │       ├── DiscoBallEffect.java
        │       ├── NoteParticleEffect.java
        │       └── ProgressDisplay.java
        └── resources/
            ├── plugin.yml
            ├── config.yml
            ├── messages.yml
            └── songs/             # Default .nbs audio files
```

---

## 3. Core Architectural Rules

1. **No Source Code Comments**: The codebase must strictly preserve clean code formatting without inline comments or header comments.
2. **English Exclusively**: All user messages, console logs, config keys, and command descriptions are in clear English.
3. **Pure Bukkit/Paper API**: Never import `net.minecraft.server.*` (NMS). Always use standard Bukkit 1.21+ entities (`ArmorStand`, `ItemDisplay`), standard particles, and `sendBlockChange(...)`.
4. **O(1) Tick Playback**: Songs are indexed by tick into `Map<Integer, List<AudioNote>>`. The playback engine retrieves only the notes for the current tick instead of iterating through all notes.
5. **Thread Safety**: File I/O and SQLite operations must execute asynchronously via `Bukkit.getScheduler().runTaskAsynchronously`. Sound scheduling and entity interactions must execute on the server thread or compatible region schedulers.
6. **No Backdoors or Remote Pings**: All network checks, external license queries, or phone-home DRM are strictly prohibited.

---

## 4. Commands & Permissions

- **Command**: `/custommusic` (Aliases: `/cmusic`, `/cm`, `/musicbox`)
  - `/cm give [player] [lobby]` -> `custommusic.give` (self) / `custommusic.give.others`
  - `/cm stopall` -> `custommusic.admin`
  - `/cm scan` -> `custommusic.admin`
  - `/cm add <id> <price> <name>` -> `custommusic.admin`
  - `/cm delete <id>` -> `custommusic.admin`
  - `/cm reload` -> `custommusic.admin`
  - `/cm help [page]` -> `custommusic.use`
- **Player Permissions**:
  - `custommusic.use`: Open and listen to music boxes.
  - `custommusic.place`: Place and break placed boxes.
  - `custommusic.portable`: Pick up and carry portable companion box.
  - `custommusic.modify.others`: Control unlocked boxes belonging to other players.
  - `custommusic.break.others`: Break unlocked boxes belonging to other players.
  - `custommusic.limit.<n>`: Max boxes per player (default 1).
  - `custommusic.bypass.limit`: Unlimited boxes.

---

## 5. Development Guidelines for Agents

- **When adding a new GUI**: Extend `GuiWindow`, register in `GuiManager`, and handle clicks through the view callback.
- **When adding an audio format**: Update `NbsParser` or create a new reader implementing `SongTrack`.
- **When adding visual styles**: Add new enum constants to `VisualSettings` and register corresponding math algorithms in `DiscoFloorEffect` or `NoteParticleEffect`.
