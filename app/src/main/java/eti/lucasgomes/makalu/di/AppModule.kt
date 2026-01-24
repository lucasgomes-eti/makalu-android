package eti.lucasgomes.makalu.di

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.navigation.OSNavigation
import eti.lucasgomes.makalu.shared.settings.Settings
import eti.lucasgomes.makalu.shared.settings.SettingsSerializer
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.io.File

private val Context.dataStore: DataStore<Settings> by dataStore(
    fileName = "settings.json",
    serializer = SettingsSerializer,
)

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
    single<DataStore<Settings>> {
        androidApplication().dataStore
    }
}