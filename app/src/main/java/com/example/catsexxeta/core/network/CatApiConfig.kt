package com.example.catsexxeta.core.network

object CatApiConfig {
    // Trailing slash matters: request paths resolve relative to this via standard URL join rules.
    const val BASE_URL = "https://api.thecatapi.com/v1/"
    const val API_KEY_HEADER = "x-api-key"

    // The /breeds endpoint only returns a reference_image_id, not a full image URL.
    // The Cat API serves breed reference images from this CDN by id, avoiding an extra
    // network round-trip per breed just to resolve an image URL.
    fun breedImageUrl(referenceImageId: String): String =
        "https://cdn2.thecatapi.com/images/$referenceImageId.jpg"
}
