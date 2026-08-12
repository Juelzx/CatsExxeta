package com.example.catsexxeta.data.remote

import com.example.catsexxeta.data.remote.dto.BreedDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorCatApi(private val httpClient: HttpClient) : CatApi {
    override suspend fun getBreeds(): List<BreedDto> =
        httpClient.get("breeds").body()

    override suspend fun getBreed(breedId: String): BreedDto =
        httpClient.get("breeds/$breedId").body()
}
