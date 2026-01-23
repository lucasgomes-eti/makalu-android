package eti.lucasgomes.makalu.di

import android.content.ContentResolver
import android.util.Log
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.navigation.OSNavigation
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<File> { androidApplication().cacheDir }
    single<ContentResolver> { androidApplication().contentResolver }
    single<MkLogger> {
        object : MkLogger {
            override fun logDebug(tag: String, message: String) {
                Log.d(tag, message)
            }
        }
    }
    single<OSNavigation> {
        object : OSNavigation {
            override fun openApplicationSettings() {
                androidApplication().openApplicationSettings()
            }
        }
    }
}