package com.example.catsexxeta.data.repository

import com.example.catsexxeta.core.model.Breed
import com.example.catsexxeta.core.network.CatApiConfig
import com.example.catsexxeta.data.remote.dto.BreedDto

fun BreedDto.toDomain(): Breed = Breed(
    id = id,
    name = name,
    origin = origin,
    description = description,
    temperament = temperament,
    lifeSpan = lifeSpan,
    weightKg = weight?.metric,
    imageUrl = referenceImageId?.let { CatApiConfig.breedImageUrl(it) }
)
