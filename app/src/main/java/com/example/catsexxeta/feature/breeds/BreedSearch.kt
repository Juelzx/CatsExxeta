package com.example.catsexxeta.feature.breeds

import com.example.catsexxeta.core.model.Breed

fun List<Breed>.filterByQuery(query: String): List<Breed> {
    if (query.isBlank()) return this
    return filter { it.name.contains(query, ignoreCase = true) }
}
