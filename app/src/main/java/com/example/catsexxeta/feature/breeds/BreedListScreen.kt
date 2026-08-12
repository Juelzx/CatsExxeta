package com.example.catsexxeta.feature.breeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catsexxeta.core.designsystem.components.EmptyState
import com.example.catsexxeta.core.designsystem.components.ErrorState
import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.Breed
import org.koin.androidx.compose.koinViewModel

@Composable
fun BreedListRoute(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
    viewModel: BreedListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BreedListEffect.NavigateToBreedDetail -> onNavigateToDetail(effect.breedId)
            }
        }
    }

    BreedListScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun BreedListScreen(
    modifier: Modifier = Modifier,
    state: BreedListState,
    onIntent: (BreedListIntent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
//        Text(
//            text = "Cat Explorer",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
//        )

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(BreedListIntent.SearchChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search breeds...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
        )

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.error != null -> ErrorState(
                    title = "Unable to load breeds",
                    message = state.error.toMessage(),
                    onRetry = { onIntent(BreedListIntent.Retry) }
                )

                state.isLoading && state.breeds.isEmpty() -> BreedGridSkeleton()

                state.breeds.isEmpty() -> EmptyState(
                    message = "No cat breeds found.",
                    actionLabel = "Retry",
                    onAction = { onIntent(BreedListIntent.Retry) }
                )

                state.visibleBreeds.isEmpty() -> EmptyState(
                    message = "No breeds match your search.\nTry another search term."
                )

                else -> BreedGrid(
                    breeds = state.visibleBreeds,
                    favoriteBreedIds = state.favoriteBreedIds,
                    onBreedClick = { onIntent(BreedListIntent.BreedClicked(it)) },
//                    onToggleFavorite = { onIntent(BreedListIntent.ToggleFavorite(it)) }
                )
            }
        }
    }
}

@Composable
private fun BreedGrid(
    modifier: Modifier = Modifier,
    breeds: List<Breed>,
    favoriteBreedIds: Set<String>,
    onBreedClick: (String) -> Unit,
//    onToggleFavorite: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(breeds, key = { it.id }) { breed ->
            BreedCard(
                breed = breed,
                onClick = { onBreedClick(breed.id) },
            )
        }
    }
}

@Composable
private fun BreedGridSkeleton(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            BreedCardSkeleton()
        }
    }
}

private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> "You're offline. Check your connection and try again."
    AppError.Timeout -> "The request took too long. Please try again."
    AppError.Unauthorized -> "Access to the Cat API was denied. Check the API key configuration."
    AppError.NotFound -> "The requested data could not be found."
    AppError.Server -> "Something went wrong on our side. Please try again later."
    AppError.Unknown -> "Unable to load cat breeds."
}
