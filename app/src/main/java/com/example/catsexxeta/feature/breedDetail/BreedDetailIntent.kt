package com.example.catsexxeta.feature.breedDetail

sealed interface BreedDetailIntent {
    data class Load(val breedId: String) : BreedDetailIntent
    data object Retry : BreedDetailIntent

}