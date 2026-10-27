<p align="center">
  <img src="assets/icon.png" alt="CustomMusic Logo" width="128" height="128" />
</p>

<h1 align="center">CustomMusic</h1>

<p align="center">
  <strong>The Ultimate Next-Generation Music Studio & Portable Jukebox for Minecraft</strong>
  <br />
  <em>Fully compatible with Spigot, Paper, Purpur & Folia from 1.21.x through 26.x+</em>
</p>

<p align="center">
  <a href="https://github.com/lino9999/CustomMusic/releases"><img src="https://img.shields.io/github/v/release/lino9999/CustomMusic?color=00D2FF&style=for-the-badge" alt="Release" /></a>
  <a href="https://www.java.com"><img src="https://img.shields.io/badge/Java-21+-orange?style=for-the-badge" alt="Java 21+" /></a>
  <a href="https://papermc.io"><img src="https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Folia-blue?style=for-the-badge" alt="Platforms" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License" /></a>
</p>

---

## 📖 About CustomMusic

> **CustomMusic** is a high-performance, clean-room engineered Minecraft music studio & portable companion jukebox plugin crafted by **Lino**. It empowers Minecraft server owners, builders, and players to deploy physical stereo music boxes, broadcast synchronized 24/7 radio stations, purchase songs with in-game currency via Vault, and carry floating companion speakers across survival and lobby worlds—with **100% vanilla client compatibility** and **zero client mods required**.

### 🔍 Search Engine Optimization (SEO) Highlights & Compatibility
- **Minecraft Versions**: Native support from **Minecraft 1.21.1 up to 26.3+**.
- **Supported Platforms**: Paper, Purpur, Spigot, and multi-threaded **Folia**.
- **Audio Formats**: Note Block Studio (.nbs v0–v5), Vanilla Jukebox Music Discs, Custom Resource Pack OGG sounds.
- **Performance**: Zero-reflection, pure modern Bukkit API with $O(1)$ tick-indexed note lookup, eliminating CPU spikes and server lag.
- **Visuals**: Grief-safe client-side packet lightshows (`sendBlockChange`), rotating crystal disco balls, and multi-color note halos.

### 🏷️ Repository Tags & Topics
`minecraft` • `minecraft-plugin` • `spigot-plugin` • `paper-plugin` • `folia` • `purpur` • `custom-music` • `note-block-studio` • `nbs-player` • `jukebox` • `boombox` • `minecraft-radio` • `portable-music` • `spigotmc` • `vault-economy` • `minecraft-1-21` • `minecraft-26` • `java21`

---

## ✨ Features

- **🎵 Full Note Block Studio (.nbs) Support**: Native decoding of all NBS file versions (v0 to v5). Renders multi-layered chords, instrument notes, tempo modulations, and custom note velocities with crystal-clear fidelity.
- **⚡ High-Performance $O(1)$ Audio Engine**: Notes are pre-indexed by tick. Playback never does expensive song-wide loops, resulting in zero server TPS drop even with dozens of active music boxes.
- **🎒 Portable Companion Mode**: Sneak + Right-Click any deployed box to pick it up! A companion speaker hovers near your shoulder, playing music as you move across the world.
- **📻 Global 24/7 Radio Broadcaster**: A synchronized server radio station that rotates through your music catalog. Players and stereo boxes can tune in or disconnect at any time.
- **💃 Grief-Free Client-Side Disco Floor**: Real-time shimmering block lightshows rendered exclusively through client packet changes (`sendBlockChange`). Zero block physics or world damage!
- **✨ Particle Halos & Floating Disco Ball**: Customizable particle effects (Rainbow, Spirals, Funnel Fire, Notes) and rotating crystal disco balls mounted above the speaker.
- **💰 In-Game Song Catalog & Store**: Seamlessly integrates with **Vault**. Players can browse, preview, and purchase songs using server currency.
- **🎧 Private Headphones Mode**: Easily switch between public ambient mode and private listening mode where only you hear the audio.
- **💿 Vanilla Music Discs Library**: Insert and spin any vanilla music disc (from Cat and Pigstep to Relic, Precipice, and Creator).
- **📊 Real-Time Analytics & Leaderboards**: Track total plays and purchase counts with in-game studio leaderboards.
- **🌐 100% Pure Modern API (Folia Compatible)**: Clean Bukkit/Paper API without version-dependent NMS imports. Works seamlessly on Folia regional threading.

---

## 🎮 In-Game Showcase & GUIs

