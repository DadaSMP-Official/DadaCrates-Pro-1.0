# DadaCratesPro 1.0

> **Advanced crates, keys, rewards, previews and opening animations for Minecraft servers.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-green?style=for-the-badge)
![Paper](https://img.shields.io/badge/Paper%20%2F%20Spigot-Compatible-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0-purple?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge)

## Overview

**DadaCratesPro 1.0** is a complete crates plugin for Minecraft Paper/Spigot servers.

It gives server owners a full in-game crate system with admin GUI, crate blocks, keys, reward previews, animations, sounds, holograms, particles and permissions. The public version is ready to use without custom resource-pack symbols: all default crates use clean readable names and normal Minecraft items.

## Feature Panels

| System | What it does |
| --- | --- |
| **Crate GUI** | Manage crates directly in game using `/crate` or `/crates`. |
| **Key System** | Give normal keys, multi-use keys and pick-mode keys. |
| **Key List GUI** | View all crate keys from a dedicated menu. |
| **Reward Editor** | Configure rewards with item, amount, lore, commands, chance and broadcast. |
| **Preview Menu** | Players can preview possible rewards before opening a crate. |
| **Opening Animations** | Crates can use animated openings with sounds and final effects. |
| **Physical Crates** | Set crate blocks in the world and protect them from normal breaking. |
| **Holograms & Particles** | Show floating crate info and idle visual effects. |
| **Permissions** | Control access per crate with permission nodes. |

## Default Crates

The plugin includes clean default crates:

| Crate | ID | Style |
| --- | --- | --- |
| Comune | `comune` | Starter/common rewards |
| Rara | `rara` | Better survival rewards |
| Epica | `epica` | High-value rewards |
| Leggendaria | `leggendaria` | Rare premium rewards |
| Mitica | `mitica` | Very rare endgame rewards |
| Voto | `voto` | Voting rewards |
| Oscura | `oscura` | Dark/rare reward set |

All default crates use normal `CHEST` blocks and readable key names.

## Commands

| Command | Description |
| --- | --- |
| `/dadacrates` | Main plugin command. |
| `/dcrates` | Alias for the main command. |
| `/crate` | Opens the DadaCratesPro GUI. |
| `/crates` | Opens the DadaCratesPro GUI. |
| `/lootcrate` | Alias command. |
| `/loot` | Alias command. |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `dadacrates.admin` | `op` | Allows editing crates, keys, rewards and settings. |
| `dadacrates.use` | `op` | Allows using crate features. |
| `dadacrates.open.*` | `op` | Allows opening every configured crate. |
| `dadacrates.open.<crate>` | custom | Allows opening one specific crate. |

## Configuration

The plugin ships with a ready-to-use `config.yml`.

You can configure:

- crate names and IDs
- crate permissions
- key item material, name, lore and uses
- rewards and chances
- reward commands
- broadcasts
- GUI titles
- sounds
- animations
- hologram lines
- physical crate blocks

Example crate key:

```yml
key:
  material: "TRIPWIRE_HOOK"
  name: "&aKey Comune"
  lore:
    - "&7Apre la &aCrate Comune&7."
    - "&7Utilizzi: &f%uses%"
  default-uses: 1
```

## Installation

1. Download the jar from the `release` folder.
2. Put it inside your server `plugins` folder.
3. Restart the server.
4. Use `/crate` or `/crates` in game.
5. Edit `plugins/DadaCratesPro/config.yml` if you want to customize crates.

Compiled jar:

`release/DadaCratesPro-1.0.jar`

## Requirements

| Requirement | Status |
| --- | --- |
| Paper/Spigot | Recommended |
| Minecraft | 1.20.1+ |
| Java | 17+ |
| Resource pack | Not required |

## Tested

- Paper `1.21.11`
- Java `21`

## Support

Need help, want to report a bug, or want support for your setup?

Join the Discord server:

https://discord.gg/yXrDpKCGAs

## Version History

| Version | Notes |
| --- | --- |
| `1.0` | Public release with clean default crates, key GUI, previews, rewards and animations. |

## Notes

This is the public GitHub release of **DadaCratesPro 1.0**. It is packaged for distribution with source, default configuration and a ready-to-use compiled jar.
