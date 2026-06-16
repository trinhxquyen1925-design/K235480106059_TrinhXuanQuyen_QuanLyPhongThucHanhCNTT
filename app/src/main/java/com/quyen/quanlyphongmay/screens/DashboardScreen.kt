@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.quyen.quanlyphongmay.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.navigation.AppRoutes
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.UserRoleBadge
import com.quyen.quanlyphongmay.ui.design.UserStatusBadge
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DashboardScreen(
    user: User,
    onNavigate: (String) -> Unit
) {
    val safeUser = user.normalizedCopy()
    val state = remember(
        safeUser.uid,
        safeUser.email,
        safeUser.role,
        safeUser.status
    ) {
        DashboardState.fromUser(safeUser)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = 0.dp,
            bottom = 18.dp
        )
    ) {
        item {
            EntryAnimation(delayMillis = 0) {
                PremiumDashboardHero(
                    user = safeUser,
                    state = state
                )
            }
        }

        item {
            EntryAnimation(delayMillis = 90) {
                DashboardFeatureList(
                    state = state,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    currentUser: User? = null,
    onNavigateToRooms: () -> Unit = {},
    onNavigateToComputers: () -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    onNavigateToPracticeShifts: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToComputerReports: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToAccountApproval: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val safeUser = currentUser?.normalizedCopy() ?: User().normalizedCopy()

    DashboardScreen(
        user = safeUser,
        onNavigate = { route ->
            when (route) {
                AppRoutes.ROOMS -> onNavigateToRooms()
                AppRoutes.COMPUTERS -> onNavigateToComputers()
                AppRoutes.COMPUTER_MAP -> onNavigateToComputers()
                AppRoutes.BOOKINGS -> onNavigateToBookings()
                AppRoutes.BOOKING_FORM -> onNavigateToBookings()
                AppRoutes.BOOKING_APPROVAL -> onNavigateToBookings()
                AppRoutes.SCHEDULE -> onNavigateToPracticeShifts()
                AppRoutes.MACHINE_REPORTS -> onNavigateToReports()
                AppRoutes.MACHINE_REPORT_FORM -> onNavigateToComputerReports()
                AppRoutes.NOTIFICATIONS -> onNavigateToNotifications()
                AppRoutes.ACCOUNT_MANAGEMENT -> onNavigateToUsers()
                AppRoutes.ACCOUNT_APPROVAL -> onNavigateToAccountApproval()
                AppRoutes.EXPORT_DATA -> onNavigateToExport()
                AppRoutes.SETTINGS -> onNavigateToSettings()
                AppRoutes.CHANGE_PASSWORD -> onNavigateToChangePassword()
                else -> onBack()
            }
        }
    )
}

@Composable
private fun EntryAnimation(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 520,
                delayMillis = delayMillis
            )
        ) + slideInVertically(
            initialOffsetY = { it / 8 },
            animationSpec = tween(
                durationMillis = 520,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 420,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            expandFrom = Alignment.Top
        )
    ) {
        content()
    }
}

@Composable
private fun PremiumDashboardHero(
    user: User,
    state: DashboardState
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "premium_dashboard_hero"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -540f,
        targetValue = 760f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_shimmer"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    val orbit by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_orbit"
    )

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        shadowElevation = 16.dp
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 176.dp)
        ) {
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            state.softColor.copy(alpha = 0.66f),
                            Color(0xFFFAFDFF)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )

                val rightGlow = Offset(
                    x = size.width * 0.94f,
                    y = size.height * 0.18f
                )
                val leftGlow = Offset(
                    x = size.width * 0.12f,
                    y = size.height * 0.92f
                )
                val orbitCenter = Offset(
                    x = size.width * 0.80f,
                    y = size.height * 0.44f
                )

                drawCircle(
                    color = state.primaryColor.copy(alpha = 0.12f),
                    radius = size.width * 0.34f,
                    center = rightGlow
                )
                drawCircle(
                    color = state.primaryColor.copy(alpha = 0.08f),
                    radius = size.width * 0.24f,
                    center = leftGlow
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.68f),
                    radius = size.width * 0.18f,
                    center = Offset(size.width * 1.02f, size.height * 0.78f)
                )

                for (index in 0 until 7) {
                    val angle = Math.toRadians((orbit + index * 51f).toDouble())
                    val radius = size.minDimension * (0.16f + index * 0.006f)
                    val dotCenter = Offset(
                        x = orbitCenter.x + cos(angle).toFloat() * radius,
                        y = orbitCenter.y + sin(angle).toFloat() * radius * 0.68f
                    )

                    drawCircle(
                        color = state.primaryColor.copy(alpha = 0.16f),
                        radius = 4.5f + index,
                        center = dotCenter
                    )
                }

                drawLine(
                    color = Color.White.copy(alpha = 0.70f),
                    start = Offset(size.width * 0.62f, size.height * 0.22f),
                    end = Offset(size.width * 0.96f, size.height * 0.74f),
                    strokeWidth = 2.6f,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(18f, 14f),
                        phase = orbit / 3f
                    )
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.92f),
                    radius = 5.6f,
                    center = Offset(size.width * 0.84f, size.height * 0.48f)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.78f),
                    radius = 3.2f,
                    center = Offset(size.width * 0.67f, size.height * 0.25f)
                )

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.58f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + size.width * 0.28f, size.height)
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = floatingOffset.roundToInt()
                            )
                        },
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White.copy(alpha = 0.80f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = state.primaryColor.copy(alpha = 0.20f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(19.dp),
                            color = state.softColor
                        ) {
                            Icon(
                                imageVector = state.heroIcon,
                                contentDescription = null,
                                tint = state.primaryColor,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(15.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.greeting(user),
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
                        text = state.subtitle,
                        color = AppColors.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.6.sp,
                            lineHeight = 18.sp
                        )
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRoleBadge(role = user.role)
                        UserStatusBadge(status = user.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardFeatureList(
    state: DashboardState,
    onNavigate: (String) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(15.dp),
        shadowElevation = 14.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardSectionTitle(
                title = state.featureTitle,
                subtitle = state.featureSubtitle,
                icon = Icons.Default.GridView,
                tone = state.tone
            )

            state.actions.forEachIndexed { index, action ->
                DashboardActionCard(
                    index = index + 1,
                    item = action,
                    onClick = {
                        onNavigate(action.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun DashboardActionCard(
    index: Int,
    item: DashboardAction,
    onClick: () -> Unit
) {
    val colors = dashboardToneColors(item.tone)
    val infiniteTransition = rememberInfiniteTransition(
        label = "action_card_$index"
    )

    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -260f,
        targetValue = 520f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3600 + index * 95,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "action_shine_$index"
    )

    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1450 + index * 80,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "action_icon_pulse_$index"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE0EAF5)
        ),
        shadowElevation = 4.dp
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFAFDFF),
                            Color(0xFFF2F7FF)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(
                        x = 26.dp.toPx(),
                        y = 26.dp.toPx()
                    )
                )

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.56f),
                            Color.Transparent
                        ),
                        start = Offset(shineOffset, 0f),
                        end = Offset(shineOffset + size.width * 0.20f, size.height)
                    )
                )

                drawLine(
                    color = Color.White.copy(alpha = 0.72f),
                    start = Offset(size.width * 0.18f, size.height * 0.18f),
                    end = Offset(size.width * 0.86f, size.height * 0.18f),
                    strokeWidth = 1.2f
                )

                drawLine(
                    color = Color(0xFF2F6FE4).copy(alpha = 0.08f),
                    start = Offset(size.width * 0.18f, size.height * 0.82f),
                    end = Offset(size.width * 0.82f, size.height * 0.82f),
                    strokeWidth = 1.3f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = Color(0xFFF7FBFF),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFE2EBF6)
                    )
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = index.toString().padStart(2, '0'),
                            color = AppColors.Muted,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.2.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer(
                            scaleX = iconPulse,
                            scaleY = iconPulse
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFEAF3FF),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFD9E8FF)
                    ),
                    shadowElevation = 3.dp
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color(0xFF2F6FE4),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.4.sp,
                            lineHeight = 19.sp
                        )
                    )

                    Text(
                        text = item.subtitle,
                        color = AppColors.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.9.sp,
                            lineHeight = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF2F6FE4),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionTitle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tone: AppBadgeTone
) {
    val colors = dashboardToneColors(tone)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(18.dp),
            color = colors.background,
            border = BorderStroke(
                width = 1.dp,
                color = colors.border
            ),
            shadowElevation = 3.dp
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.padding(11.dp)
            )
        }

        Spacer(modifier = Modifier.width(11.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = AppColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    lineHeight = 26.sp
                )
            )

            Text(
                text = subtitle,
                color = AppColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.4.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Immutable
private data class DashboardState(
    val fallbackName: String,
    val heroIcon: ImageVector,
    val primaryColor: Color,
    val softColor: Color,
    val tone: AppBadgeTone,
    val subtitle: String,
    val featureTitle: String,
    val featureSubtitle: String,
    val actions: List<DashboardAction>
) {
    fun greeting(
        user: User
    ): String {
        val displayName = user.displayName.ifBlank {
            user.email.ifBlank {
                fallbackName
            }
        }

        return "Xin chào, $displayName"
    }

    companion object {
        fun fromUser(
            user: User
        ): DashboardState {
            return when {
                user.isAdmin -> admin()
                user.isTeacher -> teacher()
                else -> student()
            }
        }

        private fun admin(): DashboardState {
            return DashboardState(
                fallbackName = "Quản trị viên",
                heroIcon = Icons.Default.AdminPanelSettings,
                primaryColor = AppColors.Purple,
                softColor = AppColors.PurpleSoft,
                tone = AppBadgeTone.Purple,
                subtitle = "Quản lý toàn bộ hệ thống phòng máy, tài khoản, booking và dữ liệu.",
                featureTitle = "Chức năng quản trị",
                featureSubtitle = "Bảng điều khiển module chính, gọn và thống nhất.",
                actions = listOf(
                    DashboardAction(
                        title = "Duyệt tài khoản",
                        subtitle = "Xem danh sách tài khoản chờ duyệt và phê duyệt.",
                        icon = Icons.Default.VerifiedUser,
                        tone = AppBadgeTone.Purple,
                        route = AppRoutes.ACCOUNT_APPROVAL
                    ),
                    DashboardAction(
                        title = "Quản lý tài khoản",
                        subtitle = "Quản lý thông tin người dùng và phân quyền.",
                        icon = Icons.Default.Groups,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.ACCOUNT_MANAGEMENT
                    ),
                    DashboardAction(
                        title = "Duyệt booking phòng máy",
                        subtitle = "Kiểm tra và duyệt các yêu cầu đặt phòng.",
                        icon = Icons.Default.EventAvailable,
                        tone = AppBadgeTone.Warning,
                        route = AppRoutes.BOOKING_APPROVAL
                    ),
                    DashboardAction(
                        title = "Quản lý phòng máy",
                        subtitle = "Quản lý thông tin phòng, số hàng và số máy.",
                        icon = Icons.Default.MeetingRoom,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.ROOMS
                    ),
                    DashboardAction(
                        title = "Quản lý máy tính",
                        subtitle = "Thêm máy, thêm hàng loạt và cập nhật tình trạng.",
                        icon = Icons.Default.Computer,
                        tone = AppBadgeTone.Success,
                        route = AppRoutes.COMPUTERS
                    ),
                    DashboardAction(
                        title = "Sơ đồ phòng máy",
                        subtitle = "Xem bố cục phòng máy theo sơ đồ trực quan.",
                        icon = Icons.Default.GridView,
                        tone = AppBadgeTone.Purple,
                        route = AppRoutes.COMPUTER_MAP
                    ),
                    DashboardAction(
                        title = "Thông báo hệ thống",
                        subtitle = "Tạo và gửi thông báo đến người dùng.",
                        icon = Icons.Default.Notifications,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.NOTIFICATIONS
                    ),
                    DashboardAction(
                        title = "Xử lý báo lỗi máy",
                        subtitle = "Tiếp nhận, theo dõi và cập nhật trạng thái lỗi máy tính.",
                        icon = Icons.Default.ReportProblem,
                        tone = AppBadgeTone.Error,
                        route = AppRoutes.MACHINE_REPORTS
                    ),
                    DashboardAction(
                        title = "Xuất dữ liệu CSV",
                        subtitle = "Xuất dữ liệu tài khoản, phòng máy và máy tính.",
                        icon = Icons.Default.CloudDownload,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.EXPORT_DATA
                    )
                )
            )
        }

        private fun teacher(): DashboardState {
            return DashboardState(
                fallbackName = "Giáo viên",
                heroIcon = Icons.Default.School,
                primaryColor = AppColors.Blue,
                softColor = AppColors.BlueSoft,
                tone = AppBadgeTone.Info,
                subtitle = "Tạo booking, xem lịch dạy, theo dõi phòng máy và gửi thông báo.",
                featureTitle = "Chức năng giảng viên",
                featureSubtitle = "Các module phục vụ đăng ký phòng và quản lý buổi học.",
                actions = listOf(
                    DashboardAction(
                        title = "Tạo booking phòng máy",
                        subtitle = "Đăng ký phòng, lớp, môn học, thời gian và số lượng sinh viên.",
                        icon = Icons.Default.CalendarMonth,
                        tone = AppBadgeTone.Warning,
                        route = AppRoutes.BOOKING_FORM
                    ),
                    DashboardAction(
                        title = "Xem lịch dạy",
                        subtitle = "Theo dõi lịch phòng máy đã được duyệt.",
                        icon = Icons.Default.EventAvailable,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.SCHEDULE
                    ),
                    DashboardAction(
                        title = "Xem phòng máy",
                        subtitle = "Xem danh sách phòng, trạng thái phòng và sức chứa.",
                        icon = Icons.Default.MeetingRoom,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.ROOMS
                    ),
                    DashboardAction(
                        title = "Xem máy tính",
                        subtitle = "Theo dõi cấu hình, trạng thái và vị trí máy.",
                        icon = Icons.Default.Computer,
                        tone = AppBadgeTone.Success,
                        route = AppRoutes.COMPUTERS
                    ),
                    DashboardAction(
                        title = "Sơ đồ phòng máy",
                        subtitle = "Xem bố cục phòng máy và chỗ ngồi sinh viên.",
                        icon = Icons.Default.GridView,
                        tone = AppBadgeTone.Purple,
                        route = AppRoutes.COMPUTER_MAP
                    ),
                    DashboardAction(
                        title = "Báo lỗi máy",
                        subtitle = "Gửi và theo dõi lỗi máy tính trong buổi thực hành.",
                        icon = Icons.Default.ReportProblem,
                        tone = AppBadgeTone.Error,
                        route = AppRoutes.MACHINE_REPORTS
                    ),
                    DashboardAction(
                        title = "Gửi thông báo sinh viên",
                        subtitle = "Gửi thông báo học tập hoặc thay đổi lịch học.",
                        icon = Icons.Default.Send,
                        tone = AppBadgeTone.Purple,
                        route = AppRoutes.NOTIFICATIONS
                    )
                )
            )
        }

        private fun student(): DashboardState {
            return DashboardState(
                fallbackName = "Sinh viên",
                heroIcon = Icons.Default.School,
                primaryColor = AppColors.Green,
                softColor = AppColors.GreenSoft,
                tone = AppBadgeTone.Success,
                subtitle = "Xem lịch học, chỗ ngồi, sơ đồ phòng máy và thông báo.",
                featureTitle = "Chức năng sinh viên",
                featureSubtitle = "Theo dõi lịch học, chỗ ngồi, báo lỗi máy và thông báo.",
                actions = listOf(
                    DashboardAction(
                        title = "Xem lịch học",
                        subtitle = "Xem ngày học, thời gian, phòng máy và môn học.",
                        icon = Icons.Default.CalendarMonth,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.SCHEDULE
                    ),
                    DashboardAction(
                        title = "Xem chỗ ngồi",
                        subtitle = "Xem vị trí máy được phân công theo sơ đồ.",
                        icon = Icons.Default.GridView,
                        tone = AppBadgeTone.Success,
                        route = AppRoutes.SCHEDULE
                    ),
                    DashboardAction(
                        title = "Sơ đồ phòng máy",
                        subtitle = "Xem bố cục máy tính, hàng, cột và vị trí trong phòng.",
                        icon = Icons.Default.MeetingRoom,
                        tone = AppBadgeTone.Purple,
                        route = AppRoutes.COMPUTER_MAP
                    ),
                    DashboardAction(
                        title = "Báo lỗi máy",
                        subtitle = "Chọn máy theo sơ đồ phòng và gửi mô tả lỗi.",
                        icon = Icons.Default.ReportProblem,
                        tone = AppBadgeTone.Error,
                        route = AppRoutes.MACHINE_REPORTS
                    ),
                    DashboardAction(
                        title = "Thông báo của bạn",
                        subtitle = "Đọc thông báo từ giáo viên và quản trị viên.",
                        icon = Icons.Default.Notifications,
                        tone = AppBadgeTone.Info,
                        route = AppRoutes.NOTIFICATIONS
                    ),
                    DashboardAction(
                        title = "Tài khoản cá nhân",
                        subtitle = "Xem thông tin cá nhân và trạng thái tài khoản.",
                        icon = Icons.Default.Person,
                        tone = AppBadgeTone.Success,
                        route = AppRoutes.ACCOUNT
                    )
                )
            )
        }
    }
}

@Immutable
private data class DashboardAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tone: AppBadgeTone,
    val route: String
)

@Immutable
private data class DashboardToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun dashboardToneColors(
    tone: AppBadgeTone
): DashboardToneColors {
    return when (tone) {
        AppBadgeTone.Info -> DashboardToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.20f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> DashboardToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.20f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> DashboardToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.22f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> DashboardToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.20f),
            content = AppColors.Rose
        )

        AppBadgeTone.Purple -> DashboardToneColors(
            background = Color(0xFFEAF3FF),
            border = Color(0xFFD9E8FF),
            content = Color(0xFF2F6FE4)
        )

        AppBadgeTone.Neutral -> DashboardToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )
    }
}
