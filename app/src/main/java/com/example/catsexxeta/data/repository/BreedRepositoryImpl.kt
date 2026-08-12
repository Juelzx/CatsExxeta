package com.example.catsexxeta.data.repository

import com.example.catsexxeta.core.model.AppException
import com.example.catsexxeta.core.model.Breed
import com.example.catsexxeta.core.network.toAppError
import com.example.catsexxeta.data.remote.CatApi
import kotlinx.coroutines.CancellationException

class BreedRepositoryImpl(private val api: CatApi) : BreedRepository {
    override suspend fun getBreeds(): Result<List<Breed>> = try {
        Result.success(api.getBreeds().map { it.toDomain() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(AppException(e.toAppError(), e))
    }

    override suspend fun getBreed(breedId: String): Result<Breed> {
        return try {
            Result.success(api.getBreed(breedId).toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppException(e.toAppError(), e))
        }
    }
}
