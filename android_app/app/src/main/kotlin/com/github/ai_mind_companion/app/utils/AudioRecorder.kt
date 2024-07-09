package com.github.ai_mind_companion.app.utils

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    @Suppress("DEPRECATION")
    fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                val filename = "recording_${System.currentTimeMillis()}.m4a"
                audioFile = File(context.filesDir, filename)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            Log.d("AudioRecorder", "Recording started")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
        }
    }

    fun stopRecording(): File? {
        if (mediaRecorder != null) {
            try {
                mediaRecorder?.apply {
                    stop()
                    reset()
                    release()
                }
                Log.d("AudioRecorder", "Recording stopped")
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Error stopping recording", e)
                // Handle cleanup even if stopping failed
            } finally {
                mediaRecorder = null
            }
        }
        return audioFile
    }
}