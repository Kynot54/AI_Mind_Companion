package com.github.ai_mind_companion.app.utils

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


fun sendAudioFileToServer(audioFile: File, userId: String, url: String, onUpdate: (String) -> Unit) {
    val requestBody = audioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
    val filePart = MultipartBody.Part.createFormData("audio", audioFile.name, requestBody)
    val userIdBody = userId.toRequestBody("text/plain".toMediaType())

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()


    val retrofit = Retrofit.Builder()
        .baseUrl(url.substringBeforeLast('/'))
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiService::class.java)

    service.uploadAudioFile(url, filePart, userIdBody).enqueue(object : retrofit2.Callback<ResponseBody> {
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Handler(Looper.getMainLooper()).post {
                onUpdate("Error: ${t.message}")
            }
        }

    private var mediaPlayer: MediaPlayer? = null

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        if (response.isSuccessful) {
            val jsonResponse = JSONObject(response.body()?.string() ?: "{}")
            Log.d("MediaPlayer", "Complete JSON Response: ${jsonResponse.toString(2)}")
            val audioURL = jsonResponse.optString("response_audio_url", "")
            val transcribedText = jsonResponse.optString("corrected_transcript")
            val aiResponseText = jsonResponse.optString("response_text")
            val dementiaAnalysis = jsonResponse.optString("dementia_analysis")

            Handler(Looper.getMainLooper()).post {
                Log.d("MediaPlayer", "Audio URL: $audioURL")
                val messages = "$transcribedText~$aiResponseText~$dementiaAnalysis"
                onUpdate(messages)

                mediaPlayer = MediaPlayer().apply {
                    setOnPreparedListener { start() }

                    setOnErrorListener { _: MediaPlayer?, what: Int, extra: Int ->
                        Log.w("MediaPlayer Error", "What: $what Extra: $extra")
                        true
                    }
                   setOnCompletionListener {
                       mediaPlayer?.release()
                       mediaPlayer = null
                   }

                    try {
                        reset()
                        setDataSource(audioURL)
                        prepareAsync()
                    } catch (e: java.io.IOException) {
                        Log.w("Error setting data source", e)
                    }
                }
            }
        } else {
                Handler(Looper.getMainLooper()).post {
                    onUpdate("Server responded with error: ${response.message()}")
                }
            }
    } })
}