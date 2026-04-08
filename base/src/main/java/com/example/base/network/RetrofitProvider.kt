package com.example.base.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap

object RetrofitProvider {

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofitCache = ConcurrentHashMap<String, Retrofit>()

    fun getRetrofit(baseUrl: String = NetworkConstants.DEFAULT_BASE_URL): Retrofit {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        return retrofitCache.computeIfAbsent(normalizedBaseUrl) { safeBaseUrl ->
            buildRetrofit(safeBaseUrl)
        }
    }

    fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    inline fun <reified T> createService(baseUrl: String = NetworkConstants.DEFAULT_BASE_URL): T {
        return getRetrofit(baseUrl).create(T::class.java)
    }

    inline fun <reified T> createService(retrofit: Retrofit): T {
        return retrofit.create(T::class.java)
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        require(baseUrl.isNotBlank()) { "Base URL cannot be blank." }
        return if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    }
}
