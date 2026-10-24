package eti.lucasgomes.makalu.components.ext

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ViewModelExtTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class TestViewModel : ViewModel()

    @Test
    fun `withViewModelScope runs the block in the view model scope`() {
        var ran = false

        TestViewModel().withViewModelScope { ran = true }

        assertTrue(ran)
    }

    @Test
    fun `withViewModelScope runs suspending work after resume`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var finished = false

        TestViewModel().withViewModelScope {
            gate.await()
            finished = true
        }
        val finishedBeforeResume = finished
        gate.complete(Unit)

        assertFalse(finishedBeforeResume)
        assertTrue(finished)
    }
}
