package eti.lucasgomes.makalu.di

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.navigation.OSNavigation
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppModuleTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        startKoin {
            androidContext(app)
            modules(appModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `logger writes debug log in debug builds`() {
        val logger = GlobalContext.get().get<MkLogger>()

        logger.logDebug("AppModuleTest", "msg")

        val logs = ShadowLog.getLogsForTag("AppModuleTest")
        assertEquals(1, logs.size)
        assertEquals(Log.DEBUG, logs.single().type)
        assertEquals("msg", logs.single().msg)
    }

    @Test
    fun `os navigation opens application details settings for this package`() {
        val osNavigation = GlobalContext.get().get<OSNavigation>()

        osNavigation.openApplicationSettings()

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.parse("package:eti.lucasgomes.makalu"), intent.data)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `cache dir and content resolver come from the application`() {
        val koin = GlobalContext.get()

        val cacheDir = koin.get<File>()
        val contentResolver = koin.get<ContentResolver>()

        assertEquals(app.cacheDir, cacheDir)
        assertSame(app.contentResolver, contentResolver)
    }
}
