package eti.lucasgomes.makalu.components.ext

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class ApplicationExtTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `opens the application details settings for this package`() {
        app.openApplicationSettings()

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.fromParts("package", app.packageName, null), intent.data)
    }

    @Test
    fun `starts the settings activity in a new task`() {
        app.openApplicationSettings()

        val intent = shadowOf(app).nextStartedActivity
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }
}
