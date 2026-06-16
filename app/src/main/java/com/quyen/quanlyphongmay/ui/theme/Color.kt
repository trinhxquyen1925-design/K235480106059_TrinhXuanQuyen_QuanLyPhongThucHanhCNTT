package com.quyen.quanlyphongmay.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val AppBgTop = Color(0xFFF9FDFF)
val AppBgMiddle = Color(0xFFF1F8FF)
val AppBgBottom = Color(0xFFE7F8FF)

val AppSurface = Color(0xFFFFFFFF)
val AppSurfacePremium = Color(0xFCFFFFFF)
val AppSurfaceSoft = Color(0xFFF7FAFF)
val AppSurfaceMuted = Color(0xFFF4F7FB)
val AppField = Color(0xFFFFFFFF)

val AppInk = Color(0xFF0E1A33)
val AppText = Color(0xFF20314C)
val AppTextSecondary = Color(0xFF4A5B73)
val AppMuted = Color(0xFF73849B)
val AppSoftText = Color(0xFF96A7BA)
val AppDisabledText = Color(0xFFB8C4D2)

val AppBorder = Color(0xFFD9E7F5)
val AppBorderSoft = Color(0xFFEAF2FB)
val AppBorderFocus = Color(0xFF2F83FF)

val AppCyan = Color(0xFF25C7D8)
val AppCyanDeep = Color(0xFF15A8CB)
val AppBlue = Color(0xFF4287F5)
val AppBlueDeep = Color(0xFF2666EA)
val AppPurple = Color(0xFF8B5CF6)
val AppMint = Color(0xFF2FD4BA)
val AppGreen = Color(0xFF16A34A)
val AppAmber = Color(0xFFF5A524)
val AppOrange = Color(0xFFF97316)
val AppRose = Color(0xFFE64B66)
val AppRed = Color(0xFFDC2626)
val AppSlate = Color(0xFF64748B)

val AppCyanSoft = Color(0xFFE8FCFF)
val AppBlueSoft = Color(0xFFEDF5FF)
val AppPurpleSoft = Color(0xFFF4EEFF)
val AppMintSoft = Color(0xFFE9FFF8)
val AppGreenSoft = Color(0xFFEAFBF1)
val AppAmberSoft = Color(0xFFFFF7E6)
val AppOrangeSoft = Color(0xFFFFF3E8)
val AppRoseSoft = Color(0xFFFFEEF3)
val AppRedSoft = Color(0xFFFFEDED)
val AppSlateSoft = Color(0xFFF1F5F9)

val AppWhiteAlpha96 = Color(0xF5FFFFFF)
val AppWhiteAlpha90 = Color(0xE6FFFFFF)
val AppWhiteAlpha80 = Color(0xCCFFFFFF)
val AppWhiteAlpha70 = Color(0xB3FFFFFF)

val AppScrim = Color(0x660E1A33)
val AppTransparent = Color(0x00000000)

val AppShadowBlue = Color(0x1F4287F5)
val AppShadowCyan = Color(0x1F25C7D8)
val AppShadowDark = Color(0x260E1A33)

val AppChartBlue = Color(0xFF3B82F6)
val AppChartCyan = Color(0xFF06B6D4)
val AppChartGreen = Color(0xFF22C55E)
val AppChartAmber = Color(0xFFF59E0B)
val AppChartRose = Color(0xFFF43F5E)
val AppChartPurple = Color(0xFF8B5CF6)

@Immutable
data class AppGradientColors(
    val start: Color,
    val middle: Color,
    val end: Color
)

@Immutable
data class AppStatusColors(
    val foreground: Color,
    val background: Color,
    val border: Color
)

@Immutable
data class AppRoleColors(
    val foreground: Color,
    val background: Color,
    val border: Color,
    val gradient: AppGradientColors
)

object QuanLyPhongMayColors {
    val PrimaryGradient = AppGradientColors(
        start = AppCyan,
        middle = AppBlue,
        end = AppBlueDeep
    )

    val CyanGradient = AppGradientColors(
        start = AppCyan,
        middle = AppCyanDeep,
        end = AppBlue
    )

    val BlueGradient = AppGradientColors(
        start = AppBlue,
        middle = AppBlueDeep,
        end = Color(0xFF1D4ED8)
    )

    val PurpleGradient = AppGradientColors(
        start = AppPurple,
        middle = Color(0xFF7C3AED),
        end = AppBlueDeep
    )

    val MintGradient = AppGradientColors(
        start = AppMint,
        middle = AppCyan,
        end = AppBlue
    )

    val GreenGradient = AppGradientColors(
        start = Color(0xFF34D399),
        middle = AppGreen,
        end = Color(0xFF15803D)
    )

    val AmberGradient = AppGradientColors(
        start = Color(0xFFFBBF24),
        middle = AppAmber,
        end = AppOrange
    )

    val RoseGradient = AppGradientColors(
        start = Color(0xFFFB7185),
        middle = AppRose,
        end = AppRed
    )

    val AdminRole = AppRoleColors(
        foreground = AppPurple,
        background = AppPurpleSoft,
        border = AppPurple.copy(alpha = 0.22f),
        gradient = PurpleGradient
    )

    val TeacherRole = AppRoleColors(
        foreground = AppBlueDeep,
        background = AppBlueSoft,
        border = AppBlue.copy(alpha = 0.22f),
        gradient = BlueGradient
    )

    val StudentRole = AppRoleColors(
        foreground = AppCyanDeep,
        background = AppCyanSoft,
        border = AppCyan.copy(alpha = 0.22f),
        gradient = CyanGradient
    )

