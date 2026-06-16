@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.quyen.quanlyphongmay.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.ExportReportRepository
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.AppGradientButton
import com.quyen.quanlyphongmay.ui.design.AppOutlinedButton
import com.quyen.quanlyphongmay.ui.design.AppSectionHeader
import com.quyen.quanlyphongmay.ui.design.StatCard
import com.quyen.quanlyphongmay.ui.design.StatusBadge

@Composable
fun ExportReportScreen(
    currentUser: User,
    onShowMessage: (String) -> Unit
) {
    val safeUser = currentUser.normalizedCopy()
    val context = LocalContext.current
    val repository = remember {
        ExportReportRepository()
    }

    var includeAccount by rememberSaveable {
        mutableStateOf(true)
    }

    var includeRoom by rememberSaveable {
        mutableStateOf(true)
    }

    var includeComputer by rememberSaveable {
        mutableStateOf(true)
    }

    var includeBooking by rememberSaveable {
        mutableStateOf(true)
    }

    var includeNotification by rememberSaveable {
        mutableStateOf(true)
    }

    var includeMachineReport by rememberSaveable {
        mutableStateOf(true)
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var exporting by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    var snapshot by remember {
        mutableStateOf(ExportReportRepository.ExportReportSnapshot())
    }

    var lastExportMessage by rememberSaveable {
        mutableStateOf("")
    }

    val currentOptions = remember(
        includeAccount,
        includeRoom,
        includeComputer,
        includeBooking,
        includeNotification,
        includeMachineReport
    ) {
        ExportReportRepository.ExportReportOptions(
            reportType = "full",
            format = "csv",
            includeAccounts = includeAccount,
            includeRooms = includeRoom,
            includeComputers = includeComputer,
            includeBookings = includeBooking,
            includeNotifications = includeNotification,
            includeMachineReports = includeMachineReport
        ).normalizedCopy()
    }

    fun refreshData() {
        if (!safeUser.isAdmin) {
            errorMessage = "Chỉ quản trị viên có quyền xuất dữ liệu."
            loading = false
            return
        }

        loading = true
        errorMessage = ""

        repository.loadReportSnapshot(
            currentUser = safeUser
        ) { success, message, result ->
            loading = false

            if (success) {
                snapshot = result.normalizedCopy()
            } else {
                errorMessage = message ?: "Không thể tải dữ liệu."
            }
        }
    }

    fun exportCsv() {
        if (!safeUser.isAdmin) {
            onShowMessage("Chỉ quản trị viên có quyền xuất dữ liệu.")
            return
        }

        if (loading || exporting) {
            onShowMessage("Đang xử lý dữ liệu, vui lòng chờ.")
            return
        }

        val selectedAny = includeAccount ||
                includeRoom ||
                includeComputer ||
                includeBooking ||
                includeNotification ||
                includeMachineReport

        if (!selectedAny) {
            onShowMessage("Vui lòng chọn ít nhất một nhóm dữ liệu để xuất.")
            return
        }

        exporting = true
        errorMessage = ""
        lastExportMessage = ""

        repository.exportSeparatedCsvFilesToDownloads(
            context = context,
            currentUser = safeUser,
            options = currentOptions
        ) { success, message, _ ->
            exporting = false

            if (success) {
                lastExportMessage = message ?: "Đã xuất CSV vào Download/QuanLyPhongMay."
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể xuất CSV."
            }

            onShowMessage(
                message ?: if (success) {
                    "Đã xuất CSV vào Download/QuanLyPhongMay."
                } else {
                    "Không thể xuất CSV."
                }
            )
        }
    }

    LaunchedEffect(
        safeUser.uid,
        safeUser.email,
        reloadKey
    ) {
        refreshData()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = 2.dp,
            bottom = 18.dp
        )
    ) {
        if (!safeUser.isAdmin) {
            item {
                ExportAccessDeniedCard()
            }
        } else {
            item {
                ExportOverviewCard(
                    snapshot = snapshot,
                    loading = loading || exporting,
                    errorMessage = errorMessage,
                    onRefresh = {
                        reloadKey++
                    }
                )
            }

            item {
                ExportScopeCard(
                    includeAccount = includeAccount,
                    includeRoom = includeRoom,
                    includeComputer = includeComputer,
                    includeBooking = includeBooking,
                    includeNotification = includeNotification,
                    includeMachineReport = includeMachineReport,
                    onToggleAccount = { includeAccount = !includeAccount },
                    onToggleRoom = { includeRoom = !includeRoom },
                    onToggleComputer = { includeComputer = !includeComputer },
                    onToggleBooking = { includeBooking = !includeBooking },
                    onToggleNotification = { includeNotification = !includeNotification },
                    onToggleMachineReport = { includeMachineReport = !includeMachineReport }
                )
            }

            item {
                ExportActionCard(
                    loading = loading,
                    exporting = exporting,
                    selectedCount = listOf(
                        includeAccount,
                        includeRoom,
                        includeComputer,
                        includeBooking,
                        includeNotification,
                        includeMachineReport
                    ).count { it },
                    onExport = { exportCsv() },
                    onRefresh = { reloadKey++ },
                    onSelectAll = {
                        includeAccount = true
                        includeRoom = true
                        includeComputer = true
                        includeBooking = true
                        includeNotification = true
                        includeMachineReport = true
                    }
                )
            }

            if (lastExportMessage.isNotBlank()) {
                item {
                    ExportResultCard(
                        message = lastExportMessage
                    )
                }
            }
        }
    }
}

