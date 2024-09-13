package de.lflab.screensight.di

import de.lflab.screensight.BakingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { BakingViewModel() }
}