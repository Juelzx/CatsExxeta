package com.example.catsexxeta.feature.breedDetail

import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.Breed

data class BreedDetailState(
    val breed: Breed? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
)
