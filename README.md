# Planetarium Player

Плеер с плейлистами для показа 360° и fulldome-видео на куполе планетария.

The application consists of two cooperating parts:

- `C:\opt\player` — Java Swing control UI and embedded JCEF/Chromium;
- `E:\www\player` — Three.js fulldome/360 web renderer served by nginx.

## Build and run

Requirements:

- Windows and Java 19 or newer;
- JCEF Java archives in `bin`;
- the matching native JCEF/Chromium runtime in `bin/lib/win64`;
- `ffmpeg` and `ffprobe` available through `PATH` or configured in
  `player.properties`;
- the web renderer served locally at the URL from `player.url`.

The native JCEF runtime is intentionally not stored in Git: Chromium contains
large platform-specific binaries, including files above GitHub's 100 MB limit.
Copy a compatible runtime into `bin/lib/win64` before launching the player.

Run commands from any working directory:

```bat
C:\opt\player\cdnd.bat
C:\opt\player\player.bat
```

To open a specific playlist:

```bat
C:\opt\player\player.bat test.js
```

An absolute HTTP(S) URL may be passed instead of a playlist name.

## Configuration

Runtime settings are in `player.properties`:

- `media.root` — nginx document/media root;
- `playlist.root` — folder displayed by the playlist browser;
- `player.url` — default renderer URL;
- `chromium.remoteDebuggingPort` — Chromium debugging port;
- `dome.screenIndex` — explicit dome display index, or `-1` for automatic selection;
- `dome.minimumWidth` — minimum width used for automatic dome display selection.

Use `-Dplayer.config=C:\path\custom.properties` to load another file.

## Playlist format

UTF-8 text, one video per line:

```text
begin,end,rotation,pitch,flags,/path/to/video.webm
```

The legacy five-field form without `flags` remains supported. Blank lines and
lines beginning with `#` are ignored. An inline `# comment` is also supported.
Known video duration is stored without changing the playback fields:

```text
0,0,75.3,60.45,m,/avideo/movie.webm # duration=11.25
```

The second field remains the playback end (`0` means play to the media end).
The editor obtains missing duration values with `ffprobe` in the background
when a file is dropped or a playlist is opened for editing.

Flags:

- `f` — fulldome;
- `o` — 360°;
- `m` — muted;
- `s` — start immediately;
- `p` — pause marker;
- `h<number>` — vertical offset;
- `n<number>` — near/far offset.

## Browser protocol

Browser to Java uses JCEF queries:

```text
cmd:onload;
cmd:videoinfo;<duration>
cmd:currenttime;<seconds>
cmd:videoend;
cmd:error;<message>
```

Java to browser calls the public renderer functions (`videoNext`, `play`,
`pause`, `stop`, seeking and projection controls). Arguments are JavaScript
escaped before being sent.

## Java source structure

- `PlanetariumPlayer` — application entry point and main Swing composition;
- `DomeBrowserWindow` — JCEF window placed on the dome display;
- `BrowserQueryHandler` / `BrowserCommand` — browser-to-Java protocol;
- `PlaybackControlPanel` — transport controls and time-range editing;
- `TimeRangeSlider` / `TimeRangeSliderUI` — two-thumb interval component;
- `PlaylistItem` / `PlaylistCodec` — playlist domain model and persistence;
- `MediaDurationProbe` — background ffprobe integration;
- `PreviewStoryboardCache` / `PlaylistPreviewPanel` — cached interactive video previews;
- `FileDragTree` — drag-capable file browser;
- `DarkSwingTheme` — high-contrast dark operator interface;
- `PlayerConfig` — runtime configuration.

## Verification

```bat
C:\opt\player\cdnd.bat
java -cp C:\opt\player\bin\class dnd.PlaylistCodecTest
node --check E:\www\player\libs\FullDomeRenderer.js
```

Or run all checks with `C:\opt\player\verify.ps1`.

`FullDomeRenderer.js` is the canonical renderer. The legacy
`FullDomeRendererPlayer.js` filename is kept as an identical compatibility
copy; `verify.ps1` prevents the two files from silently diverging.

`debug.log` is rotated to `debug.previous.log` after it grows beyond 10 MB.
