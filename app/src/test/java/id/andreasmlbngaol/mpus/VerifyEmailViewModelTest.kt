package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.PushRegistrar
import id.andreasmlbngaol.mpus.data.Session
import id.andreasmlbngaol.mpus.data.User
import id.andreasmlbngaol.mpus.ui.auth.VerifyEmailViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VerifyEmailViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val unverified =
        User(id = "u1", username = "sana", nickname = "Sana", email = "sana@example.com", emailVerified = false)

    /** Avoids Firebase entirely: only the "forget this device" call matters here. */
    private class FakeRegistrar : PushRegistrar(FakeApi(), FakeSession()) {
        override suspend fun unregisterCurrent() = Unit
    }

    private fun vm(api: FakeApi, session: Session) = VerifyEmailViewModel(api, session, FakeRegistrar())

    @Test
    fun `verify flips the session user and clears busy`() = runTest {
        val session = FakeSession(token = "tok", user = unverified)
        val verified = unverified.copy(emailVerified = true)
        val api = FakeApi(onVerifyEmail = { verified })
        val vm = vm(api, session)

        vm.onCode("123456")
        vm.verify()
        advanceUntilIdle()

        assertEquals(true, session.user.value?.emailVerified)
        assertFalse(vm.state.value.busy)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `resend uses the signed-in email`() = runTest {
        var sentTo: String? = null
        val api = FakeApi(onResendVerification = { email -> sentTo = email; "on its way" })
        val session = FakeSession(token = "tok", user = unverified)
        val vm = vm(api, session)

        vm.resend()
        advanceUntilIdle()

        assertEquals("sana@example.com", sentTo)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `logout clears the session`() = runTest {
        val session = FakeSession(token = "tok", user = unverified)
        val vm = vm(FakeApi(), session)

        vm.logout()
        advanceUntilIdle()

        assertTrue(session.cleared)
        assertNull(session.token.value)
    }
}
