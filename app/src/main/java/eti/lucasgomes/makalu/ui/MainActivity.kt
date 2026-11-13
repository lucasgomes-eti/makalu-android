package eti.lucasgomes.makalu.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.settings.Settings
import eti.lucasgomes.makalu.shared.tracking.OrderTrackingLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val dataStore by inject<DataStore<Settings>>()
    private val navigator by inject<Navigator>()
    private val orderTracking by inject<OrderTrackingLauncher>()
    private val hasAccessToken = MutableStateFlow<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInitialData()
        openOrderFrom(intent)
        enableEdgeToEdge()
        setContent {
            val hasAccessToken by hasAccessToken.collectAsState()
            hasAccessToken?.let { MainComposable(it) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openOrderFrom(intent)
    }

    override fun onStart() {
        super.onStart()
        // The app is on screen: the one moment Android lets the tracking service start.
        lifecycleScope.launch {
            if (hasAccessToken.filterNotNull().first()) orderTracking.onAppOpened()
        }
    }

    /** Opens the order a tracking notification was about. */
    private fun openOrderFrom(intent: Intent?) {
        val orderId = intent
            ?.getLongExtra(OrderTrackingLauncher.EXTRA_ORDER_ID, NO_ORDER)
            ?.takeIf { it != NO_ORDER }
            ?: return
        // Consumed, so a configuration change does not open the order a second time.
        intent.removeExtra(OrderTrackingLauncher.EXTRA_ORDER_ID)
        lifecycleScope.launch {
            if (hasAccessToken.filterNotNull().first()) {
                navigator.navigate(Destination.Screen.OderDetail(orderId))
            }
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            hasAccessToken.value = runCatching {
                dataStore.data.first().accessToken.isNullOrBlank().not()
            }.getOrDefault(false)
        }

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check whether the initial data is ready.
                    return if (hasAccessToken.value != null) {
                        // The content is ready. Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content isn't ready. Suspend.
                        false
                    }
                }
            }
        )
    }

    private companion object {
        const val NO_ORDER = -1L
    }
}
