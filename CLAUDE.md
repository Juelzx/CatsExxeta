# CLAUDE.md — Cat Explorer

## Project Context

Build a native Android application for a Mobile coding challenge using **The Cat API**.

This is a native Android implementation.

The company also works with Kotlin Multiplatform, so the project should demonstrate strong Kotlin fundamentals and use **Ktor Client for networking** instead of Retrofit. The use of Ktor is intentional because the networking layer should be easily portable to Kotlin Multiplatform in a future iteration.

Do **not** turn this project into a KMP project.

Do **not** use Compose Multiplatform.

Use native Android technologies.

---

# 1. Technology Stack

Use:

* Kotlin
* Android
* Jetpack Compose
* Material 3
* Coroutines
* Flow / StateFlow
* Ktor Client
* kotlinx.serialization
* Koin
* AndroidX Navigation Compose
* Coil for image loading
* Gradle Kotlin DSL

Prefer current stable versions already compatible with the project.

Do not introduce unnecessary libraries.

---

# 2. Architecture

Use **MVI with unidirectional data flow**.

The architecture should be:

```text
Compose UI
    ↓
Intent
    ↓
MVI ViewModel / Store
    ↓
Repository
    ↓
Ktor API Client
    ↓
The Cat API
```

For persistence:

```text
ViewModel
    ↓
Repository
    ├── Remote Data Source
    └── Local Data Source
```

The UI must never communicate directly with Ktor.

The UI must never access repositories directly.

The UI communicates exclusively through intents/actions.

---

# 3. Important Architectural Rule

Do NOT implement MVVM-style state mutation.

Although Android `ViewModel` may be used as the lifecycle-aware host for the MVI state, it must behave as an **MVI Store**, not as a traditional MVVM ViewModel.

Avoid patterns such as:

```kotlin
viewModel.loadBreeds()
viewModel.searchBreeds()
viewModel.vote()
```

Prefer:

```kotlin
viewModel.onIntent(BreedIntent.Load)
viewModel.onIntent(BreedIntent.SearchChanged(query))
viewModel.onIntent(BreedIntent.Vote(imageId, value))
```

All user actions should enter through the intent pipeline.

---

# 4. MVI Structure

Each feature should have:

```text
Intent
State
Effect
ViewModel / Store
Screen
```

Example:

```kotlin
sealed interface BreedListIntent {
    data object Load : BreedListIntent
    data class SearchChanged(val query: String) : BreedListIntent
    data class BreedClicked(val breedId: String) : BreedListIntent
    data object Retry : BreedListIntent
}
```

State:

```kotlin
data class BreedListState(
    val breeds: List<Breed> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: AppError? = null
)
```

Effects:

```kotlin
sealed interface BreedListEffect {
    data class NavigateToBreed(val breedId: String) : BreedListEffect
    data class ShowMessage(val message: String) : BreedListEffect
}
```

Expose:

```kotlin
val state: StateFlow<BreedListState>
val effects: Flow<BreedListEffect>
```

The state must be immutable from the UI.

---

# 5. State Flow

Use a single source of truth.

Prefer:

```kotlin
private val _state = MutableStateFlow(BreedListState())
val state = _state.asStateFlow()
```

Never expose `MutableStateFlow`.

State transitions should be explicit and predictable.

Prefer:

```kotlin
_state.update {
    it.copy(
        isLoading = true,
        error = null
    )
}
```

over scattered mutable properties.

---

# 6. Package Structure

Use feature-oriented organization.

Recommended:

```text
app/
├── core/
│   ├── network/
│   ├── designsystem/
│   ├── model/
│   └── util/
│
├── data/
│   ├── remote/
│   │   ├── CatApi.kt
│   │   └── dto/
│   ├── local/
│   └── repository/
│
├── feature/
│   ├── breeds/
│   │   ├── BreedListScreen.kt
│   │   ├── BreedListViewModel.kt
│   │   ├── BreedListIntent.kt
│   │   ├── BreedListState.kt
│   │   └── BreedListEffect.kt
│   │
│   ├── breedDetail/
│   │
│   ├── discover/
│   │
│   └── favorites/
│
├── navigation/
│
└── di/
```

Do not create layers that have no meaningful responsibility.

---

# 7. Domain Models vs API DTOs

Never expose API DTOs directly to Compose.

Example:

```kotlin
@Serializable
data class BreedDto(...)
```