@Composable
fun ExportReportScreen(
    currentUser: User? = null,
    onShowMessage: (String) -> Unit = {}
) {
    ExportReportScreen(
        currentUser = currentUser?.normalizedCopy() ?: User().normalizedCopy(),
        onShowMessage = onShowMessage
    )
}

@Composable
private fun ExportAccessDeniedCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(20.dp),
                color = AppColors.RoseSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Rose.copy(alpha = 0.18f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = AppColors.Rose,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Không có quyền xuất dữ liệu",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                )

                Text(
                    text = "Chỉ quản trị viên được tải dữ liệu hệ thống và tạo file CSV.",
                    color = AppColors.Muted,
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

@Composable
private fun ExportOverviewCard(
    snapshot: ExportReportRepository.ExportReportSnapshot,
    loading: Boolean,
    errorMessage: String,
    onRefresh: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppSectionHeader(
                    title = "Tổng quan dữ liệu",
                    subtitle = when {
                        loading -> "Đang tải dữ liệu từ Firestore."
                        errorMessage.isNotBlank() -> errorMessage
                        snapshot.hasAnyData -> "Cập nhật lúc ${snapshot.generatedTimeText}."
                        else -> "Chưa có dữ liệu trong các collection."
                    },
                    modifier = Modifier.weight(1f)
                )

                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = AppColors.Blue
                    )
                } else {
                    ExportSmallButton(
                        text = "Tải lại",
                        icon = Icons.Default.Refresh,
                        tone = AppBadgeTone.Info,
                        onClick = onRefresh
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Tài khoản",
                    value = snapshot.accountTotal.toString(),
                    icon = Icons.Default.Groups,
                    tone = AppBadgeTone.Purple,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Phòng",
                    value = snapshot.roomTotal.toString(),
                    icon = Icons.Default.MeetingRoom,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Máy",
                    value = snapshot.computerTotal.toString(),
                    icon = Icons.Default.Computer,
                    tone = AppBadgeTone.Success,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Booking",
                    value = snapshot.bookingTotal.toString(),
                    icon = Icons.Default.CalendarMonth,
                    tone = AppBadgeTone.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Thông báo",
                    value = snapshot.notificationTotal.toString(),
                    icon = Icons.Default.Notifications,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Báo lỗi",
                    value = snapshot.machineReportTotal.toString(),
                    icon = Icons.Default.ReportProblem,
                    tone = AppBadgeTone.Error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExportScopeCard(
    includeAccount: Boolean,
    includeRoom: Boolean,
    includeComputer: Boolean,
    includeBooking: Boolean,
    includeNotification: Boolean,
    includeMachineReport: Boolean,
    onToggleAccount: () -> Unit,
    onToggleRoom: () -> Unit,
    onToggleComputer: () -> Unit,
    onToggleBooking: () -> Unit,
    onToggleNotification: () -> Unit,
    onToggleMachineReport: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppSectionHeader(
                title = "Chọn dữ liệu xuất",
                subtitle = "Mỗi nhóm được chọn sẽ tạo một file CSV riêng trong Download/QuanLyPhongMay."
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportScopeChip(
                    text = "Tài khoản",
                    selected = includeAccount,
                    icon = Icons.Default.Groups,
                    tone = AppBadgeTone.Purple,
                    onClick = onToggleAccount
                )

                ExportScopeChip(
                    text = "Phòng máy",
                    selected = includeRoom,
                    icon = Icons.Default.MeetingRoom,
                    tone = AppBadgeTone.Info,
                    onClick = onToggleRoom
                )

                ExportScopeChip(
                    text = "Máy tính",
                    selected = includeComputer,
                    icon = Icons.Default.Computer,
                    tone = AppBadgeTone.Success,
                    onClick = onToggleComputer
                )

                ExportScopeChip(
                    text = "Booking",
                    selected = includeBooking,
                    icon = Icons.Default.CalendarMonth,
                    tone = AppBadgeTone.Warning,
                    onClick = onToggleBooking
                )

                ExportScopeChip(
                    text = "Thông báo",
                    selected = includeNotification,
                    icon = Icons.Default.Notifications,
                    tone = AppBadgeTone.Info,
                    onClick = onToggleNotification
                )

                ExportScopeChip(
                    text = "Báo lỗi",
                    selected = includeMachineReport,
                    icon = Icons.Default.ReportProblem,
                    tone = AppBadgeTone.Error,
                    onClick = onToggleMachineReport
                )
            }
        }
    }
}

@Composable
private fun ExportActionCard(
    loading: Boolean,
    exporting: Boolean,
    selectedCount: Int,
    onExport: () -> Unit,
    onRefresh: () -> Unit,
    onSelectAll: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppSectionHeader(
                title = "Xuất CSV",
                subtitle = when {
                    exporting -> "Đang tạo file trong Download/QuanLyPhongMay."
                    loading -> "Đang tải dữ liệu, vui lòng chờ."
                    selectedCount <= 0 -> "Chọn ít nhất một nhóm dữ liệu để xuất."
                    else -> "Đã chọn $selectedCount nhóm dữ liệu."
                }
            )

            AppGradientButton(
                text = if (exporting) "Đang xuất CSV..." else "Xuất CSV",
                onClick = onExport,
                leadingIcon = Icons.Default.CloudDownload,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppOutlinedButton(
                    text = "Chọn tất cả",
                    onClick = onSelectAll,
                    leadingIcon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )

                AppOutlinedButton(
                    text = "Tải lại",
                    onClick = onRefresh,
                    leadingIcon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExportResultCard(
    message: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(18.dp),
                color = AppColors.GreenSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Green.copy(alpha = 0.18f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SaveAlt,
                    contentDescription = null,
                    tint = AppColors.Green,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Xuất CSV thành công",
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = message,
                    color = AppColors.Muted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }

            StatusBadge(
                text = "Xong",
                tone = AppBadgeTone.Success
            )
        }
    }
}

@Composable
private fun ExportScopeChip(
    text: String,
    selected: Boolean,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = exportToneColors(tone)

    Surface(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) colors.background else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) colors.border else AppColors.Border
        ),
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .height(42.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (selected) colors.content else AppColors.Muted,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                text = text,
                color = if (selected) colors.content else AppColors.Muted,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun ExportSmallButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = exportToneColors(tone)

    Surface(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
        border = BorderStroke(
            width = 1.dp,
            color = colors.border
        )
    ) {
        Row(
            modifier = Modifier
                .height(38.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                text = text,
                color = colors.content,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Immutable
private data class ExportToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun exportToneColors(
    tone: AppBadgeTone
): ExportToneColors {
    return when (tone) {
        AppBadgeTone.Info -> ExportToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.20f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> ExportToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.20f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> ExportToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.22f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> ExportToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.20f),
            content = AppColors.Rose
        )

        AppBadgeTone.Purple -> ExportToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.20f),
            content = AppColors.Purple
        )

        AppBadgeTone.Neutral -> ExportToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )
    }
}
