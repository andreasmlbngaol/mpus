package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
import id.andreasmlbngaol.mpus.profile.domain.usecase.ProfileUseCase
import id.andreasmlbngaol.mpus.profile.ui.EditProfileUiEvent
import id.andreasmlbngaol.mpus.profile.ui.EditProfileViewModel
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

    private fun vm(repo: FakeProfileRepository, session: FakeSession = FakeSession(user = user)) =
        EditProfileViewModel(ProfileUseCase(repo), SessionUseCase(session))

    @Test
    fun `seeds the fields from the cached user`() = runTest {
        val vm = vm(FakeProfileRepository())

        assertEquals("sana", vm.state.value.username)
        assertEquals("Sana", vm.state.value.nickname)
    }

    @Test
    fun `save trims, patches through the api and marks saved`() = runTest {
        val renamed = user.copy(username = "sana_b", nickname = "Sana B")
        val session = FakeSession(user = user)
        val repo = FakeProfileRepository(onUpdateProfile = { _, _ -> renamed })
        val vm = vm(repo, session)

        vm.onEvent(EditProfileUiEvent.NicknameChanged("  Sana B  "))
        vm.onEvent(EditProfileUiEvent.UsernameChanged("  sana_b  "))
        vm.onEvent(EditProfileUiEvent.Save)
        advanceUntilIdle()

        assertEquals(renamed, session.user.value)
        assertTrue(vm.state.value.saved)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `a failed save clears busy and does not mark saved`() = runTest {
        val repo = FakeProfileRepository(onUpdateProfile = { _, _ -> throw RuntimeException("nope") })
        val vm = vm(repo)

        vm.onEvent(EditProfileUiEvent.Save)
        advanceUntilIdle()

        assertFalse(vm.state.value.saved)
        assertFalse(vm.state.value.busy)
    }
}
