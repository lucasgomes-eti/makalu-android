package eti.lucasgomes.makalu.components

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraCaptureManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val providedUri = Uri.parse("content://eti.lucasgomes.makalu.provider/cache/photo.jpg")
    private val providedFile = slot<File>()

    private fun manager() = CameraCaptureManager(tempFolder.root, app)

    private fun stubFileProvider() {
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), capture(providedFile)) } returns
            providedUri
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `image uri is null before capturing`() {
        assertNull(manager().imageUri)
    }

    @Test
    fun `capture creates a jpg temp file with the prefix in the cache dir`() = runTest {
        stubFileProvider()

        manager().capture("prefix_") {}

        val file = providedFile.captured
        assertEquals(tempFolder.root, file.parentFile)
        assertTrue(file.exists())
        assertTrue(file.name.startsWith("prefix_"))
        assertTrue(file.name.endsWith(".jpg"))
    }

    @Test
    fun `capture requests the uri from the app file provider authority`() = runTest {
        stubFileProvider()

        manager().capture("prefix_") {}

        verify { FileProvider.getUriForFile(app, "eti.lucasgomes.makalu.provider", any<File>()) }
    }

    @Test
    fun `capture stores the uri and launches the camera with it`() = runTest {
        stubFileProvider()
        val launched = mutableListOf<Uri>()
        val manager = manager()

        manager.capture("prefix_") { launched += it }

        assertEquals(listOf(providedUri), launched)
        assertEquals(providedUri, manager.imageUri)
    }

    @Test
    fun `capture fails without launching the camera when the uri cannot be created`() = runTest {
        val launched = mutableListOf<Uri>()
        val manager = manager()

        try {
            manager.capture("prefix_") { launched += it }
            fail("Expected capture to throw")
        } catch (expected: IllegalArgumentException) {
        }

        assertTrue(launched.isEmpty())
        assertNull(manager.imageUri)
    }
}
