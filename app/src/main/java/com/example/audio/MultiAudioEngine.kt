package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.data.LocalSoundCatalog
import com.example.data.sound.SoundRepository
import com.example.model.SleepTimerState
import com.example.model.SoundTrackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MultiAudioEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundRepository = SoundRepository.getInstance(context)

    // Map of soundId -> active MediaPlayer
    private val mediaPlayers = mutableMapOf<String, MediaPlayer>()

    // Track states
    private val _tracksState = MutableStateFlow<Map<String, SoundTrackState>>(emptyMap())
    val tracksState: StateFlow<Map<String, SoundTrackState>> = _tracksState.asStateFlow()

    // Master volume (0.0 to 1.0)
    private val _masterVolume = MutableStateFlow(0.85f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    // Master play/pause state
    private val _isMasterPlaying = MutableStateFlow(false)
    val isMasterPlaying: StateFlow<Boolean> = _isMasterPlaying.asStateFlow()

    // Sleep Timer
    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    private var timerJob: Job? = null
    private var fadeOutMultiplier = 1.0f

    // Memory of which sounds were active so resuming after stop restores them
    private var lastActiveSoundIds: Set<String> = setOf("rain")

    // Stable Audio Focus Management
    private var hasAudioFocus = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                pauseAll()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pauseAll()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                fadeOutMultiplier = 0.3f
                applyVolumeToAllPlayers()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                fadeOutMultiplier = 1.0f
                applyVolumeToAllPlayers()
                if (_isMasterPlaying.value) {
                    resumeActiveSounds()
                }
            }
        }
    }

    private val audioFocusRequest: AudioFocusRequest? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
        } else {
            null
        }
    }

    // Callback when playback state changes (used by Foreground Service for notification and wakelock)
    var onPlaybackStateChanged: ((isPlaying: Boolean, activeCount: Int) -> Unit)? = null

    init {
        // Initialize with built-in sounds immediately
        val initialMap = LocalSoundCatalog.builtInSounds.associate { sound ->
            sound.id to SoundTrackState(
                definition = sound,
                isPlaying = false,
                volume = sound.defaultVolume,
                isMuted = false
            )
        }
        _tracksState.value = initialMap

        // Dynamically observe all sounds (built-in + user added)
        scope.launch {
            soundRepository.allSoundsFlow.collectLatest { soundDefs ->
                _tracksState.update { current ->
                    soundDefs.associate { sound ->
                        val existing = current[sound.id]
                        sound.id to (existing?.copy(definition = sound) ?: SoundTrackState(
                            definition = sound,
                            isPlaying = false,
                            volume = sound.defaultVolume,
                            isMuted = false
                        ))
                    }
                }

                // Stop any players for removed sounds
                val currentIds = soundDefs.map { it.id }.toSet()
                val removed = mediaPlayers.keys.filter { !currentIds.contains(it) }
                removed.forEach { stopSoundPlayer(it) }
            }
        }
    }

    fun toggleSoundPlay(soundId: String) {
        val currentTrack = _tracksState.value[soundId] ?: return
        val newPlaying = !currentTrack.isPlaying

        _tracksState.update { current ->
            current + (soundId to currentTrack.copy(isPlaying = newPlaying))
        }

        if (newPlaying) {
            _isMasterPlaying.value = true
            startSoundPlayer(soundId)
            saveActiveSoundsMemory()
        } else {
            stopSoundPlayer(soundId)
            checkAndUpdateMasterPlayingState()
        }
        notifyStateChanged()
    }

    fun setSoundVolume(soundId: String, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val currentTrack = _tracksState.value[soundId] ?: return

        _tracksState.update { current ->
            current + (soundId to currentTrack.copy(volume = clamped))
        }
        updatePlayerVolume(soundId)
    }

    fun toggleSoundMute(soundId: String) {
        val currentTrack = _tracksState.value[soundId] ?: return
        val newMuted = !currentTrack.isMuted

        _tracksState.update { current ->
            current + (soundId to currentTrack.copy(isMuted = newMuted))
        }
        updatePlayerVolume(soundId)
    }

    fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _masterVolume.value = clamped
        applyVolumeToAllPlayers()
    }

    fun toggleMasterPlayPause() {
        if (_isMasterPlaying.value) {
            pauseAll()
        } else {
            resumeActiveSounds()
        }
    }

    fun pauseAll() {
        _isMasterPlaying.value = false
        mediaPlayers.values.forEach { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error pausing player: ${e.message}")
            }
        }
        notifyStateChanged()
    }

    fun resumeActiveSounds() {
        val activeSounds = _tracksState.value.values.filter { it.isPlaying }

        if (activeSounds.isEmpty()) {
            val toRestore = if (lastActiveSoundIds.isNotEmpty()) lastActiveSoundIds else setOf("rain")
            _tracksState.update { current ->
                current.mapValues { (id, state) ->
                    state.copy(isPlaying = toRestore.contains(id))
                }
            }
        }

        requestAudioFocus()
        _isMasterPlaying.value = true
        saveActiveSoundsMemory()

        val currentPlayingTracks = _tracksState.value.values.filter { it.isPlaying }
        for (track in currentPlayingTracks) {
            val soundId = track.definition.id
            val existingPlayer = mediaPlayers[soundId]

            if (existingPlayer != null) {
                try {
                    updatePlayerVolume(soundId)
                    if (!existingPlayer.isPlaying) {
                        existingPlayer.start()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Existing player for $soundId unusable, recreating: ${e.message}")
                    try {
                        existingPlayer.reset()
                        existingPlayer.release()
                    } catch (_: Exception) {}
                    mediaPlayers.remove(soundId)
                    startSoundPlayer(soundId)
                }
            } else {
                startSoundPlayer(soundId)
            }
        }

        notifyStateChanged()
    }

    fun stopAll() {
        saveActiveSoundsMemory()
        _isMasterPlaying.value = false
        stopSleepTimer()

        mediaPlayers.forEach { (_, player) ->
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing player in stopAll: ${e.message}")
            }
        }
        mediaPlayers.clear()

        _tracksState.update { current ->
            current.mapValues { (_, state) -> state.copy(isPlaying = false) }
        }

        abandonAudioFocus()
        notifyStateChanged()
    }

    fun applyPreset(volumes: Map<String, Float>) {
        _tracksState.update { current ->
            current.mapValues { (id, state) ->
                val targetVol = volumes[id]
                if (targetVol != null && targetVol > 0.01f) {
                    state.copy(isPlaying = true, volume = targetVol, isMuted = false)
                } else {
                    state.copy(isPlaying = false)
                }
            }
        }

        val activeIds = volumes.filter { it.value > 0.01f }.keys
        val toRemove = mediaPlayers.keys.filter { !activeIds.contains(it) }
        toRemove.forEach { stopSoundPlayer(it) }

        _isMasterPlaying.value = true
        saveActiveSoundsMemory()
        requestAudioFocus()

        activeIds.forEach { soundId ->
            startSoundPlayer(soundId)
        }

        applyVolumeToAllPlayers()
        notifyStateChanged()
    }

    private fun startSoundPlayer(soundId: String) {
        val track = _tracksState.value[soundId] ?: return

        val existing = mediaPlayers[soundId]
        if (existing != null) {
            try {
                updatePlayerVolume(soundId)
                if (_isMasterPlaying.value && !existing.isPlaying) {
                    existing.start()
                }
                return
            } catch (e: Exception) {
                try {
                    existing.reset()
                    existing.release()
                } catch (_: Exception) {}
                mediaPlayers.remove(soundId)
            }
        }

        requestAudioFocus()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                isLooping = true

                if (track.definition.rawResId != null) {
                    val afd = context.resources.openRawResourceFd(track.definition.rawResId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else if (!track.definition.customFilePath.isNullOrBlank()) {
                    setDataSource(track.definition.customFilePath)
                } else {
                    Log.w(TAG, "No audio source found for sound $soundId")
                    return
                }

                setOnPreparedListener { mp ->
                    try {
                        val current = _tracksState.value[soundId]
                        if (current?.isPlaying == true && _isMasterPlaying.value) {
                            updatePlayerVolume(soundId)
                            mp.start()
                        } else {
                            mp.pause()
                        }
                        notifyStateChanged()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed onPrepared for $soundId: ${e.message}")
                    }
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error for $soundId: what=$what, extra=$extra")
                    try {
                        mp.reset()
                        mp.release()
                    } catch (_: Exception) {}
                    mediaPlayers.remove(soundId)
                    true
                }

                prepareAsync()
            }
            mediaPlayers[soundId] = player
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing player for $soundId: ${e.message}", e)
        }
    }

    private fun stopSoundPlayer(soundId: String) {
        val player = mediaPlayers.remove(soundId) ?: return
        try {
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing player for $soundId: ${e.message}")
        }
    }

    private fun updatePlayerVolume(soundId: String) {
        val player = mediaPlayers[soundId] ?: return
        val track = _tracksState.value[soundId] ?: return

        val effective = if (track.isMuted || !track.isPlaying || !_isMasterPlaying.value) {
            0f
        } else {
            track.volume * _masterVolume.value * fadeOutMultiplier
        }.coerceIn(0f, 1f)

        try {
            player.setVolume(effective, effective)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting volume for $soundId: ${e.message}")
        }
    }

    private fun applyVolumeToAllPlayers() {
        mediaPlayers.keys.forEach { updatePlayerVolume(it) }
    }

    private fun checkAndUpdateMasterPlayingState() {
        val anyActive = _tracksState.value.values.any { it.isPlaying }
        if (!anyActive) {
            _isMasterPlaying.value = false
        }
    }

    private fun saveActiveSoundsMemory() {
        val active = _tracksState.value.values.filter { it.isPlaying }.map { it.definition.id }.toSet()
        if (active.isNotEmpty()) {
            lastActiveSoundIds = active
        }
    }

    // --- Sleep Timer with 30s Smooth Fade-Out ---

    fun startSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            stopSleepTimer()
            return
        }

        timerJob?.cancel()
        val totalSeconds = minutes * 60L
        fadeOutMultiplier = 1.0f

        _sleepTimerState.value = SleepTimerState(
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = true,
            isFadingOut = false
        )

        timerJob = scope.launch {
            while (isActive && _sleepTimerState.value.remainingSeconds > 0) {
                delay(1000L)
                val remaining = _sleepTimerState.value.remainingSeconds - 1

                val isFading = remaining in 1..30
                if (isFading) {
                    fadeOutMultiplier = (remaining / 30f).coerceIn(0f, 1f)
                    applyVolumeToAllPlayers()
                } else if (remaining > 30) {
                    fadeOutMultiplier = 1.0f
                }

                _sleepTimerState.value = _sleepTimerState.value.copy(
                    remainingSeconds = remaining,
                    isFadingOut = isFading
                )

                if (remaining <= 0) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    fun stopSleepTimer() {
        timerJob?.cancel()
        timerJob = null
        fadeOutMultiplier = 1.0f
        applyVolumeToAllPlayers()
        _sleepTimerState.value = SleepTimerState()
    }

    private fun onTimerFinished() {
        fadeOutMultiplier = 0f
        applyVolumeToAllPlayers()
        stopAll()
        fadeOutMultiplier = 1.0f
        _sleepTimerState.value = SleepTimerState()
    }

    // --- Audio Focus ---

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) } ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
        hasAudioFocus = false
    }

    private fun notifyStateChanged() {
        val playingCount = _tracksState.value.values.count { it.isPlaying }
        onPlaybackStateChanged?.invoke(_isMasterPlaying.value && playingCount > 0, playingCount)
    }

    fun release() {
        stopSleepTimer()
        abandonAudioFocus()
        mediaPlayers.values.forEach {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) {}
        }
        mediaPlayers.clear()
    }

    companion object {
        private const val TAG = "MultiAudioEngine"

        @Volatile
        private var instance: MultiAudioEngine? = null

        fun getInstance(context: Context): MultiAudioEngine {
            return instance ?: synchronized(this) {
                instance ?: MultiAudioEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
