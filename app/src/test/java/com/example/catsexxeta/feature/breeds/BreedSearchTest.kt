package com.example.catsexxeta.feature.breeds

import com.example.catsexxeta.core.model.Breed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BreedSearchTest {

    private val breeds = listOf(
        testBreed("British Shorthair"),
        testBreed("British Longhair"),
        testBreed("Siamese")
    )

    @Test
    fun `filtering by british matches both british breeds case-insensitively`() {
        val result = breeds.filterByQuery("british")

        assertEquals(listOf("British Shorthair", "British Longhair"), result.map { it.name })
    }

    @Test
    fun `blank query returns all breeds`() {
        assertEquals(breeds, breeds.filterByQuery(""))
    }

    @Test
    fun `query with no matches returns empty list`() {
        assertTrue(breeds.filterByQuery("sphynx").isEmpty())
    }
}

internal fun testBreed(name: String) = Breed(
    id = name.lowercase().replace(" ", "-"),
    name = name,
    origin = null,
    description = null,
    temperament = null,
    lifeSpan = null,
    weightKg = null,
    imageUrl = null
)
