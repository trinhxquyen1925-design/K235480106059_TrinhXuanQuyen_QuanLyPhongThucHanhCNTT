package com.quyen.quanlyphongmay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val PremiumLightColorScheme = lightColorScheme(
    primary = AppBlueDeep,
    onPrimary = Color.White,
    primaryContainer = AppBlueSoft,
    onPrimaryContainer = AppInk,

    secondary = AppCyanDeep,
    onSecondary = Color.White,
    secondaryContainer = AppCyanSoft,
    onSecondaryContainer = AppInk,

    tertiary = AppPurple,
    onTertiary = Color.White,
    tertiaryContainer = AppPurpleSoft,
    onTertiaryContainer = AppInk,

    background = AppBgMiddle,
    onBackground = AppText,

    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSurfaceSoft,
    onSurfaceVariant = AppMuted,

    surfaceTint = AppBlue,
    inverseSurface = AppInk,
    inverseOnSurface = Color.White,
    inversePrimary = AppCyan,

    outline = AppBorder,
    outlineVariant = AppBorderSoft,

    error = AppRose,
    onError = Color.White,
    errorContainer = AppRoseSoft,
    onErrorContainer = AppRose,

    scrim = AppScrim
)

private val PremiumDarkColorScheme = darkColorScheme(
    primary = AppCyan,
    onPrimary = AppInk,
    primaryContainer = Color(0xFF12345F),
    onPrimaryContainer = Color.White,

    secondary = AppBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF14375B),
    onSecondaryContainer = Color.White,

    tertiary = AppPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3D2A66),
    onTertiaryContainer = Color.White,

    background = Color(0xFF07111F),
    onBackground = Color(0xFFEAF2FB),

    surface = Color(0xFF0D1B2E),
    onSurface = Color(0xFFEAF2FB),
    surfaceVariant = Color(0xFF12243A),
    onSurfaceVariant = Color(0xFFB8C4D2),

    surfaceTint = AppCyan,
    inverseSurface = Color(0xFFEAF2FB),
    inverseOnSurface = Color(0xFF07111F),
    inversePrimary = AppBlueDeep,

    outline = Color(0xFF2B405B),
    outlineVariant = Color(0xFF1E314A),

    error = Color(0xFFFF8A9D),
    onError = Color(0xFF3B0712),
    errorContainer = Color(0xFF5E1022),
    onErrorContainer = Color(0xFFFFD9DF),

    scrim = Color(0x99000000)
)

private val PremiumTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.2).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.0).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.7).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.5).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.3).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.2).sp,
        color = AppInk,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = AppText,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
        color = AppMuted,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = AppText,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
        color = AppMuted,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = AppSoftText,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
)

private val PremiumShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32)
)

@Immutable
data class PremiumSystemBars(
    val statusBarColor: Color,
    val navigationBarColor: Color,
    val darkIcons: Boolean
)

@Stable
object QuanLyPhongMayThemeDefaults {
    val LightSystemBars = PremiumSystemBars(
        statusBarColor = AppBgTop,
        navigationBarColor = AppBgBottom,
        darkIcons = true
    )

    val DarkSystemBars = PremiumSystemBars(
        statusBarColor = Color(0xFF07111F),
        navigationBarColor = Color(0xFF07111F),
        darkIcons = false
    )

    fun colorScheme(
        darkTheme: Boolean
    ): ColorScheme {
        return if (darkTheme) {
            PremiumDarkColorScheme
        } else {
            PremiumLightColorScheme
        }
    }

    fun systemBars(
        darkTheme: Boolean
    ): PremiumSystemBars {
        return if (darkTheme) {
            DarkSystemBars
        } else {
            LightSystemBars
        }
    }
}

@Composable
fun QuanLyPhongMayTheme(
    darkTheme: Boolean = false,
    useSystemDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val shouldUseDarkTheme = if (useSystemDarkTheme) {
        isSystemInDarkTheme()
    } else {
        darkTheme
    }

    val colorScheme = QuanLyPhongMayThemeDefaults.colorScheme(
        darkTheme = shouldUseDarkTheme
    )

    val systemBars = QuanLyPhongMayThemeDefaults.systemBars(
        darkTheme = shouldUseDarkTheme
    )

    ApplyPremiumSystemBars(
        systemBars = systemBars
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PremiumTypography,
        shapes = PremiumShapes,
        content = content
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    useSystemDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    QuanLyPhongMayTheme(
        darkTheme = darkTheme,
        useSystemDarkTheme = useSystemDarkTheme,
        content = content
    )
}

@Composable
private fun ApplyPremiumSystemBars(
    systemBars: PremiumSystemBars
) {
    val view = LocalView.current

    if (view.isInEditMode) {
        return
    }

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect

        window.statusBarColor = systemBars.statusBarColor.toArgb()
        window.navigationBarColor = systemBars.navigationBarColor.toArgb()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = systemBars.darkIcons
        insetsController.isAppearanceLightNavigationBars = systemBars.darkIcons

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }
}