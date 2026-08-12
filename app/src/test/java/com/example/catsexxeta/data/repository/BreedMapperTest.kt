package com.example.catsexxeta.data.repository

import com.example.catsexxeta.data.remote.dto.BreedDto
import com.example.catsexxeta.data.remote.dto.WeightDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BreedMapperTest {

    @Test
    fun `maps BreedDto to Breed domain model`() {
        val dto = BreedDto(
            id = "abys",
            name = "Abyssinian",
            origin = "Egypt",
            description = "Active and social",
            temperament = "Active, Energetic",
            lifeSpan = "14 - 15",
            weight = WeightDto(metric = "3 - 5"),
            referenceImageId = "0XYvRd7oD"
        )

        val breed = dto.toDomain()

        assertEquals("abys", breed.id)
        assertEquals("Abyssinian", breed.name)
        assertEquals("Egypt", breed.origin)
        assertEquals("Active and social", breed.description)
        assertEquals("Active, Energetic", breed.temperament)
        assertEquals("14 - 15", breed.lifeSpan)
        assertEquals("3 - 5", breed.weightKg)
        assertEquals("https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg", breed.imageUrl)
    }

    @Test
    fun `missing weight maps to null weightKg`() {
        val dto = BreedDto(id = "abys", name = "Abyssinian", weight = null)

        val breed = dto.toDomain()

        assertNull(breed.weightKg)
    }

    @Test
    fun `missing reference image id maps to null image url`() {
        val dto = BreedDto(id = "abys", name = "Abyssinian", referenceImageId = null)

        val breed = dto.toDomain()

        assertNull(breed.imageUrl)
    }
}
