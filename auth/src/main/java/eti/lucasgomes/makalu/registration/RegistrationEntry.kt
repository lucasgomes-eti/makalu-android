package eti.lucasgomes.makalu.registration

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegistrationEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        val viewModel = koinViewModel<RegistrationViewModel>()
        RegistrationScreen { viewModel.goToHome() }
    }
}