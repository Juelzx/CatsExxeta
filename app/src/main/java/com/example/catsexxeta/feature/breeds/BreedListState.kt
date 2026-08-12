package com.example.catsexxeta.feature.breeds

import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.Breed

data class BreedListState(
    val breeds: List<Breed> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val favoriteBreedIds: Set<String> = emptySet()
) {
    val visibleBreeds: List<Breed>
        get() = breeds.filterByQuery(searchQuery)
}
