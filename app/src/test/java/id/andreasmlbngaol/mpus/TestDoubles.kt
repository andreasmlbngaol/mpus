package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.AppNotification
import id.andreasmlbngaol.mpus.data.CatDetail
import id.andreasmlbngaol.mpus.data.CatMarker
import id.andreasmlbngaol.mpus.data.LikeData
import id.andreasmlbngaol.mpus.data.LoginData
import id.andreasmlbngaol.mpus.data.MergeRequest
import id.andreasmlbngaol.mpus.data.MySightings
import id.andreasmlbngaol.mpus.data.Page
import id.andreasmlbngaol.mpus.data.ReportResult
import id.andreasmlbngaol.mpus.data.ResolveData
import id.andreasmlbngaol.mpus.data.Session
import id.andreasmlbngaol.mpus.data.Sighting
import id.andreasmlbngaol.mpus.data.SightingResult
import id.andreasmlbngaol.mpus.data.UnreadCount
import id.andreasmlbngaol.mpus.data.User
import id.andreasmlbngaol.mpus.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [Session], so ViewModels can be tested without SharedPreferences. */
class FakeSession(
    token: String? = null,
    user: User? = null,
) : Session {
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
 * An [ApiClient] with every network call stubbed. Each property is a lambda so a test
 * can override just the call it cares about; anything untouched throws, which keeps a
 * test from silently depending on a call it didn't set up.
 */
class FakeApi(
    var onLogin: suspend (String, String) -> LoginData = { _, _ -> error("login not stubbed") },
    var onSignup: suspend (String, String, String, String) -> LoginData = { _, _, _, _ -> error("signup not stubbed") },
    var onLogout: suspend () -> Unit = { },
    var onVerifyEmail: suspend (String) -> User = { error("verifyEmail not stubbed") },
    var onResendVerification: suspend (String) -> String = { error("resendVerification not stubbed") },
    var onMe: suspend () -> User = { error("me not stubbed") },
    var onPatchMe: suspend (String?, String?) -> User = { _, _ -> error("patchMe not stubbed") },
    var onUploadAvatar: suspend (ByteArray, String) -> User = { _, _ -> error("uploadAvatar not stubbed") },
    var onDeleteAvatar: suspend () -> User = { error("deleteAvatar not stubbed") },
    var onMySightings: suspend (String?) -> MySightings = { error("mySightings not stubbed") },
    var onCats: suspend (Double, Double, Double, Double) -> List<CatMarker> = { _, _, _, _ -> error("cats not stubbed") },
    var onCatDetail: suspend (String) -> CatDetail = { error("catDetail not stubbed") },
    var onSetName: suspend (String, String) -> CatDetail = { _, _ -> error("setName not stubbed") },
    var onLike: suspend (String) -> LikeData = { error("like not stubbed") },
    var onSetReview: suspend (String, String, Int) -> CatDetail = { _, _, _ -> error("setReview not stubbed") },
    var onLikeReview: suspend (String) -> LikeData = { error("likeReview not stubbed") },
    var onUserProfile: suspend (String, String?) -> UserProfile = { _, _ -> error("userProfile not stubbed") },
    var onReport: suspend (String, String, String?) -> ReportResult = { _, _, _ -> error("report not stubbed") },
    var onNotifications: suspend (String?) -> Page<AppNotification> = { error("notifications not stubbed") },
    var onUnread: suspend () -> UnreadCount = { UnreadCount(0) },
    var onMarkSeen: suspend () -> Unit = { },
    var onRequestMerge: suspend (String, String) -> MergeRequest = { _, _ -> error("requestMerge not stubbed") },
    var onPendingMerges: suspend () -> List<MergeRequest> = { emptyList() },
    var onApproveMerge: suspend (String) -> MergeRequest = { error("approveMerge not stubbed") },
    var onRejectMerge: suspend (String) -> Unit = { },
    var onRegisterDevice: suspend (String, String) -> Unit = { _, _ -> },
    var onUnregisterDevice: suspend (String) -> Unit = { },
) : ApiClient("http://test.invalid", FakeSession()) {

    override suspend fun login(email: String, password: String): LoginData = onLogin(email, password)
    override suspend fun signup(email: String, password: String, username: String, nickname: String): LoginData =
        onSignup(email, password, username, nickname)
    override suspend fun logout() = onLogout()
    override suspend fun verifyEmail(token: String): User = onVerifyEmail(token)
    override suspend fun resendVerification(email: String): String = onResendVerification(email)
    override suspend fun me(): User = onMe()
    override suspend fun patchMe(username: String?, nickname: String?): User = onPatchMe(username, nickname)
    override suspend fun uploadAvatar(bytes: ByteArray, filename: String): User = onUploadAvatar(bytes, filename)
    override suspend fun deleteAvatar(): User = onDeleteAvatar()
    override suspend fun mySightings(cursor: String?): MySightings = onMySightings(cursor)
    override suspend fun cats(minLat: Double, minLng: Double, maxLat: Double, maxLng: Double): List<CatMarker> =
        onCats(minLat, minLng, maxLat, maxLng)
    override suspend fun catDetail(id: String): CatDetail = onCatDetail(id)
    override suspend fun setName(catId: String, name: String): CatDetail = onSetName(catId, name)
    override suspend fun like(nameId: String): LikeData = onLike(nameId)
    override suspend fun setReview(catId: String, body: String, rating: Int): CatDetail =
        onSetReview(catId, body, rating)
    override suspend fun likeReview(reviewId: String): LikeData = onLikeReview(reviewId)
    override suspend fun userProfile(id: String, cursor: String?): UserProfile = onUserProfile(id, cursor)
    override suspend fun report(targetKind: String, targetId: String, reason: String?): ReportResult =
        onReport(targetKind, targetId, reason)
    override suspend fun notifications(cursor: String?): Page<AppNotification> = onNotifications(cursor)
    override suspend fun unreadCount(): UnreadCount = onUnread()
    override suspend fun markNotificationsSeen() = onMarkSeen()
    override suspend fun requestMerge(sourceCatId: String, targetCatId: String): MergeRequest =
        onRequestMerge(sourceCatId, targetCatId)
    override suspend fun pendingMerges(): List<MergeRequest> = onPendingMerges()
    override suspend fun approveMerge(id: String): MergeRequest = onApproveMerge(id)
    override suspend fun rejectMerge(id: String) = onRejectMerge(id)
    override suspend fun registerDevice(token: String, platform: String) = onRegisterDevice(token, platform)
    override suspend fun unregisterDevice(token: String) = onUnregisterDevice(token)
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
)

fun testResolve(catId: String) = ResolveData(catId)
