package dev.zhafran.velnyx.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val AntonFont = GoogleFont("Anton")
private val InterFont = GoogleFont("Inter")

// Anton is a display font with a single weight (400/Normal).
private val AntonFontFamily = FontFamily(
    Font(googleFont = AntonFont, fontProvider = fontProvider, weight = FontWeight.Normal),
)

private val InterFontFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.Bold),
)

fun velnyxTypography() = Typography(
    displayLarge = TextStyle(
        fontFamily = AntonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 96.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = AntonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = AntonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
)
