package eti.lucasgomes.makalu.ui

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
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val dataStore by inject<DataStore<Settings>>()
    private val hasAccessToken = MutableStateFlow<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInitialData()
        enableEdgeToEdge()
        setContent {
            val hasAccessToken by hasAccessToken.collectAsState()
            hasAccessToken?.let { MainComposable(it) }
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
}
