package id.andreasmlbngaol.mpus.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

/**
 * The squircle corner used across the expressive UI, ported from PixelPlayer.
 *
 * [AbsoluteSmoothCornerShape] draws a superellipse-ish corner (a "squircle") rather than
 * a circular arc, which is what gives Material 3 Expressive surfaces their soft, pill-like
 * silhouette. `smoothnessAsPercent = 60` is the value PixelPlayer uses everywhere; the
 * higher the percentage the straighter the corner run into the edge.
 *
 * These are singletons on purpose: the shape allocates nothing per draw and Compose can
 * skip work when the same instance is passed repeatedly.
 */
object ShapeCache {
    val smooth12 = AbsoluteSmoothCornerShape(12.dp, 60)
    val smooth16 = AbsoluteSmoothCornerShape(16.dp, 60)
    val smooth18 = AbsoluteSmoothCornerShape(18.dp, 60)
    val smooth20 = AbsoluteSmoothCornerShape(20.dp, 60)
    val smooth24 = AbsoluteSmoothCornerShape(24.dp, 60)
    val smooth28 = AbsoluteSmoothCornerShape(28.dp, 60)
    val smooth32 = AbsoluteSmoothCornerShape(32.dp, 60)
    val smoothPill = AbsoluteSmoothCornerShape(50.dp, 60)
}

/**
 * Theme shape tokens, all squircled. Wired into [MaterialExpressiveTheme] so every stock
 * component that reads `MaterialTheme.shapes` (cards, dialogs, sheets, text fields) picks
 * up the smooth corners without a per-call-site change.
 */
internal val MPUSShapes = Shapes(
    extraSmall = ShapeCache.smooth12,
    small = ShapeCache.smooth16,
    medium = ShapeCache.smooth20,
    large = ShapeCache.smooth24,
    extraLarge = ShapeCache.smooth32,
)

/** Hero and body buttons. Kept as named aliases so call sites read the same as before. */
internal val SquircleShape = ShapeCache.smooth28

/** Larger squircle for full-width primary buttons. */
internal val SquircleShapeLarge = ShapeCache.smooth32
