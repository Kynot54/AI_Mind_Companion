package com.github.ai_mind_companion.app.utils

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface ApiService {
    @Multipart
    @POST
    fun uploadAudioFile(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @Part ("user_id") userId: RequestBody
    ): Call<ResponseBody>
}