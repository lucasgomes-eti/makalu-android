package eti.lucasgomes.makalu

import android.app.Application
import eti.lucasgomes.adress.di.addressModule
import eti.lucasgomes.features.store.di.storeModule
import eti.lucasgomes.makalu.components.componentsModule
import eti.lucasgomes.makalu.di.appModule
import eti.lucasgomes.makalu.features.auth.di.authModule
import eti.lucasgomes.makalu.features.home.di.homeModule
import eti.lucasgomes.makalu.features.orders.ordersModule
import eti.lucasgomes.makalu.features.profile.profileModule
import eti.lucasgomes.makalu.shared.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MakaluApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MakaluApplication)
            modules(
                appModule,
                sharedModule,
                componentsModule,
                authModule,
                homeModule,
                profileModule,
                ordersModule,
                addressModule,
                storeModule
            )
        }
    }
}