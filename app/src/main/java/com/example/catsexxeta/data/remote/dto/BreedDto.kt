package com.example.catsexxeta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreedDto(
    val id: String,
    val name: String,
    val origin: String? = null,
    val description: String? = null,
    val temperament: String? = null,
    @SerialName("life_span")
    val lifeSpan: String? = null,
    val weight: WeightDto? = null,
    @SerialName("reference_image_id")
    val referenceImageId: String? = null
)

@Serializable
data class WeightDto(
    val metric: String? = null
)
