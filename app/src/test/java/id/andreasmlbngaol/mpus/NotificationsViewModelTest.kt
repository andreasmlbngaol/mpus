package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.AppNotification
import id.andreasmlbngaol.mpus.data.MergeRequest
import id.andreasmlbngaol.mpus.data.Page
import id.andreasmlbngaol.mpus.ui.notifications.NotificationsViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun notif(id: String, kind: String = "name_liked") = AppNotification(
        id = id,
        kind = kind,
        actorNickname = "Sana",
        catName = "Milo",
        createdAt = "2026-01-01T00:00:00Z",
    )

    private fun merge(id: String) = MergeRequest(
        id = id,
        sourceCatId = "c1",
        targetCatId = "c2",
        sourceName = "Milo",
        targetName = "Kitty",
        requestedBy = "u2",
        status = "pending",
    )

    @Test
    fun `load populates inbox and pending merges`() = runTest {
        val api = FakeApi(
            onNotifications = { Page(listOf(notif("n1"), notif("n2")), null) },
            onPendingMerges = { listOf(merge("m1")) },
        )
        val vm = NotificationsViewModel(api)
        advanceUntilIdle()

        assertEquals(2, vm.state.value.items.size)
        assertEquals(1, vm.state.value.merges.size)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `load failure surfaces an error`() = runTest {
        val api = FakeApi(onNotifications = { throw RuntimeException("boom") })
        val vm = NotificationsViewModel(api)
        advanceUntilIdle()

        assertTrue(vm.state.value.error != null)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `loadMore appends the next page`() = runTest {
        val api = FakeApi(
            onNotifications = { cursor ->
                if (cursor == null) Page(listOf(notif("n1")), "next") else Page(listOf(notif("n2")), null)
            },
        )
        val vm = NotificationsViewModel(api)
        advanceUntilIdle()
        vm.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("n1", "n2"), vm.state.value.items.map { it.id })
        assertNull(vm.state.value.nextCursor)
    }

    @Test
    fun `approve removes the merge from the list`() = runTest {
        val api = FakeApi(
            onNotifications = { Page() },
            onPendingMerges = { listOf(merge("m1"), merge("m2")) },
            onApproveMerge = { merge(it).copy(status = "merged") },
        )
        val vm = NotificationsViewModel(api)
        advanceUntilIdle()
        vm.approve("m1")
        advanceUntilIdle()

        assertEquals(listOf("m2"), vm.state.value.merges.map { it.id })
    }

    @Test
    fun `reject removes the merge from the list`() = runTest {
        val api = FakeApi(
            onNotifications = { Page() },
            onPendingMerges = { listOf(merge("m1")) },
            onRejectMerge = { },
        )
        val vm = NotificationsViewModel(api)
        advanceUntilIdle()
        vm.reject("m1")
        advanceUntilIdle()

        assertTrue(vm.state.value.merges.isEmpty())
    }
}
