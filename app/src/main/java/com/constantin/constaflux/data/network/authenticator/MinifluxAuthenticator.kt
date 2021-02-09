package com.constantin.constaflux.data.network.authenticator

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class MinifluxAuthenticator(username: String, password: String) {
    private val client: OkHttpClient

    init {
        client = OkHttpClient.Builder()
            .authenticator { _, response ->
                if (response.request().header("Authorization") != null) {
                    null
                } else {
                    val credential = Credentials.basic(username, password)
                    response.request().newBuilder()
                        .header("Authorization", credential)
                        .build()
                }
            }
            .build()
    }

    @Throws(Exception::class)
    fun run(url: String) {
        val request = Request.Builder()
            .url("$url/v1/me")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
        }
    }
}
