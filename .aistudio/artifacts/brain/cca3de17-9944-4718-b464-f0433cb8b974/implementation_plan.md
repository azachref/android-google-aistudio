# Somnia - Ambient White Noise & Sound Mixer

A battery-optimized, offline-first white noise and ambient soundscape player engineered for restful sleep and deep focus. Allows users to layer multiple audio loops simultaneously, tune individual volumes, save favorite custom mixes, and set an automatic sleep timer with gentle volume fade-out.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and user experience decisions were confirmed through Phase 1 clarification:

- **Audio Asset Caching**: Automatically download and cache all 8 soundscape tracks into local storage on first launch with visual download indicators, enabling 100% offline playback thereafter.
- **Sleep Timer Transition**: When the sleep timer approaches zero, the app applies a gentle, gradual volume fade-out across the final 30 seconds before stopping playback and stopping the foreground service.
- **Aesthetic Direction**: True pitch-black AMOLED dark theme (`#000000` / `#080B11`) with subtle neon glow accents (cyan `#00E5FF` and violet `#A855F7`) to eliminate eye strain in the dark and conserve OLED battery.

---

## 1. Overview & Core Concept

- **What It Does**: Provides an ambient sound mixer where users can play any combination of 8 natural and urban sounds (Rain, Thunder, Wind, Cafe, Lofi, White Noise, Traffic, Cars) at independent volume levels. Runs continuously in the background via a dedicated Foreground Service with media notification controls.
- **Target Audience**: Nighttime sleepers, light sleepers, meditators, students, and focused workers seeking customizable, non-distracting soundscapes.
- **Key Value**: Low-battery background streaming, gapless seamless looping, instant offline accessibility, and rapid preset switching without jarring interruptions or adverts.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **First-Launch Onboarding & Caching**:
   - The user opens the app. A clean, non-intrusive status bar shows soundscape caching progress with immediate playback available as soon as each track caches.
   - Default starter mix ("Night Rainfall" with gentle Rain + Wind) is immediately ready to play.

2. **Sound Mixer & Multi-Sound Layering**:
   - The home screen features an interactive grid/list of sound cards (Rain, Thunder, Wind, Cafe, Lofi, White Noise, Traffic, Cars).
   - Tapping an icon or play toggle enables the sound.
   - An intuitive volume slider adjusts the track's level (0% to 100%).
   - Mute button allows silencing a sound without losing its tuned volume level.
   - Master playback bar docked at the bottom provides Master Volume, Master Play/Pause, and active sound count badge.

3. **Sleep Timer with Smooth Fade-Out**:
   - Tapping the Sleep Timer icon opens a sheet with quick presets (15m, 30m, 45m, 1h, 2h) and custom minute picker.
   - Once active, an elegant circular ring / countdown ticker displays remaining time.
   - During the last 30 seconds, sound master volume eases smoothly to zero before halting background playback to avoid startling the sleeper.

4. **Favorites & Custom Presets**:
   - Users can tap "Save as Mix" to save their current active sound combinations and volume levels under a custom name (e.g. "Midnight Thunderstorm", "Study in Paris Cafe").
   - A dedicated Favorites bar / drawer lets users load or overwrite their saved mixes with a single tap.
   - Includes bundled default presets: *Deep Sleep Rain*, *Cafe Study*, *Cabin in the Storm*, and *Gentle Night*.

### Visual Identity & Theme

- **Color Palette (Pitch Black AMOLED)**:
  - `Background`: `#000000` (Pure AMOLED black)
  - `Surface`: `#0A0E17` (Deep midnight container)
  - `Surface Variant`: `#121826` with border outline `#1E293B`
  - `Primary Glow`: `#00E5FF` (Electric cyan for active playing indicators and progress)
  - `Secondary Accent`: `#A855F7` (Cosmic violet for sleep timer & presets)
  - `Text High Contrast`: `#F1F5F9`
  - `Text Muted`: `#64748B`
- **Typography & Atmosphere**:
  - Clean M3 typography with generous letter spacing for headings.
  - Ambient wave animation: subtle breathing pulse around active track cards without heavy GPU shaders to maximize battery life.

---

## 3. Key Product Decisions & Trade-Offs

- **Audio Engine**:
  - *Chosen Approach*: Multi-instance `MediaPlayer` pool managed inside an Android `ForegroundService`, configured with `isLooping = true` and `AudioAttributes.USAGE_MEDIA`.
  - *Why*: Native `MediaPlayer` looping is hardware-accelerated, highly battery-efficient, and provides gapless looping without the overhead of heavy video-centric media pipelines.
- **Battery Optimization**:
  - Foreground Service with `WAKE_LOCK` held only while actively playing and released immediately when paused or when the sleep timer expires.
  - AMOLED dark mode with minimal recompositions to keep display hardware power draw close to zero on OLED screens.
- **Offline Storage**:
  - Sound files are downloaded via OkHttp into internal app cache (`context.cacheDir/sounds/`) with fallback and local checksum validation.
  - Favorites and mix settings stored locally in Room / SQLite database for zero-cloud, instant offline recall.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────┐  │
│  │   MixerScreen   │  │  SleepTimerSheet │  │ PresetsRow │  │
│  └────────┬────────┘  └────────┬─────────┘  └─────┬──────┘  │
└───────────┼────────────────────┼──────────────────┼─────────┘
            ▼                    ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                      SoundMixerViewModel                    │
│   • activeSoundsState: StateFlow<List<SoundTrackState>>     │
│   • timerState: StateFlow<SleepTimerState>                  │
│   • presetsState: StateFlow<List<SoundPreset>>              │
└───────────────────────────┬─────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│     PresetRepository      │   │    AudioPlaybackManager   │
│   (Room Database / DAO)   │   │  (Foreground Service +    │
│  • Custom user presets    │   │   Multi-MediaPlayer Pool) │
│  • Bundled default mixes  │   │  • Gapless looping        │
└───────────────────────────┘   │  • Track volume balance   │
                                │  • 30s gentle fade-out    │
                                └─────────────┬─────────────┘
                                              ▼
                                ┌───────────────────────────┐
                                │   AudioDownloadManager    │
                                │   (OkHttp + Local Cache)  │
                                │   • Preload 8 audio files │
                                │   • Cache validation      │
                                └───────────────────────────┘
```

### Data Models & State Entities

- `SoundTrack`: ID, title, icon category, remote URL, local cached file, current volume (0.0f - 1.0f), isPlaying, isMuted, downloadProgress.
- `SoundPreset`: ID, name, icon, isBuiltIn, map of `soundId -> volumeLevel`.
- `SleepTimerState`: totalDurationMs, remainingMs, isRunning, isFadingOut.
- `PlaybackNotification`: Native media style notification with action buttons to Pause, Play, or Stop playback directly from the lock screen.