Map into:

```kotlin
data class Breed(
    val id: String,
    val name: String,
    val description: String?,
    val origin: String?,
    val temperament: String?,
    val lifeSpan: String?,
    val imageUrl: String?
)
```

The API contract should remain isolated inside the data layer.

This is important because the networking layer should be easily extractable into a KMP shared module later.

---

# 8. Networking — Ktor

Use **Ktor Client** for every network request.

Do NOT use Retrofit.

Do NOT use Volley.

Do NOT mix networking libraries.

Configure one shared Ktor `HttpClient` through dependency injection.

Use:

* ContentNegotiation
* kotlinx.serialization
* Logging
* HttpTimeout
* response validation

Example:

```kotlin
HttpClient(Android) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                isLenient = true
            }
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}
```

Use `LogLevel.NONE` or reduced logging for release builds.

Never log API keys or sensitive data.

---

# 9. API Service

Create a typed API abstraction.

For example:

```kotlin
interface CatApi {

    suspend fun getBreeds(): List<BreedDto>

    suspend fun searchImages(
        breedId: String? = null,
        page: Int = 0,
        limit: Int = 20
    ): List<CatImageDto>

    suspend fun vote(
        imageId: String,
        value: Int
    )

    suspend fun addFavorite(
        imageId: String
    )

    suspend fun removeFavorite(
        favoriteId: String
    )
}
```

Use the actual current The Cat API documentation to determine:

* endpoints
* HTTP methods
* query parameters
* request bodies
* response models
* authentication requirements

Do not invent endpoint behavior.

---

# 10. API Key

Do not hardcode secrets in Kotlin source code.

If an API key is required:

* use `local.properties` / BuildConfig or another local configuration mechanism
* do not commit the key
* document how to configure it in README

Never print the API key in logs.

---

# 11. Error Handling

Never expose raw exceptions to the UI.

Create:

```kotlin
sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data object Server : AppError
    data object Unknown : AppError
}
```

Map Ktor exceptions and HTTP status codes into `AppError`.

The UI should only deal with application-level errors.

Example:

```text
Network
→ "You're offline. Check your connection."

Timeout
→ "The request took too long."

Server
→ "Something went wrong on our side."

Unknown
→ "Unable to load cats."
```

---

# 12. Repository

Repositories abstract data access.

Example:

```kotlin
interface BreedRepository {
    suspend fun getBreeds(): Result<List<Breed>>
}
```

The repository is responsible for:

* calling the API
* mapping DTOs
* handling data-source concerns
* optionally caching data

The ViewModel must not know about Ktor.

The ViewModel must not know about HTTP status codes.

---

# 13. Task 1 — Breeds

Implement first.

Required:

* Load all cat breeds
* Display breeds
* Search/filter breeds
* Open breed detail
* Loading state
* Error state
* Empty state
* Retry

Use:

```kotlin
LazyVerticalGrid
```

or another appropriate lazy layout.

Each breed card should contain:

* image
* breed name
* origin
* relevant short information

---

# 14. Breed Detail

Display useful API information:

* Name
* Image
* Origin
* Description
* Temperament
* Life span
* Weight
* Relevant characteristics

Do not display every available API property.

Prioritize information that is useful to the user.

---

# 15. Task 3 — Image Search

Implement:

* image search
* optional breed filtering
* image grid
* pagination
* image loading states
* image error state
* image detail

Use lazy loading.

Avoid downloading hundreds of images at once.

Prevent duplicate pagination requests.

---

# 16. Vote

Implement Like and Dislike.

All interactions must go through MVI intents.

Example:

```kotlin
data class Vote(
    val imageId: String,
    val value: Int
) : DiscoverIntent
```

Flow:

```text
User taps Like
    ↓
Vote intent
    ↓
ViewModel
    ↓
Repository
    ↓
Ktor
    ↓
API result
    ↓
New State / Effect
    ↓
Compose
```

Never call the API directly from a Composable.

Provide:

* loading state
* success feedback
* error feedback

---

# 17. Favorites

Implement only if time permits.

Support:

* add favorite
* remove favorite
* favorites list
* empty state
* loading state
* error state

The Discover screen should immediately reflect favorite state.

Avoid maintaining multiple unrelated sources of truth.

---

# 18. Offline / Poor Network

The application should behave gracefully when the network is unavailable.

At minimum:

