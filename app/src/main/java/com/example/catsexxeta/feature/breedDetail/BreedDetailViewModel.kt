package com.example.catsexxeta.feature.breedDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.AppException
import com.example.catsexxeta.data.repository.BreedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreedDetailViewModel(
    private val repository: BreedRepository,
    private val breedId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(BreedDetailState())
    val state: StateFlow<BreedDetailState> = _state.asStateFlow()

    init {
        onIntent(BreedDetailIntent.Load(breedId))
    }

    fun onIntent(intent: BreedDetailIntent) {
        when (intent) {
            is BreedDetailIntent.Load -> loadBreedDetail(intent.breedId)
            is BreedDetailIntent.Retry -> loadBreedDetail(breedId)
        }
    }

    private fun loadBreedDetail(breedId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getBreed(breedId).fold(
                onSuccess = { breed ->
                    _state.update { it.copy(isLoading = false, breed = breed, error = null) }
                },
                onFailure = { throwable ->
                    val error = (throwable as? AppException)?.error ?: AppError.Unknown
                    _state.update { it.copy(isLoading = false, error = error) }
                }
            )
        }
    }
}