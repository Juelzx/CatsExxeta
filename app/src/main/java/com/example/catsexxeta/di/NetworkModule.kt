package com.example.catsexxeta.di

import com.example.catsexxeta.BuildConfig
import com.example.catsexxeta.core.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        createHttpClient(
            apiKey = BuildConfig.CAT_API_KEY,
            enableLogging = BuildConfig.DEBUG
        )
    }
}
