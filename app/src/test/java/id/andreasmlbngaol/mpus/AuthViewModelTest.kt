package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.LoginData
import id.andreasmlbngaol.mpus.data.User
import id.andreasmlbngaol.mpus.ui.UiText
import id.andreasmlbngaol.mpus.ui.auth.AuthTab
import id.andreasmlbngaol.mpus.ui.auth.AuthViewModel
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

    @Test
    fun `login success clears the form so it does not linger after sign-out`() = runTest {
        val session = FakeSession()
        val api = FakeApi(onLogin = { _, _ -> LoginData("tok", user) })
        val vm = AuthViewModel(api, session)

        vm.onEmail("sana@example.com")
        vm.onPassword("hunter22222")
        vm.submit()
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
        val api = FakeApi(onLogin = { _, _ -> throw RuntimeException("nope") })
        val vm = AuthViewModel(api, FakeSession())

        vm.onEmail("sana@example.com")
        vm.onPassword("hunter22222")
        vm.submit()
        advanceUntilIdle()

        assertEquals("sana@example.com", vm.state.value.signInEmail)
        assertTrue(vm.state.value.error != null)
        assertFalse(vm.state.value.submitting)
    }

    @Test
    fun `blank credentials are rejected before any network call`() = runTest {
        var called = false
        val api = FakeApi(onLogin = { _, _ -> called = true; LoginData("t", user) })
        val vm = AuthViewModel(api, FakeSession())

        vm.submit()
        advanceUntilIdle()

        assertFalse(called)
        assertTrue(vm.state.value.error != null)
    }

    @Test
    fun `sign-in and sign-up keep separate credentials`() = runTest {
        val vm = AuthViewModel(FakeApi(), FakeSession())

        vm.onEmail("signin@example.com")
        vm.onTab(AuthTab.SignUp)
        vm.onEmail("signup@example.com")

        // Each tab remembers its own email; switching back restores the first.
        assertEquals("signup@example.com", vm.state.value.email)
        vm.onTab(AuthTab.SignIn)
        assertEquals("signin@example.com", vm.state.value.email)
    }

    @Test
    fun `signup signs the new user in straight away`() = runTest {
        val session = FakeSession()
        val unverified = User(id = "u2", username = "newuser", nickname = "New User", emailVerified = false)
        val api = FakeApi(onSignup = { _, _, _, _ -> LoginData("tok2", unverified) })
        val vm = AuthViewModel(api, session)

        vm.onTab(AuthTab.SignUp)
        vm.onEmail("new@example.com")
        vm.onPassword("hunter22222")
        vm.onUsername("newuser")
        vm.onNickname("New User")
        vm.submit()
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
        val api = FakeApi(onSignup = { _, _, _, _ -> called = true; LoginData("t", user) })
        val vm = AuthViewModel(api, FakeSession())

        vm.onTab(AuthTab.SignUp)
        vm.onEmail("new@example.com")
        vm.onPassword("short")
        vm.submit()
        advanceUntilIdle()

        assertFalse(called)
        assertTrue(vm.state.value.error is UiText.Res)
    }
}
