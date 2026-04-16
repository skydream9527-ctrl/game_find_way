package com.gameway.presentation.di

import com.gameway.data.local.DataStoreManager
import com.gameway.data.repository.GameProgressRepositoryImpl
import com.gameway.data.repository.LeaderboardRepositoryImpl
import com.gameway.data.repository.SettingsRepositoryImpl
import com.gameway.domain.repository.GameProgressRepository
import com.gameway.domain.repository.LeaderboardRepository
import com.gameway.domain.repository.SettingsRepository
import com.gameway.domain.usecase.GetCharacterStatsUseCase
import com.gameway.domain.usecase.GetChaptersUseCase
import com.gameway.domain.usecase.GetLevelUseCase
import com.gameway.domain.usecase.SaveProgressUseCase
import com.gameway.domain.usecase.SaveScoreUseCase
import com.gameway.presentation.screens.game.GameViewModel
import com.gameway.presentation.screens.leaderboard.LeaderboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { DataStoreManager(get()) }
    single<GameProgressRepository> { GameProgressRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<LeaderboardRepository> { LeaderboardRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetChaptersUseCase() }
    factory { GetLevelUseCase() }
    factory { SaveProgressUseCase(get()) }
    factory { GetCharacterStatsUseCase(get()) }
    factory { SaveScoreUseCase(get()) }
}

val presentationModule = module {
    viewModel { GameViewModel(get(), get(), get(), GameEngine(get())) }
    factory { LeaderboardViewModel(get()) }
}