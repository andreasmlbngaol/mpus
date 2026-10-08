package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.auth.domain.model.AuthTab
import id.andreasmlbngaol.mpus.auth.domain.model.LoginResult
import id.andreasmlbngaol.mpus.auth.domain.usecase.AuthUseCase
import id.andreasmlbngaol.mpus.auth.ui.AuthUiEvent
import id.andreasmlbngaol.mpus.auth.ui.AuthViewModel
import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
import id.andreasmlbngaol.mpus.core.ui.UiText
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val user = User(id = "u1", username = "sana", nickname = "Sana")

    private fun vm(repo: FakeAuthRepository, session: FakeSession = FakeSession()) =
        AuthViewModel(AuthUseCase(repo), SessionUseCase(session))

    @Test
    fun `login success clears the form so it does not linger after sign-out`() = runTest {
        val session = FakeSession()
        val repo = FakeAuthRepository(onLogin = { _, _ -> LoginResult("tok", user) })
        val vm = vm(repo, session)

        vm.onEvent(AuthUiEvent.EmailChanged("sana@example.com"))
        vm.onEvent(AuthUiEvent.PasswordChanged("hunter22222"))
        vm.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()

        assertEquals("tok", session.token.value)
        assertEquals(user, session.user.value)
        // The Activity-scoped VM must not keep the old credentials.
        assertEquals("", vm.state.value.signInEmail)
        assertEquals("", vm.state.value.signInPassword)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `login failure keeps credentials and surfaces an error, and clears the spinner`() = runTest {
        val repo = FakeAuthRepository(onLogin = { _, _ -> throw RuntimeException("nope") })
        val vm = vm(repo)

        vm.onEvent(AuthUiEvent.EmailChanged("sana@example.com"))
        vm.onEvent(AuthUiEvent.PasswordChanged("hunter22222"))
        vm.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()

        assertEquals("sana@example.com", vm.state.value.signInEmail)
        assertTrue(vm.state.value.error != null)
        assertFalse(vm.state.value.submitting)
    }

    @Test
    fun `blank credentials are rejected before any network call`() = runTest {
        var called = false
        val repo = FakeAuthRepository(onLogin = { _, _ -> called = true; LoginResult("t", user) })
        val vm = vm(repo)

        vm.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()

        assertFalse(called)
        assertTrue(vm.state.value.error != null)
    }

    @Test
    fun `sign-in and sign-up keep separate credentials`() = runTest {
        val vm = vm(FakeAuthRepository())

        vm.onEvent(AuthUiEvent.EmailChanged("signin@example.com"))
        vm.onEvent(AuthUiEvent.SelectTab(AuthTab.SignUp))
        vm.onEvent(AuthUiEvent.EmailChanged("signup@example.com"))

        // Each tab remembers its own email; switching back restores the first.
        assertEquals("signup@example.com", vm.state.value.email)
        vm.onEvent(AuthUiEvent.SelectTab(AuthTab.SignIn))
        assertEquals("signin@example.com", vm.state.value.email)
    }

    @Test
    fun `signup signs the new user in straight away`() = runTest {
        val session = FakeSession()
        val unverified = User(id = "u2", username = "newuser", nickname = "New User", emailVerified = false)
        val repo = FakeAuthRepository(onSignup = { _, _, _, _ -> LoginResult("tok2", unverified) })
        val vm = vm(repo, session)

        vm.onEvent(AuthUiEvent.SelectTab(AuthTab.SignUp))
        vm.onEvent(AuthUiEvent.EmailChanged("new@example.com"))
        vm.onEvent(AuthUiEvent.PasswordChanged("hunter22222"))
        vm.onEvent(AuthUiEvent.UsernameChanged("newuser"))
        vm.onEvent(AuthUiEvent.NicknameChanged("New User"))
        vm.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()

        // The session is what routes the app: token set + email_verified false → verify screen.
        assertEquals("tok2", session.token.value)
        assertEquals(unverified, session.user.value)
        assertFalse(vm.state.value.submitting)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `incomplete signup is rejected without a network call`() = runTest {
        var called = false
        val repo = FakeAuthRepository(onSignup = { _, _, _, _ -> called = true; LoginResult("t", user) })
        val vm = vm(repo)

        vm.onEvent(AuthUiEvent.SelectTab(AuthTab.SignUp))
        vm.onEvent(AuthUiEvent.EmailChanged("new@example.com"))
        vm.onEvent(AuthUiEvent.PasswordChanged("short"))
        vm.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()

        assertFalse(called)
        assertTrue(vm.state.value.error is UiText.Res)
    }
}
