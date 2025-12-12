package eti.lucasgomes.makalu.shared.navigation

sealed interface RequestKey {
    data object ImageCroppedUriOutput : RequestKey
}