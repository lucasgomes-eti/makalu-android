package eti.lucasgomes.makalu.components

import eti.lucasgomes.makalu.components.imagePreview.ui.ImagePreviewViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val componentsModule = module {
    factory { params ->
        ImagePreviewViewModel(
            navigator = get(),
            contentResolver = get(),
            cacheDir = get(),
            app = androidApplication(),
            uri = params.get()
        )
    }
}