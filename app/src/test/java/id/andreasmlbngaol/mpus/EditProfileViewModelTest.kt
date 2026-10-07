package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.User
import id.andreasmlbngaol.mpus.ui.profile.EditProfileViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditProfileViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val user = User(id = "u1", username = "sana", nickname = "Sana")

    @Test
    fun `seeds the fields from the cached user`() = runTest {
        val vm = EditProfileViewModel(FakeApi(), FakeSession(user = user))

        assertEquals("sana", vm.state.value.username)
        assertEquals("Sana", vm.state.value.nickname)
    }

    @Test
    fun `save trims, patches through the api and marks saved`() = runTest {
        val renamed = user.copy(username = "sana_b", nickname = "Sana B")
        val session = FakeSession(user = user)
        val api = FakeApi(onPatchMe = { _, _ -> renamed })
        val vm = EditProfileViewModel(api, session)

        vm.onNickname("  Sana B  ")
        vm.onUsername("  sana_b  ")
        vm.save()
        advanceUntilIdle()

        assertEquals(renamed, session.user.value)
        assertTrue(vm.state.value.saved)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `a failed save clears busy and does not mark saved`() = runTest {
        val api = FakeApi(onPatchMe = { _, _ -> throw RuntimeException("nope") })
        val vm = EditProfileViewModel(api, FakeSession(user = user))

        vm.save()
        advanceUntilIdle()

        assertFalse(vm.state.value.saved)
        assertFalse(vm.state.value.busy)
    }
}
