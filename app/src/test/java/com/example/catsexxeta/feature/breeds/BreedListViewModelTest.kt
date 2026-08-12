package com.example.catsexxeta.feature.breeds

import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.AppException
import com.example.catsexxeta.core.model.Breed
import com.example.catsexxeta.data.repository.BreedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BreedListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load succeeds and populates breeds`() = runTest {
        val breeds = listOf(testBreed("Abyssinian"))
        val repository = FakeBreedRepository(Result.success(breeds))
        val viewModel = BreedListViewModel(repository)

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(breeds, state.breeds)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `load fails and populates error`() = runTest {
        val repository = FakeBreedRepository(Result.failure(AppException(AppError.Network)))
        val viewModel = BreedListViewModel(repository)

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AppError.Network, state.error)
        assertEquals(false, state.isLoading)
        assertTrue(state.breeds.isEmpty())
    }

    @Test
    fun `retry after error reloads breeds successfully`() = runTest {
        val repository = FakeBreedRepository(Result.failure(AppException(AppError.Network)))
        val viewModel = BreedListViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(AppError.Network, viewModel.state.value.error)

        val breeds = listOf(testBreed("Siamese"))
        repository.result = Result.success(breeds)
        viewModel.onIntent(BreedListIntent.Retry)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(breeds, state.breeds)
        assertNull(state.error)
    }

    private class FakeBreedRepository(
        var result: Result<List<Breed>>
    ) : BreedRepository {
        var callCount = 0
            private set

        override suspend fun getBreeds(): Result<List<Breed>> {
            callCount++
            return result
        }

        override suspend fun getBreed(breedId: String): Result<Breed> {
            TODO("Not yet implemented")
        }
    }
}
