@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quyen.quanlyphongmay.navigation

import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.AppNotification
import com.quyen.quanlyphongmay.repository.AppNotificationType
import com.quyen.quanlyphongmay.repository.AuthRepository
import com.quyen.quanlyphongmay.repository.NotificationRepository
import com.quyen.quanlyphongmay.screens.BookingManagementScreen
import com.quyen.quanlyphongmay.screens.ChangePasswordScreen
import com.quyen.quanlyphongmay.screens.ComputerManagementScreen
import com.quyen.quanlyphongmay.screens.ComputerRoomMapScreen
import com.quyen.quanlyphongmay.screens.DashboardScreen
import com.quyen.quanlyphongmay.screens.ExportReportScreen
import com.quyen.quanlyphongmay.screens.MachineReportScreen
import com.quyen.quanlyphongmay.screens.RoomManagementScreen
import com.quyen.quanlyphongmay.screens.UserManagementScreen
import com.quyen.quanlyphongmay.ui.design.AppBackground
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.AppDrawer
import com.quyen.quanlyphongmay.ui.design.AppDrawerItem
import com.quyen.quanlyphongmay.ui.design.AppGradientButton
import com.quyen.quanlyphongmay.ui.design.AppNavItem
import com.quyen.quanlyphongmay.ui.design.AppOutlinedButton
import com.quyen.quanlyphongmay.ui.design.AppSectionHeader
import com.quyen.quanlyphongmay.ui.design.AppTextField
import com.quyen.quanlyphongmay.ui.design.ConfirmDialog
import com.quyen.quanlyphongmay.ui.design.EmptyState
import com.quyen.quanlyphongmay.ui.design.PermissionDeniedCard
import com.quyen.quanlyphongmay.ui.design.StatCard
import com.quyen.quanlyphongmay.ui.design.StatusBadge
import com.quyen.quanlyphongmay.ui.design.UserRoleBadge
import com.quyen.quanlyphongmay.ui.design.UserStatusBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuanLyPhongMayAppShell(
    user: User,
    authRepository: AuthRepository,
    onSignedOut: () -> Unit
) {
    val safeUser = user.normalizedCopy()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val notificationRepository = remember {
        NotificationRepository()
    }

    var currentRoute by rememberSaveable {
        mutableStateOf(AppRoutes.defaultStartRouteFor(safeUser))
    }

    var showLogoutDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var unreadNotificationCount by rememberSaveable {
        mutableIntStateOf(0)
    }

    var notificationReloadKey by rememberSaveable {
        mutableIntStateOf(0)
    }

    var floatingNotification by remember {
        mutableStateOf<AppNotification?>(null)
    }

    var lastFloatingNotificationId by rememberSaveable {
        mutableStateOf("")
    }

    val safeRoute = AppRoutes.firstAllowedRouteOrHome(
        user = safeUser,
        requestedRoute = currentRoute
    )

    if (safeRoute != currentRoute) {
        currentRoute = safeRoute
    }

    fun notificationRouteOf(notification: AppNotification): String {
        return mapNotificationRouteToAppRoute(
            rawRoute = notification.targetRoute,
            user = safeUser
        )
    }

    fun openNotification(notification: AppNotification) {
        val targetRoute = notificationRouteOf(notification)

        notificationRepository.markNotificationRead(
            notification = notification,
            currentUser = safeUser
        ) { _, _ ->
            notificationReloadKey++
        }

        floatingNotification = null

        if (AppRoutes.canOpenRoute(safeUser, targetRoute)) {
            currentRoute = targetRoute
        } else {
            currentRoute = AppRoutes.NOTIFICATIONS
        }
    }

    fun refreshUnreadNotifications(
        allowFloating: Boolean
    ) {
        notificationRepository.getNotifications(
            currentUser = safeUser,
            filter = NotificationRepository.NotificationFilter()
        ) { success, _, result ->
            if (success) {
                unreadNotificationCount = result.unreadCount

                val newestUnread = result.notifications.firstOrNull { notification ->
                    !notification.isReadBy(safeUser)
                }

                if (
                    allowFloating &&
                    newestUnread != null &&
                    newestUnread.id.isNotBlank() &&
                    newestUnread.id != lastFloatingNotificationId &&
                    currentRoute != AppRoutes.NOTIFICATIONS
                ) {
                    lastFloatingNotificationId = newestUnread.id
                    floatingNotification = newestUnread
                }
            }
        }
    }

    LaunchedEffect(
        safeUser.uid,
        safeUser.email,
        notificationReloadKey,
        currentRoute
    ) {
        refreshUnreadNotifications(
            allowFloating = currentRoute != AppRoutes.NOTIFICATIONS
        )
    }

    LaunchedEffect(
        safeUser.uid,
        safeUser.email
    ) {
        while (true) {
            refreshUnreadNotifications(
                allowFloating = currentRoute != AppRoutes.NOTIFICATIONS
            )

            delay(6000L)
        }
    }

    LaunchedEffect(floatingNotification?.id) {
        if (floatingNotification != null) {
            delay(4500L)
            floatingNotification = null
        }
    }

    val bottomItems = AppRoutes.bottomRoutesFor(safeUser).map { route ->
        AppNavItem(
            route = route.route,
            title = route.title,
            selectedIcon = selectedIconForRoute(route.route),
            unselectedIcon = unselectedIconForRoute(route.route),
            badgeCount = if (route.route == AppRoutes.NOTIFICATIONS) {
                unreadNotificationCount
            } else {
                0
            }
        )
    }

    val drawerAllowedRoutes = drawerAllowedRoutesFor(safeUser)

    val drawerItems = drawerAllowedRoutes
        .toList()
        .distinct()
        .map { route ->
            AppDrawerItem(
                route = route,
                title = drawerTitleForRoute(route, AppRoutes.find(route)?.title ?: route),
                icon = selectedIconForRoute(route),
                enabled = AppRoutes.canOpenRoute(safeUser, route),
                badgeText = when (route) {
                    AppRoutes.ACCOUNT_APPROVAL -> if (safeUser.isAdmin) "Duyệt" else ""
                    AppRoutes.BOOKING_APPROVAL -> if (safeUser.isAdmin) "Chờ" else ""
                    AppRoutes.MACHINE_REPORTS -> if (safeUser.isAdmin) "Xử lý" else ""
                    AppRoutes.NOTIFICATIONS -> if (unreadNotificationCount > 0) unreadNotificationCount.toString() else ""
                    else -> ""
                }
            )
        }

    ConfirmDialog(
        visible = showLogoutDialog,
        title = "Đăng xuất",
        message = "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
        confirmText = "Đăng xuất",
        danger = true,
        onConfirm = {
            authRepository.logout()
            showLogoutDialog = false
            onSignedOut()
        },
        onDismiss = {
            showLogoutDialog = false
        }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                user = safeUser,
                drawerItems = drawerItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    if (AppRoutes.canOpenRoute(safeUser, item.route)) {
                        currentRoute = item.route
                        scope.launch {
                            drawerState.close()
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(AppRoutes.permissionMessageFor(item.route))
                        }
                    }
                },
                onLogoutClick = {
                    showLogoutDialog = true
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                AppTopBar(
                    title = AppRoutes.titleOf(currentRoute),
                    subtitle = buildHeaderSubtitle(
                        user = safeUser,
                        route = currentRoute
                    ),
                    navigationIcon = Icons.Default.Menu,
                    onNavigationClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    actionIcon = Icons.Default.Notifications,
                    actionBadgeCount = unreadNotificationCount,
                    onActionClick = {
                        currentRoute = AppRoutes.NOTIFICATIONS
                        floatingNotification = null
                    }
                )
            },
            bottomBar = {
                ShellBottomBar(
                    items = bottomItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        if (AppRoutes.canOpenRoute(safeUser, item.route)) {
                            currentRoute = item.route

                            if (item.route == AppRoutes.NOTIFICATIONS) {
                                floatingNotification = null
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(AppRoutes.permissionMessageFor(item.route))
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 5.dp)
                        .navigationBarsPadding()
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            val compactShell = isCompactShellRoute(currentRoute)

            AppBackground {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(
                                start = if (compactShell) 14.dp else 18.dp,
                                end = if (compactShell) 14.dp else 18.dp,
                                top = if (compactShell) 6.dp else 2.dp,
                                bottom = if (compactShell) 6.dp else 2.dp
                            )
                    ) {
                        ShellRouteContent(
                            user = safeUser,
                            authRepository = authRepository,
                            notificationRepository = notificationRepository,
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                if (AppRoutes.canOpenRoute(safeUser, route)) {
                                    currentRoute = route

                                    if (route == AppRoutes.NOTIFICATIONS) {
                                        floatingNotification = null
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(AppRoutes.permissionMessageFor(route))
                                    }
                                }
                            },
                            onLogoutClick = {
                                showLogoutDialog = true
                            },
                            onNotificationChanged = {
                                notificationReloadKey++
                            },
                            onShowMessage = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }

                    FloatingNotificationBanner(
                        notification = floatingNotification,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                top = 84.dp
                            )
                            .zIndex(10f),
                        onOpen = { notification ->
                            openNotification(notification)
                        },
                        onDismiss = {
                            floatingNotification = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShellRouteContent(
    user: User,
    authRepository: AuthRepository,
    notificationRepository: NotificationRepository,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationChanged: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    if (!AppRoutes.canOpenRoute(user, currentRoute)) {
        PermissionDeniedCard(
            message = AppRoutes.permissionMessageFor(currentRoute),
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    when (currentRoute) {
        AppRoutes.HOME -> {
            DashboardScreen(
                user = user,
                onNavigate = onNavigate
            )
        }

        AppRoutes.ACCOUNT_MANAGEMENT -> {
            UserManagementScreen(
                currentUser = user,
                onlyPending = false,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.ACCOUNT_APPROVAL -> {
            UserManagementScreen(
                currentUser = user,
                onlyPending = true,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.ROOMS -> {
            RoomManagementScreen(
                currentUser = user,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.COMPUTERS -> {
            ComputerManagementScreen(
                currentUser = user,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.COMPUTER_MAP -> {
            ComputerRoomMapScreen(
                currentUser = user,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.BOOKINGS -> {
            BookingManagementScreen(
                currentUser = user,
                onlyPending = false,
                openCreatePanel = false,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.BOOKING_FORM -> {
            BookingManagementScreen(
                currentUser = user,
                onlyPending = false,
                openCreatePanel = true,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.BOOKING_APPROVAL -> {
            BookingManagementScreen(
                currentUser = user,
                onlyPending = true,
                openCreatePanel = false,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.BOOKING_HISTORY -> {
            BookingManagementScreen(
                currentUser = user,
                onlyPending = false,
                openCreatePanel = false,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.SCHEDULE -> {
            BookingManagementScreen(
                currentUser = user,
                onlyPending = false,
                openCreatePanel = false,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.MACHINE_REPORTS,
        AppRoutes.MACHINE_REPORT_FORM,
        AppRoutes.MACHINE_REPORT_DETAIL -> {
            MachineReportScreen(
                currentUser = user,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.CHANGE_PASSWORD -> {
            ChangePasswordScreen(
                currentUser = user,
                authRepository = authRepository,
                onPasswordChanged = {
                    onShowMessage("Đổi mật khẩu thành công.")
                    onNavigate(AppRoutes.ACCOUNT)
                },
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.NOTIFICATIONS -> {
            NotificationShellContent(
                user = user,
                notificationRepository = notificationRepository,
                onNavigate = onNavigate,
                onNotificationChanged = onNotificationChanged,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.EXPORT_DATA -> {
            ExportReportScreen(
                currentUser = user,
                onShowMessage = onShowMessage
            )
        }

        AppRoutes.ACCOUNT -> {
            AccountShellContent(
                user = user,
                onNavigate = onNavigate,
                onLogoutClick = onLogoutClick
            )
        }

        else -> {
            ModuleShellContent(
                user = user,
                route = AppRoutes.find(currentRoute),
                onNavigateHome = {
                    onNavigate(AppRoutes.HOME)
                }
            )
        }
    }
}

@Composable
private fun FloatingNotificationBanner(
    notification: AppNotification?,
    modifier: Modifier = Modifier,
    onOpen: (AppNotification) -> Unit,
    onDismiss: () -> Unit
) {
    var cachedNotification by remember {
        mutableStateOf<AppNotification?>(null)
    }

    LaunchedEffect(notification?.id) {
        if (notification != null) {
            cachedNotification = notification.normalizedCopy()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(
                durationMillis = 420,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 260)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(
                durationMillis = 320,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(
            animationSpec = tween(durationMillis = 220)
        ),
        modifier = modifier
    ) {
        val safeNotification = cachedNotification ?: return@AnimatedVisibility
        val senderText = notificationSenderText(safeNotification)

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        onOpen(safeNotification)
                    }
                ),
            contentPadding = PaddingValues(14.dp),
            shadowElevation = 24.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = notificationSoftColor(safeNotification.typeEnum),
                        border = BorderStroke(
                            width = 1.dp,
                            color = notificationMainColor(safeNotification.typeEnum).copy(alpha = 0.28f)
                        ),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = notificationIcon(safeNotification.typeEnum),
                            contentDescription = null,
                            tint = notificationMainColor(safeNotification.typeEnum),
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Thông báo mới",
                                color = notificationMainColor(safeNotification.typeEnum),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            )

                            Text(
                                text = "•",
                                color = AppColors.Muted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )

                            Text(
                                text = safeNotification.displayTimeText,
                                color = AppColors.Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Text(
                            text = safeNotification.title,
                            color = AppColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                        )

                        Text(
                            text = senderText,
                            color = AppColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    SmallShellButton(
                        text = "Đóng",
                        icon = Icons.Default.NotificationsNone,
                        tone = AppBadgeTone.Neutral,
                        onClick = onDismiss
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = notificationSoftColor(safeNotification.typeEnum),
                    border = BorderStroke(
                        width = 1.dp,
                        color = notificationMainColor(safeNotification.typeEnum).copy(alpha = 0.16f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nội dung",
                            color = notificationMainColor(safeNotification.typeEnum),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        )

                        Text(
                            text = safeNotification.message.ifBlank { "Không có nội dung thông báo." },
                            color = AppColors.Text,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationShellContent(
    user: User,
    notificationRepository: NotificationRepository,
    onNavigate: (String) -> Unit,
    onNotificationChanged: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var keyword by rememberSaveable {
        mutableStateOf("")
    }

    var onlyUnread by rememberSaveable {
        mutableStateOf(false)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf(NotificationRepository.NotificationResult())
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    var showCreateNotification by rememberSaveable {
        mutableStateOf(false)
    }

    var manualTitle by rememberSaveable {
        mutableStateOf("")
    }

    var manualMessage by rememberSaveable {
        mutableStateOf("")
    }

    var sendToStudents by rememberSaveable {
        mutableStateOf(true)
    }

    var sendToTeachers by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedNotification by remember {
        mutableStateOf<AppNotification?>(null)
    }

    val canSendToTeacher = user.isAdmin
    val canCreateManualNotification = user.isAdmin || user.isTeacher

    fun reloadNotifications() {
        loading = true
        errorMessage = ""

        notificationRepository.getNotifications(
            currentUser = user,
            filter = NotificationRepository.NotificationFilter(
                keyword = keyword,
                onlyUnread = onlyUnread
            )
        ) { success, message, notificationResult ->
            loading = false

            if (success) {
                result = notificationResult
                onNotificationChanged()
            } else {
                result = NotificationRepository.NotificationResult()
                errorMessage = message ?: "Không thể tải thông báo."
            }
        }
    }

    fun markNotificationRead(notification: AppNotification) {
        notificationRepository.markNotificationRead(
            notification = notification,
            currentUser = user
        ) { success, message ->
            if (!success) {
                onShowMessage(message ?: "Không thể đánh dấu đã đọc.")
            }

            onNotificationChanged()
            reloadKey++
        }
    }

    fun showNotificationDetail(notification: AppNotification) {
        selectedNotification = notification.normalizedCopy()

        if (!notification.isReadBy(user)) {
            markNotificationRead(notification)
        }
    }

    fun navigateFromNotification(notification: AppNotification) {
        val targetRoute = mapNotificationRouteToAppRoute(
            rawRoute = notification.targetRoute,
            user = user
        )

        selectedNotification = null

        if (AppRoutes.canOpenRoute(user, targetRoute)) {
            onNavigate(targetRoute)
        }
    }

    fun markAllRead() {
        notificationRepository.markAllRead(
            currentUser = user
        ) { success, message ->
            onShowMessage(
                message ?: if (success) {
                    "Đã đánh dấu đã đọc."
                } else {
                    "Không thể cập nhật thông báo."
                }
            )

            if (success) {
                onNotificationChanged()
                reloadKey++
            }
        }
    }

    fun createManualNotification() {
        val cleanTitle = manualTitle.trim()
        val cleanMessage = manualMessage.trim()

        if (!canCreateManualNotification) {
            onShowMessage("Bạn không có quyền tạo thông báo.")
            return
        }

        if (cleanTitle.isBlank()) {
            onShowMessage("Vui lòng nhập tiêu đề thông báo.")
            return
        }

        if (cleanMessage.isBlank()) {
            onShowMessage("Vui lòng nhập nội dung thông báo.")
            return
        }

        val targetRoles = buildList {
            if (sendToStudents) {
                add("student")
            }

            if (sendToTeachers && user.isAdmin) {
                add("teacher")
            }
        }

        if (targetRoles.isEmpty()) {
            onShowMessage("Vui lòng chọn ít nhất một nhóm nhận thông báo.")
            return
        }

        notificationRepository.createSystemNotification(
            title = cleanTitle,
            message = cleanMessage,
            targetRoles = targetRoles,
            targetUserIds = emptyList(),
            targetEmails = emptyList(),
            targetClassName = "",
            targetRoute = "notifications",
            createdBy = user
        ) { success, message, _ ->
            if (success) {
                manualTitle = ""
                manualMessage = ""
                sendToStudents = true
                sendToTeachers = false
                showCreateNotification = false
                reloadKey++
                onNotificationChanged()
            }

            onShowMessage(
                message ?: if (success) {
                    "Đã tạo thông báo."
                } else {
                    "Không thể tạo thông báo."
                }
            )
        }
    }

    LaunchedEffect(
        reloadKey,
        keyword,
        onlyUnread,
        user.uid,
        user.email
    ) {
        reloadNotifications()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        item {
            NotificationActionCard(
                subtitle = notificationSubtitleForUser(user),
                actionText = if (result.unreadCount > 0) "Đọc hết" else "Tải lại",
                onActionClick = {
                    if (result.unreadCount > 0) {
                        markAllRead()
                    } else {
                        reloadKey++
                    }
                }
            )
        }

        if (canCreateManualNotification) {
            item {
                ManualNotificationEntryCard(
                    expanded = showCreateNotification,
                    title = manualTitle,
                    message = manualMessage,
                    sendToStudents = sendToStudents,
                    sendToTeachers = sendToTeachers,
                    canSendToTeacher = canSendToTeacher,
                    currentUser = user,
                    onToggle = {
                        showCreateNotification = !showCreateNotification
                    },
                    onTitleChange = {
                        manualTitle = it
                    },
                    onMessageChange = {
                        manualMessage = it
                    },
                    onToggleStudents = {
                        sendToStudents = !sendToStudents
                    },
                    onToggleTeachers = {
                        if (canSendToTeacher) {
                            sendToTeachers = !sendToTeachers
                        }
                    },
                    onSubmit = {
                        createManualNotification()
                    }
                )
            }
        }

        item {
            NotificationOverviewCard(
                result = result,
                loading = loading
            )
        }

        item {
            NotificationFilterCard(
                keyword = keyword,
                onlyUnread = onlyUnread,
                onKeywordChange = { keyword = it },
                onToggleUnread = { onlyUnread = !onlyUnread },
                onClear = {
                    keyword = ""
                    onlyUnread = false
                }
            )
        }

        if (errorMessage.isNotBlank()) {
            item {
                EmptyState(
                    title = "Không thể tải thông báo",
                    message = errorMessage,
                    icon = Icons.Default.ErrorOutline,
                    actionText = "Tải lại",
                    onActionClick = {
                        reloadKey++
                    }
                )
            }
        }

        if (loading) {
            item {
                ShellLoadingCard(
                    message = "Đang tải thông báo"
                )
            }
        } else if (result.notifications.isEmpty()) {
            item {
                EmptyState(
                    title = if (onlyUnread) "Không có thông báo chưa đọc" else "Chưa có thông báo",
                    message = if (onlyUnread) {
                        "Tất cả thông báo của bạn đã được đọc."
                    } else {
                        "Thông báo booking, duyệt phòng, lịch học và tài khoản sẽ hiển thị tại đây."
                    },
                    icon = Icons.Default.NotificationsNone
                )
            }
        } else {
            itemsIndexed(
                items = result.notifications,
                key = { index, notification ->
                    notification.id.ifBlank { "notification_$index" }
                }
            ) { _, notification ->
                RealNotificationCard(
                    user = user,
                    notification = notification,
                    onOpen = {
                        showNotificationDetail(notification)
                    },
                    onMarkRead = {
                        markNotificationRead(notification)
                    }
                )
            }
        }
    }

    NotificationDetailDialog(
        notification = selectedNotification,
        user = user,
        onDismiss = {
            selectedNotification = null
        },
        onOpenRoute = { notification ->
            navigateFromNotification(notification)
        }
    )
}

@Composable
private fun NotificationDetailDialog(
    notification: AppNotification?,
    user: User,
    onDismiss: () -> Unit,
    onOpenRoute: (AppNotification) -> Unit
) {
    val safeNotification = notification?.normalizedCopy() ?: return
    val targetRoute = mapNotificationRouteToAppRoute(
        rawRoute = safeNotification.targetRoute,
        user = user
    )
    val canOpenRoute = AppRoutes.canOpenRoute(user, targetRoute) && targetRoute != AppRoutes.NOTIFICATIONS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = safeNotification.title,
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )

                Text(
                    text = notificationSenderText(safeNotification),
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = safeNotification.displayTimeText,
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = notificationSoftColor(safeNotification.typeEnum),
                border = BorderStroke(
                    width = 1.dp,
                    color = notificationMainColor(safeNotification.typeEnum).copy(alpha = 0.16f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nội dung thông báo",
                        color = notificationMainColor(safeNotification.typeEnum),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )

                    Text(
                        text = safeNotification.message.ifBlank { "Không có nội dung thông báo." },
                        color = AppColors.Text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canOpenRoute) {
                    SmallShellButton(
                        text = "Đi tới",
                        icon = notificationIcon(safeNotification.typeEnum),
                        tone = AppBadgeTone.Info,
                        onClick = {
                            onOpenRoute(safeNotification)
                        }
                    )
                }

                SmallShellButton(
                    text = "Đóng",
                    icon = Icons.Default.NotificationsNone,
                    tone = AppBadgeTone.Neutral,
                    onClick = onDismiss
                )
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun ManualNotificationEntryCard(
    expanded: Boolean,
    title: String,
    message: String,
    sendToStudents: Boolean,
    sendToTeachers: Boolean,
    canSendToTeacher: Boolean,
    currentUser: User,
    onToggle: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onToggleStudents: () -> Unit,
    onToggleTeachers: () -> Unit,
    onSubmit: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = AppColors.BlueSoft,
                    border = BorderStroke(
                        width = 1.dp,
                        color = AppColors.Blue.copy(alpha = 0.18f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = AppColors.Blue,
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tạo thông báo",
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (currentUser.isAdmin) {
                            "Admin gửi thông báo đến sinh viên, giáo viên hoặc cả hai nhóm cùng lúc."
                        } else {
                            "Giáo viên gửi thông báo đến sinh viên."
                        },
                        color = AppColors.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }

                SmallShellButton(
                    text = if (expanded) "Đóng" else "Mở",
                    icon = Icons.Default.Notifications,
                    tone = AppBadgeTone.Info,
                    onClick = onToggle
                )
            }

            if (expanded) {
                AppTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = "Tiêu đề",
                    placeholder = "VD: Thông báo lịch học phòng máy",
                    leadingIcon = Icons.Default.Notifications,
                    minHeight = 56.dp
                )

                AppTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = "Nội dung",
                    placeholder = "Nhập nội dung thông báo cần gửi...",
                    leadingIcon = Icons.Default.MarkEmailRead,
                    minHeight = 72.dp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gửi đến",
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ManualTargetButton(
                            text = "Sinh viên",
                            selected = sendToStudents,
                            enabled = true,
                            icon = Icons.Default.Groups,
                            onClick = onToggleStudents,
                            modifier = Modifier.weight(1f)
                        )

                        ManualTargetButton(
                            text = "Giáo viên",
                            selected = sendToTeachers,
                            enabled = canSendToTeacher,
                            icon = Icons.Default.AccountCircle,
                            onClick = onToggleTeachers,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = if (canSendToTeacher) {
                            "Admin có thể chọn một hoặc cả hai nhóm nhận thông báo."
                        } else {
                            "Giáo viên chỉ có thể gửi thông báo đến Sinh viên."
                        },
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }

                AppGradientButton(
                    text = "Gửi thông báo",
                    onClick = onSubmit,
                    leadingIcon = Icons.Default.Send
                )
            }
        }
    }
}

@Composable
private fun ManualTargetButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        !enabled -> Color(0xFFF3F7FB)
        selected -> AppColors.BlueSoft
        else -> Color.White
    }

    val border = when {
        !enabled -> AppColors.Border
        selected -> AppColors.Blue.copy(alpha = 0.75f)
        else -> AppColors.Border
    }

    val content = when {
        !enabled -> AppColors.Muted.copy(alpha = 0.55f)
        selected -> AppColors.BlueDeep
        else -> AppColors.Muted
    }

    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(
            width = 1.dp,
            color = border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun NotificationActionCard(
    subtitle: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = subtitle,
                color = AppColors.Muted,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )

            AppOutlinedButton(
                text = actionText,
                onClick = onActionClick,
                leadingIcon = if (actionText == "Đọc hết") {
                    Icons.Default.MarkEmailRead
                } else {
                    Icons.Default.CloudDownload
                },
                height = 44.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NotificationOverviewCard(
    result: NotificationRepository.NotificationResult,
    loading: Boolean
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppSectionHeader(
                title = "Tổng quan",
                subtitle = if (loading) {
                    "Đang đồng bộ Firestore..."
                } else {
                    "Thông báo được tải từ collection notifications."
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Tổng",
                    value = result.total.toString(),
                    icon = Icons.Default.Notifications,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Chưa đọc",
                    value = result.unreadCount.toString(),
                    icon = Icons.Default.MarkEmailRead,
                    tone = if (result.unreadCount > 0) AppBadgeTone.Warning else AppBadgeTone.Success,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Booking",
                    value = result.bookingCount.toString(),
                    icon = Icons.Default.EventAvailable,
                    tone = AppBadgeTone.Warning,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Tài khoản",
                    value = result.accountCount.toString(),
                    icon = Icons.Default.Groups,
                    tone = AppBadgeTone.Purple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NotificationFilterCard(
    keyword: String,
    onlyUnread: Boolean,
    onKeywordChange: (String) -> Unit,
    onToggleUnread: () -> Unit,
    onClear: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                label = "Tìm thông báo",
                placeholder = "Booking, lớp, phòng, tài khoản...",
                leadingIcon = Icons.Default.Notifications,
                minHeight = 56.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppOutlinedButton(
                    text = if (onlyUnread) "Đang lọc chưa đọc" else "Chỉ chưa đọc",
                    onClick = onToggleUnread,
                    leadingIcon = Icons.Default.MarkEmailRead,
                    height = 46.dp,
                    modifier = Modifier.weight(1f)
                )

                AppOutlinedButton(
                    text = "Xóa lọc",
                    onClick = onClear,
                    leadingIcon = Icons.Default.NotificationsNone,
                    height = 46.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RealNotificationCard(
    user: User,
    notification: AppNotification,
    onOpen: () -> Unit,
    onMarkRead: () -> Unit
) {
    val safeNotification = notification.normalizedCopy()
    val unread = !safeNotification.isReadBy(user)
    val senderText = notificationSenderText(safeNotification)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpen
            ),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = if (unread) 14.dp else 7.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = notificationSoftColor(safeNotification.typeEnum),
                    border = BorderStroke(
                        width = 1.dp,
                        color = notificationMainColor(safeNotification.typeEnum).copy(alpha = if (unread) 0.35f else 0.18f)
                    ),
                    shadowElevation = if (unread) 4.dp else 0.dp
                ) {
                    Icon(
                        imageVector = notificationIcon(safeNotification.typeEnum),
                        contentDescription = null,
                        tint = notificationMainColor(safeNotification.typeEnum),
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = safeNotification.title,
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = senderText,
                        color = AppColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )

                    Text(
                        text = safeNotification.displayTimeText,
                        color = AppColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                StatusBadge(
                    text = if (unread) "Mới" else "Đã đọc",
                    tone = if (unread) AppBadgeTone.Warning else AppBadgeTone.Neutral
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = notificationSoftColor(safeNotification.typeEnum),
                border = BorderStroke(
                    width = 1.dp,
                    color = notificationMainColor(safeNotification.typeEnum).copy(alpha = 0.14f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nội dung",
                        color = notificationMainColor(safeNotification.typeEnum),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )

                    Text(
                        text = safeNotification.message.ifBlank { "Không có nội dung thông báo." },
                        color = AppColors.Text,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallShellButton(
                    text = "Mở",
                    icon = notificationIcon(safeNotification.typeEnum),
                    tone = AppBadgeTone.Info,
                    onClick = onOpen
                )

                if (unread) {
                    SmallShellButton(
                        text = "Đánh dấu đã đọc",
                        icon = Icons.Default.MarkEmailRead,
                        tone = AppBadgeTone.Success,
                        onClick = onMarkRead
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountShellContent(
    user: User,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val safeUser = user.normalizedCopy()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp),
                shadowElevation = 14.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = roleSoftColor(safeUser).copy(alpha = 0.96f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = roleMainColor(safeUser).copy(alpha = 0.20f)
                            ),
                            shadowElevation = 5.dp
                        ) {
                            Icon(
                                imageVector = roleIconForAccount(safeUser),
                                contentDescription = null,
                                tint = roleMainColor(safeUser),
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(13.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = safeUser.displayName.ifBlank { "Người dùng" },
                                color = AppColors.Ink,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 23.sp,
                                    lineHeight = 28.sp
                                )
                            )

                            Text(
                                text = safeUser.email.ifBlank { "Chưa có email" },
                                color = AppColors.Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp
                                )
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserRoleBadge(role = safeUser.role)
                                UserStatusBadge(status = safeUser.status)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFF8FBFF),
                        border = BorderStroke(
                            width = 1.dp,
                            color = AppColors.Border
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AccountDetailRow(
                                title = accountIdentifierLabel(safeUser),
                                value = safeUser.displayIdentifier.ifBlank { "Chưa cập nhật" },
                                icon = Icons.Default.AccountCircle
                            )

                            AccountDetailRow(
                                title = accountUnitLabel(safeUser),
                                value = safeUser.displayDepartment.ifBlank { "Chưa cập nhật" },
                                icon = Icons.Default.Class
                            )

                            AccountDetailRow(
                                title = "Số điện thoại",
                                value = safeUser.phone.ifBlank { "Chưa cập nhật" },
                                icon = Icons.Default.AccountCircle
                            )
                        }
                    }
                }
            }
        }

        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                shadowElevation = 9.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    AppSectionHeader(
                        title = "Tài khoản",
                        subtitle = "Quản lý thông tin cá nhân và bảo mật đăng nhập."
                    )

                    AccountActionRow(
                        title = "Đổi mật khẩu",
                        subtitle = "Cập nhật mật khẩu đăng nhập an toàn.",
                        icon = Icons.Default.Key,
                        onClick = {
                            onNavigate(AppRoutes.CHANGE_PASSWORD)
                        }
                    )

                    AppGradientButton(
                        text = "Đăng xuất",
                        onClick = onLogoutClick,
                        leadingIcon = Icons.Default.AccountCircle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountDetailRow(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(14.dp),
            color = AppColors.BlueSoft,
            border = BorderStroke(
                width = 1.dp,
                color = AppColors.Blue.copy(alpha = 0.16f)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.BlueDeep,
                modifier = Modifier.padding(9.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                color = AppColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )

            Text(
                text = value,
                color = AppColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun AccountActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.BlueSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Blue.copy(alpha = 0.16f)
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.BlueDeep,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                )

                Text(
                    text = subtitle,
                    color = AppColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.Muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ModuleShellContent(
    user: User,
    route: AppRoute?,
    onNavigateHome: () -> Unit
) {
    val safeRoute = route

    if (safeRoute == null) {
        EmptyState(
            title = "Không tìm thấy mục",
            message = "Mục này không tồn tại trong hệ thống điều hướng.",
            icon = Icons.Default.ListAlt,
            actionText = "Về trang chủ",
            onActionClick = onNavigateHome
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        item {
            AppSectionHeader(
                title = safeRoute.title,
                subtitle = safeRoute.description
            )
        }

        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Nhóm",
                            value = safeRoute.group.title,
                            icon = selectedIconForRoute(safeRoute.route),
                            tone = toneForRoute(safeRoute.route),
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Quyền",
                            value = user.roleNameVi,
                            icon = Icons.Default.AdminPanelSettings,
                            tone = roleTone(user),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    EmptyState(
                        title = safeRoute.title,
                        message = "Mục này đã được nối vào hệ thống điều hướng và phân quyền. Dữ liệu chi tiết sẽ được tải theo repository tương ứng ở bước triển khai module.",
                        icon = selectedIconForRoute(safeRoute.route)
                    )
                }
            }
        }

        item {
            AppGradientButton(
                text = "Về trang chủ",
                onClick = onNavigateHome,
                leadingIcon = Icons.Default.Home
            )
        }
    }
}

@Composable
private fun ShellLoadingCard(
    message: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = AppColors.Blue
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Vui lòng chờ trong giây lát.",
                    color = AppColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    subtitle: String,
    navigationIcon: ImageVector,
    onNavigationClick: () -> Unit,
    actionIcon: ImageVector,
    actionBadgeCount: Int,
    onActionClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNavigationClick
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.BlueSoft,
                    border = BorderStroke(
                        width = 1.dp,
                        color = AppColors.Blue.copy(alpha = 0.16f)
                    )
                ) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = null,
                        tint = AppColors.BlueDeep,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )

                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = subtitle,
                            color = AppColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onActionClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (actionBadgeCount > 0) AppColors.AmberSoft else AppColors.BlueSoft,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (actionBadgeCount > 0) {
                                AppColors.Amber.copy(alpha = 0.20f)
                            } else {
                                AppColors.Blue.copy(alpha = 0.16f)
                            }
                        )
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            tint = if (actionBadgeCount > 0) AppColors.Amber else AppColors.BlueDeep,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (actionBadgeCount > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(17.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = AppColors.Rose
                        ) {
                            Text(
                                text = if (actionBadgeCount > 9) "9+" else actionBadgeCount.toString(),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellBottomBar(
    items: List<AppNavItem>,
    currentRoute: String,
    onItemClick: (AppNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.98f),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items.forEach { item ->
                ShellBottomBarItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = {
                        onItemClick(item)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShellBottomBarItem(
    item: AppNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (selected) item.selectedIcon else item.unselectedIcon
    val contentColor = if (selected) AppColors.BlueDeep else AppColors.Muted
    val badgeCount = item.badgeCount.coerceAtLeast(0)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(22.dp),
            color = if (selected) AppColors.BlueSoft else Color.Transparent,
            border = BorderStroke(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) AppColors.Blue.copy(alpha = 0.14f) else Color.Transparent
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier.size(25.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.title,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )

                    if (badgeCount > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 0.dp)
                                .size(18.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = AppColors.Rose,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Text(
                    text = item.title,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SmallShellButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = shellToneColors(tone)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier
                .height(34.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = text,
                color = colors.content,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

private data class ShellToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun shellToneColors(
    tone: AppBadgeTone
): ShellToneColors {
    return when (tone) {
        AppBadgeTone.Info -> ShellToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> ShellToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> ShellToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> ShellToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Purple -> ShellToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )

        AppBadgeTone.Neutral -> ShellToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )
    }
}

private fun mapNotificationRouteToAppRoute(
    rawRoute: String,
    user: User
): String {
    val cleanRoute = rawRoute.trim()

    val mappedRoute = when (cleanRoute) {
        "home",
        "dashboard" -> AppRoutes.HOME

        "notifications",
        "notification" -> AppRoutes.NOTIFICATIONS

        "booking_approval",
        "bookingApproval",
        "approve_booking",
        "duyet_booking" -> AppRoutes.BOOKING_APPROVAL

        "bookings",
        "booking",
        "booking_management" -> AppRoutes.BOOKINGS

        "booking_form",
        "create_booking" -> AppRoutes.BOOKING_FORM

        "schedule",
        "lich",
        "calendar" -> AppRoutes.SCHEDULE

        "account_approval",
        "approve_account" -> AppRoutes.ACCOUNT_APPROVAL

        "accounts",
        "account_management" -> AppRoutes.ACCOUNT_MANAGEMENT

        "computers",
        "computer_management" -> AppRoutes.COMPUTERS

        "computer_map",
        "room_map" -> AppRoutes.COMPUTER_MAP

        "rooms",
        "room_management" -> AppRoutes.ROOMS

        else -> cleanRoute.ifBlank { AppRoutes.NOTIFICATIONS }
    }

    return if (AppRoutes.canOpenRoute(user, mappedRoute)) {
        mappedRoute
    } else {
        AppRoutes.NOTIFICATIONS
    }
}

private fun notificationSenderText(
    notification: AppNotification
): String {
    val senderName = notification.createdByName.trim()
    val senderEmail = notification.createdByEmail.trim()

    return when {
        senderName.isNotBlank() && senderEmail.isNotBlank() -> {
            "Từ: $senderName • $senderEmail"
        }

        senderName.isNotBlank() -> {
            "Từ: $senderName"
        }

        senderEmail.isNotBlank() -> {
            "Từ: $senderEmail"
        }

        else -> {
            "Từ: Hệ thống"
        }
    }
}

private fun notificationIcon(
    type: AppNotificationType
): ImageVector {
    return when (type) {
        AppNotificationType.Booking -> Icons.Default.EventAvailable
        AppNotificationType.Seat -> Icons.Default.Computer
        AppNotificationType.Account -> Icons.Default.Groups
        AppNotificationType.Computer -> Icons.Default.Computer
        AppNotificationType.Room -> Icons.Default.MeetingRoom
        AppNotificationType.System -> Icons.Default.Notifications
    }
}

private fun notificationMainColor(
    type: AppNotificationType
): Color {
    return when (type) {
        AppNotificationType.Booking -> AppColors.Amber
        AppNotificationType.Seat -> AppColors.Blue
        AppNotificationType.Account -> AppColors.Purple
        AppNotificationType.Computer -> AppColors.Green
        AppNotificationType.Room -> AppColors.BlueDeep
        AppNotificationType.System -> AppColors.Blue
    }
}

private fun notificationSoftColor(
    type: AppNotificationType
): Color {
    return when (type) {
        AppNotificationType.Booking -> AppColors.AmberSoft
        AppNotificationType.Seat -> AppColors.BlueSoft
        AppNotificationType.Account -> AppColors.PurpleSoft
        AppNotificationType.Computer -> AppColors.GreenSoft
        AppNotificationType.Room -> AppColors.BlueSoft
        AppNotificationType.System -> AppColors.BlueSoft
    }
}

private fun isCompactShellRoute(
    route: String
): Boolean {
    return route == AppRoutes.ACCOUNT_MANAGEMENT ||
            route == AppRoutes.ACCOUNT_APPROVAL ||
            route == AppRoutes.ROOMS ||
            route == AppRoutes.COMPUTERS ||
            route == AppRoutes.COMPUTER_MAP ||
            route == AppRoutes.BOOKINGS ||
            route == AppRoutes.BOOKING_FORM ||
            route == AppRoutes.BOOKING_APPROVAL ||
            route == AppRoutes.BOOKING_HISTORY ||
            route == AppRoutes.SCHEDULE ||
            route == AppRoutes.NOTIFICATIONS
}

private fun buildHeaderSubtitle(
    user: User,
    route: String
): String {
    return when (route) {
        AppRoutes.ACCOUNT_MANAGEMENT -> ""
        AppRoutes.ACCOUNT_APPROVAL -> ""
        AppRoutes.ROOMS -> ""
        AppRoutes.COMPUTERS -> ""
        AppRoutes.COMPUTER_MAP -> ""
        AppRoutes.BOOKINGS -> ""
        AppRoutes.BOOKING_FORM -> ""
        AppRoutes.BOOKING_APPROVAL -> ""
        AppRoutes.BOOKING_HISTORY -> ""
        AppRoutes.SCHEDULE -> ""
        AppRoutes.NOTIFICATIONS -> "Thông báo Firestore theo tài khoản"
        AppRoutes.ACCOUNT -> "Thông tin tài khoản"
        AppRoutes.HOME -> "${user.roleNameVi} • ${user.statusNameVi}"
        else -> {
            val routeDescription = AppRoutes.descriptionOf(route)

            if (routeDescription.isNotBlank()) {
                routeDescription
            } else {
                "${user.roleNameVi} • ${user.statusNameVi}"
            }
        }
    }
}

private fun notificationSubtitleForUser(
    user: User
): String {
    return when {
        user.isAdmin -> "Booking mới, tài khoản chờ duyệt, booking bị hủy và thông báo hệ thống."
        user.isTeacher -> "Trạng thái booking phòng, lịch dạy, sơ đồ chỗ ngồi và phản hồi từ quản trị viên."
        else -> "Lịch phòng máy, chỗ ngồi, phòng học và thông báo từ giáo viên/quản trị viên."
    }
}

private fun roleTone(
    user: User
): AppBadgeTone {
    return when {
        user.isAdmin -> AppBadgeTone.Purple
        user.isTeacher -> AppBadgeTone.Info
        else -> AppBadgeTone.Success
    }
}

private fun toneForRoute(
    route: String
): AppBadgeTone {
    return when (route) {
        AppRoutes.ACCOUNT_MANAGEMENT,
        AppRoutes.ACCOUNT_APPROVAL -> AppBadgeTone.Purple

        AppRoutes.ROOMS,
        AppRoutes.ROOM_DETAIL,
        AppRoutes.ROOM_FORM -> AppBadgeTone.Info

        AppRoutes.COMPUTERS,
        AppRoutes.COMPUTER_DETAIL,
        AppRoutes.COMPUTER_FORM,
        AppRoutes.COMPUTER_BULK_CREATE,
        AppRoutes.COMPUTER_MAP -> AppBadgeTone.Success

        AppRoutes.BOOKINGS,
        AppRoutes.BOOKING_FORM,
        AppRoutes.BOOKING_APPROVAL,
        AppRoutes.BOOKING_HISTORY,
        AppRoutes.SCHEDULE -> AppBadgeTone.Warning

        AppRoutes.MACHINE_REPORTS,
        AppRoutes.MACHINE_REPORT_FORM,
        AppRoutes.MACHINE_REPORT_DETAIL -> AppBadgeTone.Error

        AppRoutes.STATISTICS,
        AppRoutes.EXPORT_DATA -> AppBadgeTone.Purple

        AppRoutes.NOTIFICATIONS -> AppBadgeTone.Info

        else -> AppBadgeTone.Info
    }
}

private fun accountIdentifierLabel(
    user: User
): String {
    return when {
        user.isStudent -> "Mã số sinh viên"
        user.isTeacher -> "Mã giảng viên"
        user.isAdmin -> "Mã quản trị"
        else -> "Mã tài khoản"
    }
}

private fun accountUnitLabel(
    user: User
): String {
    return when {
        user.isStudent -> "Lớp"
        user.isTeacher -> "Khoa / Bộ môn"
        user.isAdmin -> "Đơn vị"
        else -> "Thông tin"
    }
}


private fun drawerAllowedRoutesFor(
    user: User
): Set<String> {
    val safeUser = user.normalizedCopy()

    return when {
        safeUser.isAdmin -> setOf(
            AppRoutes.HOME,
            AppRoutes.ACCOUNT_APPROVAL,
            AppRoutes.ACCOUNT_MANAGEMENT,
            AppRoutes.BOOKING_APPROVAL,
            AppRoutes.ROOMS,
            AppRoutes.COMPUTERS,
            AppRoutes.COMPUTER_MAP,
            AppRoutes.MACHINE_REPORTS,
            AppRoutes.NOTIFICATIONS,
            AppRoutes.EXPORT_DATA,
            AppRoutes.ACCOUNT,
            AppRoutes.CHANGE_PASSWORD
        )

        safeUser.isTeacher -> setOf(
            AppRoutes.HOME,
            AppRoutes.BOOKING_FORM,
            AppRoutes.BOOKINGS,
            AppRoutes.SCHEDULE,
            AppRoutes.ROOMS,
            AppRoutes.COMPUTERS,
            AppRoutes.COMPUTER_MAP,
            AppRoutes.MACHINE_REPORTS,
            AppRoutes.NOTIFICATIONS,
            AppRoutes.ACCOUNT,
            AppRoutes.CHANGE_PASSWORD
        )

        else -> setOf(
            AppRoutes.HOME,
            AppRoutes.SCHEDULE,
            AppRoutes.ROOMS,
            AppRoutes.COMPUTERS,
            AppRoutes.COMPUTER_MAP,
            AppRoutes.MACHINE_REPORTS,
            AppRoutes.NOTIFICATIONS,
            AppRoutes.ACCOUNT,
            AppRoutes.CHANGE_PASSWORD
        )
    }
}

private fun drawerTitleForRoute(
    route: String,
    fallback: String
): String {
    return when (route) {
        AppRoutes.HOME -> "Trang chủ"
        AppRoutes.ACCOUNT_APPROVAL -> "Duyệt tài khoản"
        AppRoutes.ACCOUNT_MANAGEMENT -> "Quản lý tài khoản"
        AppRoutes.BOOKING_FORM -> "Tạo booking"
        AppRoutes.BOOKINGS -> "Booking"
        AppRoutes.BOOKING_APPROVAL -> "Duyệt booking"
        AppRoutes.SCHEDULE -> "Lịch"
        AppRoutes.ROOMS -> "Phòng máy"
        AppRoutes.COMPUTERS -> "Máy tính"
        AppRoutes.COMPUTER_MAP -> "Sơ đồ phòng máy"
        AppRoutes.MACHINE_REPORTS -> "Báo lỗi máy"
        AppRoutes.NOTIFICATIONS -> "Thông báo"
        AppRoutes.EXPORT_DATA -> "Xuất CSV"
        AppRoutes.ACCOUNT -> "Tài khoản"
        AppRoutes.CHANGE_PASSWORD -> "Đổi mật khẩu"
        AppRoutes.SETTINGS -> "Cài đặt"
        else -> fallback
    }
}

private fun roleIconForAccount(
    user: User
): ImageVector {
    val safeUser = user.normalizedCopy()

    return when {
        safeUser.isAdmin -> Icons.Default.AdminPanelSettings
        safeUser.isTeacher -> Icons.Default.Class
        else -> Icons.Default.AccountCircle
    }
}

private fun roleMainColor(
    user: User
): Color {
    val safeUser = user.normalizedCopy()

    return when {
        safeUser.isAdmin -> AppColors.Purple
        safeUser.isTeacher -> AppColors.BlueDeep
        else -> AppColors.Green
    }
}

private fun roleSoftColor(
    user: User
): Color {
    val safeUser = user.normalizedCopy()

    return when {
        safeUser.isAdmin -> AppColors.PurpleSoft
        safeUser.isTeacher -> AppColors.BlueSoft
        else -> AppColors.GreenSoft
    }
}

private fun selectedIconForRoute(
    route: String
): ImageVector {
    return when (route) {
        AppRoutes.HOME -> Icons.Default.Home
        AppRoutes.SCHEDULE -> Icons.Default.CalendarMonth
        AppRoutes.NOTIFICATIONS -> Icons.Default.Notifications
        AppRoutes.ACCOUNT -> Icons.Default.AccountCircle

        AppRoutes.ACCOUNT_MANAGEMENT -> Icons.Default.Groups
        AppRoutes.ACCOUNT_APPROVAL -> Icons.Default.Approval

        AppRoutes.ROOMS,
        AppRoutes.ROOM_DETAIL,
        AppRoutes.ROOM_FORM -> Icons.Default.MeetingRoom

        AppRoutes.COMPUTERS,
        AppRoutes.COMPUTER_DETAIL,
        AppRoutes.COMPUTER_FORM -> Icons.Default.Computer

        AppRoutes.COMPUTER_BULK_CREATE -> Icons.Default.ViewModule
        AppRoutes.COMPUTER_MAP -> Icons.Default.Class

        AppRoutes.BOOKINGS,
        AppRoutes.BOOKING_FORM,
        AppRoutes.BOOKING_HISTORY -> Icons.Default.EventAvailable

        AppRoutes.BOOKING_APPROVAL -> Icons.Default.Approval

        AppRoutes.MACHINE_REPORTS,
        AppRoutes.MACHINE_REPORT_FORM,
        AppRoutes.MACHINE_REPORT_DETAIL -> Icons.Default.ErrorOutline

        AppRoutes.STATISTICS -> Icons.Default.Assessment
        AppRoutes.EXPORT_DATA -> Icons.Default.CloudDownload
        AppRoutes.SETTINGS -> Icons.Default.Settings
        AppRoutes.CHANGE_PASSWORD -> Icons.Default.Key

        else -> Icons.Default.Dashboard
    }
}

private fun unselectedIconForRoute(
    route: String
): ImageVector {
    return when (route) {
        AppRoutes.HOME -> Icons.Outlined.Home
        AppRoutes.SCHEDULE -> Icons.Outlined.CalendarMonth
        AppRoutes.NOTIFICATIONS -> Icons.Outlined.Notifications
        AppRoutes.MACHINE_REPORTS -> Icons.Default.ReportProblem
        AppRoutes.ACCOUNT -> Icons.Outlined.AccountCircle
        else -> selectedIconForRoute(route)
    }
}