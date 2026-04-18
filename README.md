# 🏹 Advanced Manhunt

[![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-gold.svg)](https://papermc.io/)
[![Version](https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

**The definitive automated engine for competitive Minecraft speedrun chases.**

Advanced Manhunt removes the "honor system" and manual setup, replacing it with a high-performance framework that handles roles, dimensional tracking, and game flow for you.

---

## 🛰️ Smart Pulse Tracking
Never lose your target to dimension hopping or manual compass resets.

* **Target Logic:** Compass automatically pings the nearest Runner.
* **Team Swapping:** Shift + Right-click to cycle through different Runners.
* **Dimension-Link:** If a Runner enters the Nether or End, the tracker automatically locks onto their **last used portal** until you follow them.

---

## ⚡ Game Engine & Fair Play
Built for competitive balance to ensure the hunt is fair for both sides.

### 🛡️ Anti-Throw Mechanics
* **Dragon Protection:** Hunters are blocked from damaging the Ender Dragon.
* **Auto-Win:** If a Hunter accidentally (or intentionally) steals the final kill, the Runners are instantly declared winners.

### ⏳ Cinematic Start System
* **Tactical Formation:** Runners are placed in the center, surrounded by Hunters in a 3-block circle.
* **Frozen Start:** Hunters are frozen for a configurable delay (5s, 10s, or "Wait for Move") while Runners get a head start.
* **Countdown:** High-quality on-screen titles handle the **3... 2... 1... GO!** sequence.

---

## 📊 Analytics & Recovery
* **Precision Stats:** View **TTKD** (Time To Kill Dragon) and **Survival Time** down to the centisecond (HH:mm:ss:CS).
* **Auto-Spectator:** Eliminated Runners are instantly moved to Spectator mode to watch the finale.
* **Reconnection:** Server crashes or disconnects won't break the game; player roles and timers are restored immediately upon rejoin.

---

## ⌨️ Commands & Configuration
**Main Command:** `/manhunt` or `/mh` (Requires OP)

| Command | Description |
|---|---|
| **/mh setup** | Open the visual GUI to toggle rules and cooldowns. |
| **/mh start** | Initiates teleport, formation, and countdown. |
| **/mh restart** | Full game reset with a short delay. |
| **/mh addhunter / addrunner** | Assign players to their respective teams. |
| **/msgh / /msgr** | Private team-only chat channels. |

---

## 🔧 Technical Information
* **Server:** Spigot / Paper 1.18 - 1.21+
* **Developer:** [Shenigennz (Swartzz)](https://github.com/Schwzz)
* **More:** [Modrinth](https://modrinth.com/plugin/advanced-manhunt)