CustomMusic includes an intuitive, fully interactive GUI suite:
- **Studio Control Center**: Master controls for playback, pause, volume sliders, track skipping, speed tempo (0.75x, 1.0x, 1.25x), headphones, and lock protection.
- **Song Catalog**: Categorized library with pagination and instant filters (*All*, *Purchased*, *Most Played*, *Most Purchased*).
- **Active Playlist**: Queue manager allowing players to create automated song loops.
- **Visual Customizer**: Real-time toggles for particle modes, disco floor light patterns, disco ball heads, and BossBar colors/styles.
- **Discs Cabinet**: Instant jukebox selector for vanilla Minecraft discs.

---

## ⌨️ Commands Reference

The main command is `/custommusic` (Aliases: `/cmusic`, `/cm`, `/musicbox`).

| Command | Permission | Description |
|---|---|---|
| `/cm` | `custommusic.use` | Opens the Studio GUI of your active companion box. |
| `/cm give [player] [lobby]` | `custommusic.give` | Grants a deployable Music Box (or auto-play Lobby Box). |
| `/cm scan` | `custommusic.admin` | Scans the `songs/` folder and registers new `.nbs` tracks. |
| `/cm add <id> <price> <name>` | `custommusic.admin` | Registers an audio track with a custom price and name. |
| `/cm delete <id>` | `custommusic.admin` | Removes an audio track from the registry. |
| `/cm stopall` | `custommusic.admin` | Immediately silences and halts all music boxes server-wide. |
| `/cm reload` | `custommusic.admin` | Reloads all configuration files and song statistics. |
| `/cm help [page]` | `custommusic.use` | Displays formatted command help. |

---

## 🔒 Permissions

- `custommusic.use` *(Default: Everyone)* - Open and interact with music boxes.
- `custommusic.place` *(Default: Everyone)* - Deploy and dismantle music boxes.
- `custommusic.portable` *(Default: Everyone)* - Pick up and carry companion music boxes.
- `custommusic.admin` *(Default: OP)* - Full administrative command access.
- `custommusic.give` *(Default: OP)* - Permission to give music boxes to yourself.
- `custommusic.give.others` *(Default: OP)* - Permission to give music boxes to others.
- `custommusic.modify.others` *(Default: OP)* - Modify unlocked boxes placed by other players.
- `custommusic.break.others` *(Default: OP)* - Break unlocked boxes placed by other players.
- `custommusic.limit.<number>` *(Default: 1)* - Maximum concurrent deployed music boxes per player.
- `custommusic.bypass.limit` *(Default: OP)* - Bypasses placement limits.

---

## 🔌 PlaceholderAPI Expansions

CustomMusic provides native PlaceholderAPI integration:

- `%custommusic_current_song%` - The title of the song currently playing in your companion box.
- `%custommusic_is_playing%` - `true` or `false` based on current playback status.
- `%custommusic_volume%` - Current volume percentage of your companion box.
- `%custommusic_radio_song%` - The title of the track currently broadcasting on the server radio.

---

## 🚀 Installation & Setup

1. Download `CustomMusic.jar` from the [Releases](https://github.com/lino9999/CustomMusic/releases) tab.
2. Place the `.jar` inside your server's `plugins/` folder.
3. *(Optional)* Install **Vault** and **PlaceholderAPI** for economy and scoreboard support.
4. Start your server. CustomMusic will automatically extract 24 popular royalty-free Note Block songs into `plugins/CustomMusic/songs/`.
5. Drop any additional `.nbs` files into `plugins/CustomMusic/songs/` and execute `/cm scan` in-game.

---

## 💻 Developer API

Add CustomMusic to your plugin project to trigger songs and inspect music boxes programmatically:

```java
import com.lino.custommusic.api.CustomMusicAPI;
import com.lino.custommusic.model.MusicBox;

// Play a registered track at a world location
CustomMusicAPI.playSongAt(player.getLocation(), "faded", 80);

// Inspect a player's companion box
Optional<MusicBox> companion = CustomMusicAPI.getCompanionBox(player);
companion.ifPresent(box -> {
    box.setVolume(100);
});
```

---

## 🏷️ Tags & SEO Keywords

`minecraft-plugin` `spigot-plugin` `paper-plugin` `purpur` `folia` `custom-music` `note-block-studio` `nbs-player` `minecraft-music` `jukebox` `boombox` `minecraft-radio` `portable-music` `spigotmc` `vault-economy` `minecraft-1.21` `minecraft-26`

---

## 📄 License & Credits

- **Author**: Lino
- **License**: [MIT License](LICENSE)
- Open source and free for the entire Minecraft community.
