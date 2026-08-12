package com.example.catsexxeta.feature.breeds

sealed interface BreedListEffect {
    data class NavigateToBreedDetail(val breedId: String) : BreedListEffect
}
