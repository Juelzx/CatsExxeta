package com.example.catsexxeta.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    apiKey: String,
    enableLogging: Boolean
): HttpClient = HttpClient(Android) {

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                isLenient = true
            }
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }

    install(Logging) {
        level = if (enableLogging) LogLevel.INFO else LogLevel.NONE
        sanitizeHeader { header -> header == CatApiConfig.API_KEY_HEADER }
    }

    expectSuccess = true

    defaultRequest {
        url(CatApiConfig.BASE_URL)
        header(CatApiConfig.API_KEY_HEADER, apiKey)
    }
}
