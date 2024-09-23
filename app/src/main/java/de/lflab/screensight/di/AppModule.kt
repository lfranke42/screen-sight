package de.lflab.screensight.di

import de.lflab.screensight.network.GenerativeAiRepository
import de.lflab.screensight.network.GenerativeAiRepositoryImpl
import org.koin.dsl.module

val appModule = module {
    single<GenerativeAiRepository> { GenerativeAiRepositoryImpl() }
}