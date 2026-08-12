package com.example.catsexxeta.di

import com.example.catsexxeta.feature.breedDetail.BreedDetailViewModel
import com.example.catsexxeta.feature.breeds.BreedListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { BreedListViewModel(get()) }
    viewModel { (breedId: String) -> BreedDetailViewModel(get(), breedId) }
}
