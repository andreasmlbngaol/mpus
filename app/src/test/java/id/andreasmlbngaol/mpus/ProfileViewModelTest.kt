package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
import id.andreasmlbngaol.mpus.profile.domain.usecase.ProfileUseCase
import id.andreasmlbngaol.mpus.profile.ui.ProfileUiEvent
import id.andreasmlbngaol.mpus.profile.ui.ProfileViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val user = User(id = "u1", username = "sana", nickname = "Sana")

    private fun vm(repo: FakeProfileRepository, session: FakeSession = FakeSession(user = user)) =
        ProfileViewModel(ProfileUseCase(repo), SessionUseCase(session), FakePushRepository())

    @Test
    fun `load populates sightings`() = runTest {
        val repo = FakeProfileRepository(onMySightings = { testSightingsPage(listOf(testSightingResult("s1").sighting)) })
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.sightings.size)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `load failure surfaces an error and clears loading`() = runTest {
        val repo = FakeProfileRepository(onMySightings = { throw RuntimeException("boom") })
        val vm = vm(repo)
        advanceUntilIdle()

        assertTrue(vm.state.value.error != null)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `loadMore appends the next page and stops at the last`() = runTest {
        val page1 = testSightingsPage(listOf(testSightingResult("s1").sighting), nextCursor = "cur1")
        val page2 = testSightingsPage(listOf(testSightingResult("s2").sighting), nextCursor = null)
        val repo = FakeProfileRepository(onMySightings = { cursor -> if (cursor == null) page1 else page2 })
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals(listOf("s1"), vm.state.value.sightings.map { it.id })

        vm.onEvent(ProfileUiEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(listOf("s1", "s2"), vm.state.value.sightings.map { it.id })
        assertNull(vm.state.value.nextCursor)

        // No cursor left: another call is a no-op, not a duplicate fetch.
        vm.onEvent(ProfileUiEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.sightings.size)
    }

    @Test
    fun `load refreshes the unread badge`() = runTest {
        val repo = FakeProfileRepository(
            onMySightings = { testSightingsPage() },
            onUnread = { 4L },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals(4L, vm.state.value.unread)
    }

    @Test
    fun `avatar upload replaces the cached user`() = runTest {
        val updated = user.copy(avatarUrl = "http://test.invalid/a.webp")
        val session = FakeSession(user = user)
        val repo = FakeProfileRepository(
            onMySightings = { testSightingsPage() },
            onUploadAvatar = { _, _ -> updated },
        )
        val vm = vm(repo, session)
        advanceUntilIdle()

        vm.onEvent(ProfileUiEvent.UploadAvatar(byteArrayOf(1, 2, 3)))
        advanceUntilIdle()

        assertEquals(updated, session.user.value)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `avatar upload failure clears busy`() = runTest {
        val repo = FakeProfileRepository(
            onMySightings = { testSightingsPage() },
            onUploadAvatar = { _, _ -> throw RuntimeException("nope") },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(ProfileUiEvent.UploadAvatar(byteArrayOf(1)))
        advanceUntilIdle()

        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `logout clears the session even if the call fails`() = runTest {
        val session = FakeSession(user = user, token = "tok")
        val repo = FakeProfileRepository(
            onMySightings = { testSightingsPage() },
            onLogout = { throw RuntimeException("server down") },
        )
        val vm = vm(repo, session)
        advanceUntilIdle()

        vm.onEvent(ProfileUiEvent.Logout)
        advanceUntilIdle()

        assertTrue(session.cleared)
        assertNull(session.token.value)
    }
}
