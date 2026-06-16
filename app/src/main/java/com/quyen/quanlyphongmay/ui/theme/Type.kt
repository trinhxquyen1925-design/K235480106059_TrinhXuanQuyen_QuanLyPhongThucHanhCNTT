package com.quyen.quanlyphongmay.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val NoFontPaddingPlatformStyle = PlatformTextStyle(
    includeFontPadding = false
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.2).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.0).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.7).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.5).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.3).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.2).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = AppSoftText,
        platformStyle = NoFontPaddingPlatformStyle
    )
)

@Immutable
data class PremiumTextScale(
    val compactTitle: TextUnit,
    val normalTitle: TextUnit,
    val largeTitle: TextUnit,
    val body: TextUnit,
    val caption: TextUnit
)

@Stable
object AppTextScale {
    val Phone = PremiumTextScale(
        compactTitle = 18.sp,
        normalTitle = 22.sp,
        largeTitle = 32.sp,
        body = 15.sp,
        caption = 12.sp
    )

    val Tablet = PremiumTextScale(
        compactTitle = 20.sp,
        normalTitle = 24.sp,
        largeTitle = 36.sp,
        body = 16.sp,
        caption = 13.sp
    )
}

@Stable
object AppTextStyles {
    val ScreenTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.35).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ScreenTitleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.6).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val LoginHeroTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.7).sp,
        color = AppInk,
        shadow = Shadow(
            color = AppBlue.copy(alpha = 0.12f),
            offset = Offset(0f, 5f),
            blurRadius = 12f
        ),
        platformStyle = NoFontPaddingPlatformStyle
    )

    val TopBarTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.25).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val SectionTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val SectionSubtitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val CardTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.1).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val CardTitleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.15).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val CardBody = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        lineBreak = LineBreak.Paragraph,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val CardBodyStrong = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = AppText,
        lineBreak = LineBreak.Paragraph,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ButtonLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = AppSurface,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ButtonMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = AppSurface,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ButtonSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = AppSurface,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val FieldText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val FieldPlaceholder = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val FieldLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val Badge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.05.sp,
        color = AppBlueDeep,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val BadgeSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.05.sp,
        color = AppBlueDeep,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val StatValue = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.3).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val StatLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val BottomNavLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val DrawerItem = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val DialogTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.2).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val DialogBody = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        lineBreak = LineBreak.Paragraph,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val EmptyTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp,
        color = AppInk,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val EmptyMessage = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        textAlign = TextAlign.Center,
        lineBreak = LineBreak.Paragraph,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ErrorTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = AppRose,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ErrorMessage = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = AppRose,
        lineBreak = LineBreak.Paragraph,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val CalendarDayNumber = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
        color = AppInk,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val CalendarWeekDay = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = AppMuted,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ScheduleTime = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = AppBlueDeep,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ScheduleTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
        color = AppInk,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val ScheduleMeta = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val RoomSeatLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.sp,
        color = AppText,
        textAlign = TextAlign.Center,
        platformStyle = NoFontPaddingPlatformStyle
    )

    val TinyInfo = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.sp,
        color = AppSoftText,
        platformStyle = NoFontPaddingPlatformStyle
    )
}

fun TextStyle.withColor(
    color: Color
): TextStyle {
    return copy(color = color)
}

fun TextStyle.withWeight(
    fontWeight: FontWeight
): TextStyle {
    return copy(fontWeight = fontWeight)
}

fun TextStyle.withSize(
    fontSize: TextUnit
): TextStyle {
    return copy(
        fontSize = fontSize,
        lineHeight = when {
            fontSize.value <= 10f -> (fontSize.value + 3f).sp
            fontSize.value <= 12f -> (fontSize.value + 4f).sp
            fontSize.value <= 15f -> (fontSize.value + 6f).sp
            fontSize.value <= 18f -> (fontSize.value + 7f).sp
            fontSize.value <= 24f -> (fontSize.value + 8f).sp
            else -> (fontSize.value + 9f).sp
        }
    )
}

fun TextStyle.asMuted(): TextStyle {
    return copy(color = AppMuted)
}

fun TextStyle.asSoft(): TextStyle {
    return copy(color = AppSoftText)
}

fun TextStyle.asPrimary(): TextStyle {
    return copy(color = AppBlueDeep)
}

fun TextStyle.asSuccess(): TextStyle {
    return copy(color = AppGreen)
}

fun TextStyle.asWarning(): TextStyle {
    return copy(color = AppAmber)
}

fun TextStyle.asError(): TextStyle {
    return copy(color = AppRose)
}