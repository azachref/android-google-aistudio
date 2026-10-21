package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MultiAudioEngine
import com.example.data.preset.PresetRepository
import com.example.data.sound.SoundRepository
import com.example.model.SleepTimerState
import com.example.model.SoundPreset
import com.example.model.SoundTrackState
import com.example.service.SoundMixerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundMixerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = MultiAudioEngine.getInstance(application)
    private val soundRepository = SoundRepository.getInstance(application)
    private val presetRepository = PresetRepository.getInstance(application)

    // Tracks as ordered list
    val tracksList: StateFlow<List<SoundTrackState>> = audioEngine.tracksState
        .map { map -> map.values.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isMasterPlaying: StateFlow<Boolean> = audioEngine.isMasterPlaying
    val masterVolume: StateFlow<Float> = audioEngine.masterVolume
    val sleepTimerState: StateFlow<SleepTimerState> = audioEngine.sleepTimerState

    val presets: StateFlow<List<SoundPreset>> = presetRepository.allPresetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = tracksList.map { list ->
        list.count { it.isPlaying }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Dialog & Sheet states
    private val _showSavePresetDialog = MutableStateFlow(false)
    val showSavePresetDialog: StateFlow<Boolean> = _showSavePresetDialog.asStateFlow()

    private val _showTimerSheet = MutableStateFlow(false)
    val showTimerSheet: StateFlow<Boolean> = _showTimerSheet.asStateFlow()

    private val _showManageSoundsSheet = MutableStateFlow(false)
    val showManageSoundsSheet: StateFlow<Boolean> = _showManageSoundsSheet.asStateFlow()

    private val _showAddSoundDialog = MutableStateFlow(false)
    val showAddSoundDialog: StateFlow<Boolean> = _showAddSoundDialog.asStateFlow()

    init {
        // Hook engine callback to service start
        audioEngine.onPlaybackStateChanged = { isPlaying, _ ->
            if (isPlaying) {
                SoundMixerService.startService(getApplication())
            }
        }
    }

    fun toggleSound(soundId: String) {
        audioEngine.toggleSoundPlay(soundId)
    }

    fun setSoundVolume(soundId: String, volume: Float) {
        audioEngine.setSoundVolume(soundId, volume)
    }

    fun toggleMute(soundId: String) {
        audioEngine.toggleSoundMute(soundId)
    }

    fun setMasterVolume(volume: Float) {
        audioEngine.setMasterVolume(volume)
    }

    fun toggleMasterPlay() {
        audioEngine.toggleMasterPlayPause()
    }

    fun stopAll() {
        audioEngine.stopAll()
        SoundMixerService.stopService(getApplication())
    }

    fun applyPreset(preset: SoundPreset) {
        audioEngine.applyPreset(preset.soundVolumes)
        SoundMixerService.startService(getApplication())
    }

    fun openSavePresetDialog() {
        _showSavePresetDialog.value = true
    }

    fun closeSavePresetDialog() {
        _showSavePresetDialog.value = false
    }

    fun saveCurrentMix(name: String) {
        if (name.isBlank()) return
        val currentVolumes = tracksList.value
            .filter { it.isPlaying && !it.isMuted }
            .associate { it.definition.id to it.volume }

        if (currentVolumes.isNotEmpty()) {
            viewModelScope.launch {
                presetRepository.saveCustomPreset(name, currentVolumes)
                closeSavePresetDialog()
            }
        }
    }

    fun deleteCustomPreset(id: String) {
        viewModelScope.launch {
            presetRepository.deleteCustomPreset(id)
        }
    }

    fun openTimerSheet() {
        _showTimerSheet.value = true
    }

    fun closeTimerSheet() {
        _showTimerSheet.value = false
    }

    fun startTimer(minutes: Int) {
        audioEngine.startSleepTimer(minutes)
        closeTimerSheet()
    }

    fun stopTimer() {
        audioEngine.stopSleepTimer()
    }

    // --- Sound Management ---

    fun openManageSounds() {
        _showManageSoundsSheet.value = true
    }

    fun closeManageSounds() {
        _showManageSoundsSheet.value = false
    }

    fun openAddSoundDialog() {
        _showAddSoundDialog.value = true
    }

    fun closeAddSoundDialog() {
        _showAddSoundDialog.value = false
    }

    fun addCustomSound(
        title: String,
        iconName: String,
        audioUri: Uri,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = soundRepository.addCustomSound(title, iconName, audioUri)
            if (result.isSuccess) {
                closeAddSoundDialog()
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Failed to import sound")
            }
        }
    }

    fun deleteCustomSound(id: String) {
        viewModelScope.launch {
            soundRepository.deleteCustomSound(id)
        }
    }
}
