package com.example.catsexxeta.di

import com.example.catsexxeta.data.remote.CatApi
import com.example.catsexxeta.data.remote.KtorCatApi
import com.example.catsexxeta.data.repository.BreedRepository
import com.example.catsexxeta.data.repository.BreedRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    single<CatApi> { KtorCatApi(get()) }
    single<BreedRepository> { BreedRepositoryImpl(get()) }
}
