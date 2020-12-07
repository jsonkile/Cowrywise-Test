package com.golde.cowrywise

import android.app.Application
import com.golde.cowrywise.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ThisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Start Koin Dependency Injection
        startKoin{
            androidLogger()
            androidContext(this@ThisApplication)
            modules(appModule)
        }
    }
}