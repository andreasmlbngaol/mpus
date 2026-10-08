package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.CatName
import id.andreasmlbngaol.mpus.cat.domain.model.CatReview
import id.andreasmlbngaol.mpus.cat.domain.usecase.CatUseCase
import id.andreasmlbngaol.mpus.cat.ui.CatUiEvent
import id.andreasmlbngaol.mpus.cat.ui.CatViewModel
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CatViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun vm(repo: FakeCatRepository) = CatViewModel(CatUseCase(repo), "c1")

    @Test
    fun `load populates detail`() = runTest {
        val repo = FakeCatRepository(onDetail = { detail(names = listOf(name("n1", likes = 2))) })
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.detail?.names?.size)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `load failure surfaces an error`() = runTest {
        val repo = FakeCatRepository(onDetail = { throw RuntimeException("gone") })
        val vm = vm(repo)
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `addName ignores blank input without a call`() = runTest {
        var called = false
        val repo = FakeCatRepository(
            onDetail = { detail() },
            onSetName = { _, _ -> called = true; detail() },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(CatUiEvent.NameSubmitted("   "))
        advanceUntilIdle()

        assertFalse(called)
    }

    @Test
    fun `addName trims and refreshes the detail`() = runTest {
        var sent: String? = null
        val repo = FakeCatRepository(
            onDetail = { detail() },
            onSetName = { _, n -> sent = n; detail(names = listOf(name("n1", likes = 1))) },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(CatUiEvent.NameSubmitted("  Milo  "))
        advanceUntilIdle()

        assertEquals("Milo", sent)
        assertEquals(1, vm.state.value.detail?.names?.size)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `toggleLike refetches the detail`() = runTest {
        var refetches = 0
        val repo = FakeCatRepository(
            onDetail = { refetches++; detail(names = listOf(name("n1", likes = refetches))) },
            onLikeName = { },
        )
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals(1, refetches)

        vm.onEvent(CatUiEvent.NameLiked("n1"))
        advanceUntilIdle()

        // One for init, one for the refresh after the like.
        assertEquals(2, refetches)
        assertEquals(2, vm.state.value.detail?.names?.first()?.likes)
    }

    @Test
    fun `addReview ignores blank bodies`() = runTest {
        var called = false
        val repo = FakeCatRepository(
            onDetail = { detail() },
            onSetReview = { _, _, _ -> called = true; detail() },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(CatUiEvent.ReviewSubmitted("  ", 8))
        advanceUntilIdle()

        assertFalse(called)
    }

    @Test
    fun `a failed write clears busy and reports it`() = runTest {
        val repo = FakeCatRepository(
            onDetail = { detail() },
            onSetName = { _, _ -> throw RuntimeException("no") },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(CatUiEvent.NameSubmitted("Milo"))
        advanceUntilIdle()

        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `report refetches when the item gets hidden`() = runTest {
        var refetches = 0
        val repo = FakeCatRepository(
            onDetail = { refetches++; detail(names = listOf(name("n1", likes = 1))) },
            onReport = { _, _ -> true },
        )
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals(1, refetches)

        vm.onEvent(CatUiEvent.Report("name", "n1"))
        advanceUntilIdle()

        // Hidden content must drop out of the list, so a refetch is required.
        assertEquals(2, refetches)
    }

    @Test
    fun `report does not refetch when nothing was hidden yet`() = runTest {
        var refetches = 0
        val repo = FakeCatRepository(
            onDetail = { refetches++; detail() },
            onReport = { _, _ -> false },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(CatUiEvent.Report("review", "r1"))
        advanceUntilIdle()

        assertEquals(1, refetches)
    }

    @Test
    fun `mergeInto sends the target and surfaces an instant merge`() = runTest {
        var target: String? = null
        val repo = FakeCatRepository(
            onDetail = { detail() },
            onRequestMerge = { _, t -> target = t; MergeRequest("m1", "c1", t, "Milo", "Kitty", "u1", "merged") },
        )
        val vm = vm(repo)
        advanceUntilIdle()

        vm.onEvent(CatUiEvent.MergeInto("c2"))
        advanceUntilIdle()

        assertEquals("c2", target)
        assertFalse(vm.state.value.busy)
        // A completed merge navigates to the surviving cat's detail.
        assertNotNull(vm.state.value.detail)
    }

    private fun detail(names: List<CatName> = emptyList(), reviews: List<CatReview> = emptyList()) = CatDetail(
        id = "c1",
        displayName = names.firstOrNull()?.name,
        createdAt = "2026-01-01T00:00:00Z",
        names = names,
        reviews = reviews,
    )

    private fun name(id: String, likes: Int) = CatName(
        id = id,
        userId = "u1",
        nickname = "Sana",
        name = "Milo",
        likes = likes,
        likedByMe = false,
    )
}
