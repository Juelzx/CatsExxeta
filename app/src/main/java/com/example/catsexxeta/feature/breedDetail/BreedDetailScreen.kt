package com.example.catsexxeta.feature.breedDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.catsexxeta.core.designsystem.components.ErrorState
import com.example.catsexxeta.core.model.AppError
import com.example.catsexxeta.core.model.Breed
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BreedDetailScreen(
    modifier: Modifier = Modifier,
    breedId: String,
    onBack: () -> Unit = {},
    viewModel: BreedDetailViewModel = koinViewModel(parameters = { parametersOf(breedId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BreedDetailContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedDetailContent(
    modifier: Modifier = Modifier,
    state: BreedDetailState,
    onIntent: (BreedDetailIntent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.breed?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Navigate back" }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.error != null -> ErrorState(
                    title = "Unable to load breed",
                    message = state.error.toMessage(),
                    onRetry = { onIntent(BreedDetailIntent.Retry) }
                )

                state.isLoading && state.breed == null -> BreedDetailLoading()

                state.breed != null -> BreedDetailBody(breed = state.breed)

                else -> Unit
            }
        }
    }
}

@Composable
private fun BreedDetailLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...")
    }
}

@Composable
private fun BreedDetailBody(modifier: Modifier = Modifier, breed: Breed) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        BreedHeroImage(
            imageUrl = breed.imageUrl,
            contentDescription = "${breed.name} cat photo",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            if (!breed.origin.isNullOrBlank()) {
                Text(
                    text = breed.origin,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }

            if (breed.lifeSpan != null || breed.weightKg != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (breed.lifeSpan != null) {
                        InfoTile(
                            label = "Life span",
                            value = "${breed.lifeSpan} years",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (breed.weightKg != null) {
                        InfoTile(
                            label = "Weight",
                            value = "${breed.weightKg} kg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (!breed.description.isNullOrBlank()) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = breed.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
            }

            val traits = breed.temperament
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                .orEmpty()
            if (traits.isNotEmpty()) {
                Text(
                    text = "Temperament",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    traits.forEach { trait -> TraitChip(text = trait) }
                }
            }
        }
    }
}

@Composable
private fun BreedHeroImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🐱", fontSize = 40.sp)
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun InfoTile(modifier: Modifier = Modifier, label: String, value: String,) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TraitChip(modifier: Modifier = Modifier, text: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/*
    TODO: centralize error messages elsewhere and use string res for localization
 */
private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> "You're offline. Check your connection and try again."
    AppError.Timeout -> "The request took too long. Please try again."
    AppError.Unauthorized -> "Access to the Cat API was denied. Check the API key configuration."
    AppError.NotFound -> "This breed could not be found."
    AppError.Server -> "Something went wrong on our side. Please try again later."
    AppError.Unknown -> "Unable to load this breed."
}
