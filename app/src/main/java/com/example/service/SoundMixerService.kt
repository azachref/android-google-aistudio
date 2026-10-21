package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.MultiAudioEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SoundMixerService : Service() {

    private val binder = LocalBinder()
    private lateinit var audioEngine: MultiAudioEngine
    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isForeground = false

    inner class LocalBinder : Binder() {
        fun getService(): SoundMixerService = this@SoundMixerService
    }

    override fun onCreate() {
        super.onCreate()
        audioEngine = MultiAudioEngine.getInstance(this)
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "somnia:sound_mixer_wake_lock"
        ).apply {
            setReferenceCounted(false)
        }

        // Listen for playback state changes
        scope.launch {
            audioEngine.isMasterPlaying.collectLatest { isPlaying ->
                updateWakeLock(isPlaying)
                updateNotification()
            }
        }

        scope.launch {
            audioEngine.tracksState.collectLatest {
                updateNotification()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> audioEngine.resumeActiveSounds()
            ACTION_PAUSE -> audioEngine.pauseAll()
            ACTION_TOGGLE -> audioEngine.toggleMasterPlayPause()
            ACTION_STOP -> {
                audioEngine.stopAll()
                stopForegroundService()
                return START_NOT_STICKY
            }
        }

        startForegroundWithNotification()
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForeground = true
    }

    private fun stopForegroundService() {
        updateWakeLock(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        isForeground = false
        stopSelf()
    }

    private fun updateWakeLock(isPlaying: Boolean) {
        val activeCount = audioEngine.tracksState.value.values.count { it.isPlaying }
        if (isPlaying && activeCount > 0) {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(8 * 60 * 60 * 1000L) // 8 hour safety timeout
            }
        } else {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        }
    }

    private fun updateNotification() {
        if (!isForeground) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = audioEngine.isMasterPlaying.value
        val activeTracks = audioEngine.tracksState.value.values.filter { it.isPlaying }

        val activeNames = if (activeTracks.isEmpty()) {
            getString(R.string.app_tagline)
        } else {
            activeTracks.joinToString(", ") { it.definition.title }
        }

        val toggleActionIntent = Intent(this, SoundMixerService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopActionIntent = Intent(this, SoundMixerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val toggleTitle = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Somnia Soundscapes")
            .setContentText(activeNames)
            .setSubText(if (isPlaying) "Playing" else "Paused")
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(toggleIcon, toggleTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Somnia Soundscapes Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for Somnia background ambient audio mixer"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        updateWakeLock(false)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "somnia_playback"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.ACTION_START"
        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_TOGGLE = "com.example.ACTION_TOGGLE"
        const val ACTION_STOP = "com.example.ACTION_STOP"

        fun startService(context: Context) {
            val intent = Intent(context, SoundMixerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SoundMixerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
