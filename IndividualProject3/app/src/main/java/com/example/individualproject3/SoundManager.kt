package com.example.individualproject3
import android.content.Context
import android.media.MediaPlayer

class SoundManager(private val context: Context) {

    fun playSuccess() {
        playRawSound(R.raw.zelda_victory_ost)
    }

    fun playFailure() {
        playRawSound(R.raw.game_over_sound)
    }

    // NEW: light “bonk” sound for hitting walls
    // Right now it reuses game_over_sound; you can swap it
    // to a different raw resource later (e.g. R.raw.bonk_sound)
    fun playBonk() {
        playRawSound(R.raw.bump_into_wall)
    }

    fun playSplash() {
        playRawSound(R.raw.falling_in_water)   // ← replace splash_sound with your real filename
    }

    private fun playRawSound(resId: Int) {
        val player = MediaPlayer.create(context, resId)
        player?.setOnCompletionListener {
            it.release()
        }
        player?.start()
    }
}