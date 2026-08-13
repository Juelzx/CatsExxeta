# Cat Explorer

Small Android app built on top of The Cat API. Browse cat breeds, search by name, and look at breed details (origin, temperament, life span, weight, description).

## Stack

- Kotlin, Jetpack Compose, Material 3
- Ktor Client for networking
- kotlinx.serialization
- Koin for DI
- Coroutines / StateFlow
- Navigation Compose
- Coil for images

## Architecture

MVI, one direction:

```
Compose UI → Intent → ViewModel → Repository → Ktor → Cat API
```

The UI only ever sends intents and renders state, nothing else. ViewModels don't know Ktor exists — repositories handle the API call, map the DTOs into domain models, and hand back a `Result`. Errors get translated into a small `AppError` type (Network, Timeout, Unauthorized, NotFound, Server, Unknown) so the UI is never dealing with raw exceptions or HTTP codes directly.

## Why Ktor instead of Retrofit

Mainly to keep the networking layer Kotlin-first instead of tied to annotation-processing/OkHttp-Android specifics. Not planning to make this a KMP module right now, but if that ever happens the networking and domain code should port over with minimal rework.

## What's implemented

- Breed list with search/filter
- Breed detail screen
- Loading / error / empty states with retry
- Unit tests for the mapper, breed search, and the list ViewModel

## What's not (yet)

Image search (Discover), voting, and favorites aren't built yet. Given the time frame I focused on getting the breeds feature done properly — search, detail, error handling, tests — rather than spreading the same time across four half-finished features. Happy to walk through how I'd approach the rest.

## Running it

You need a Cat API key (free, get one at thecatapi.com). Add it to `local.properties`:

```
CAT_API_KEY=your_key_here
```

Then build normally from Android Studio.
