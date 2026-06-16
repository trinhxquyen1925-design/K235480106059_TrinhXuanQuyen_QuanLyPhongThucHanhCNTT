package com.quyen.quanlyphongmay.navigation

import com.quyen.quanlyphongmay.model.User

object AppRoutes {
    const val HOME = "home"

    const val ACCOUNT = "account"
    const val ACCOUNT_MANAGEMENT = "account_management"
    const val ACCOUNT_APPROVAL = "account_approval"

    const val ROOMS = "rooms"
    const val ROOM_DETAIL = "room_detail"
    const val ROOM_FORM = "room_form"

    const val COMPUTERS = "computers"
    const val COMPUTER_DETAIL = "computer_detail"
    const val COMPUTER_FORM = "computer_form"
    const val COMPUTER_BULK_CREATE = "computer_bulk_create"
    const val COMPUTER_MAP = "computer_map"

    const val BOOKINGS = "bookings"
    const val BOOKING_FORM = "booking_form"
    const val BOOKING_APPROVAL = "booking_approval"
    const val BOOKING_HISTORY = "booking_history"

    const val SCHEDULE = "schedule"

    const val MACHINE_REPORTS = "machine_reports"
    const val MACHINE_REPORT_FORM = "machine_report_form"
    const val MACHINE_REPORT_DETAIL = "machine_report_detail"

    const val STATISTICS = "statistics"
    const val EXPORT_DATA = "export_data"

    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val CHANGE_PASSWORD = "change_password"

    private val allRoutes: List<AppRoute> = listOf(
        AppRoute(
            route = HOME,
            title = "Trang chủ",
            description = "Tổng quan hệ thống phòng máy.",
            group = AppRouteGroup.Main,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = true,
            drawerVisible = true
        ),

        AppRoute(
            route = SCHEDULE,
            title = "Lịch",
            description = "Xem lịch booking, lịch học và sơ đồ chỗ ngồi.",
            group = AppRouteGroup.Schedule,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = true,
            drawerVisible = true
        ),

        AppRoute(
            route = NOTIFICATIONS,
            title = "Thông báo",
            description = "Thông báo Firestore theo tài khoản.",
            group = AppRouteGroup.Notification,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = true,
            drawerVisible = true
        ),

        AppRoute(
            route = ACCOUNT,
            title = "Tài khoản",
            description = "Thông tin tài khoản.",
            group = AppRouteGroup.Account,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = true,
            drawerVisible = true
        ),

        AppRoute(
            route = ACCOUNT_MANAGEMENT,
            title = "Quản lý tài khoản",
            description = "Tìm kiếm, lọc, khóa, mở khóa và xử lý tài khoản.",
            group = AppRouteGroup.Account,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = ACCOUNT_APPROVAL,
            title = "Duyệt tài khoản",
            description = "Duyệt hoặc từ chối tài khoản đăng ký mới.",
            group = AppRouteGroup.Account,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = ROOMS,
            title = "Phòng máy",
            description = "Quản lý phòng máy, hàng/cột, sức chứa và trạng thái.",
            group = AppRouteGroup.Room,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = ROOM_DETAIL,
            title = "Chi tiết phòng",
            description = "Xem thông tin chi tiết của phòng máy.",
            group = AppRouteGroup.Room,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = ROOM_FORM,
            title = "Thêm phòng máy",
            description = "Tạo hoặc cập nhật thông tin phòng máy.",
            group = AppRouteGroup.Room,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = COMPUTERS,
            title = "Máy tính",
            description = "Quản lý máy tính, cấu hình, trạng thái và vị trí.",
            group = AppRouteGroup.Computer,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = COMPUTER_DETAIL,
            title = "Chi tiết máy",
            description = "Xem chi tiết cấu hình và trạng thái máy tính.",
            group = AppRouteGroup.Computer,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = COMPUTER_FORM,
            title = "Thêm máy tính",
            description = "Thêm hoặc cập nhật máy tính.",
            group = AppRouteGroup.Computer,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = COMPUTER_BULK_CREATE,
            title = "Thêm máy hàng loạt",
            description = "Tạo máy tính hàng loạt theo sơ đồ hàng/cột của phòng.",
            group = AppRouteGroup.Computer,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = COMPUTER_MAP,
            title = "Sơ đồ phòng",
            description = "Xem sơ đồ phòng máy dạng hàng/cột.",
            group = AppRouteGroup.Computer,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = BOOKINGS,
            title = "Booking",
            description = "Quản lý booking phòng máy.",
            group = AppRouteGroup.Booking,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = BOOKING_FORM,
            title = "Tạo booking",
            description = "Tạo booking phòng máy cho lớp học.",
            group = AppRouteGroup.Booking,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = BOOKING_APPROVAL,
            title = "Duyệt booking",
            description = "Duyệt hoặc từ chối booking phòng máy.",
            group = AppRouteGroup.Booking,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = BOOKING_HISTORY,
            title = "Lịch sử booking",
            description = "Xem lịch sử booking phòng máy.",
            group = AppRouteGroup.Booking,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = MACHINE_REPORTS,
            title = "Báo lỗi máy",
            description = "Theo dõi báo lỗi máy tính.",
            group = AppRouteGroup.Report,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = MACHINE_REPORT_FORM,
            title = "Tạo báo lỗi",
            description = "Gửi báo lỗi máy tính trong phòng máy.",
            group = AppRouteGroup.Report,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = MACHINE_REPORT_DETAIL,
            title = "Chi tiết báo lỗi",
            description = "Xem chi tiết báo lỗi máy tính.",
            group = AppRouteGroup.Report,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = false
        ),

        AppRoute(
            route = STATISTICS,
            title = "Thống kê",
            description = "Thống kê tài khoản, phòng máy, máy tính và booking.",
            group = AppRouteGroup.Report,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = EXPORT_DATA,
            title = "Xuất dữ liệu",
            description = "Xuất dữ liệu phục vụ quản trị hệ thống.",
            group = AppRouteGroup.Report,
            adminAllowed = true,
            teacherAllowed = false,
            studentAllowed = false,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = SETTINGS,
            title = "Cài đặt",
            description = "Cài đặt hệ thống và tùy chỉnh tài khoản.",
            group = AppRouteGroup.System,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = true
        ),

        AppRoute(
            route = CHANGE_PASSWORD,
            title = "Đổi mật khẩu",
            description = "Đổi mật khẩu tài khoản hiện tại.",
            group = AppRouteGroup.Account,
            adminAllowed = true,
            teacherAllowed = true,
            studentAllowed = true,
            bottomVisible = false,
            drawerVisible = false
        )
    )

