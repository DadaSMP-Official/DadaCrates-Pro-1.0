# DadaCratesPro HostMR

**DadaCratesPro HostMR** is a Minecraft crates plugin for Paper/Spigot servers.

This version is based on the original DadaSMP `DadaCratesPro` plugin and includes the HostMR patch:

- clean default crates without resource-pack symbols
- readable crate IDs: `comune`, `rara`, `epica`, `leggendaria`, `mitica`, `voto`, `oscura`
- default crate blocks set to normal `CHEST`
- original `/crate` and `/crates` admin GUI preserved
- improved Keys GUI from the main menu
- key list GUI with all available crate keys
- key editor GUI with normal key, 16x key, uses editor and pick mode keys
- configurable rewards, chances, commands, lore and broadcasts
- crate preview, opening animations, sounds, holograms and idle particles
- per-crate permissions

## Version

`1.0.1-HOSTMR`

## Commands

- `/dadacrates`
- `/dcrates`
- `/crate`
- `/crates`
- `/lootcrate`
- `/loot`

## Permissions

- `dadacrates.admin` - allows editing crates, keys, rewards and settings
- `dadacrates.use` - allows using crates
- `dadacrates.open.*` - allows opening all configured crates
- `dadacrates.open.<crate>` - allows opening a specific crate

## Download

The compiled plugin jar is in:

`release/DadaCratesPro-1.0.1-HOSTMR.jar`

Copy the jar into your server `plugins` folder and restart the server.

## Tested

- Paper 1.21.11
- Java 21

## Notes

This repository is the HostMR/GitHub version. It does not modify the live DadaSMP plugin.
