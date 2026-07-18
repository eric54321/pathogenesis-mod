# Pathogenesis — Play With Friends

This folder always has the latest playable build of the mod. To join a Pathogenesis world, everyone needs the same setup:

## 1. Install requirements

- **Minecraft: Java Edition 1.21.1**
- **Java 21** (not 25) — [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21)
- **Fabric Loader 1.21.1** — install via the [Fabric installer](https://fabricmc.net/use/installer/), selecting Minecraft version `1.21.1` and loader version `0.16.5`

## 2. Copy the mod files

Copy both files from this `dist` folder into your `mods` folder:

- `pathogenesis-1.0.0.jar` (the mod itself)
- `fabric-api-0.116.12+1.21.1.jar` (required dependency)

Your `mods` folder is usually at:

```
%APPDATA%\.minecraft\mods
```

(or whatever custom Minecraft instance folder you use — create a `mods` folder there if it doesn't exist)

## 3. Launch

1. Open the Minecraft Launcher
2. Select the **Fabric 1.21.1** profile (the Fabric installer creates this automatically)
3. Click **Play**

## 4. Join a world

- **Same network (LAN):** the host opens their singleplayer world, presses `Esc` → **Open to LAN**, and shares the port shown. Everyone else uses **Multiplayer → Direct Connect** with the host's IP and port.
- **Over the internet:** the host needs a dedicated Fabric server running the same mod + Fabric API, with the port forwarded (or hosted through a service like Aternos).

## Updating

Whenever the mod updates, just replace `pathogenesis-1.0.0.jar` in your `mods` folder with the new one from this `dist` folder.
