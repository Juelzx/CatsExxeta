package com.example.catsexxeta.feature.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.AppException
import com.example.catsexxeta.data.repository.BreedRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreedListViewModel(
    private val repository: BreedRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BreedListState())
    val state: StateFlow<BreedListState> = _state.asStateFlow()

    private val _effects = Channel<BreedListEffect>(Channel.BUFFERED)
    val effects: Flow<BreedListEffect> = _effects.receiveAsFlow()

    init {
        onIntent(BreedListIntent.Load)
    }

    fun onIntent(intent: BreedListIntent) {
        when (intent) {
            BreedListIntent.Load -> loadBreeds()
            BreedListIntent.Retry -> loadBreeds()
            is BreedListIntent.SearchChanged -> _state.update { it.copy(searchQuery = intent.query) }
            is BreedListIntent.BreedClicked -> viewModelScope.launch {
                _effects.send(BreedListEffect.NavigateToBreedDetail(intent.breedId))
            }
//            is BreedListIntent.ToggleFavorite -> toggleFavorite(intent.breedId)
        }
    }

    private fun loadBreeds() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getBreeds().fold(
                onSuccess = { breeds ->
                    _state.update { it.copy(isLoading = false, breeds = breeds, error = null) }
                },
                onFailure = { throwable ->
                    val error = (throwable as? AppException)?.error ?: AppError.Unknown
                    _state.update { it.copy(isLoading = false, error = error) }
                }
            )
        }
    }

    private fun toggleFavorite(breedId: String) {
        // Local-only toggle for now
        _state.update { current ->
            val favorites = current.favoriteBreedIds
            current.copy(
                favoriteBreedIds = if (breedId in favorites) favorites - breedId else favorites + breedId
            )
        }
    }
}
