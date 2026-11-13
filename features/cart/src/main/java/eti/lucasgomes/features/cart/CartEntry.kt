package eti.lucasgomes.features.cart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.features.cart.ui.CartAction
import eti.lucasgomes.features.cart.ui.CartScreen
import eti.lucasgomes.features.cart.ui.CartViewModel
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CartEntry(innerPadding: PaddingValues, storeId: Long) {
    val viewModel = koinViewModel<CartViewModel> { parametersOf(storeId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Placing an order is when notifications start to matter, so that is when the
    // permission is asked for. The order goes ahead whatever the answer: without the
    // permission the order is still tracked and the order screen still updates.
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onAction(CartAction.MakeOrderClicked)
    }

    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        CartScreen(uiState) { action ->
            val mustAsk = action == CartAction.MakeOrderClicked &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            if (mustAsk) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onAction(action)
            }
        }
    }
}
