package com.example.catsexxeta.navigation

import kotlinx.serialization.Serializable

@Serializable
data object BreedsRoute

@Serializable
data class BreedDetailRoute(val breedId: String)

@Serializable
data object FavoritesRoute