* display useful error states
* provide Retry
* preserve already loaded data where possible
* avoid replacing existing content with a blank screen
* optionally cache the breed list

If persistence is implemented, keep it behind a repository/local data-source abstraction.

Do not build an unnecessarily complicated synchronization system for the coding challenge.

---

# 19. Persistence

If implementing persistence, use a modern Android-compatible solution such as Room.

However, because this project uses Ktor to demonstrate KMM readiness, keep the persistence abstraction independent from Android-specific database APIs.

Example:

```kotlin
interface BreedLocalDataSource {
    suspend fun getBreeds(): List<Breed>
    suspend fun saveBreeds(breeds: List<Breed>)
}
```

The repository should decide when to use local vs remote data.

---

# 20. Dependency Injection

Use Koin.

Provide:

```text
HttpClient
CatApi
Repositories
Local Data Sources
ViewModels
```

Do not instantiate dependencies manually inside Composables.

Avoid global singleton access.

---

# 21. Compose

Use Jetpack Compose.

Screens should be mostly stateless where practical.

Prefer:

```kotlin
@Composable
fun BreedListScreen(
    state: BreedListState,
    onIntent: (BreedListIntent) -> Unit
)
```

rather than coupling the screen directly to a ViewModel.

The route/container can collect the ViewModel state and pass it to the UI.

This makes the UI easier to preview and test.

---

# 22. UI Components

Create reusable components where repetition exists:

```text
BreedCard
CatImageCard
FavoriteButton
VoteButtons
LoadingState
ErrorState
EmptyState
RetryButton
SectionHeader
```

Do not create a component for every tiny `Row` or `Column`.

---

# 23. UI States

Every network-driven screen should consider:

```text
Loading
Success
Empty
Error
Refreshing
Pagination Loading
```

Do not design only for the successful API response.

---

# 24. Navigation

Use Navigation Compose.

Recommended flow:

```text
Breeds
   ↓
Breed Detail

Discover
   ↓
Cat Image Detail

Favorites
   ↓
Cat Image Detail
```

Keep navigation centralized.

Navigation should be triggered through MVI effects where appropriate.

Example:

```kotlin
sealed interface BreedListEffect {
    data class NavigateToBreed(
        val breedId: String
    ) : BreedListEffect
}
```

---

# 25. Design

The UI should feel:

* modern
* clean
* premium
* friendly
* minimal
* image-focused

Use Material 3.

Use:

* rounded cards
* consistent spacing
* strong typography hierarchy
* subtle elevation
* high-quality image presentation

Avoid excessive colors, gradients and animations.

The cat imagery should be the visual focus.

---

# 26. Accessibility

Ensure:

* touch targets around 48dp
* sufficient contrast
* content descriptions
* meaningful semantics
* scalable typography
* no important information conveyed only through color

Examples:

```text
"Add to favorites"
"Remove from favorites"
"Like cat"
"Dislike cat"
"Open British Shorthair details"
"Retry loading breeds"
```

---

# 27. Localization

Do not hardcode user-facing strings inside Composables.

Use Android resources:

```text
strings.xml
```

Prepare strings for:

* screen titles
* buttons
* errors
* empty states
* accessibility labels

Initial language can be English.

The architecture should make German localization straightforward.

---

# 28. Testing

Prioritize meaningful tests.

### Unit Tests

Test:

* DTO → domain mapping
* API error mapping
* breed search
* repository behavior
* pagination
* MVI state transitions

Example:

```text
Given a list of breeds
When searching for "British"
Then only matching breeds are returned
```

### MVI Tests

Verify:

```text
Load
→ Loading
→ Success
```

and:

```text
Load
→ Loading
→ Error
```

and:

```text
Retry
→ Loading
→ Success
```

### UI Tests

Implement only the most valuable UI tests if time permits.

Do not sacrifice feature quality for test quantity.

---

# 29. Coroutines

Use structured concurrency.

Never use:

```kotlin
GlobalScope
```

Use `viewModelScope`.

Handle cancellation correctly.

For search:

* debounce user input
* avoid unnecessary API calls
* cancel obsolete work

Do not trigger network requests for every individual keystroke.

---

# 30. Performance

Pay attention to:

* LazyColumn/LazyGrid
* stable keys
* image caching
* pagination
* unnecessary recomposition
* unnecessary API calls

Use Coil for image loading and caching.

Avoid expensive computation inside Composables.

---

