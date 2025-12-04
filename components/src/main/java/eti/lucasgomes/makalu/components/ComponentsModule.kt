package eti.lucasgomes.makalu.components

import eti.lucasgomes.makalu.components.imagePreview.ui.ImagePreviewViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val componentsModule = module {
    factoryOf(::ImagePreviewViewModel)
}