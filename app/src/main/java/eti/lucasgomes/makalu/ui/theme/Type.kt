package eti.lucasgomes.makalu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import eti.lucasgomes.makalu.R

val didactGothicFamily = FontFamily(
    Font(R.font.didact_gothic)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.0.sp,
        letterSpacing = (-0.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.0.sp,
        letterSpacing = 0.0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.0.sp,
        letterSpacing = 0.0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.0.sp,
        letterSpacing = 0.0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.0.sp,
        letterSpacing = 0.0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.0.sp,
        letterSpacing = 0.0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.0.sp,
        letterSpacing = 0.2.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.0.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.0.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.0.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.0.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.0.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = didactGothicFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)