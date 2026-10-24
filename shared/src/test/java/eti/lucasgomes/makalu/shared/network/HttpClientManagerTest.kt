package eti.lucasgomes.makalu.shared.network

import eti.lucasgomes.makalu.shared.settings.FakeSettingsDataStore
import io.mockk.mockk
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class HttpClientManagerTest {

    private val manager = HttpClientManager(
        mkLogger = mockk(relaxed = true),
        dataStore = FakeSettingsDataStore(),
        logoutUseCase = mockk(relaxed = true)
    )

    @Test
    fun `getClient returns the same client on repeated calls`() {
        val first = manager.getClient()

        val second = manager.getClient()

        assertSame(first, second)
    }

    @Test
    fun `installAuth replaces the cached client with a new one`() {
        val before = manager.getClient()

        manager.installAuth()

        assertNotSame(before, manager.getClient())
    }
}
