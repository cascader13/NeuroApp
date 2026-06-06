package com.neuroproject.neuro.presentation.screens.calibration

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetronomePlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    fun start() {
        stop()
        try {
            val resourceId = context.resources.getIdentifier(
                "metronom", "raw", context.packageName
            )
            if (resourceId != 0) {
                mediaPlayer = MediaPlayer.create(context, resourceId)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}