    val Pending = AppStatusColors(
        foreground = AppAmber,
        background = AppAmberSoft,
        border = AppAmber.copy(alpha = 0.22f)
    )

    val Approved = AppStatusColors(
        foreground = AppGreen,
        background = AppGreenSoft,
        border = AppGreen.copy(alpha = 0.22f)
    )

    val Rejected = AppStatusColors(
        foreground = AppRose,
        background = AppRoseSoft,
        border = AppRose.copy(alpha = 0.22f)
    )

    val Locked = AppStatusColors(
        foreground = AppRed,
        background = AppRedSoft,
        border = AppRed.copy(alpha = 0.22f)
    )

    val Free = AppStatusColors(
        foreground = AppGreen,
        background = AppGreenSoft,
        border = AppGreen.copy(alpha = 0.22f)
    )

    val InUse = AppStatusColors(
        foreground = AppBlueDeep,
        background = AppBlueSoft,
        border = AppBlue.copy(alpha = 0.22f)
    )

    val Broken = AppStatusColors(
        foreground = AppRose,
        background = AppRoseSoft,
        border = AppRose.copy(alpha = 0.22f)
    )

    val Maintenance = AppStatusColors(
        foreground = AppPurple,
        background = AppPurpleSoft,
        border = AppPurple.copy(alpha = 0.22f)
    )

    val Processing = AppStatusColors(
        foreground = AppBlueDeep,
        background = AppBlueSoft,
        border = AppBlue.copy(alpha = 0.22f)
    )

    val Done = AppStatusColors(
        foreground = AppGreen,
        background = AppGreenSoft,
        border = AppGreen.copy(alpha = 0.22f)
    )

    val Cancelled = AppStatusColors(
        foreground = AppSlate,
        background = AppSlateSoft,
        border = AppSlate.copy(alpha = 0.18f)
    )

    val Info = AppStatusColors(
        foreground = AppBlueDeep,
        background = AppBlueSoft,
        border = AppBlue.copy(alpha = 0.18f)
    )

    val Warning = AppStatusColors(
        foreground = AppOrange,
        background = AppOrangeSoft,
        border = AppOrange.copy(alpha = 0.20f)
    )

    val Error = AppStatusColors(
        foreground = AppRose,
        background = AppRoseSoft,
        border = AppRose.copy(alpha = 0.22f)
    )

    fun statusColors(status: String?): AppStatusColors {
        val value = status.orEmpty().trim().lowercase()

        return when (value) {
            "pending", "waiting", "cho_duyet", "chờ duyệt" -> Pending
            "approved", "active", "da_duyet", "đã duyệt" -> Approved
            "rejected", "tu_choi", "từ chối" -> Rejected
            "locked", "khoa", "khóa", "đã khóa" -> Locked
            "free", "available", "rảnh" -> Free
            "in-use", "in_use", "busy", "dang_su_dung", "đang sử dụng" -> InUse
            "broken", "hong", "hỏng" -> Broken
            "maintenance", "bao_tri", "bảo trì" -> Maintenance
            "processing", "dang_xu_ly", "đang xử lý" -> Processing
            "done", "resolved", "da_xu_ly", "đã xử lý" -> Done
            "cancelled", "canceled", "da_huy", "đã hủy" -> Cancelled
            "warning", "canh_bao", "cảnh báo" -> Warning
            "error", "loi", "lỗi" -> Error
            else -> Info
        }
    }

    fun roleColors(role: String?): AppRoleColors {
        val value = role.orEmpty().trim().lowercase()

        return when (value) {
            "admin", "administrator", "quan_tri_vien", "quản trị viên" -> AdminRole
            "teacher", "lecturer", "giao_vien", "giáo viên" -> TeacherRole
            "student", "sinh_vien", "sinh viên" -> StudentRole
            else -> StudentRole
        }
    }

    fun roleLabel(role: String?): String {
        val value = role.orEmpty().trim().lowercase()

        return when (value) {
            "admin", "administrator", "quan_tri_vien", "quản trị viên" -> "Quản trị viên"
            "teacher", "lecturer", "giao_vien", "giáo viên" -> "Giáo viên"
            "student", "sinh_vien", "sinh viên" -> "Sinh viên"
            else -> "Tài khoản"
        }
    }

    fun statusLabel(status: String?): String {
        val value = status.orEmpty().trim().lowercase()

        return when (value) {
            "pending", "waiting", "cho_duyet", "chờ duyệt" -> "Chờ duyệt"
            "approved", "active", "da_duyet", "đã duyệt" -> "Đã duyệt"
            "rejected", "tu_choi", "từ chối" -> "Từ chối"
            "locked", "khoa", "khóa", "đã khóa" -> "Đã khóa"
            "free", "available", "rảnh" -> "Rảnh"
            "in-use", "in_use", "busy", "dang_su_dung", "đang sử dụng" -> "Đang sử dụng"
            "broken", "hong", "hỏng" -> "Hỏng"
            "maintenance", "bao_tri", "bảo trì" -> "Bảo trì"
            "processing", "dang_xu_ly", "đang xử lý" -> "Đang xử lý"
            "done", "resolved", "da_xu_ly", "đã xử lý" -> "Đã xử lý"
            "cancelled", "canceled", "da_huy", "đã hủy" -> "Đã hủy"
            "warning", "canh_bao", "cảnh báo" -> "Cảnh báo"
            "error", "loi", "lỗi" -> "Lỗi"
            else -> "Thông tin"
        }
    }
}