package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.auth.domain.model.LoginResult
import id.andreasmlbngaol.mpus.auth.domain.repository.AuthRepository
import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.MergeTarget
import id.andreasmlbngaol.mpus.cat.domain.repository.CatRepository
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.domain.model.GeoPoint
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.domain.model.Page
import id.andreasmlbngaol.mpus.core.domain.model.Sighting
import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.repository.DeviceRepository
import id.andreasmlbngaol.mpus.core.domain.repository.LocationRepository
import id.andreasmlbngaol.mpus.core.domain.repository.PushRepository
import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import id.andreasmlbngaol.mpus.map.domain.repository.MapRepository
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification
import id.andreasmlbngaol.mpus.notifications.domain.repository.NotificationsRepository
import id.andreasmlbngaol.mpus.profile.domain.model.MySightings
import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile
import id.andreasmlbngaol.mpus.profile.domain.repository.ProfileRepository
import id.andreasmlbngaol.mpus.profile.domain.repository.UserRepository
import id.andreasmlbngaol.mpus.sighting.domain.model.Candidate
import id.andreasmlbngaol.mpus.sighting.domain.model.SightingResult
import id.andreasmlbngaol.mpus.sighting.domain.repository.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [SessionRepository], so ViewModels can be tested without SharedPreferences. */
class FakeSession(
    token: String? = null,
    user: User? = null,
) : SessionRepository {
    private val _token = MutableStateFlow(token)
    override val token: StateFlow<String?> = _token
    private val _user = MutableStateFlow(user)
    override val user: StateFlow<User?> = _user

    var cleared = false
        private set

    override fun save(token: String, user: User) {
        _token.value = token
        _user.value = user
    }

    override fun updateUser(user: User) {
        _user.value = user
    }

    override fun clear() {
        cleared = true
        _token.value = null
        _user.value = null
    }
}

/**
 * Fakes for every domain repository. Each method is a lambda property so a test can
 * override just the call it cares about; anything untouched throws, which keeps a test
 * from silently depending on a call it didn't set up.
 */
class FakeAuthRepository(
    var onLogin: suspend (String, String) -> LoginResult = { _, _ -> error("login not stubbed") },
    var onSignup: suspend (String, String, String, String) -> LoginResult = { _, _, _, _ -> error("signup not stubbed") },
    var onLogout: suspend () -> Unit = { },
    var onVerifyEmail: suspend (String) -> User = { error("verifyEmail not stubbed") },
    var onResend: suspend (String) -> String = { error("resendVerification not stubbed") },
) : AuthRepository {
    override suspend fun login(email: String, password: String) = onLogin(email, password)
    override suspend fun signup(email: String, password: String, username: String, nickname: String) =
        onSignup(email, password, username, nickname)
    override suspend fun logout() = onLogout()
    override suspend fun verifyEmail(code: String) = onVerifyEmail(code)
    override suspend fun resendVerification(email: String) = onResend(email)
}

class FakeMapRepository(
    var onCats: suspend (Double, Double, Double, Double) -> List<CatMarker> = { _, _, _, _ -> error("cats not stubbed") },
) : MapRepository {
    override suspend fun cats(minLat: Double, minLng: Double, maxLat: Double, maxLng: Double) =
        onCats(minLat, minLng, maxLat, maxLng)
}

class FakeCatRepository(
    var onDetail: suspend (String) -> CatDetail = { error("detail not stubbed") },
    var onSetName: suspend (String, String) -> CatDetail = { _, _ -> error("setName not stubbed") },
    var onLikeName: suspend (String) -> Unit = { error("likeName not stubbed") },
    var onSetReview: suspend (String, String, Int) -> CatDetail = { _, _, _ -> error("setReview not stubbed") },
    var onLikeReview: suspend (String) -> Unit = { error("likeReview not stubbed") },
    var onReport: suspend (String, String) -> Boolean = { _, _ -> error("report not stubbed") },
    var onMergeTargets: suspend (String) -> List<MergeTarget> = { emptyList() },
    var onRequestMerge: suspend (String, String) -> MergeRequest = { _, _ -> error("requestMerge not stubbed") },
) : CatRepository {
    override suspend fun detail(id: String) = onDetail(id)
    override suspend fun setName(catId: String, name: String) = onSetName(catId, name)
    override suspend fun likeName(nameId: String) = onLikeName(nameId)
    override suspend fun setReview(catId: String, body: String, rating: Int) = onSetReview(catId, body, rating)
    override suspend fun likeReview(reviewId: String) = onLikeReview(reviewId)
    override suspend fun report(targetKind: String, targetId: String) = onReport(targetKind, targetId)
    override suspend fun mergeTargets(excludeCatId: String) = onMergeTargets(excludeCatId)
    override suspend fun requestMerge(sourceCatId: String, targetCatId: String) =
        onRequestMerge(sourceCatId, targetCatId)
}

