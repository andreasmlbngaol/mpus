package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.domain.model.Page
import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile
import id.andreasmlbngaol.mpus.profile.domain.usecase.UserUseCase
import id.andreasmlbngaol.mpus.profile.ui.UserUiEvent
import id.andreasmlbngaol.mpus.profile.ui.UserViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class UserViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun vm(repo: FakeUserRepository) = UserViewModel(UserUseCase(repo), "u1")

    @Test
    fun `load populates the public profile`() = runTest {
        val repo = FakeUserRepository(onProfile = { _, _ -> profile() })
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals("Sana", vm.state.value.profile?.nickname)
        assertEquals(3L, vm.state.value.profile?.sightingsCount)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `load failure surfaces an error`() = runTest {
        val repo = FakeUserRepository(onProfile = { _, _ -> throw RuntimeException("gone") })
        val vm = vm(repo)
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `loadMore appends the next page of cats`() = runTest {
        val repo = FakeUserRepository(onProfile = { _, cursor ->
            if (cursor == null) profile(cats = Page(listOf(marker("c1")), nextCursor = "cur1"))
            else profile(cats = Page(listOf(marker("c2")), nextCursor = null))
        })
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals(listOf("c1"), vm.state.value.cats.map { it.id })

        vm.onEvent(UserUiEvent.LoadMore)
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
