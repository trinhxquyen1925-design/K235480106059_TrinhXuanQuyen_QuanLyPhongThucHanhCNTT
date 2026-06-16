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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.core.AppAuthContract
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.UserManagementRepository
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.AppOutlinedButton
import com.quyen.quanlyphongmay.ui.design.AppTextField
import com.quyen.quanlyphongmay.ui.design.ConfirmDialog
import com.quyen.quanlyphongmay.ui.design.EmptyState
import com.quyen.quanlyphongmay.ui.design.ErrorBanner
import com.quyen.quanlyphongmay.ui.design.LoadingStateCard
import com.quyen.quanlyphongmay.ui.design.PermissionDeniedCard
import com.quyen.quanlyphongmay.ui.design.StatusBadge
import com.quyen.quanlyphongmay.ui.design.UserRoleBadge
import com.quyen.quanlyphongmay.ui.design.UserStatusBadge

@Composable
fun UserManagementScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    repository: UserManagementRepository = UserManagementRepository(),
    onlyPending: Boolean = false,
    onShowMessage: (String) -> Unit = {}
) {
    val adminUser = currentUser.normalizedCopy()

    if (!adminUser.isAdmin) {
        PermissionDeniedCard(
            modifier = modifier.fillMaxWidth(),
            message = "Chỉ quản trị viên mới có quyền quản lý tài khoản."
        )
        return
    }

    var keyword by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable {
        mutableStateOf(
            if (onlyPending) AppAuthContract.Status.Pending.firestoreValue else ""
        )
    }

    var loading by remember { mutableStateOf(true) }
    var actionLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var usersResult by remember { mutableStateOf(UserManagementRepository.UsersResult()) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var confirmState by remember { mutableStateOf<UserActionConfirmState?>(null) }

    fun reloadUsers() {
        loading = true
        errorMessage = ""

        if (onlyPending) {
            repository.getPendingUsers { success, message, users ->
                loading = false
                if (success) {
                    usersResult = buildResultFromList(users)
                } else {
                    errorMessage = message ?: "Không thể tải danh sách tài khoản."
                    usersResult = UserManagementRepository.UsersResult()
                }
            }
        } else {
            repository.getUsers(
                filter = UserManagementRepository.UserFilter(
                    keyword = keyword,
                    role = selectedRole,
                    status = selectedStatus
                )
            ) { success, message, result ->
                loading = false
                if (success) {
                    usersResult = result
                } else {
                    errorMessage = message ?: "Không thể tải danh sách tài khoản."
                    usersResult = UserManagementRepository.UsersResult()
                }
            }
        }
    }

    fun executeAction(state: UserActionConfirmState) {
        actionLoading = true
        errorMessage = ""

        val callback: (Boolean, String?) -> Unit = { success, message ->
            actionLoading = false
            confirmState = null

            if (success) {
                onShowMessage(message ?: "Đã cập nhật tài khoản.")
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể cập nhật tài khoản."
            }
        }

        when (state.action) {
            UserAction.Approve -> {
                repository.approveUser(
                    targetUser = state.user,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            UserAction.Reject -> {
                repository.rejectUser(
                    targetUser = state.user,
                    adminUser = adminUser,
                    reason = "Tài khoản không đủ điều kiện phê duyệt.",
                    callback = callback
                )
            }

            UserAction.Lock -> {
                repository.lockUser(
                    targetUser = state.user,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            UserAction.Unlock -> {
                repository.unlockUser(
                    targetUser = state.user,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            UserAction.Delete -> {
                repository.deleteUserDocument(
                    targetUser = state.user,
                    adminUser = adminUser,
                    callback = callback
                )
            }
        }
    }

    LaunchedEffect(reloadKey, keyword, selectedRole, selectedStatus, onlyPending) {
        reloadUsers()
    }

    ConfirmDialog(
        visible = confirmState != null,
        title = confirmState?.title.orEmpty(),
        message = confirmState?.message.orEmpty(),
        confirmText = confirmState?.confirmText.orEmpty(),
        danger = confirmState?.danger == true,
        loading = actionLoading,
        onConfirm = {
            val state = confirmState
            if (state != null) {
                executeAction(state)
            }
        },
        onDismiss = {
            if (!actionLoading) {
                confirmState = null
            }
        }
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = 8.dp
        )
    ) {
        item {
            if (onlyPending) {
                PendingApprovalTopPanel(
                    result = usersResult,
                    onRefresh = { reloadKey++ }
                )
            } else {
                AccountManagementTopPanel(
                    result = usersResult,
                    onRefresh = { reloadKey++ }
                )
            }
        }

        if (!onlyPending) {
            item {
                AccountFilterPanel(
                    keyword = keyword,
                    onKeywordChange = { keyword = it },
                    selectedRole = selectedRole,
                    onRoleChange = { selectedRole = it },
                    selectedStatus = selectedStatus,
                    onStatusChange = { selectedStatus = it },
                    onClearFilter = {
                        keyword = ""
                        selectedRole = ""
                        selectedStatus = ""
                    }
                )
            }
        }

        item {
            ErrorBanner(
                message = errorMessage,
                visible = errorMessage.isNotBlank()
            )
        }

        if (loading) {
            item {
                LoadingStateCard(
                    message = if (onlyPending) {
                        "Đang tải tài khoản chờ duyệt"
                    } else {
                        "Đang tải danh sách tài khoản"
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            if (usersResult.users.isEmpty()) {
                item {
                    EmptyState(
                        title = if (onlyPending) "Không có tài khoản chờ duyệt" else "Không tìm thấy tài khoản",
                        message = if (onlyPending) {
                            "Hiện tại không có tài khoản nào cần phê duyệt."
                        } else {
                            "Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc."
                        },
                        icon = Icons.Default.Groups,
                        actionText = "Tải lại",
                        onActionClick = { reloadKey++ }
                    )
                }
            } else {
                items(
                    items = usersResult.users,
                    key = { user -> user.safeId.ifBlank { user.email } }
                ) { user ->
                    if (onlyPending) {
                        PendingApprovalUserCard(
                            user = user,
                            adminUser = adminUser,
                            onActionClick = { action, target ->
                                confirmState = buildConfirmState(action, target)
                            }
                        )
                    } else {
                        ManagementUserCard(
                            user = user,
                            adminUser = adminUser,
                            onActionClick = { action, target ->
                                confirmState = buildConfirmState(action, target)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountManagementTopPanel(
    result: UserManagementRepository.UsersResult,
    onRefresh: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng quan",
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                SmallPillButton(
                    text = "Tải lại",
                    icon = Icons.Default.Refresh,
                    tone = AppBadgeTone.Info,
                    onClick = onRefresh
                )
            }

            CompactStatRow(result = result)
        }
    }
}

@Composable
private fun PendingApprovalTopPanel(
    result: UserManagementRepository.UsersResult,
    onRefresh: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tài khoản chờ duyệt",
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "Kiểm tra thông tin đăng ký trước khi phê duyệt.",
                        color = AppColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }

                SmallPillButton(
                    text = "Tải lại",
                    icon = Icons.Default.Refresh,
                    tone = AppBadgeTone.Info,
                    onClick = onRefresh
                )
            }

            CompactStatRow(result = result)
        }
    }
}

@Composable
private fun CompactStatRow(
    result: UserManagementRepository.UsersResult
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniStatTile(
            title = "Tổng",
            value = result.total.toString(),
            icon = Icons.Default.Groups,
            tone = AppBadgeTone.Info,
            modifier = Modifier.weight(1f)
        )

        MiniStatTile(
            title = "Chờ",
            value = result.pendingCount.toString(),
            icon = Icons.Default.HourglassTop,
            tone = AppBadgeTone.Warning,
            modifier = Modifier.weight(1f)
        )

        MiniStatTile(
            title = "Duyệt",
            value = result.approvedCount.toString(),
            icon = Icons.Default.DoneAll,
            tone = AppBadgeTone.Success,
            modifier = Modifier.weight(1f)
        )

        MiniStatTile(
            title = "Khóa",
            value = result.lockedCount.toString(),
            icon = Icons.Default.Lock,
            tone = AppBadgeTone.Neutral,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniStatTile(
    title: String,
    value: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    modifier: Modifier = Modifier
) {
    val colors = actionButtonColors(tone)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.height(19.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                color = AppColors.Ink,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            )

            Text(
                text = title,
                color = AppColors.Muted,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountFilterPanel(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    selectedRole: String,
    onRoleChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        shadowElevation = 6.dp
    ) {
        Column {
            AppTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                label = "Tìm kiếm",
                placeholder = "Tên, email, mã, lớp/khoa, số điện thoại",
                leadingIcon = Icons.Default.Search,
                minHeight = 54.dp,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            FilterTitle("Quyền tài khoản")

            Spacer(modifier = Modifier.height(7.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterChipButton("Tất cả", "", selectedRole, Icons.Default.FilterAlt, onRoleChange)
                FilterChipButton("Sinh viên", AppAuthContract.Role.Student.firestoreValue, selectedRole, Icons.Default.School, onRoleChange)
                FilterChipButton("Giáo viên", AppAuthContract.Role.Teacher.firestoreValue, selectedRole, Icons.Default.Work, onRoleChange)
                FilterChipButton("Quản trị", AppAuthContract.Role.Admin.firestoreValue, selectedRole, Icons.Default.AdminPanelSettings, onRoleChange)
            }

            Spacer(modifier = Modifier.height(10.dp))

            FilterTitle("Trạng thái")

            Spacer(modifier = Modifier.height(7.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterChipButton("Tất cả", "", selectedStatus, Icons.Default.FilterAlt, onStatusChange)
                FilterChipButton("Chờ duyệt", AppAuthContract.Status.Pending.firestoreValue, selectedStatus, Icons.Default.HourglassTop, onStatusChange)
                FilterChipButton("Đã duyệt", AppAuthContract.Status.Approved.firestoreValue, selectedStatus, Icons.Default.DoneAll, onStatusChange)
                FilterChipButton("Từ chối", AppAuthContract.Status.Rejected.firestoreValue, selectedStatus, Icons.Default.Cancel, onStatusChange)
                FilterChipButton("Đã khóa", AppAuthContract.Status.Locked.firestoreValue, selectedStatus, Icons.Default.Lock, onStatusChange)
            }

            Spacer(modifier = Modifier.height(10.dp))

            AppOutlinedButton(
                text = "Xóa bộ lọc",
                onClick = onClearFilter,
                leadingIcon = Icons.Default.Refresh,
                height = 44.dp
            )
        }
    }
}

@Composable
private fun FilterTitle(
    text: String
) {
    Text(
        text = text,
        color = AppColors.Text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    )
}

@Composable
private fun FilterChipButton(
    text: String,
    value: String,
    selectedValue: String,
    icon: ImageVector,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    val colors = if (selected) {
        actionButtonColors(AppBadgeTone.Info)
    } else {
        LocalColorSet(
            background = AppColors.CardSolid,
            border = AppColors.Border,
            content = AppColors.Muted
        )
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = if (selected) 3.dp else 0.dp,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onSelected(value) }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.height(17.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = text,
                color = colors.content,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun ManagementUserCard(
    user: User,
    adminUser: User,
    onActionClick: (UserAction, User) -> Unit
) {
    val safeUser = user.normalizedCopy()
    val canChange = adminUser.isAdmin && !safeUser.isSystemAdmin

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        shadowElevation = 7.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            CompactUserTopLine(user = safeUser)

            CompactUserInfoRows(user = safeUser)

            if (safeUser.isSystemAdmin) {
                ProtectedNotice()
            } else if (canChange) {
                ManagementActions(
                    user = safeUser,
                    onActionClick = onActionClick
                )
            }
        }
    }
}

@Composable
private fun PendingApprovalUserCard(
    user: User,
    adminUser: User,
    onActionClick: (UserAction, User) -> Unit
) {
    val safeUser = user.normalizedCopy()
    val canChange = adminUser.isAdmin && !safeUser.isSystemAdmin

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        shadowElevation = 7.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ApprovalUserTopLine(user = safeUser)

            ApprovalFullRegisterInfo(user = safeUser)

            if (safeUser.isSystemAdmin) {
                ProtectedNotice()
            } else if (canChange) {
                ApprovalActions(
                    user = safeUser,
                    onActionClick = onActionClick
                )
            }
        }
    }
}

@Composable
private fun CompactUserTopLine(
    user: User
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = user)

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.displayName,
                color = AppColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = user.email,
                color = AppColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        UserRoleBadge(role = user.role)
        UserStatusBadge(status = user.status)

        if (user.isSystemAdmin) {
            StatusBadge(
                text = "Hệ thống",
                tone = AppBadgeTone.Purple
            )
        }
    }
}

@Composable
private fun ApprovalUserTopLine(
    user: User
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = user)

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.displayName,
                color = AppColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (user.isTeacher) "Tài khoản giáo viên" else if (user.isStudent) "Tài khoản sinh viên" else "Tài khoản quản trị",
                color = AppColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        UserRoleBadge(role = user.role)
        UserStatusBadge(status = user.status)
    }
}

@Composable
private fun UserAvatar(
    user: User
) {
    Surface(
        shape = RoundedCornerShape(17.dp),
        color = badgeBackgroundForRole(user),
        modifier = Modifier
            .height(48.dp)
            .width(48.dp)
    ) {
        Icon(
            imageVector = iconForUser(user),
            contentDescription = null,
            tint = badgeColorForRole(user),
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun CompactUserInfoRows(
    user: User
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        InfoLine(
            icon = Icons.Default.Email,
            label = "Email",
            value = user.email
        )

        InfoLine(
            icon = Icons.Default.Phone,
            label = "Số điện thoại",
            value = user.phone.ifBlank { "Chưa cập nhật" }
        )

        InfoLine(
            icon = Icons.Default.Badge,
            label = firstCodeLabel(user),
            value = user.displayIdentifier.ifBlank { "Chưa cập nhật" }
        )

        InfoLine(
            icon = if (user.isStudent) Icons.Default.Class else Icons.Default.Business,
            label = secondUnitLabel(user),
            value = user.displayDepartment.ifBlank { "Chưa cập nhật" }
        )
    }
}

@Composable
private fun ApprovalFullRegisterInfo(
    user: User
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        InfoLine(
            icon = Icons.Default.Person,
            label = "Họ tên",
            value = user.displayName
        )

        InfoLine(
            icon = Icons.Default.Email,
            label = "Email",
            value = user.email
        )

        InfoLine(
            icon = Icons.Default.Badge,
            label = firstCodeLabel(user),
            value = user.displayIdentifier.ifBlank { "Chưa cập nhật" }
        )

        InfoLine(
            icon = if (user.isStudent) Icons.Default.Class else Icons.Default.Business,
            label = secondUnitLabel(user),
            value = user.displayDepartment.ifBlank { "Chưa cập nhật" }
        )

        InfoLine(
            icon = Icons.Default.Phone,
            label = "Số điện thoại",
            value = user.phone.ifBlank { "Chưa cập nhật" }
        )
    }
}

@Composable
private fun InfoLine(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, AppColors.BorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Muted,
                modifier = Modifier.height(19.dp)
            )

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = label,
                color = AppColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                ),
                modifier = Modifier.width(108.dp)
            )

            Text(
                text = value,
                color = AppColors.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProtectedNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.PurpleSoft,
        border = BorderStroke(1.dp, AppColors.Purple.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = AppColors.Purple,
                modifier = Modifier.height(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Tài khoản hệ thống được bảo vệ.",
                color = AppColors.Purple,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManagementActions(
    user: User,
    onActionClick: (UserAction, User) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (user.isPending) {
            SmallPillButton(
                text = "Duyệt",
                icon = Icons.Default.CheckCircle,
                tone = AppBadgeTone.Success,
                onClick = { onActionClick(UserAction.Approve, user) }
            )

            SmallPillButton(
                text = "Từ chối",
                icon = Icons.Default.Cancel,
                tone = AppBadgeTone.Error,
                onClick = { onActionClick(UserAction.Reject, user) }
            )
        }

        if (user.isApproved || user.isRejected) {
            SmallPillButton(
                text = "Khóa",
                icon = Icons.Default.Block,
                tone = AppBadgeTone.Neutral,
                onClick = { onActionClick(UserAction.Lock, user) }
            )
        }

        if (user.isLocked) {
            SmallPillButton(
                text = "Mở khóa",
                icon = Icons.Default.LockOpen,
                tone = AppBadgeTone.Success,
                onClick = { onActionClick(UserAction.Unlock, user) }
            )
        }

        SmallPillButton(
            text = "Xóa hồ sơ",
            icon = Icons.Default.Delete,
            tone = AppBadgeTone.Error,
            onClick = { onActionClick(UserAction.Delete, user) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApprovalActions(
    user: User,
    onActionClick: (UserAction, User) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        SmallPillButton(
            text = "Duyệt tài khoản",
            icon = Icons.Default.CheckCircle,
            tone = AppBadgeTone.Success,
            onClick = { onActionClick(UserAction.Approve, user) }
        )

        SmallPillButton(
            text = "Từ chối",
            icon = Icons.Default.Cancel,
            tone = AppBadgeTone.Error,
            onClick = { onActionClick(UserAction.Reject, user) }
        )
    }
}

@Composable
private fun SmallPillButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = actionButtonColors(tone)

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
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.height(17.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = text,
                color = colors.content,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

private enum class UserAction {
    Approve,
    Reject,
    Lock,
    Unlock,
    Delete
}

private data class UserActionConfirmState(
    val action: UserAction,
    val user: User,
    val title: String,
    val message: String,
    val confirmText: String,
    val danger: Boolean
)

private data class LocalColorSet(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun buildConfirmState(
    action: UserAction,
    user: User
): UserActionConfirmState {
    val displayName = user.displayName

    return when (action) {
        UserAction.Approve -> {
            UserActionConfirmState(
                action = action,
                user = user,
                title = "Duyệt tài khoản",
                message = "Xác nhận duyệt tài khoản của $displayName?",
                confirmText = "Duyệt",
                danger = false
            )
        }

        UserAction.Reject -> {
            UserActionConfirmState(
                action = action,
                user = user,
                title = "Từ chối tài khoản",
                message = "Xác nhận từ chối tài khoản của $displayName?",
                confirmText = "Từ chối",
                danger = true
            )
        }

        UserAction.Lock -> {
            UserActionConfirmState(
                action = action,
                user = user,
                title = "Khóa tài khoản",
                message = "Tài khoản của $displayName sẽ không thể truy cập hệ thống sau khi bị khóa.",
                confirmText = "Khóa",
                danger = true
            )
        }

        UserAction.Unlock -> {
            UserActionConfirmState(
                action = action,
                user = user,
                title = "Mở khóa tài khoản",
                message = "Xác nhận mở khóa tài khoản của $displayName?",
                confirmText = "Mở khóa",
                danger = false
            )
        }

        UserAction.Delete -> {
            UserActionConfirmState(
                action = action,
                user = user,
                title = "Xóa hồ sơ tài khoản",
                message = "Hồ sơ Firestore của $displayName sẽ bị xóa. Tài khoản Firebase Authentication không bị xóa ở bước này.",
                confirmText = "Xóa",
                danger = true
            )
        }
    }
}

private fun buildResultFromList(
    users: List<User>
): UserManagementRepository.UsersResult {
    return UserManagementRepository.UsersResult(
        users = users,
        total = users.size,
        pendingCount = users.count { it.isPending },
        approvedCount = users.count { it.isApproved },
        rejectedCount = users.count { it.isRejected },
        lockedCount = users.count { it.isLocked },
        studentCount = users.count { it.isStudent },
        teacherCount = users.count { it.isTeacher },
        adminCount = users.count { it.isAdmin }
    )
}

private fun firstCodeLabel(
    user: User
): String {
    return when {
        user.isStudent -> "Mã số sinh viên"
        user.isTeacher -> "Mã giảng viên"
        user.isAdmin -> "Mã quản trị"
        else -> "Mã tài khoản"
    }
}

private fun secondUnitLabel(
    user: User
): String {
    return when {
        user.isStudent -> "Lớp"
        user.isTeacher -> "Khoa / Bộ môn"
        user.isAdmin -> "Đơn vị"
        else -> "Thông tin"
    }
}

private fun iconForUser(
    user: User
): ImageVector {
    return when {
        user.isAdmin -> Icons.Default.AdminPanelSettings
        user.isTeacher -> Icons.Default.Work
        user.isStudent -> Icons.Default.School
        else -> Icons.Default.Person
    }
}

private fun badgeBackgroundForRole(
    user: User
): Color {
    return when {
        user.isAdmin -> AppColors.PurpleSoft
        user.isTeacher -> AppColors.BlueSoft
        user.isStudent -> AppColors.GreenSoft
        else -> AppColors.BlueSoft
    }
}

private fun badgeColorForRole(
    user: User
): Color {
    return when {
        user.isAdmin -> AppColors.Purple
        user.isTeacher -> AppColors.BlueDeep
        user.isStudent -> AppColors.Green
        else -> AppColors.BlueDeep
    }
}

private fun actionButtonColors(
    tone: AppBadgeTone
): LocalColorSet {
    return when (tone) {
        AppBadgeTone.Info -> LocalColorSet(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> LocalColorSet(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> LocalColorSet(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> LocalColorSet(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Neutral -> LocalColorSet(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )

        AppBadgeTone.Purple -> LocalColorSet(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )
    }
}