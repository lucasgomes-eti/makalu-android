package eti.lucasgomes.makalu

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MakaluApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MakaluApplication)
            modules(appModule, sharedModule, authModule, homeModule)
        }
    }
}