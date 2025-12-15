package eti.lucasgomes.makalu.components

import eti.lucasgomes.makalu.components.imageCropper.ui.ImageCropperViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val componentsModule = module {
    factory { params ->
        ImageCropperViewModel(
            navigator = get(),
            contentResolver = get(),
            cacheDir = get(),
            app = androidApplication(),
            cameraCaptureManager = get(),
            uriStr = params.get()
        )
    }

    factoryOf(::CameraCaptureManager)
}