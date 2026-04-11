package com.gameway

import android.app.Application
import com.gameway.presentation.di.dataModule
import com.gameway.presentation.di.domainModule
import com.gameway.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FindWayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@FindWayApp)
            modules(dataModule, domainModule, presentationModule)
        }
    }
}