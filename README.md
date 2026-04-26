# Slimefun 4 — Experimental Patch for Minecraft 26.1.2

> **⚠️ UNOFFICIAL BUILD** — This is a one-person, vibe-coded patch / update of [Slimefun 4](https://github.com/Slimefun/Slimefun4) to get it running on Minecraft **Paper 26.1.2** (not a typo). Spigot is untested.

---

## What is this?

This fork takes the original Slimefun 4 plugin and patches it to work on the experimental **Paper 26.1.2** or **Purpur 26.1.2** version of Minecraft. It is **not** an official release — just a solo effort to see if slimefun would work on these server versions earlier than the official release.

Some compatibility layers have been implemented (to varying degrees of completeness), so other Slimefun addons *may* work, but nothing is guaranteed beyond what's been personally tested.

---

## ✅ Tested & Working Addons

The following addons have been tested and confirmed working with this build:

| Addon | Version |
| --- | --- |
| **EcoPower** | dev 5 |
| **ElectricSpawners** | dev 23 |
| **ExtraGear** | dev 13 |
| **ExtraTools** | dev 36 |
| **HotbarPets** | dev 38 |
| **InfinityExpansion** | dev 144 |
| **LuckyBlocks-SF** | dev 36 |
| **SlimeHUD** | latest |
| **Supreme** | latest |
| **UltimateGenerators2** | latest |

> **Note:** Other Slimefun addons or different versions of the above addons are **unknown / untested**. They might work, they might not.

---

## 🔧 Requirements

- **Minecraft Server:** [Paper](https://papermc.io/) **26.1.2** (experimental) or [Purpur](https://purpurmc.org/) **26.1.2** (experimental)
- **Spigot:** Untested — use at your own risk
- **Java:** 25 or higher

---

## ⚡ Quick Start

1. Download the latest build from this repository.
2. Drop the `.jar` into your Paper 26.1.2 server's `plugins/` folder.
3. Start (or restart) the server.
4. Optionally install any of the tested addons listed above.

---

## ❓ FAQ

**Q: Will this work on Spigot?**
A: Untested. It was developed and tested against Paper 26.1.2 only.

**Q: Will addon X work?**
A: If it's in the tested addons table above, yes. Otherwise, it's unknown. Some compatibility layers have been partially implemented, so there's a chance — but no promises.

**Q: Is this an official Slimefun release?**
A: No. This is an unofficial, solo-maintained one-time patch. For the official project, see [Slimefun/Slimefun4](https://github.com/Slimefun/Slimefun4).

**Q: Can I report bugs?**
A: Sure — open an issue on this repository. Just keep in mind this is a one-person project, so response times may vary.

---

## 📜 Original Project

This is a fork of [Slimefun 4](https://github.com/Slimefun/Slimefun4), which is licensed under [GNU GPLv3](https://github.com/Slimefun/Slimefun4/blob/master/LICENSE). All original credits go to TheBusyBiscuit and the hundreds of contributors to the original project.

For documentation on Slimefun itself (items, recipes, mechanics), see the [Slimefun Wiki](https://github.com/Slimefun/Slimefun4/wiki).

---

## ⚠️ Disclaimers

- This is an **unofficial**, **experimental** build. Things may break.
- Not affiliated with or endorsed by the original Slimefun team, Mojang Studios, or Minecraft.
- Use at your own risk. Back up your world and data before installing.
- Auto-updater and metrics from the original Slimefun have **not** been modified — you may want to disable them in `/plugins/Slimefun/config.yml` since they point to the upstream project.
