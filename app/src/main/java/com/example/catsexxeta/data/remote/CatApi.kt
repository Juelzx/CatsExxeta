package com.example.catsexxeta.data.remote

import com.example.catsexxeta.data.remote.dto.BreedDto

interface CatApi {
    suspend fun getBreeds(): List<BreedDto>
    suspend fun getBreed(breedId: String): BreedDto
}
