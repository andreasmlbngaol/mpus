package id.andreasmlbngaol.mpus

import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.map.domain.model.Bounds
import id.andreasmlbngaol.mpus.map.domain.usecase.MapUseCase
import id.andreasmlbngaol.mpus.map.ui.MapUiEvent
import id.andreasmlbngaol.mpus.map.ui.MapViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val bounds = Bounds(minLat = -6.3, minLng = 106.7, maxLat = -6.1, maxLng = 106.9)

    private fun vm(repo: FakeMapRepository) = MapViewModel(MapUseCase(repo), FakeLocationRepository())

    @Test
    fun `viewport load is debounced then lands markers`() = runTest {
        var calls = 0
        val repo = FakeMapRepository(onCats = { _, _, _, _ ->
            calls++
            listOf(marker("c1"))
        })
        val vm = vm(repo)

        vm.onEvent(MapUiEvent.ViewportChanged(bounds, 15.0))
        advanceUntilIdle()

        assertEquals(1, calls)
        assertEquals(listOf("c1"), vm.state.value.markers.map { it.id })
        assertTrue(vm.state.value.loadedOnce)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `a huge viewport is ignored`() = runTest {
        var calls = 0
        val repo = FakeMapRepository(onCats = { _, _, _, _ -> calls++; emptyList() })
        val vm = vm(repo)

        vm.onEvent(MapUiEvent.ViewportChanged(Bounds(minLat = -20.0, minLng = 90.0, maxLat = 20.0, maxLng = 130.0), 15.0))
        advanceUntilIdle()

        assertEquals(0, calls)
    }

    @Test
    fun `only the first load shows the full-screen spinner`() = runTest {
        val repo = FakeMapRepository(onCats = { _, _, _, _ -> listOf(marker("c1")) })
        val vm = vm(repo)

        vm.onEvent(MapUiEvent.ViewportChanged(bounds, 15.0))
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)

        // A second load must update in place — never flip the spinner back on.
        vm.onEvent(MapUiEvent.ViewportChanged(bounds, 15.0))
        advanceTimeBy(450)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `auto-refresh reloads the last viewport on its interval`() = runTest {
        var calls = 0
        val repo = FakeMapRepository(onCats = { _, _, _, _ -> calls++; listOf(marker("c1")) })
        val vm = vm(repo)

        vm.onEvent(MapUiEvent.ViewportChanged(bounds, 15.0))
        advanceUntilIdle()
        assertEquals(1, calls)

        // The refresh loop reschedules itself forever, so advance by exactly one
        // interval — and cancel the scope before the test drains the scheduler, or it
        // spins chasing the next tick with no end.
        vm.onEvent(MapUiEvent.Started)
        advanceTimeBy(30_001)
        assertEquals(2, calls)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `a failed load surfaces an error and stops the spinner`() = runTest {
        val repo = FakeMapRepository(onCats = { _, _, _, _ -> throw RuntimeException("offline") })
        val vm = vm(repo)

        vm.onEvent(MapUiEvent.ViewportChanged(bounds, 15.0))
        advanceUntilIdle()

        assertTrue(vm.state.value.error != null)
        assertFalse(vm.state.value.loading)
    }

    private fun marker(id: String) = CatMarker(
        id = id,
        displayName = "Milo",
        thumbUrl = "http://test.invalid/t.webp",
        lat = -6.2,
        lng = 106.8,
        sightingCount = 1,
    )
}