    fun all(): List<AppRoute> {
        return allRoutes
    }

    fun find(route: String): AppRoute? {
        val cleanRoute = normalizeRoute(route)
        return allRoutes.firstOrNull { item ->
            item.route == cleanRoute
        }
    }

    fun titleOf(route: String): String {
        return find(route)?.title ?: "Trang chủ"
    }

    fun descriptionOf(route: String): String {
        return find(route)?.description.orEmpty()
    }

    fun defaultStartRouteFor(user: User): String {
        val safeUser = user.normalizedCopy()

        return when {
            safeUser.isAdmin -> HOME
            safeUser.isTeacher -> HOME
            safeUser.isStudent -> HOME
            else -> HOME
        }
    }

    fun firstAllowedRouteOrHome(
        user: User,
        requestedRoute: String
    ): String {
        val cleanRoute = normalizeRoute(requestedRoute)

        if (canOpenRoute(user, cleanRoute)) {
            return cleanRoute
        }

        val defaultRoute = defaultStartRouteFor(user)

        if (canOpenRoute(user, defaultRoute)) {
            return defaultRoute
        }

        return ACCOUNT
    }

    fun canOpenRoute(
        user: User,
        route: String
    ): Boolean {
        val safeUser = user.normalizedCopy()
        val cleanRoute = normalizeRoute(route)
        val targetRoute = find(cleanRoute) ?: return false

        if (cleanRoute == ACCOUNT || cleanRoute == CHANGE_PASSWORD) {
            return true
        }

        if (!safeUser.isApproved) {
            return cleanRoute == ACCOUNT ||
                    cleanRoute == NOTIFICATIONS ||
                    cleanRoute == CHANGE_PASSWORD
        }

        return when {
            safeUser.isAdmin -> targetRoute.adminAllowed
            safeUser.isTeacher -> targetRoute.teacherAllowed
            safeUser.isStudent -> targetRoute.studentAllowed
            else -> cleanRoute == HOME ||
                    cleanRoute == ACCOUNT ||
                    cleanRoute == NOTIFICATIONS ||
                    cleanRoute == CHANGE_PASSWORD
        }
    }

    fun bottomRoutesFor(user: User): List<AppRoute> {
        val safeUser = user.normalizedCopy()

        return listOf(
            HOME,
            SCHEDULE,
            NOTIFICATIONS,
            ACCOUNT
        ).mapNotNull { route ->
            find(route)
        }.filter { route ->
            route.bottomVisible && canOpenRoute(safeUser, route.route)
        }
    }

