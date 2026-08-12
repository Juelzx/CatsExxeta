package com.example.catsexxeta.feature.breeds

sealed interface BreedListIntent {
    data object Load : BreedListIntent
    data object Retry : BreedListIntent
    data class SearchChanged(val query: String) : BreedListIntent
    data class BreedClicked(val breedId: String) : BreedListIntent
//    data class ToggleFavorite(val breedId: String) : BreedListIntent
}
