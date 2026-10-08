package id.andreasmlbngaol.mpus.core.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Shared-axis navigation transitions, ported from PixelPlayer.
 *
 * The M3 expressive "emphasized" easing is `CubicBezierEasing(0.2, 0, 0, 1)` — a slow
 * start and a long settle. Screen-to-screen motion slides a third of the width and
 * cross-fades, which reads as one plane moving rather than two swapping.
 *
 * Back (pop) is a plain slide-right + fade: no scale, so going back never looks like the
 * screen is being pinched away.
 */
private const val DURATION = 380
private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val AccelerateEasing: Easing = Easing { f -> f * f * f }

fun sharedAxisEnter(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it / 3 },
        animationSpec = tween(DURATION, easing = EmphasizedEasing),
    ) + fadeIn(animationSpec = tween(DURATION, easing = EmphasizedEasing))

fun sharedAxisExit(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(DURATION, easing = EmphasizedEasing),
    ) + fadeOut(animationSpec = tween(DURATION, easing = EmphasizedEasing))

fun sharedAxisPopEnter(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(DURATION, easing = EmphasizedEasing),
    ) + fadeIn(animationSpec = tween(DURATION, easing = EmphasizedEasing))

fun sharedAxisPopExit(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DURATION, easing = AccelerateEasing),
    ) + fadeOut(animationSpec = tween(DURATION, easing = EmphasizedEasing))

/**
 * Predictive back. The framework's own default for these is `scaleOut(0.7f)` — the screen
 * visibly pinches as you drag the edge, which is exactly the "mengecil kayak dicubit" the
 * user rejected. Override both so the gesture only slides and fades, same as a plain pop.
 */
fun sharedAxisPredictivePopEnter(): EnterTransition =
    fadeIn(animationSpec = tween(DURATION, easing = EmphasizedEasing))

fun sharedAxisPredictivePopExit(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DURATION, easing = AccelerateEasing),
    ) + fadeOut(animationSpec = tween(DURATION, easing = EmphasizedEasing))