    fun drawerRoutesFor(user: User): List<AppRoute> {
        val safeUser = user.normalizedCopy()

        val preferredRoutes = when {
            safeUser.isAdmin -> listOf(
                HOME,
                ACCOUNT_APPROVAL,
                ACCOUNT_MANAGEMENT,
                BOOKING_APPROVAL,
                BOOKINGS,
                ROOMS,
                COMPUTERS,
                COMPUTER_MAP,
                NOTIFICATIONS,
                STATISTICS,
                EXPORT_DATA,
                SETTINGS,
                ACCOUNT
            )

            safeUser.isTeacher -> listOf(
                HOME,
                BOOKING_FORM,
                BOOKINGS,
                BOOKING_HISTORY,
                SCHEDULE,
                ROOMS,
                COMPUTERS,
                COMPUTER_MAP,
                NOTIFICATIONS,
                MACHINE_REPORTS,
                SETTINGS,
                ACCOUNT
            )

            safeUser.isStudent -> listOf(
                HOME,
                SCHEDULE,
                COMPUTER_MAP,
                ROOMS,
                COMPUTERS,
                NOTIFICATIONS,
                MACHINE_REPORTS,
                SETTINGS,
                ACCOUNT
            )

            else -> listOf(
                HOME,
                NOTIFICATIONS,
                ACCOUNT
            )
        }

        return preferredRoutes
            .mapNotNull { route -> find(route) }
            .filter { route ->
                route.drawerVisible && canOpenRoute(safeUser, route.route)
            }
            .distinctBy { route -> route.route }
    }

    fun permissionMessageFor(route: String): String {
        val title = titleOf(route)

        return when (normalizeRoute(route)) {
            ACCOUNT_APPROVAL,
            ACCOUNT_MANAGEMENT -> {
                "Bạn cần quyền quản trị viên để mở mục $title."
            }

            BOOKING_APPROVAL,
            STATISTICS,
            EXPORT_DATA,
            ROOM_FORM,
            COMPUTER_FORM,
            COMPUTER_BULK_CREATE -> {
                "Bạn không có quyền thao tác với mục $title."
            }

            BOOKING_FORM -> {
                "Chỉ giáo viên hoặc quản trị viên có thể tạo booking."
            }

            BOOKINGS,
            BOOKING_HISTORY -> {
                "Sinh viên xem lịch học tại mục Lịch."
            }

            else -> {
                "Bạn không có quyền mở mục $title."
            }
        }
    }

    fun normalizeRoute(route: String): String {
        return when (route.trim()) {
            "",
            "dashboard",
            "main",
            "trang_chu" -> HOME

            "notification",
            "thong_bao" -> NOTIFICATIONS

            "account_info",
            "profile",
            "tai_khoan" -> ACCOUNT

            "accounts",
            "users",
            "user_management",
            "quan_ly_tai_khoan" -> ACCOUNT_MANAGEMENT

            "approve_account",
            "account_pending",
            "duyet_tai_khoan" -> ACCOUNT_APPROVAL

            "room",
            "room_management",
            "phong_may" -> ROOMS

            "computer",
            "computer_management",
            "may_tinh" -> COMPUTERS

            "room_map",
            "map",
            "so_do" -> COMPUTER_MAP

            "booking",
            "booking_management" -> BOOKINGS

            "create_booking",
            "booking_create",
            "tao_booking" -> BOOKING_FORM

            "approve_booking",
            "booking_pending",
            "duyet_booking" -> BOOKING_APPROVAL

            "calendar",
            "lich" -> SCHEDULE

            else -> route.trim()
        }
    }
}

data class AppRoute(
    val route: String,
    val title: String,
    val description: String,
    val group: AppRouteGroup,
    val adminAllowed: Boolean,
    val teacherAllowed: Boolean,
    val studentAllowed: Boolean,
    val bottomVisible: Boolean,
    val drawerVisible: Boolean
)

enum class AppRouteGroup(
    val title: String
) {
    Main(
        title = "Chính"
    ),

    Account(
        title = "Tài khoản"
    ),

    Room(
        title = "Phòng máy"
    ),

    Computer(
        title = "Máy tính"
    ),

    Booking(
        title = "Booking"
    ),

    Schedule(
        title = "Lịch"
    ),

    Notification(
        title = "Thông báo"
    ),

    Report(
        title = "Báo cáo"
    ),

    System(
        title = "Hệ thống"
    )
}