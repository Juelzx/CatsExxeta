# CLAUDE.md — Cat Explorer

Notes for working on this project. Built with The Cat API
(https://thecatapi.com). Native Android, not KMP — even though I picked Ktor
over Retrofit for networking, the goal isn't to actually go multiplatform right
now, just to not paint myself into a corner if that becomes relevant later. So:
Compose (not Compose Multiplatform), plain Android, Ktor for the network layer.

## Stack

Kotlin, Jetpack Compose, Material 3, Coroutines/Flow, Ktor Client,
kotlinx.serialization, Koin, Navigation Compose, Coil, Gradle Kotlin DSL.
Nothing beyond that unless there's a real reason — I'd rather keep the dependency
list short than pull in something "because it's standard."

## Why Ktor instead of Retrofit

Mostly this: I want the networking layer to stay Kotlin-first, since it's the part
of the app most likely to get reused if this ever ends up as a shared module.
Retrofit works fine too, but Ktor doesn't need annotation processing and plays
nicer if this code ever moves outside androidJar. Not a KMP project though — I
don't want expect/actual stubs or a shared module scaffolded just for the sake of
looking portable. If it stays Android-only forever, that's completely fine.

## Architecture — MVI, unidirectional

```
Compose UI → Intent → ViewModel (MVI store) → Repository → Ktor → Cat API
```

and for anything persisted:

```
ViewModel → Repository → { Remote source, Local source }
```

The important part: UI never talks to Ktor or the repository directly, only
through intents. I keep catching myself wanting to write `viewModel.loadBreeds()`
out of habit — that's the MVVM instinct and it's wrong here. It should always be
`viewModel.onIntent(BreedIntent.Load)`. If I see a public function on a
ViewModel that isn't `onIntent`, something's drifted back into MVVM territory and
needs fixing.

Each feature gets its own Intent / State / Effect / ViewModel / Screen. Something
like:

```kotlin
sealed interface BreedListIntent {
    data object Load : BreedListIntent
    data class SearchChanged(val query: String) : BreedListIntent
    data class BreedClicked(val breedId: String) : BreedListIntent
    data object Retry : BreedListIntent
}

data class BreedListState(
    val breeds: List<Breed> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: AppError? = null
)

sealed interface BreedListEffect {
    data class NavigateToBreed(val breedId: String) : BreedListEffect
    data class ShowMessage(val message: String) : BreedListEffect
}
```

State stays immutable from the UI's side — expose `StateFlow`, never the mutable
version. State changes go through `.update { it.copy(...) }`, not scattered field
mutations somewhere in the middle of a function.

## Folder structure

Feature-oriented, roughly:

```
app/
├── core/            (network, designsystem, model, util)
├── data/            (remote + dto, local, repository)
├── feature/
│   ├── breeds/
│   ├── breedDetail/
│   ├── discover/
│   └── favorites/
├── navigation/
└── di/
```

I don't add a layer unless it's actually doing something. An empty "manager"
class or a repository interface with one implementation and no real reason to
swap it isn't worth the indirection.

## Domain models vs DTOs

DTOs never touch Compose. `BreedDto` (the `@Serializable` API shape) gets mapped
to a `Breed` domain model before it leaves the data layer — that mapping is what
keeps the API contract from leaking upward, and it's also what makes this code
realistic to lift into a shared module someday without dragging serialization
annotations into the UI layer.

## Networking

Ktor everywhere, one shared `HttpClient` via DI. Needs ContentNegotiation +
kotlinx.serialization, HttpTimeout, Logging, and response validation turned on.
Logging should drop to `LogLevel.NONE` (or close to it) for release — and under
no circumstances should the API key end up in a log line, sanitize that header
explicitly.

```kotlin
HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }
    install(Logging) { level = LogLevel.INFO }
}
```

API surface should be a typed interface, e.g. something like:

```kotlin
interface CatApi {
    suspend fun getBreeds(): List<BreedDto>
    suspend fun searchImages(breedId: String? = null, page: Int = 0, limit: Int = 20): List<CatImageDto>
    suspend fun vote(imageId: String, value: Int)
    suspend fun addFavorite(imageId: String)
    suspend fun removeFavorite(favoriteId: String)
}
```

Check the actual current Cat API docs for endpoints/params/response shapes rather
than assuming — I don't want to guess at behavior that's one docs page away.

API key goes through `local.properties` → BuildConfig, never hardcoded, never
committed, and documented in the README so it's obvious how to set it up locally.

## Errors

Raw exceptions don't reach the UI. Everything gets mapped into:

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

with user-facing copy like "You're offline, check your connection" for Network,
"Something went wrong on our side" for Server, and so on. The UI should only ever
reason about `AppError`, never about HTTP codes or Ktor exception types directly.

## Repository

```kotlin
interface BreedRepository {
    suspend fun getBreeds(): Result<List<Breed>>
}
```

Repository owns the API call, the DTO mapping, and (optionally) caching. The
ViewModel shouldn't know Ktor exists, and definitely shouldn't know what a 503
is.

## Breeds feature (build this first)

List + search/filter + detail + loading/error/empty states + retry.
`LazyVerticalGrid` or similar. Each card: image, name, origin, maybe one more
useful detail. Detail screen: name, image, origin, description, temperament,
life span, weight — not literally every field the API returns, just what's
actually useful to someone looking at a cat breed.

## Image search / Discover

Separate from the breed list — this hits `/images/search`, optionally filtered
by breed. Grid, paginated, with its own loading/error states per page, and
guard against firing duplicate pagination requests when the user scrolls fast.
Don't try to load hundreds of images up front.

## Vote

Like/dislike, entirely through intents:

```kotlin
data class Vote(val imageId: String, val value: Int) : DiscoverIntent
```

Flow is tap → intent → ViewModel → repository → Ktor → result → new state/effect
→ recompose. No API calls from inside a Composable, ever. Needs loading, success,
and error feedback — a vote that silently fails isn't acceptable.

## Favorites

Only if there's time left after Discover + Vote are solid. Add/remove, list,
empty/loading/error states. The Discover grid should reflect favorite status
immediately after toggling it — I don't want two screens disagreeing about
whether something's favorited because they're reading from different sources of
truth.

## Offline / bad network

At minimum: real error states, a retry action, and don't nuke already-loaded
content just because a refresh failed. If breed list caching happens, keep the
persistence behind a small abstraction like:

```kotlin
interface BreedLocalDataSource {
    suspend fun getBreeds(): List<Breed>
    suspend fun saveBreeds(breeds: List<Breed>)
}
```

so the repository decides remote vs local, and it's not wired straight to
Android's DB APIs everywhere. Not trying to build a sync engine here — just
graceful degradation.

## DI

Koin, providing HttpClient, CatApi, repositories, local sources, ViewModels.
No manual instantiation inside Composables, no reaching for globals.

## Compose conventions

Screens take state + an intent callback, not a ViewModel reference directly:

```kotlin
@Composable
fun BreedListScreen(
    state: BreedListState,
    onIntent: (BreedListIntent) -> Unit
)
```

The route/container collects the ViewModel and passes state down. Keeps the
screen previewable and testable without needing a real ViewModel around.

Reusable components where there's actual repetition — BreedCard, CatImageCard,
FavoriteButton, VoteButtons, Loading/Error/Empty states, RetryButton. Not
everything needs to be extracted; a one-off Row doesn't need its own file.

Every network-backed screen should have an opinion about: loading, success,
empty, error, refreshing, and pagination-loading. Designing only for the happy
path is how you end up with a screen that just freezes on a bad connection.

## Navigation

Navigation Compose, roughly Breeds → Breed Detail, Discover → Image Detail,
Favorites → Image Detail. Keep it centralized, and trigger navigation through
MVI effects rather than passing NavController deep into feature code:

```kotlin
sealed interface BreedListEffect {
    data class NavigateToBreed(val breedId: String) : BreedListEffect
}
```

## Look and feel

Modern, clean, a bit premium, image-focused — the cat photos should carry the
visual weight, not the chrome around them. Material 3, rounded cards, consistent
spacing, a real typography hierarchy, subtle elevation. Skip heavy gradients and
animation for their own sake.

## Accessibility

~48dp touch targets, real contrast, content descriptions that actually describe
the action ("Add to favorites", "Like cat", "Retry loading breeds"), nothing
conveyed by color alone, text that scales properly. This should be baked in as
I build things, not bolted on at the end.

## Localization

No hardcoded UI strings in Composables — everything through `strings.xml`, even
if English is the only language shipped for now. The point is that adding German
later should be a translation exercise, not a refactor.

## Testing

Focus on what's actually worth testing: DTO→domain mapping, error mapping,
breed search/filter, repository behavior, pagination edge cases, and MVI state
transitions (Load → Loading → Success, Load → Loading → Error,
Retry → Loading → Success). UI tests only if there's time left over — I'd rather
ship one fewer UI test and have the feature actually work.

## Coroutines

`viewModelScope`, never `GlobalScope`, cancellation handled properly. Search
input gets debounced — no firing a network request per keystroke.

## Performance

Stable keys in lists, Coil for image loading/caching, pagination instead of
loading everything, and keeping an eye on recomposition and avoiding heavy work
directly inside Composables.

## KMP readiness, without actually doing KMP

Domain models, repositories, DTOs, the Ktor client, error mapping, and MVI state
logic are the parts that would move cleanly into a shared module someday — so I
try not to couple them to Android-specific classes where it's easy to avoid.
That said: no KMP module, no fake expect/actual scaffolding, and I won't
compromise the actual native implementation just to look portable on paper. If
it never becomes KMP, none of this should have cost anything.

## Staying lean

Explicitly avoiding: a UseCase class for every single operation, Interactors,
Managers, event buses, a custom navigation framework, a networking abstraction
sitting on top of Ktor, a generic repository framework, interfaces that exist
"just in case." The flow is UI → MVI ViewModel → Repository → Ktor, and that's
enough — I don't need a Clean Architecture template's worth of layers for an app
this size.

## Rough build order

1. Project setup — Compose, Material 3, Koin, Ktor, serialization, Navigation, MVI skeleton
2. Breeds: API, DTOs, domain models, repository, MVI, list, search, loading/error/retry, detail
3. Discover: image search, grid, pagination, vote, favorites (if time allows)
4. Polish: image caching, offline handling, accessibility, localization, unit tests

## README should cover

What the app does, the tech stack, the architecture diagram, why Ktor over
Retrofit, how errors/retry work, the offline strategy, what's tested, and — this
part matters — an honest list of what got intentionally left out and why, rather
than pretending everything's fully done.
