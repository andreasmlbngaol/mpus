package id.andreasmlbngaol.mpus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Calico theme: a fixed palette inspired by a tricolour calico cat — warm cream fur,
 * ginger-orange patches, and a charcoal "black patch". Material You dynamic colour is
 * intentionally off; it would repaint the app per-device and wash the calico out.
 *
 * Every role below is one of the three calico colours. Nothing is left to a Material
 * default: an un-overridden role (error, in particular) falls back to Material's own
 * pink/blue and instantly breaks the theme.
 *
 * [MaterialExpressiveTheme] supplies the expressive shape tokens and motion scheme;
 * [MPUSTypography] swaps in Google Sans Rounded while keeping the M3 expressive type scale.
 */
@Composable
fun MPUSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) DarkCalico else LightCalico,
        motionScheme = MotionScheme.expressive(),
        typography = MPUSTypography,
        shapes = MPUSShapes,
        content = content,
    )
}

private val LightCalico: ColorScheme =
    lightColorScheme(
        primary = CalicoGinger,
        onPrimary = CalicoOnGinger,
        primaryContainer = CalicoGingerContainer,
        onPrimaryContainer = CalicoOnGingerContainer,
        inversePrimary = CalicoGingerLight,
        secondary = CalicoCharcoal,
        onSecondary = CalicoOnCharcoal,
        secondaryContainer = CalicoCharcoalContainer,
        onSecondaryContainer = CalicoOnCharcoalContainer,
        tertiary = CalicoFur,
        onTertiary = CalicoOnFur,
        tertiaryContainer = CalicoFurContainer,
        onTertiaryContainer = CalicoOnFurContainer,
        error = CalicoError,
        onError = CalicoOnError,
        errorContainer = CalicoErrorContainer,
        onErrorContainer = CalicoOnErrorContainer,
        background = CalicoSurface,
        onBackground = CalicoOnSurface,
        surface = CalicoSurface,
        onSurface = CalicoOnSurface,
        surfaceVariant = CalicoSurfaceVariant,
        onSurfaceVariant = CalicoOnSurfaceVariant,
        surfaceTint = CalicoGinger,
        surfaceBright = CalicoSurfaceBright,
        surfaceDim = CalicoSurfaceDim,
        surfaceContainer = CalicoSurfaceContainer,
        surfaceContainerHigh = CalicoSurfaceHigh,
        surfaceContainerHighest = CalicoSurfaceHighest,
        surfaceContainerLow = CalicoSurfaceLow,
        surfaceContainerLowest = CalicoSurfaceLowest,
        inverseSurface = CalicoInverseSurface,
        inverseOnSurface = CalicoInverseOnSurface,
        outline = CalicoOutline,
        outlineVariant = CalicoOutlineVariant,
    )

private val DarkCalico: ColorScheme =
    darkColorScheme(
        primary = CalicoGingerLight,
        onPrimary = CalicoOnGingerDark,
        primaryContainer = CalicoGingerContainerDark,
        onPrimaryContainer = CalicoGingerContainer,
        inversePrimary = CalicoGinger,
        secondary = CalicoCharcoalLight,
        onSecondary = CalicoOnCharcoalDark,
        secondaryContainer = CalicoCharcoalContainerDark,
        onSecondaryContainer = CalicoOnCharcoalDark,
        tertiary = CalicoFurLight,
        onTertiary = CalicoOnFurDark,
        tertiaryContainer = CalicoFurContainerDark,
        onTertiaryContainer = CalicoOnFurContainer,
        error = CalicoErrorLight,
        onError = CalicoOnErrorDark,
        errorContainer = CalicoErrorContainerDark,
        onErrorContainer = CalicoErrorContainer,
        background = CalicoDarkSurface,
        onBackground = CalicoDarkOnSurface,
        surface = CalicoDarkSurface,
        onSurface = CalicoDarkOnSurface,
        surfaceVariant = CalicoDarkSurfaceVariant,
        onSurfaceVariant = CalicoDarkOnSurfaceVariant,
        surfaceTint = CalicoGingerLight,
        surfaceBright = CalicoDarkSurfaceBright,
        surfaceDim = CalicoDarkSurfaceDim,
        surfaceContainer = CalicoDarkSurfaceContainer,
        surfaceContainerHigh = CalicoDarkSurfaceHigh,
        surfaceContainerHighest = CalicoDarkSurfaceHighest,
        surfaceContainerLow = CalicoDarkSurfaceLow,
        surfaceContainerLowest = CalicoDarkSurfaceLowest,
        inverseSurface = CalicoInverseOnSurface,
        inverseOnSurface = CalicoInverseSurface,
        outline = CalicoDarkOutline,
        outlineVariant = CalicoDarkOutlineVariant,
    )
