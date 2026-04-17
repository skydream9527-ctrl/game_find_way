package com.gameway.presentation.di

import com.gameway.data.local.DataStoreManager
import com.gameway.data.repository.FriendRepositoryImpl
import com.gameway.data.repository.GameProgressRepositoryImpl
import com.gameway.data.repository.LeaderboardRepositoryImpl
import com.gameway.data.repository.MatchRepositoryImpl
import com.gameway.data.repository.SettingsRepositoryImpl
import com.gameway.data.repository.SkinRepositoryImpl
import com.gameway.domain.repository.GameProgressRepository
import com.gameway.domain.repository.SkinRepository
import com.gameway.domain.repository.LeaderboardRepository
import com.gameway.domain.repository.SettingsRepository
import com.gameway.domain.repository.FriendRepository
import com.gameway.domain.repository.MatchRepository
import com.gameway.domain.usecase.AcceptMatchUseCase
import com.gameway.domain.usecase.CreateMatchUseCase
import com.gameway.domain.usecase.GetCharacterStatsUseCase
import com.gameway.domain.usecase.GetChaptersUseCase
import com.gameway.domain.usecase.GetFriendsUseCase
import com.gameway.domain.usecase.AddFriendUseCase
import com.gameway.domain.usecase.RemoveFriendUseCase
import com.gameway.domain.usecase.GetLevelUseCase
import com.gameway.domain.usecase.GetLeaderboardUseCase
import com.gameway.domain.usecase.SaveProgressUseCase
import com.gameway.domain.usecase.SaveScoreUseCase
import com.gameway.domain.usecase.SubmitScoreUseCase
import com.gameway.domain.usecase.EquipSkinUseCase
import com.gameway.domain.usecase.PurchaseSkinUseCase
import com.gameway.domain.engine.MatchManager
import com.gameway.presentation.screens.game.GameViewModel
import com.gameway.presentation.screens.leaderboard.LeaderboardViewModel
import com.gameway.presentation.screens.friend.FriendViewModel
import com.gameway.presentation.screens.match.MatchViewModel
import com.gameway.presentation.screens.skinstore.SkinStoreViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { DataStoreManager(get()) }
    single<GameProgressRepository> { GameProgressRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<LeaderboardRepository> { LeaderboardRepositoryImpl(get()) }
    factory<FriendRepository> { FriendRepositoryImpl(get()) }
    factory<MatchRepository> { MatchRepositoryImpl(get()) }
    factory<SkinRepository> { SkinRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetChaptersUseCase() }
    factory { GetLevelUseCase() }
    factory { SaveProgressUseCase(get()) }
    factory { GetCharacterStatsUseCase(get()) }
    factory { SaveScoreUseCase(get()) }
    factory { GetFriendsUseCase(get()) }
    factory { AddFriendUseCase(get()) }
    factory { RemoveFriendUseCase(get()) }
    factory { GetLeaderboardUseCase(get()) }
    factory { MatchManager(get()) }
    factory { CreateMatchUseCase(get()) }
    factory { AcceptMatchUseCase(get()) }
    factory { SubmitScoreUseCase(get()) }
    factory { PurchaseSkinUseCase(get()) }
    factory { EquipSkinUseCase(get()) }
    factory { SkinStoreManager(get()) }
}

val presentationModule = module {
    viewModel { GameViewModel(get(), get(), get(), GameEngine(get())) }
    factory { LeaderboardViewModel(get()) }
    factory { FriendViewModel(get(), get(), get(), get()) }
    factory { MatchViewModel(get(), get(), get(), get(), get()) }
    factory { SkinStoreViewModel(get(), get(), get()) }
}