package com.example.catsexxeta.core.model

sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data object Server : AppError
    data object Unknown : AppError
}
