package com.example.catsexxeta.data.repository

import com.example.catsexxeta.core.model.Breed

interface BreedRepository {
    suspend fun getBreeds(): Result<List<Breed>>
    suspend fun getBreed(breedId: String): Result<Breed>
}
