package id.andreasmlbngaol.mpus.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import id.andreasmlbngaol.mpus.R

/**
 * Google Sans Rounded, the typeface Material 3 Expressive uses on Pixel.
 *
 * We ship the variable "Google Sans Flex" (SIL OFL 1.1, see assets/licenses) and pin its
 * `ROND` axis to 100 to get the rounded look; the weight axis is driven per style. This is
 * the same trick PixelPlayer uses, and it keeps the expressive type scale intact — we only
 * swap the family, every size/weight/line-height still comes from the M3 tokens.
 */
private const val GoogleSansFlexRounded = 100f

@OptIn(ExperimentalTextApi::class)
private fun rounded(weight: FontWeight): Font =
    Font(
        resId = R.font.gflex_variable,
        weight = weight,
        variationSettings =
            FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.Setting("ROND", GoogleSansFlexRounded),
            ),
    )

internal val GoogleSansRounded: FontFamily =
    FontFamily(
        rounded(FontWeight.Light),
        rounded(FontWeight.Normal),
        rounded(FontWeight.Medium),
        rounded(FontWeight.SemiBold),
        rounded(FontWeight.Bold),
    )

/** M3 expressive type scale, rendered in Google Sans Rounded. */
internal val MPUSTypography: Typography = Typography(fontFamily = GoogleSansRounded)
