package eti.lucasgomes.adress.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import eti.lucasgomes.adress.AddressClient
import eti.lucasgomes.adress.model.AddressRequest
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.adress.ui.model.AddressUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.adress.R
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddressViewModel(
    private val fusedClient: FusedLocationProviderClient,
    private val app: Application,
    private val addressClient: AddressClient,
    private val navigator: Navigator,
    private val dataStore: DataStore<Settings>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    private var addressId: Long? = null

    fun onAction(action: AddressAction) {
        when (action) {
            AddressAction.InitialFetch -> onInitialFetch()
            is AddressAction.ZipCodeChanged -> onZipCodeChanged(action.text)
            is AddressAction.StreetChanged -> onStreetChanged(action.text)
            is AddressAction.NumberChanged -> onNumberChanged(action.text)
            is AddressAction.ComplementChanged -> onComplementChanged(action.text)
            AddressAction.SaveAddressClicked -> onSaveAddressClicked()
            is AddressAction.GetCurrentLocationClicked -> onGetCurrentLocationClicked(action.permissionGranted)
            AddressAction.PermissionDeniedDismissed -> onPermissionDeniedDismissed()
            AddressAction.GoToSystemSettingsClicked -> onGoToSystemSettingsClicked()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true, isLocationLoading = true) }
        addressClient.getSelfAddress().onError { error ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isLocationLoading = false,
                    generalError = if (error.httpCode == 404) UiText.Empty else UiText.PlainText(
                        error.formatedMessage
                    )
                )
            }
        }.onSuccess { response ->
            addressId = response.id
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isLocationLoading = false,
                    location = LatLng(response.latitude, response.longitude),
                    zipCode = state.zipCode.copy(text = response.zipCode),
                    street = state.street.copy(text = response.street),
                    number = state.number.copy(text = response.number ?: ""),
                    complement = state.complement.copy(text = response.complement ?: "")
                )
            }
        }
    }

    private fun onZipCodeChanged(text: String) = withViewModelScope {
        if (text.length > MAX_ZIP_CODE_LENGTH)
            return@withViewModelScope
        _uiState.update { state ->
            state.copy(
                zipCode = state.zipCode.copy(
                    text = text,
                    error = UiText.Empty
                )
            )
        }
    }

    private fun onStreetChanged(text: String) = withViewModelScope {
        _uiState.update { state ->
            state.copy(
                street = state.street.copy(
                    text = text,
                    error = UiText.Empty
                )
            )
        }
    }

    private fun onNumberChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(number = state.number.copy(text = text)) }
    }

    private fun onComplementChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(complement = state.complement.copy(text = text)) }
    }

    private fun onSaveAddressClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        withValidUiState { request ->
            addressId?.let { id ->
                updateAddress(request, id)
            } ?: saveAddress(request)
        }
    }

    private suspend fun saveAddress(request: AddressRequest) {
        addressClient.saveAddress(request).onError {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    generalError = UiText.PlainText(it.formatedMessage)
                )
            }
        }.onSuccess { response ->
            dataStore.updateData { settings ->
                settings.copy(
                    addressId = response.id,
                    addressName = "${response.street}, ${response.number}"
                )
            }
            _uiState.update { state -> state.copy(isLoading = false) }
            navigator.navigateUp()
        }
    }

    private suspend fun updateAddress(request: AddressRequest, addressId: Long) {
        addressClient.updateAddress(request, addressId).onError {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    generalError = UiText.PlainText(it.formatedMessage)
                )
            }
        }.onSuccess { response ->
            dataStore.updateData { settings ->
                settings.copy(
                    addressName = "${request.street}, ${request.number}"
                )
            }
            _uiState.update { state -> state.copy(isLoading = false) }
            navigator.navigateUp()
        }
    }

    private suspend fun withValidUiState(block: suspend (AddressRequest) -> Unit) {
        val state = uiState.value
        val isStateValid =
            isZipCodeValid(state.zipCode.text) and isStreetValid(state.street.text) and isLocationValid(
                state.location
            )

        if (isStateValid) {
            block(buildAddressRequest(state))
        } else {
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }

    private fun isZipCodeValid(zipCode: String): Boolean {
        if (zipCode.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    zipCode = state.zipCode.copy(error = UiText.StringResource(R.string.zip_code_is_required))
                )
            }
            return false
        }
        return true
    }

    private fun isStreetValid(street: String): Boolean {
        if (street.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    street = state.street.copy(error = UiText.StringResource(R.string.street_name_is_required))
                )
            }
            return false
        }
        return true
    }

    private fun isLocationValid(location: LatLng): Boolean {
        if (location.latitude == .0 || location.longitude == .0) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    locationError = UiText.StringResource(R.string.location_is_required)
                )
            }
            return false
        }
        return true
    }

    private fun buildAddressRequest(state: AddressUiState): AddressRequest = state.run {
        AddressRequest(
            zipCode = zipCode.text,
            street = street.text,
            number = number.text,
            complement = complement.text,
            longitude = location.longitude,
            latitude = location.latitude
        )
    }

    @SuppressLint("MissingPermission")
    private fun onGetCurrentLocationClicked(permissionGranted: Boolean) = withViewModelScope {
        if (permissionGranted) {
            _uiState.update { state ->
                state.copy(
                    isLocationLoading = true,
                    locationError = UiText.Empty
                )
            }
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                _uiState.update { state ->
                    state.copy(
                        location = LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                }
                reverseGeocodeLocation(location.latitude, location.longitude)
            }.addOnFailureListener {
                _uiState.update { state ->
                    state.copy(
                        locationError = UiText.PlainText("Error getting location, location might be disabled or unavailable")
                    )
                }
            }.addOnCompleteListener {
                _uiState.update { state -> state.copy(isLocationLoading = false) }
            }
        } else {
            _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = true) }
        }
    }

    private fun reverseGeocodeLocation(latitude: Double, longitude: Double) = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        val response = addressClient.reverseGeocode(latitude, longitude)
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                zipCode = state.zipCode.copy(text = response.zipCode),
                street = state.street.copy(text = response.street),
                number = state.number.copy(text = response.number),
                complement = state.complement.copy(text = response.complement)
            )
        }
    }

    private fun onPermissionDeniedDismissed() = withViewModelScope {
        _uiState.update { state ->
            state.copy(isPermissionDeniedDialogVisible = false)
        }
    }

    private fun onGoToSystemSettingsClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = false) }
        app.openApplicationSettings()
    }

    companion object {
        private const val MAX_ZIP_CODE_LENGTH = 8
    }
}
