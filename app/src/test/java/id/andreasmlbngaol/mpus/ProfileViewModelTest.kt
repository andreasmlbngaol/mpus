package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.UnreadCount
import id.andreasmlbngaol.mpus.data.User
import id.andreasmlbngaol.mpus.ui.profile.ProfileViewModel
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

    @Test
    fun `load populates sightings`() = runTest {
        val api = FakeApi(onMySightings = { testSightingsPage(listOf(testSightingResult("s1").sighting)) })
        val vm = ProfileViewModel(api, FakeSession(user = user))
        advanceUntilIdle()

        assertEquals(1, vm.state.value.sightings.size)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `load failure surfaces an error and clears loading`() = runTest {
        val api = FakeApi(onMySightings = { throw RuntimeException("boom") })
        val vm = ProfileViewModel(api, FakeSession(user = user))
        advanceUntilIdle()

        assertTrue(vm.state.value.error != null)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `loadMore appends the next page and stops at the last`() = runTest {
        val page1 = testSightingsPage(listOf(testSightingResult("s1").sighting), nextCursor = "cur1")
        val page2 = testSightingsPage(listOf(testSightingResult("s2").sighting), nextCursor = null)
        val api = FakeApi(onMySightings = { cursor -> if (cursor == null) page1 else page2 })
        val vm = ProfileViewModel(api, FakeSession(user = user))
        advanceUntilIdle()
        assertEquals(listOf("s1"), vm.state.value.sightings.map { it.id })

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("s1", "s2"), vm.state.value.sightings.map { it.id })
        assertNull(vm.state.value.nextCursor)

        // No cursor left: another call is a no-op, not a duplicate fetch.
        vm.loadMore()
        advanceUntilIdle()
        assertEquals(2, vm.state.value.sightings.size)
    }

    @Test
    fun `load refreshes the unread badge`() = runTest {
        val api = FakeApi(
            onMySightings = { testSightingsPage() },
            onUnread = { UnreadCount(4) },
        )
        val vm = ProfileViewModel(api, FakeSession(user = user))
        advanceUntilIdle()

        assertEquals(4L, vm.state.value.unread)
    }

    @Test
    fun `avatar upload replaces the cached user`() = runTest {
        val updated = user.copy(avatarUrl = "http://test.invalid/a.webp")
        val session = FakeSession(user = user)
        val api = FakeApi(
            onMySightings = { testSightingsPage() },
            onUploadAvatar = { _, _ -> updated },
        )
        val vm = ProfileViewModel(api, session)
        advanceUntilIdle()

        vm.uploadAvatar(byteArrayOf(1, 2, 3), "avatar.webp")
        advanceUntilIdle()

        assertEquals(updated, session.user.value)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `avatar upload failure clears busy`() = runTest {
        val api = FakeApi(
            onMySightings = { testSightingsPage() },
            onUploadAvatar = { _, _ -> throw RuntimeException("nope") },
        )
        val vm = ProfileViewModel(api, FakeSession(user = user))
        advanceUntilIdle()

        vm.uploadAvatar(byteArrayOf(1), "avatar.webp")
        advanceUntilIdle()

        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `logout clears the session even if the call fails`() = runTest {
        val session = FakeSession(user = user, token = "tok")
        val api = FakeApi(
            onMySightings = { testSightingsPage() },
            onLogout = { throw RuntimeException("server down") },
        )
        val vm = ProfileViewModel(api, session)
        advanceUntilIdle()

        vm.logout()
        advanceUntilIdle()

        assertTrue(session.cleared)
        assertNull(session.token.value)
    }
}
