package de.shopme.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundPlayer {

    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var loaded = false

    fun init(context: Context) {

        if (soundPool != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()

        soundId = soundPool!!.load(context, de.shopme.R.raw.fireworks, 1)

        soundPool!!.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }
    }

    fun play() {
        if (loaded) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
}