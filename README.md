# serato-sync

A Java desktop application that automatically synchronizes your music folder structure with **Serato DJ Pro 4** crates and subcrates, no drag and drop required.

---

## Inspiration

This project was inspired by serato-itch-sync by **Roman Alekseenkov**, a pioneering open-source tool that first explored the idea of mapping directory structures directly to Serato crates using Java.

Although serato-sync was built entirely from scratch with its own codebase and is not a fork or derivative of that project, Roman's work was the original spark. His initiative proved the concept and laid the groundwork for what this project aims to continue for modern versions of Serato DJ Pro. Full credit and gratitude go to him for that contribution to the DJ community.

---

## What it does

Serato DJ Pro organizes music into **crates** and **subcrates**, but populating them manually is tedious when you already have a well-organized folder structure on disk.

serato-sync reads your folder hierarchy and automatically generates the corresponding `.crate` files in the correct Serato format including nested subcrates at any depth so your library in Serato mirrors your folders exactly.

**Example:** a folder structure like this:

```
Music/
└── Electronic/
    ├── House/
    │   ├── Deep House/
    │   └── House 90's/
    └── Techno/
        └── Industrial/
```

Produces these crates in Serato:

```
Electronic
Electronic > House
Electronic > House > Deep House
Electronic > House > House 90's
Electronic > Techno
Electronic > Techno > Industrial
```

---

## Features

- Recursive folder scanning with automatic subcrate hierarchy
- Supports multiple root folders simultaneously
- Persistent configuration added folders are remembered between sessions
- Auto-detection of the Serato library path (`_Serato_`) on Windows and macOS
- Manual override of the Serato library path via Browse dialog
- Dark mode toggle
- Supports the following audio formats: `mp3`, `wl.mp3`, `flac`, `aif`, `aiff`, `wav`, `ogg`, `m4a`, `aac`, `wma`, `alac`
- Supports the following video formats: `mp4`, `mov`, `avi`
- Single `.jar` file no installation required
- Runs on Windows and macOS

---

## Requirements

### To run the application

| Requirement      | Details                                                 |
|------------------|---------------------------------------------------------|
| Java Runtime     | **Oracle JRE 8** (1.8.0+) or **Zulu JDK 8 with JavaFX** |
| Operating System | Windows 10/11 or macOS                                  |
| Serato DJ Pro    | Version 4.0 or later                                    |

> **Important:** Standard OpenJDK 8 distributions (including plain Zulu JDK) do **not** include JavaFX and will not run this application. You need one of the following:
>
> - **Oracle JRE 8** - [download here](https://www.java.com/en/download/)
> - **Zulu JDK 8 with JavaFX (Zulu FX)** - [download here](https://www.azul.com/downloads/?version=java-8-lts&package=jdk-fx)

### To build from source

| Requirement   | Details                             |
|---------------|-------------------------------------|
| JDK           | Zulu JDK 8 FX or Oracle JDK 8       |
| Maven         | 3.6+                                |
| IntelliJ IDEA | Community or Ultimate (recommended) |

> In IntelliJ, make sure both the **Project SDK** and **Maven Runner JRE** (`Settings → Build Tools → Maven → Runner → JRE`) point to the Java 8 installation with JavaFX.

---

## Running the application

### Windows

Double-click `serato-sync.jar` directly in File Explorer. Java automatically associates `.jar` files with `javaw.exe` during installation.

If double-click does not work, run via terminal:

```bash
java -jar serato-sync.jar
```

### macOS

On **macOS Catalina (10.15) or earlier** with Oracle JDK 8, double-clicking the JAR may work. On **macOS Big Sur (11) and later**, Gatekeeper typically blocks unsigned JARs from running via double-click.

The most reliable way to launch on any macOS version is to create a `.command` launcher file. Create a file named `serato-sync.command` in the same folder as the JAR with the following content:

```bash
#!/bin/bash
cd "$(dirname "$0")"
java -jar serato-sync.jar
```

Then open Terminal and make it executable (only needed once):

```bash
chmod +x /path/to/serato-sync.command
```

After that, double-clicking `serato-sync.command` in Finder will launch the app on any macOS version.

> On first launch, macOS may show a security warning. If that happens, go to **System Settings → Privacy & Security** and click **Open Anyway**.

---

## Building from source

```bash
git clone https://github.com/wendellemosmoura/serato-sync.git
cd serato-sync
mvn clean package
```

The output JAR will be at `target/serato-sync.jar`.

---

## Understanding how Serato manages libraries

This is the most important concept to understand before using serato-sync.

**Serato DJ Pro creates a separate `_Serato_` library for each drive where music is loaded.** When you load a track from drive `D:` for the first time, Serato automatically creates `D:\_Serato_\`. If you later load a track from `E:`, Serato creates `E:\_Serato_\` as a separate library.

This means **the Serato library path and the music folders must always be on the same drive.** If your music is on `D:`, the correct library path is `D:\_Serato_` not `C:\Users\YourName\Music\_Serato_`.

Using a library path from a different drive will cause tracks to appear in the crate but **fail to load** when you try to play them in Serato.

### Before running serato-sync

The `_Serato_` folder for a given drive is only created by Serato after at least one track from that drive has been loaded. **If you have never opened a track from that drive in Serato, the library folder will not exist yet** and serato-sync will have nothing to point to.

Before using serato-sync, make sure you have:

1. Opened Serato DJ Pro at least once
2. Loaded at least one track from each drive you intend to sync

After that, Serato will have created the corresponding `_Serato_` folder and serato-sync can write crates into it.

### Per-drive setup example (Windows)

| Music location             | Correct Serato library path                         |
|----------------------------|-----------------------------------------------------|
| `C:\Users\YourName\Music\` | `C:\Users\YourName\Music\_Serato_` or `C:\_Serato_` |
| `D:\Music\`                | `D:\_Serato_`                                       |
| `E:\DJ Library\`           | `E:\_Serato_`                                       |

Each drive requires a separate sync operation pointing to its own library.

> **macOS note:** On macOS, Serato typically creates a single library at `~/Music/_Serato_` for the system drive. External drives follow the same per-drive rule as Windows, creating their library at the root of the mounted volume (`/Volumes/DriveName/_Serato_`). macOS behavior has not been fully tested, contributions and reports are welcome.

---

## How to use

1. Launch the application with `java -jar serato-sync.jar`
2. Click **Browse** or type a folder path and click **Add** to add a music folder
3. Repeat for as many folders as you want to sync
4. Confirm the detected Serato library path is correct (or browse to set it manually)
5. Click **Sync library**
6. Open Serato DJ Pro, your crates will be ready

> **Note:** serato-sync writes the crate structure and track paths. Metadata such as BPM, key, artist, and duration is read by Serato directly from your audio files. If tags appear empty after syncing, select all tracks in the crate inside Serato and choose **Analyze Files** to populate them.

---

## Dark mode

Click the **Dark** button in the top-right corner to toggle dark mode. Click **Light** to switch back.

---

## License

MIT License see [LICENSE](LICENSE) for details.