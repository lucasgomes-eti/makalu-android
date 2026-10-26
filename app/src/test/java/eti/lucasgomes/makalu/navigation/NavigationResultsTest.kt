package eti.lucasgomes.makalu.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationResultsTest {

    private val key = "ImageCroppedUriOutput"
    private val handle = SavedStateHandle()
    private val entry = mockk<NavBackStackEntry> { every { savedStateHandle } returns handle }

    @Test
    fun `emits value already present when collection starts`() = runTest {
        handle[key] = "a"

        val emitted = collectResults(backgroundScope)

        assertEquals(listOf("a"), emitted)
    }

    @Test
    fun `clears the key after emitting`() = runTest {
        handle[key] = "a"

        collectResults(backgroundScope)

        assertNull(handle.get<String>(key))
    }

    @Test
    fun `does not emit while key is absent`() = runTest {
        val emitted = collectResults(backgroundScope)

        assertEquals(emptyList<String>(), emitted)
    }

    @Test
    fun `emits each value set after collection starts`() = runTest {
        val emitted = collectResults(backgroundScope)

        handle[key] = "a"
        handle[key] = "b"

        assertEquals(listOf("a", "b"), emitted)
    }

    @Test
    fun `emits the same value again when it is set twice`() = runTest {
        val emitted = collectResults(backgroundScope)

        handle[key] = "a"
        handle[key] = "a"

        assertEquals(listOf("a", "a"), emitted)
    }

    @Test
    fun `consumed value is not redelivered to a new collector`() = runTest {
        handle[key] = "a"
        val firstScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        collectResults(firstScope)
        firstScope.cancel()

        val emitted = collectResults(backgroundScope)

        assertEquals(emptyList<String>(), emitted)
    }

    @Test
    fun `uses the request key string as the saved state key`() = runTest {
        handle[RequestKey.ImageCroppedUriOutput.toString()] = "a"

        val emitted = collectResults(backgroundScope)

        assertEquals("ImageCroppedUriOutput", RequestKey.ImageCroppedUriOutput.toString())
        assertEquals(listOf("a"), emitted)
    }

    private fun TestScope.collectResults(scope: CoroutineScope): List<String> {
        val emitted = mutableListOf<String>()
        scope.launch(UnconfinedTestDispatcher(testScheduler)) {
            entry.navigationResults<String>(RequestKey.ImageCroppedUriOutput).collect {
                emitted +=
                    it
            }
        }
        return emitted
    }
}
