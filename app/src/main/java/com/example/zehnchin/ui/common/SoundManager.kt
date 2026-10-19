package com.example.zehnchin.ui.common

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.example.zehnchin.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<SoundEvent, Int>()
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isEnabled = true

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        // Preload sounds - logic for future assets
        // loadSound(SoundEvent.CORRECT, R.raw.correct)
        
        scope.launch {
            settingsRepository.getSettings().collect { settings ->
                isEnabled = settings.soundEnabled
            }
        }
    }

    private fun loadSound(event: SoundEvent, resId: Int) {
        sounds[event] = soundPool.load(context, resId, 1)
    }

    fun play(event: SoundEvent) {
        if (!isEnabled) return

        val soundId = sounds[event]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            playFallbackTone(event)
        }
    }

    private fun playFallbackTone(event: SoundEvent) {
        when (event) {
            SoundEvent.BUTTON_CLICK -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            SoundEvent.CORRECT -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            SoundEvent.INCORRECT -> toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
            SoundEvent.LEVEL_COMPLETE -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
            SoundEvent.LEVEL_FAILED -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_LOW_L, 400)
            SoundEvent.COUNTDOWN_TICK -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
            SoundEvent.COUNTDOWN_GO -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            SoundEvent.TIME_WARNING -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 100)
            SoundEvent.HINT -> toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
            SoundEvent.UNDO -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
            SoundEvent.INVALID_MOVE -> toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 150)
        }
    }

    fun release() {
        soundPool.release()
        toneGenerator.release()
    }

    enum class SoundEvent {
        BUTTON_CLICK,
        CORRECT,
        INCORRECT,
        LEVEL_COMPLETE,
        LEVEL_FAILED,
        COUNTDOWN_TICK,
        COUNTDOWN_GO,
        TIME_WARNING,
        HINT,
        UNDO,
        INVALID_MOVE
    }
}
