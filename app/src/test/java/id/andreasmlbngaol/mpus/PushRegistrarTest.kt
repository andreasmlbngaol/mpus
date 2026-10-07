package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.PushRegistrar
import id.andreasmlbngaol.mpus.data.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushRegistrarTest {

    private val user = User(id = "u1", username = "sana", nickname = "Sana")

    @Test
    fun `a rotated token is claimed when signed in`() = runTest {
        var registered: Pair<String, String>? = null
        val api = FakeApi(onRegisterDevice = { t, p -> registered = t to p })
        val registrar = PushRegistrar(api, FakeSession(token = "tok", user = user))

        registrar.onTokenRefreshed("fcm-abc")

        assertEquals("fcm-abc" to "android", registered)
    }

    @Test
    fun `a rotated token is ignored while signed out`() = runTest {
        var registered: String? = null
        val api = FakeApi(onRegisterDevice = { t, _ -> registered = t })
        val registrar = PushRegistrar(api, FakeSession(token = null))

        registrar.onTokenRefreshed("fcm-abc")

        assertNull(registered)
    }
}
