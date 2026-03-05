package eti.lucasgomes.adress.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.adress.ui.model.AddressUiState
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.features.adress.R

@Composable
internal fun AddressScreen(uiState: AddressUiState, onAction: (AddressAction) -> Unit) {
    ConfigureTopBar(stringResource(R.string.delivery_address))
    Column {
        Text("Address")
    }
}