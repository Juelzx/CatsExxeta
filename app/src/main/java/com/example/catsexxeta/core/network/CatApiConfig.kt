package com.example.catsexxeta.core.network

object CatApiConfig {
    const val BASE_URL = "https://api.thecatapi.com/v1/"
    const val API_KEY_HEADER = "x-api-key"

    fun breedImageUrl(referenceImageId: String): String =
        "https://cdn2.thecatapi.com/images/$referenceImageId.jpg"
}
