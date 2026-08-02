# OuterTune Architecture & Codebase Structure

This document provides a comprehensive overview of the OuterTune project module hierarchy, source package structure, and UI design organization for developer reference and AI assistant context.

---

## 1. Project Module Overview

OuterTune is structured as an Android project with modular Kotlinized libraries:

```
OuterTune/
├── app/                        # Main Android application module (Jetpack Compose UI, Playback, DB)
├── betterlyrics/               # TTML XML parser & lyrics models
├── paxsenix/                   # Apple Music / Paxsenix lyrics fetcher module (TTML & API stats)
├── innertube/                  # YouTube Music API client engine
├── kugou/                      # KuGou lyrics provider module
├── lrclib/                     # LrcLib lyrics provider module
├── simpmusic/                  # SimpMusic lyrics provider module
├── material-color-utilities/   # Material You / HCT color scheme generator
├── taglib/                     # Audio tag reader bindings
├── ffMetadataEx/               # Media3 metadata extractor extensions
├── media/                      # Submodule build for AndroidX Media3 player components
└── screenshot/                 # UI screenshots and assets
```

---

## 2. Main Application Module (`:app`) Package Tree

Source directory: `app/src/main/java/`

```
com.dd3boh.outertune/
├── MainActivity.kt             # Main entry point activity & root NavHost setup
├── MainActivityUtils.kt        # Utility methods for main activity state
│
├── ui/                         # Jetpack Compose UI Layer
│   ├── theme/                  # Design Tokens & Theme Provider
│   │   └── Theme.kt            # OuterTuneTheme, ColorScheme, TonalSpot, Dynamic Colors
│   │
│   ├── component/              # Reusable UI Components
│   │   ├── button/             # Custom & standard action buttons
│   │   ├── items/              # SongItem, AlbumItem, ArtistItem, PlaylistItem, etc.
│   │   ├── shimmer/            # Loading placeholder animations
│   │   ├── AnchorDraggable.kt  # Swipeable & sheet drag handlers
│   │   ├── BottomSheet.kt      # Persistent player bottom sheet container
│   │   ├── ChipsRow.kt         # Filter chips & selection rows
│   │   ├── HideOnScrollFAB.kt  # Scroll-aware Floating Action Buttons
│   │   ├── Library.kt          # Generic library list layouts
│   │   ├── Lyrics.kt           # Synced & karaoke lyrics renderer
│   │   ├── NavigationTile.kt   # Navigation items for drawer/rail
│   │   ├── PlayerSlider.kt     # Track progress slider
│   │   ├── SearchBar.kt        # Search header & query input
│   │   └── Preference.kt       # Preference row entries & switches
│   │
│   ├── player/                 # Music Player Interfaces
│   │   ├── Player.kt           # Expanded full-screen player UI
│   │   ├── Queue.kt            # Now playing queue list with drag-to-reorder
│   │   └── QueueBoard.kt       # Queue state board manager
│   │
│   ├── screens/                # Main Navigation Screens
│   │   ├── HomeScreen.kt       # Home dashboard, quick picks, recent albums
│   │   ├── PlayerScreen.kt     # Mini player & expanded player container
│   │   ├── AlbumScreen.kt      # Detailed album view & song list
│   │   ├── BrowseScreen.kt     # Explore & YouTube Music categories
│   │   ├── HistoryScreen.kt    # Listening history screen
│   │   ├── SetupWizard.kt      # First-launch OOBE wizard
│   │   ├── artist/             # Artist details & discography screens
│   │   ├── library/            # Local & cloud library filter screens
│   │   ├── playlist/           # Playlist details & management screens
│   │   ├── search/             # Search result screens & filters
│   │   └── settings/           # App settings screens & sub-fragments
│   │       └── fragments/      # Settings fragments (LyricFrag, InterfaceFrag, etc.)
│   │
│   ├── dialog/                 # Modal Dialogs & Action Overlays
│   │   └── ActionPromptDialog.kt, CounterDialog.kt, etc.
│   │
│   ├── menu/                   # Context & Popup Menus
│   │   ├── LyricsMenu.kt       # Lyrics search, preview, and selection menu
│   │   └── SongMenu.kt, AlbumMenu.kt, etc.
│   │
│   └── utils/                  # UI Utilities & Helpers
│       ├── DataStore.kt        # Preference Store extensions
│       └── FadingEdge.kt       # Gradient fade modifier utilities
│
├── playback/                   # Audio Service & Playback Engine
│   ├── MusicService.kt         # MediaSessionService (Media3 Player)
│   ├── PlayerConnection.kt     # Inter-process service binding & player state flows
│   └── QueueBoard.kt           # Playback queue manager
│
├── lyrics/                     # Lyrics Provider Architecture
│   ├── LyricsProvider.kt       # Interface contract for lyric sources
│   ├── LyricsHelper.kt         # Orchestrator & priority-based lyric resolver
│   ├── PaxsenixLyricsProvider.kt # Apple Music / TTML provider
│   └── BetterLyricsProvider.kt, SimpMusicLyricsProvider.kt, etc.
│
├── db/                         # Local Data Storage
│   ├── MusicDatabase.kt        # Room Database declaration
│   ├── DatabaseDao.kt          # Room Data Access Objects
│   └── entities/               # Entity models (Song, Album, Artist, LyricsEntity)
│
└── viewmodels/                 # Jetpack ViewModels
    ├── LyricsMenuViewModel.kt  # View model for lyrics search
    ├── PaxsenixStatsViewModel.kt # View model for Paxsenix API status
    └── HomeViewModel.kt, PlayerViewModel.kt, etc.
```

---

## 3. UI System Key Dependencies

- **Compose Material 3**: `androidx.compose.material3:material3:1.4.0`
- **Compose Foundation**: `androidx.compose.foundation:foundation:1.10.2`
- **Compose Animation**: `androidx.compose.animation:animation-graphics:1.10.2`
- **Reorderable List**: `sh.calvin.reorderable:reorderable:3.0.0`
- **Coil 3 Image Loader**: `io.coil-kt.coil3:coil-compose:3.3.0`
