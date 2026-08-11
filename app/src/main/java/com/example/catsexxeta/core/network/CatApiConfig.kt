package com.example.catsexxeta.core.network

object CatApiConfig {
    // Trailing slash matters: request paths resolve relative to this via standard URL join rules.
    const val BASE_URL = "https://api.thecatapi.com/v1/"
    const val API_KEY_HEADER = "x-api-key"
}
