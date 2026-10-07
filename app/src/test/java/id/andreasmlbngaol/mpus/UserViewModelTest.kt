package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.CatMarker
import id.andreasmlbngaol.mpus.data.Page
import id.andreasmlbngaol.mpus.data.UserProfile
import id.andreasmlbngaol.mpus.ui.user.UserViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class UserViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    @Test
    fun `load populates the public profile`() = runTest {
        val api = FakeApi(onUserProfile = { _, _ -> profile() })
        val vm = UserViewModel(api, "u1")
        advanceUntilIdle()

        assertEquals("Sana", vm.state.value.profile?.nickname)
        assertEquals(3L, vm.state.value.profile?.sightingsCount)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `load failure surfaces an error`() = runTest {
        val api = FakeApi(onUserProfile = { _, _ -> throw RuntimeException("gone") })
        val vm = UserViewModel(api, "u1")
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `loadMore appends the next page of cats`() = runTest {
        val api = FakeApi(onUserProfile = { _, cursor ->
            if (cursor == null) profile(cats = Page(listOf(marker("c1")), nextCursor = "cur1"))
            else profile(cats = Page(listOf(marker("c2")), nextCursor = null))
        })
        val vm = UserViewModel(api, "u1")
        advanceUntilIdle()
        assertEquals(listOf("c1"), vm.state.value.cats.map { it.id })

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("c1", "c2"), vm.state.value.cats.map { it.id })
    }

    private fun profile(cats: Page<CatMarker> = Page()) = UserProfile(
        id = "u1",
        username = "sana",
        nickname = "Sana",
        sightingsCount = 3,
        catsCount = 1,
        namesCount = 2,
        cats = cats,
    )

    private fun marker(id: String) = CatMarker(
        id = id,
        displayName = "Milo",
        thumbUrl = "http://test.invalid/t.webp",
        lat = -6.2,
        lng = 106.8,
        sightingCount = 1,
    )
}
