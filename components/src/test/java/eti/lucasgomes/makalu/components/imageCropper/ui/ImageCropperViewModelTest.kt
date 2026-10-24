package eti.lucasgomes.makalu.components.imageCropper.ui

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.R
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperAction
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperUiState
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

@RunWith(AndroidJUnit4::class)
class ImageCropperViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = mockk<ContentResolver>()
    private val navigator = mockk<Navigator>(relaxed = true)
    private val cameraCaptureManager = mockk<CameraCaptureManager>(relaxed = true)
    private val decoder = mockk<ImageDecoder>(relaxed = true)

    private val initialUri = "content://images/initial"
    private val initialBitmap = bitmap()
    private val images = ConcurrentHashMap<Uri, Pair<Bitmap, Size>>()
    private val sourceUris = ConcurrentHashMap<ImageDecoder.Source, Uri>()

    private val outputUri = Uri.parse("content://eti.lucasgomes.makalu.provider/cache/cropped.jpg")
    private val outputFile = slot<File>()

    @Before
    fun setUp() {
        mockkStatic(ImageDecoder::class)
        every { ImageDecoder.createSource(any<ContentResolver>(), any<Uri>()) } answers {
            mockk<ImageDecoder.Source>().also { sourceUris[it] = secondArg() }
        }
        every { ImageDecoder.decodeBitmap(any(), any<ImageDecoder.OnHeaderDecodedListener>()) } answers {
            val source = firstArg<ImageDecoder.Source>()
            val (bitmap, size) = images[sourceUris[source]] ?: throw IOException("Cannot decode")
            val info = mockk<ImageDecoder.ImageInfo> { every { this@mockk.size } returns size }
            secondArg<ImageDecoder.OnHeaderDecodedListener>().onHeaderDecoded(decoder, info, source)
            bitmap
        }
        registerImage(initialUri, initialBitmap)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun bitmap(): Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    private fun registerImage(uri: String, bitmap: Bitmap, size: Size = Size(100, 100)) {
        images[Uri.parse(uri)] = bitmap to size
    }

    private fun stubFileProvider() {
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), capture(outputFile)) } returns outputUri
    }

    private fun croppedBitmap(bytesForQuality: (Int) -> Int, qualities: MutableList<Int> = mutableListOf()) =
        mockk<Bitmap> {
            every { compress(Bitmap.CompressFormat.JPEG, any(), any()) } answers {
                val quality = secondArg<Int>()
                qualities += quality
                thirdArg<OutputStream>().write(ByteArray(bytesForQuality(quality)) { quality.toByte() })
                true
            }
        }

    private fun viewModel(uri: String = initialUri) = ImageCropperViewModel(
        navigator = navigator,
        contentResolver = contentResolver,
        cacheDir = tempFolder.root,
        app = app,
        cameraCaptureManager = cameraCaptureManager,
        uriStr = uri
    )

    private suspend fun ImageCropperViewModel.awaitState(predicate: (ImageCropperUiState) -> Boolean) =
        withContext(Dispatchers.Default) { withTimeout(5_000) { uiState.first(predicate) } }

    private suspend fun ImageCropperViewModel.awaitLoaded() = awaitState { it.bitmap != null }

    private suspend fun awaitNavigatedUpWith(result: String) = withContext(Dispatchers.Default) {
        coVerify(timeout = 5_000) { navigator.navigateUpWithResult(RequestKey.ImageCroppedUriOutput, result) }
    }

    @Test
    fun `init loads the bitmap from the given uri`() = runTest {
        val state = viewModel().awaitLoaded()

        assertSame(initialBitmap, state.bitmap)
        assertEquals(UiText.Empty, state.error)
        verify { ImageDecoder.createSource(contentResolver, Uri.parse(initialUri)) }
    }

    @Test
    fun `init sets the load error when the image cannot be decoded`() = runTest {
        val state = viewModel("content://missing/image").awaitState { it.error != UiText.Empty }

        assertNull(state.bitmap)
        assertEquals(UiText.StringResource(R.string.image_load_error), state.error)
    }

    @Test
    fun `init downscales images whose long edge exceeds 2048 px`() = runTest {
        registerImage(initialUri, initialBitmap, Size(4096, 1024))

        viewModel().awaitLoaded()

        verify { decoder.setTargetSize(2048, 512) }
    }

    @Test
    fun `init downscales portrait images by their height`() = runTest {
        registerImage(initialUri, initialBitmap, Size(1000, 4000))

        viewModel().awaitLoaded()

        verify { decoder.setTargetSize(512, 2048) }
    }

    @Test
    fun `init keeps the size of images up to 2048 px`() = runTest {
        registerImage(initialUri, initialBitmap, Size(2048, 1536))

        viewModel().awaitLoaded()

        verify(exactly = 0) { decoder.setTargetSize(any(), any()) }
    }

    @Test
    fun `init never scales a side below one pixel`() = runTest {
        registerImage(initialUri, initialBitmap, Size(10000, 1))

        viewModel().awaitLoaded()

        verify { decoder.setTargetSize(2048, 1) }
    }

    @Test
    fun `add image click shows the image picker`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ImageCropperAction.AddImageClicked)

        assertTrue(viewModel.uiState.value.isImagePickerVisible)
    }

    @Test
    fun `image picker dismissal hides the image picker`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.ImagePickerDismissed)

        assertFalse(viewModel.uiState.value.isImagePickerVisible)
    }

    @Test
    fun `gallery result without uri only hides the picker`() = runTest {
        val viewModel = viewModel()
        viewModel.awaitLoaded()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.GalleryImageObtained(null))

        val state = viewModel.uiState.value
        assertFalse(state.isImagePickerVisible)
        assertSame(initialBitmap, state.bitmap)
        verify(exactly = 1) { ImageDecoder.createSource(any<ContentResolver>(), any<Uri>()) }
    }

    @Test
    fun `gallery image replaces the bitmap and hides the picker`() = runTest {
        val galleryBitmap = bitmap()
        registerImage("content://images/gallery", galleryBitmap)
        val viewModel = viewModel()
        viewModel.awaitLoaded()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.GalleryImageObtained(Uri.parse("content://images/gallery")))

        val state = viewModel.awaitState { it.bitmap === galleryBitmap }
        assertFalse(state.isImagePickerVisible)
        assertEquals(UiText.Empty, state.error)
    }

    @Test
    fun `gallery image that cannot be decoded keeps the previous bitmap and sets the load error`() = runTest {
        val viewModel = viewModel()
        viewModel.awaitLoaded()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.GalleryImageObtained(Uri.parse("content://missing/image")))

        val state = viewModel.awaitState { it.error != UiText.Empty }
        assertEquals(UiText.StringResource(R.string.image_load_error), state.error)
        assertSame(initialBitmap, state.bitmap)
        assertFalse(state.isImagePickerVisible)
    }

    @Test
    fun `successful image load clears a previous load error`() = runTest {
        val viewModel = viewModel("content://missing/image")
        viewModel.awaitState { it.error != UiText.Empty }

        viewModel.onAction(ImageCropperAction.GalleryImageObtained(Uri.parse(initialUri)))

        val state = viewModel.awaitLoaded()
        assertEquals(UiText.Empty, state.error)
    }

    @Test
    fun `camera result not saved keeps the picker open and loads nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.awaitLoaded()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.CameraImageTaken(isImageSaved = false))

        val state = viewModel.uiState.value
        assertTrue(state.isImagePickerVisible)
        assertSame(initialBitmap, state.bitmap)
        verify(exactly = 0) { cameraCaptureManager.imageUri }
    }

    @Test
    fun `saved camera image hides the picker and loads the captured uri`() = runTest {
        val cameraBitmap = bitmap()
        registerImage("content://images/camera", cameraBitmap)
        every { cameraCaptureManager.imageUri } returns Uri.parse("content://images/camera")
        val viewModel = viewModel()
        viewModel.awaitLoaded()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.CameraImageTaken(isImageSaved = true))

        val state = viewModel.awaitState { it.bitmap === cameraBitmap }
        assertFalse(state.isImagePickerVisible)
    }

    @Test
    fun `saved camera image without a captured uri only hides the picker`() = runTest {
        every { cameraCaptureManager.imageUri } returns null
        val viewModel = viewModel()
        viewModel.awaitLoaded()
        viewModel.onAction(ImageCropperAction.AddImageClicked)

        viewModel.onAction(ImageCropperAction.CameraImageTaken(isImageSaved = true))

        val state = viewModel.uiState.value
        assertFalse(state.isImagePickerVisible)
        assertSame(initialBitmap, state.bitmap)
        verify(exactly = 1) { ImageDecoder.createSource(any<ContentResolver>(), any<Uri>()) }
    }

    @Test
    fun `granted camera permission captures with the cropper prefix and launches the camera`() = runTest {
        val cameraUri = Uri.parse("content://eti.lucasgomes.makalu.provider/cache/camera.jpg")
        coEvery { cameraCaptureManager.capture(any(), any()) } coAnswers {
            secondArg<(Uri) -> Unit>().invoke(cameraUri)
        }
        val launched = mutableListOf<Uri>()
        val viewModel = viewModel()

        viewModel.onAction(ImageCropperAction.PermissionLauncherResultReceived(true) { launched += it })

        coVerify { cameraCaptureManager.capture("cropped_profile_pic_", any()) }
        assertEquals(listOf(cameraUri), launched)
        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
    }

    @Test
    fun `denied camera permission shows the permission dialog without capturing`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ImageCropperAction.PermissionLauncherResultReceived(false) {})

        assertTrue(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        coVerify(exactly = 0) { cameraCaptureManager.capture(any(), any()) }
    }

    @Test
    fun `permission dialog dismissal hides the dialog`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ImageCropperAction.PermissionLauncherResultReceived(false) {})

        viewModel.onAction(ImageCropperAction.PermissionDeniedDialogDismissed)

        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
    }

    @Test
    fun `go to system settings hides the dialog and opens the app settings`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ImageCropperAction.PermissionLauncherResultReceived(false) {})

        viewModel.onAction(ImageCropperAction.GoToSystemSettingsClicked)

        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        val intent = shadowOf(app).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.fromParts("package", app.packageName, null), intent.data)
    }

    @Test
    fun `error dismissal clears the error`() = runTest {
        val viewModel = viewModel("content://missing/image")
        viewModel.awaitState { it.error != UiText.Empty }

        viewModel.onAction(ImageCropperAction.ErrorDismissed)

        assertEquals(UiText.Empty, viewModel.uiState.value.error)
    }

    @Test
    fun `cropped image is saved and returned to the previous screen`() = runTest {
        stubFileProvider()
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(croppedBitmap({ 10 })))

        awaitNavigatedUpWith(outputUri.toString())
        verify { FileProvider.getUriForFile(app, "eti.lucasgomes.makalu.provider", any<File>()) }
    }

    @Test
    fun `cropped image is written as a jpg temp file in the cache dir`() = runTest {
        stubFileProvider()
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(croppedBitmap({ 10 })))

        awaitNavigatedUpWith(outputUri.toString())
        val file = outputFile.captured
        assertEquals(tempFolder.root, file.parentFile)
        assertTrue(file.name.startsWith("cropped_profile_pic_"))
        assertTrue(file.name.endsWith(".jpg"))
        assertArrayEquals(ByteArray(10) { 100.toByte() }, file.readBytes())
    }

    @Test
    fun `cropped image under the size limit is compressed once at full quality`() = runTest {
        stubFileProvider()
        val qualities = mutableListOf<Int>()
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(croppedBitmap({ MAX_OUTPUT_BYTES }, qualities)))

        awaitNavigatedUpWith(outputUri.toString())
        assertEquals(listOf(100), qualities)
    }

    @Test
    fun `oversized cropped image is recompressed with lower quality until it fits`() = runTest {
        stubFileProvider()
        val qualities = mutableListOf<Int>()
        val bytesForQuality = { quality: Int -> if (quality > 80) MAX_OUTPUT_BYTES + 1 else 100 }
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(croppedBitmap(bytesForQuality, qualities)))

        awaitNavigatedUpWith(outputUri.toString())
        assertEquals(listOf(100, 90, 80), qualities)
        assertArrayEquals(ByteArray(100) { 80.toByte() }, outputFile.captured.readBytes())
    }

    @Test
    fun `cropped image stops lowering quality at 60 even if still oversized`() = runTest {
        stubFileProvider()
        val qualities = mutableListOf<Int>()
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(croppedBitmap({ MAX_OUTPUT_BYTES + 1 }, qualities)))

        awaitNavigatedUpWith(outputUri.toString())
        assertEquals(listOf(100, 90, 80, 70, 60), qualities)
        assertArrayEquals(ByteArray(MAX_OUTPUT_BYTES + 1) { 60.toByte() }, outputFile.captured.readBytes())
    }

    @Test
    fun `cropped image save failure sets the save error without navigating`() = runTest {
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(croppedBitmap({ 10 })))

        val state = viewModel.awaitState { it.error != UiText.Empty }
        assertEquals(UiText.StringResource(R.string.image_save_error), state.error)
        coVerify(exactly = 0) { navigator.navigateUpWithResult(any(), any<Any>()) }
    }

    @Test
    fun `cropped image compression failure sets the save error`() = runTest {
        stubFileProvider()
        val failingBitmap = mockk<Bitmap> {
            every { compress(any(), any(), any()) } throws IllegalStateException("Recycled")
        }
        val viewModel = viewModel()
        viewModel.awaitLoaded()

        viewModel.onAction(ImageCropperAction.ImageCropped(failingBitmap))

        val state = viewModel.awaitState { it.error != UiText.Empty }
        assertEquals(UiText.StringResource(R.string.image_save_error), state.error)
        coVerify(exactly = 0) { navigator.navigateUpWithResult(any(), any<Any>()) }
    }

    private companion object {
        const val MAX_OUTPUT_BYTES = 2 * 1024 * 1024
    }
}
