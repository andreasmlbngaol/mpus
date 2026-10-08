package id.andreasmlbngaol.mpus.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Calico palette. A calico cat is exactly three things — white fur, a black patch, and
 * ginger-orange patches — and the theme has to say that at a glance. The trick is not
 * "warm colours": it is *contrast*. An earlier pass filled every role with a soft pastel
 * and the app read as one flat beige wash. The black patch is what makes a calico pop,
 * so the neutral here is near-black ink on near-white fur, with orange as the accent
 * that never flips.
 *
 * Light is white ground / black ink / orange patch. Dark inverts the ground and the ink
 * (black ground / white ink) while the orange stays — exactly how a calico reads.
 *
 * The three M3 accent roles are the three patches:
 *   primary   = ginger orange
 *   secondary = the black patch (near-black in light, near-white in dark)
 *   tertiary  = the plain fur (a neutral, so the third stat block is a calm patch)
 *
 * `error` is the one deliberate outsider: red is a *signal*, not an identity, and a
 * black error would just look like another patch. It is a true crimson (hue ~0), well
 * clear of the orange (hue ~25), so "danger" never reads as "the primary button".
 * Nothing is left to a Material default — an un-overridden role (error especially) falls
 * back to Material's own pink/blue and breaks the illusion.
 */

// --- Ginger orange (primary: the orange patch) ---
internal val CalicoGinger = Color(0xFFB54E14)
internal val CalicoGingerLight = Color(0xFFFFB273)
internal val CalicoGingerContainer = Color(0xFFFFB273)
internal val CalicoGingerContainerDark = Color(0xFF8A3E0A)
internal val CalicoOnGingerContainer = Color(0xFF3A1400)
internal val CalicoOnGinger = Color(0xFFFFFDFB)
internal val CalicoOnGingerDark = Color(0xFF3A1400)

// --- Black patch (secondary: near-black ink in light, near-white fur in dark) ---
internal val CalicoCharcoal = Color(0xFF1C1916)
internal val CalicoCharcoalLight = Color(0xFFF3EFEA)
internal val CalicoCharcoalContainer = Color(0xFF1C1916)
internal val CalicoCharcoalContainerDark = Color(0xFFF3EFEA)
internal val CalicoOnCharcoalContainer = Color(0xFFFFFDFB)
internal val CalicoOnCharcoal = Color(0xFFFFFDFB)
internal val CalicoOnCharcoalDark = Color(0xFF1C1916)

// --- Plain fur (tertiary: a neutral patch, not a fourth hue) ---
internal val CalicoFur = Color(0xFF6E6862)
internal val CalicoFurLight = Color(0xFFCFC8C0)
internal val CalicoFurContainer = Color(0xFFE7E1DA)
internal val CalicoFurContainerDark = Color(0xFF3A342F)
internal val CalicoOnFurContainer = Color(0xFF1F1B17)
internal val CalicoOnFur = Color(0xFFFFFDFB)
internal val CalicoOnFurDark = Color(0xFF1F1B17)

// --- Crimson (error: a true red signal, deliberately not orange, not pink) ---
internal val CalicoError = Color(0xFFC62828)
internal val CalicoErrorLight = Color(0xFFFFB4AB)
internal val CalicoErrorContainer = Color(0xFFFFDAD6)
internal val CalicoErrorContainerDark = Color(0xFF93000A)
internal val CalicoOnErrorContainer = Color(0xFF410002)
internal val CalicoOnError = Color(0xFFFFFDFB)
internal val CalicoOnErrorDark = Color(0xFF690005)

// --- Fur base (neutrals): near-white in light, near-black in dark ---
internal val CalicoSurface = Color(0xFFFFFDFB)
internal val CalicoOnSurface = Color(0xFF14110E)
internal val CalicoSurfaceDim = Color(0xFFE6E0DA)
internal val CalicoSurfaceBright = Color(0xFFFFFDFB)
internal val CalicoSurfaceLowest = Color(0xFFFFFEFC)
internal val CalicoSurfaceLow = Color(0xFFFAF6F2)
internal val CalicoSurfaceContainer = Color(0xFFF3EFEA)
internal val CalicoSurfaceHigh = Color(0xFFEDE8E2)
internal val CalicoSurfaceHighest = Color(0xFFE7E1DA)
internal val CalicoSurfaceVariant = Color(0xFFE7E1DA)
internal val CalicoOnSurfaceVariant = Color(0xFF4A443E)
internal val CalicoOutline = Color(0xFF7D766E)
internal val CalicoOutlineVariant = Color(0xFFCFC8C0)

internal val CalicoDarkSurface = Color(0xFF100E0C)
internal val CalicoDarkOnSurface = Color(0xFFF3EFEA)
internal val CalicoDarkSurfaceDim = Color(0xFF100E0C)
internal val CalicoDarkSurfaceBright = Color(0xFF2A2724)
internal val CalicoDarkSurfaceLowest = Color(0xFF0B0908)
internal val CalicoDarkSurfaceLow = Color(0xFF16130F)
internal val CalicoDarkSurfaceContainer = Color(0xFF1C1916)
internal val CalicoDarkSurfaceHigh = Color(0xFF272320)
internal val CalicoDarkSurfaceHighest = Color(0xFF332E2A)
internal val CalicoDarkSurfaceVariant = Color(0xFF3A342F)
internal val CalicoDarkOnSurfaceVariant = Color(0xFFCFC8C0)
internal val CalicoDarkOutline = Color(0xFF968E86)
internal val CalicoDarkOutlineVariant = Color(0xFF4A443E)

internal val CalicoInverseSurface = Color(0xFF2E2A26)
internal val CalicoInverseOnSurface = Color(0xFFFFEDE0)
