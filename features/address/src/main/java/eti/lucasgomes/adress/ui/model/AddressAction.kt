package eti.lucasgomes.adress.ui.model

sealed interface AddressAction {
    data class ZipCodeChanged(val text: String) : AddressAction

    data class StreetChanged(val text: String) : AddressAction

    data class NumberChanged(val text: String) : AddressAction

    data class ComplementChanged(val text: String) : AddressAction

    data object SaveAddressClicked : AddressAction

    data class GetCurrentLocationClicked(val permissionGranted: Boolean) : AddressAction

    data object PermissionDeniedDismissed : AddressAction

    data object PermissionRationaleRequested : AddressAction

    data object PermissionRationaleDismissed : AddressAction

    data object GoToSystemSettingsClicked : AddressAction

    data object LocationSettingsPromptShown : AddressAction

    data class LocationSettingsResult(val enabled: Boolean) : AddressAction
}