# 31. KMM Readiness

Although this is **NOT a KMP project**, structure code with future portability in mind.

Good candidates for future shared code:

```text
Domain models
Repositories
API models
Ktor networking
Error mapping
Business logic
MVI state logic
```

Avoid unnecessarily coupling these areas to Android UI classes.

However:

**Do not create fake expect/actual abstractions.**

**Do not create a KMP module.**

**Do not compromise the native Android implementation just to make it theoretically portable.**

The goal is to demonstrate:

> "I know how to write Kotlin and networking code that could later move into a KMP shared module."

---

# 32. Do Not Overengineer

This is a roughly 4-hour coding challenge.

Do NOT create:

* UseCase classes for every operation
* Interactors
* Managers
* Event buses
* custom navigation frameworks
* custom networking abstractions on top of Ktor
* generic repository frameworks
* excessive interfaces
* excessive modules
* unnecessary design-system abstractions

In particular:

**Do not introduce UseCases just because a Clean Architecture template suggests them.**

The preferred flow is:

```text
UI
 ↓
MVI ViewModel
 ↓
Repository
 ↓
Ktor
```

Keep it simple.

---

# 33. Implementation Priority

Follow this order.

## Phase 1 — Foundation

1. Android project
2. Jetpack Compose
3. Material 3
4. Koin
5. Ktor
6. kotlinx.serialization
7. Navigation
8. MVI base structure

## Phase 2 — Task 1

9. Breed API
10. DTOs
11. Domain models
12. Repository
13. Breed MVI
14. Breed list
15. Search
16. Loading
17. Error
18. Retry
19. Breed detail

## Phase 3 — Task 3

20. Image search
21. Image grid
22. Pagination
23. Vote
24. Favorites if time permits

## Phase 4 — Quality

25. Image caching
26. Offline handling
27. Accessibility
28. Localization
29. Unit tests
30. UI polish

---

# 34. Presentation Strategy

The codebase should make it easy to explain these decisions during the interview:

### Why MVI?

* predictable state
* unidirectional data flow
* explicit user actions
* easy testing

### Why Ktor?

The application is native Android, but Ktor keeps the networking layer Kotlin-first and makes the solution naturally transferable to KMP.

### Why Repository?

The UI/business layer should not care whether data comes from the API or local cache.

### Why DTO → Domain mapping?

The API contract should not leak into the UI.

### Why StateFlow?

A single observable state makes Compose rendering predictable.

### Why Effects?

Navigation and one-shot events should not be represented as persistent UI state.

---

# 35. README

Create a concise README containing:

## Overview

What the app does.

## Tech Stack

```text
Kotlin
Android
Jetpack Compose
Material 3
Ktor
Kotlinx Serialization
Koin
Coroutines / Flow
Coil
```

## Architecture

```text
Compose UI
    ↓
MVI ViewModel
    ↓
Repository
    ↓
Ktor
    ↓
The Cat API
```

## Why Ktor?

Explain that Ktor was deliberately selected instead of Retrofit because the company also works with Kotlin Multiplatform and Ktor allows the networking implementation to be easily shared in a future KMP architecture.

## Error Handling

Explain the error model and retry behavior.

## Offline Strategy

Explain caching and behavior when connectivity is unavailable.

## Testing

Explain important test coverage.

## Trade-offs

Explicitly document what was intentionally not implemented due to the 4-hour challenge.

---

# Final Claude Code Rules

1. This is a **native Android application**.
2. Use **Jetpack Compose**, not Compose Multiplatform.
3. Use **MVI**, not MVVM.
4. Use **Ktor**, not Retrofit.
5. Use **Koin** for DI.
6. Use **Coroutines + Flow/StateFlow**.
7. Keep API DTOs separate from domain models.
8. Keep networking out of Composables.
9. Keep UI state immutable.
10. Use explicit MVI intents.
11. Use effects for one-shot events/navigation.
12. Do not introduce unnecessary UseCases.
13. Do not overengineer.
14. Prioritize the core challenge requirements first.
15. Keep networking/domain code Kotlin-first and reasonably KMP-portable.
16. Never compromise the native Android implementation just for theoretical KMP compatibility.
17. Build and test frequently while implementing.
18. Do not mark features as complete until they actually compile and work.
19. Prefer a smaller polished implementation over a large unfinished one.
20. Before finishing, run the relevant tests and verify the application builds successfully.