class FakeSightingRepository(
    var onCreate: suspend (ByteArray, String, Double, Double, String?) -> SightingResult =
        { _, _, _, _, _ -> error("create not stubbed") },
    var onResolve: suspend (String, String?, String?) -> String = { _, _, _ -> error("resolve not stubbed") },
) : SightingRepository {
    override suspend fun create(bytes: ByteArray, filename: String, lat: Double, lng: Double, takenAt: String?) =
        onCreate(bytes, filename, lat, lng, takenAt)
    override suspend fun resolve(sightingId: String, catId: String?, name: String?) =
        onResolve(sightingId, catId, name)
}

class FakeProfileRepository(
    var onMySightings: suspend (String?) -> MySightings = { error("mySightings not stubbed") },
    var onUnread: suspend () -> Long = { 0 },
    var onUploadAvatar: suspend (ByteArray, String) -> User = { _, _ -> error("uploadAvatar not stubbed") },
    var onDeleteAvatar: suspend () -> User = { error("deleteAvatar not stubbed") },
    var onUpdateProfile: suspend (String?, String?) -> User = { _, _ -> error("updateProfile not stubbed") },
    var onLogout: suspend () -> Unit = { },
) : ProfileRepository {
    override suspend fun mySightings(cursor: String?) = onMySightings(cursor)
    override suspend fun unreadCount() = onUnread()
    override suspend fun uploadAvatar(bytes: ByteArray, filename: String) = onUploadAvatar(bytes, filename)
    override suspend fun deleteAvatar() = onDeleteAvatar()
    override suspend fun updateProfile(username: String?, nickname: String?) = onUpdateProfile(username, nickname)
    override suspend fun logout() = onLogout()
}

class FakeUserRepository(
    var onProfile: suspend (String, String?) -> UserProfile = { _, _ -> error("profile not stubbed") },
) : UserRepository {
    override suspend fun profile(id: String, cursor: String?) = onProfile(id, cursor)
}

class FakeNotificationsRepository(
    var onNotifications: suspend (String?) -> Page<AppNotification> = { error("notifications not stubbed") },
    var onUnread: suspend () -> Long = { 0 },
    var onMarkSeen: suspend () -> Unit = { },
    var onPendingMerges: suspend () -> List<MergeRequest> = { emptyList() },
    var onApproveMerge: suspend (String) -> MergeRequest = { error("approveMerge not stubbed") },
    var onRejectMerge: suspend (String) -> Unit = { },
) : NotificationsRepository {
    override suspend fun notifications(cursor: String?) = onNotifications(cursor)
    override suspend fun unreadCount() = onUnread()
    override suspend fun markSeen() = onMarkSeen()
    override suspend fun pendingMerges() = onPendingMerges()
    override suspend fun approveMerge(id: String) = onApproveMerge(id)
    override suspend fun rejectMerge(id: String) = onRejectMerge(id)
}

class FakeDeviceRepository(
    var onRegister: suspend (String, String) -> Unit = { _, _ -> },
    var onUnregister: suspend (String) -> Unit = { },
) : DeviceRepository {
    override suspend fun register(token: String, platform: String) = onRegister(token, platform)
    override suspend fun unregister(token: String) = onUnregister(token)
}

class FakePushRepository : PushRepository {
    override suspend fun registerCurrent() = Unit
    override suspend fun onTokenRefreshed(token: String) = Unit
    override suspend fun unregisterCurrent() = Unit
}

class FakeLocationRepository(
    var hasPermission: Boolean = true,
    var onCurrent: suspend () -> GeoPoint? = { null },
) : LocationRepository {
    override fun hasPermission() = hasPermission
    override suspend fun current() = onCurrent()
}

/** A one-page [MySightings] wrapping the given rows. */
fun testSightingsPage(
    items: List<Sighting> = emptyList(),
    nextCursor: String? = null,
    sightingsCount: Long = items.size.toLong(),
    catsCount: Long = 0,
    unnamedCount: Long = 0,
) = MySightings(
    items = items,
    nextCursor = nextCursor,
    sightingsCount = sightingsCount,
    catsCount = catsCount,
    unnamedCount = unnamedCount,
)

/** A throwaway [SightingResult] for tests that need the shape, not the content. */
fun testSightingResult(id: String = "s1", catId: String? = null) = SightingResult(
    sighting = Sighting(
        id = id,
        catId = catId,
        photoUrl = "http://test.invalid/p.webp",
        thumbUrl = "http://test.invalid/t.webp",
        lat = -6.2,
        lng = 106.8,
        createdAt = "2026-01-01T00:00:00Z",
    ),
    candidates = emptyList<Candidate>(),
)
