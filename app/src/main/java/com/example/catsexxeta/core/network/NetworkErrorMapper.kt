package com.example.catsexxeta.core.network

import com.example.catsexxeta.core.model.AppError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toAppError(): AppError = when (this) {
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> AppError.Timeout

    is ClientRequestException -> when (response.status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> AppError.Unauthorized
        HttpStatusCode.NotFound -> AppError.NotFound
        else -> AppError.Unknown
    }

    is ServerResponseException -> AppError.Server

    is IOException -> AppError.Network

    else -> AppError.Unknown
}
