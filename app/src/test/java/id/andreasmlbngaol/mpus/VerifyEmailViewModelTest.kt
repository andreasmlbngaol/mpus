package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.auth.domain.usecase.AuthUseCase
import id.andreasmlbngaol.mpus.auth.ui.VerifyEmailViewModel
import id.andreasmlbngaol.mpus.auth.ui.VerifyUiEvent
import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
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

    private fun vm(repo: FakeAuthRepository, session: SessionRepository) =
        VerifyEmailViewModel(AuthUseCase(repo), SessionUseCase(session), FakePushRepository())

    @Test
    fun `verify flips the session user and clears busy`() = runTest {
        val session = FakeSession(token = "tok", user = unverified)
        val verified = unverified.copy(emailVerified = true)
        val repo = FakeAuthRepository(onVerifyEmail = { verified })
        val vm = vm(repo, session)

        vm.onEvent(VerifyUiEvent.CodeChanged("123456"))
        vm.onEvent(VerifyUiEvent.Verify)
        advanceUntilIdle()

        assertEquals(true, session.user.value?.emailVerified)
        assertFalse(vm.state.value.busy)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `resend uses the signed-in email`() = runTest {
        var sentTo: String? = null
        val repo = FakeAuthRepository(onResend = { email -> sentTo = email; "on its way" })
        val session = FakeSession(token = "tok", user = unverified)
        val vm = vm(repo, session)

        vm.onEvent(VerifyUiEvent.Resend)
        advanceUntilIdle()

        assertEquals("sana@example.com", sentTo)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `logout clears the session`() = runTest {
        val session = FakeSession(token = "tok", user = unverified)
        val vm = vm(FakeAuthRepository(), session)

        vm.onEvent(VerifyUiEvent.Logout)
        advanceUntilIdle()

        assertTrue(session.cleared)
        assertNull(session.token.value)
    }
}
