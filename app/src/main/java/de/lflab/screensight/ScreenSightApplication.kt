package de.lflab.screensight

import android.app.Application
import de.lflab.screensight.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class ScreenSightApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ScreenSightApplication)
            modules(appModule)
        }
    }
}