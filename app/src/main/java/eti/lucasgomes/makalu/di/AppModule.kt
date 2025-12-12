package eti.lucasgomes.makalu.di

import android.content.ContentResolver
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<File> { androidApplication().cacheDir }
    single<ContentResolver> { androidApplication().contentResolver }
}