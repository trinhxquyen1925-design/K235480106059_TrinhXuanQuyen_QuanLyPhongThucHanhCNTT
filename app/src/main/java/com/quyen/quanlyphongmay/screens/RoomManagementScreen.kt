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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.model.ComputerRoom
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.ComputerRoomRepository
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.AppGradientButton
import com.quyen.quanlyphongmay.ui.design.AppOutlinedButton
import com.quyen.quanlyphongmay.ui.design.AppSectionHeader
import com.quyen.quanlyphongmay.ui.design.AppTextField
import com.quyen.quanlyphongmay.ui.design.ConfirmDialog
import com.quyen.quanlyphongmay.ui.design.EmptyState
import com.quyen.quanlyphongmay.ui.design.ErrorBanner
import com.quyen.quanlyphongmay.ui.design.LoadingStateCard
import com.quyen.quanlyphongmay.ui.design.PermissionDeniedCard
import com.quyen.quanlyphongmay.ui.design.StatCard
import com.quyen.quanlyphongmay.ui.design.StatusBadge

@Composable
fun RoomManagementScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    repository: ComputerRoomRepository = ComputerRoomRepository(),
    onShowMessage: (String) -> Unit = {}
) {
    val adminUser = currentUser.normalizedCopy()

    if (!adminUser.isAdmin) {
        PermissionDeniedCard(
            modifier = modifier.fillMaxWidth(),
            message = "Chỉ quản trị viên mới có quyền quản lý phòng máy."
        )
        return
    }

    var keyword by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var actionLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var roomResult by remember { mutableStateOf(ComputerRoomRepository.RoomResult()) }
    var reloadKey by remember { mutableIntStateOf(0) }

    var editorState by remember { mutableStateOf(RoomEditorState.hidden()) }
    var confirmState by remember { mutableStateOf<RoomConfirmState?>(null) }

    fun reloadRooms() {
        loading = true
        errorMessage = ""

        repository.getRooms(
            filter = ComputerRoomRepository.RoomFilter(
                keyword = keyword,
                status = selectedStatus
            )
        ) { success, message, result ->
            loading = false

            if (success) {
                roomResult = result
            } else {
                errorMessage = message ?: "Không thể tải danh sách phòng máy."
                roomResult = ComputerRoomRepository.RoomResult()
            }
        }
    }

    fun executeRoomSave() {
        val validationMessage = editorState.validate()

        if (validationMessage != null) {
            errorMessage = validationMessage
            return
        }

        actionLoading = true
        errorMessage = ""

        val room = editorState.toRoom()

        if (editorState.isEditMode) {
            repository.updateRoom(
                room = room,
                adminUser = adminUser
            ) { success, message, _ ->
                actionLoading = false

                if (success) {
                    editorState = RoomEditorState.hidden()
                    onShowMessage(message ?: "Đã cập nhật phòng máy.")
                    reloadKey++
                } else {
                    errorMessage = message ?: "Không thể cập nhật phòng máy."
                }
            }
        } else {
            repository.createRoom(
                room = room,
                adminUser = adminUser
            ) { success, message, _ ->
                actionLoading = false

                if (success) {
                    editorState = RoomEditorState.hidden()
                    onShowMessage(message ?: "Đã thêm phòng máy.")
                    reloadKey++
                } else {
                    errorMessage = message ?: "Không thể thêm phòng máy."
                }
            }
        }
    }

    fun executeConfirmAction(state: RoomConfirmState) {
        actionLoading = true
        errorMessage = ""

        val callback: (Boolean, String?) -> Unit = { success, message ->
            actionLoading = false
            confirmState = null

            if (success) {
                onShowMessage(message ?: "Đã cập nhật phòng máy.")
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể cập nhật phòng máy."
            }
        }

        when (state.action) {
            RoomAction.SetActive -> {
                repository.setRoomActive(
                    room = state.room,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            RoomAction.SetMaintenance -> {
                repository.setRoomMaintenance(
                    room = state.room,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            RoomAction.Lock -> {
                repository.lockRoom(
                    room = state.room,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            RoomAction.Delete -> {
                repository.deleteRoom(
                    room = state.room,
                    adminUser = adminUser,
                    callback = callback
                )
            }
        }
    }

    LaunchedEffect(reloadKey, keyword, selectedStatus) {
        reloadRooms()
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
                executeConfirmAction(state)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            RoomManagementHeader(
                result = roomResult,
                onRefresh = { reloadKey++ },
                onAddRoom = {
                    editorState = RoomEditorState.forCreate()
                }
            )
        }

        item {
            RoomFilterCard(
                keyword = keyword,
                onKeywordChange = { keyword = it },
                selectedStatus = selectedStatus,
                onStatusChange = { selectedStatus = it },
                onClearFilter = {
                    keyword = ""
                    selectedStatus = ""
                }
            )
        }

        if (editorState.visible) {
            item {
                RoomEditorCard(
                    state = editorState,
                    loading = actionLoading,
                    onStateChange = { editorState = it },
                    onSave = { executeRoomSave() },
                    onCancel = {
                        if (!actionLoading) {
                            editorState = RoomEditorState.hidden()
                            errorMessage = ""
                        }
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
                    message = "Đang tải danh sách phòng máy",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            if (roomResult.rooms.isEmpty()) {
                item {
                    EmptyState(
                        title = "Chưa có phòng máy",
                        message = "Thêm phòng máy đầu tiên để quản lý máy tính, sơ đồ phòng và lịch sử dụng.",
                        icon = Icons.Default.MeetingRoom,
                        actionText = "Thêm phòng máy",
                        onActionClick = {
                            editorState = RoomEditorState.forCreate()
                        }
                    )
                }
            } else {
                items(
                    items = roomResult.rooms,
                    key = { room -> room.safeId.ifBlank { room.displayCode } }
                ) { room ->
                    RoomCard(
                        room = room,
                        onEdit = {
                            editorState = RoomEditorState.forEdit(room)
                        },
                        onAction = { action, targetRoom ->
                            confirmState = buildRoomConfirmState(
                                action = action,
                                room = targetRoom
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomManagementHeader(
    result: ComputerRoomRepository.RoomResult,
    onRefresh: () -> Unit,
    onAddRoom: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Tổng quan phòng máy",
                            color = AppColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )

                        Text(
                            text = "Quản lý phòng, trạng thái và sức chứa.",
                            color = AppColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }

                    SmallRoomButton(
                        text = "Tải lại",
                        icon = Icons.Default.Refresh,
                        tone = AppBadgeTone.Info,
                        onClick = onRefresh
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniRoomStat(
                        title = "Tổng",
                        value = result.total.toString(),
                        icon = Icons.Default.MeetingRoom,
                        tone = AppBadgeTone.Info,
                        modifier = Modifier.weight(1f)
                    )

                    MiniRoomStat(
                        title = "Mở",
                        value = result.activeCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        tone = AppBadgeTone.Success,
                        modifier = Modifier.weight(1f)
                    )

                    MiniRoomStat(
                        title = "Bảo trì",
                        value = result.maintenanceCount.toString(),
                        icon = Icons.Default.Settings,
                        tone = AppBadgeTone.Warning,
                        modifier = Modifier.weight(1f)
                    )

                    MiniRoomStat(
                        title = "Sức chứa",
                        value = result.totalCapacity.toString(),
                        icon = Icons.Default.GridView,
                        tone = AppBadgeTone.Purple,
                        modifier = Modifier.weight(1f)
                    )
                }

                AppGradientButton(
                    text = "Thêm phòng máy",
                    onClick = onAddRoom,
                    leadingIcon = Icons.Default.Add,
                    height = 52.dp
                )
            }
        }
    }
}

@Composable
private fun MiniRoomStat(
    title: String,
    value: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    modifier: Modifier = Modifier
) {
    val colors = localToneColors(tone)

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
private fun RoomFilterCard(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 8.dp
    ) {
        Column {
            AppTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                label = "Tìm kiếm",
                placeholder = "Mã phòng, tên phòng, vị trí, ghi chú",
                leadingIcon = Icons.Default.Search,
                minHeight = 56.dp,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Trạng thái phòng",
                color = AppColors.Text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    text = "Tất cả",
                    value = "",
                    selectedValue = selectedStatus,
                    icon = Icons.Default.FilterAlt,
                    onSelected = onStatusChange
                )

                ComputerRoom.statusOptions().forEach { status ->
                    FilterChip(
                        text = status.vietnameseName,
                        value = status.firestoreValue,
                        selectedValue = selectedStatus,
                        icon = iconForRoomStatus(status),
                        onSelected = onStatusChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppOutlinedButton(
                text = "Xóa bộ lọc",
                onClick = onClearFilter,
                leadingIcon = Icons.Default.Refresh,
                height = 46.dp
            )
        }
    }
}

@Composable
private fun RoomEditorCard(
    state: RoomEditorState,
    loading: Boolean,
    onStateChange: (RoomEditorState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 10.dp
    ) {
        Column {
            AppSectionHeader(
                title = if (state.isEditMode) "Sửa phòng máy" else "Thêm phòng máy",
                subtitle = "Thông tin phòng sẽ được lưu vào Firestore."
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = state.code,
                onValueChange = {
                    onStateChange(state.copy(code = it))
                },
                label = "Mã phòng",
                placeholder = "VD: PM01",
                leadingIcon = Icons.Default.MeetingRoom,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.name,
                onValueChange = {
                    onStateChange(state.copy(name = it))
                },
                label = "Tên phòng",
                placeholder = "VD: Phòng máy 01",
                leadingIcon = Icons.Default.Business,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.location,
                onValueChange = {
                    onStateChange(state.copy(location = it))
                },
                label = "Vị trí",
                placeholder = "VD: Tầng 3 - Nhà A",
                leadingIcon = Icons.Default.LocationOn,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppTextField(
                    value = state.rows,
                    onValueChange = {
                        onStateChange(state.copy(rows = it.filter { char -> char.isDigit() }))
                    },
                    label = "Số hàng",
                    placeholder = "5",
                    leadingIcon = Icons.Default.ViewModule,
                    minHeight = 60.dp,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                AppTextField(
                    value = state.columns,
                    onValueChange = {
                        onStateChange(state.copy(columns = it.filter { char -> char.isDigit() }))
                    },
                    label = "Số cột",
                    placeholder = "8",
                    leadingIcon = Icons.Default.GridView,
                    minHeight = 60.dp,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            RoomStatusPicker(
                selectedStatus = state.status,
                onSelected = {
                    onStateChange(state.copy(status = it.firestoreValue))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.note,
                onValueChange = {
                    onStateChange(state.copy(note = it))
                },
                label = "Ghi chú",
                placeholder = "Thông tin thêm nếu có",
                leadingIcon = Icons.Default.Info,
                singleLine = false,
                minHeight = 76.dp,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppGradientButton(
                text = if (state.isEditMode) "Lưu thay đổi" else "Thêm phòng máy",
                onClick = onSave,
                loading = loading,
                enabled = !loading,
                leadingIcon = Icons.Default.Save,
                height = 54.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            AppOutlinedButton(
                text = "Hủy",
                onClick = onCancel,
                enabled = !loading,
                height = 50.dp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomStatusPicker(
    selectedStatus: String,
    onSelected: (ComputerRoom.RoomStatus) -> Unit
) {
    Column {
        Text(
            text = "Trạng thái",
            color = AppColors.Text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComputerRoom.statusOptions().forEach { status ->
                FilterChip(
                    text = status.vietnameseName,
                    value = status.firestoreValue,
                    selectedValue = selectedStatus,
                    icon = iconForRoomStatus(status),
                    onSelected = {
                        onSelected(status)
                    }
                )
            }
        }
    }
}

@Composable
private fun RoomCard(
    room: ComputerRoom,
    onEdit: (ComputerRoom) -> Unit,
    onAction: (RoomAction, ComputerRoom) -> Unit
) {
    val safeRoom = room.normalizedCopy()

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = statusBackground(safeRoom.statusEnum),
                    modifier = Modifier
                        .height(52.dp)
                        .width(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = statusColor(safeRoom.statusEnum),
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = safeRoom.displayName,
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
                        text = "${safeRoom.displayCode} • ${safeRoom.displayLocation.ifBlank { "Chưa cập nhật vị trí" }}",
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    text = safeRoom.statusNameVi,
                    tone = toneForRoomStatus(safeRoom.statusEnum)
                )

                StatusBadge(
                    text = "${safeRoom.normalizedRows} hàng",
                    tone = AppBadgeTone.Info
                )

                StatusBadge(
                    text = "${safeRoom.normalizedColumns} cột",
                    tone = AppBadgeTone.Purple
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoomInfoTile(
                    title = "Sức chứa",
                    value = "${safeRoom.capacity} máy",
                    icon = Icons.Default.GridView,
                    modifier = Modifier.weight(1f)
                )

                RoomInfoTile(
                    title = "Booking",
                    value = if (safeRoom.canUseForBooking) "Có thể dùng" else "Tạm dừng",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            if (safeRoom.note.isNotBlank()) {
                RoomInfoLine(
                    icon = Icons.Default.Info,
                    title = "Ghi chú",
                    value = safeRoom.note
                )
            }

            RoomActionRow(
                room = safeRoom,
                onEdit = onEdit,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun RoomInfoTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, AppColors.BorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Muted,
                modifier = Modifier.height(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = AppColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                color = AppColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun RoomInfoLine(
    icon: ImageVector,
    title: String,
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
                text = title,
                color = AppColors.Muted,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                ),
                modifier = Modifier.width(80.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomActionRow(
    room: ComputerRoom,
    onEdit: (ComputerRoom) -> Unit,
    onAction: (RoomAction, ComputerRoom) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallRoomButton(
            text = "Sửa",
            icon = Icons.Default.Edit,
            tone = AppBadgeTone.Info,
            onClick = {
                onEdit(room)
            }
        )

        if (!room.isActive) {
            SmallRoomButton(
                text = "Mở",
                icon = Icons.Default.CheckCircle,
                tone = AppBadgeTone.Success,
                onClick = {
                    onAction(RoomAction.SetActive, room)
                }
            )
        }

        if (!room.isMaintenance) {
            SmallRoomButton(
                text = "Bảo trì",
                icon = Icons.Default.Settings,
                tone = AppBadgeTone.Warning,
                onClick = {
                    onAction(RoomAction.SetMaintenance, room)
                }
            )
        }

        if (!room.isLocked) {
            SmallRoomButton(
                text = "Khóa",
                icon = Icons.Default.Block,
                tone = AppBadgeTone.Neutral,
                onClick = {
                    onAction(RoomAction.Lock, room)
                }
            )
        }

        SmallRoomButton(
            text = "Xóa",
            icon = Icons.Default.Delete,
            tone = AppBadgeTone.Error,
            onClick = {
                onAction(RoomAction.Delete, room)
            }
        )
    }
}

@Composable
private fun SmallRoomButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = localToneColors(tone)

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

@Composable
private fun FilterChip(
    text: String,
    value: String,
    selectedValue: String,
    icon: ImageVector,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    val colors = if (selected) {
        localToneColors(AppBadgeTone.Info)
    } else {
        LocalToneColors(
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

private data class RoomEditorState(
    val visible: Boolean,
    val isEditMode: Boolean,
    val id: String,
    val code: String,
    val name: String,
    val location: String,
    val rows: String,
    val columns: String,
    val status: String,
    val note: String
) {
    fun validate(): String? {
        val cleanRows = rows.trim().toIntOrNull()
        val cleanColumns = columns.trim().toIntOrNull()

        return when {
            code.trim().isBlank() -> "Vui lòng nhập mã phòng."
            name.trim().isBlank() -> "Vui lòng nhập tên phòng."
            cleanRows == null -> "Số hàng không hợp lệ."
            cleanColumns == null -> "Số cột không hợp lệ."
            cleanRows !in ComputerRoom.MIN_ROWS..ComputerRoom.MAX_ROWS -> {
                "Số hàng phải từ ${ComputerRoom.MIN_ROWS} đến ${ComputerRoom.MAX_ROWS}."
            }
            cleanColumns !in ComputerRoom.MIN_COLUMNS..ComputerRoom.MAX_COLUMNS -> {
                "Số cột phải từ ${ComputerRoom.MIN_COLUMNS} đến ${ComputerRoom.MAX_COLUMNS}."
            }
            else -> null
        }
    }

    fun toRoom(): ComputerRoom {
        return ComputerRoom(
            id = id.trim(),
            code = code.trim(),
            name = name.trim(),
            location = location.trim(),
            rows = rows.trim().toIntOrNull() ?: ComputerRoom.DEFAULT_ROWS,
            columns = columns.trim().toIntOrNull() ?: ComputerRoom.DEFAULT_COLUMNS,
            status = ComputerRoom.RoomStatus.fromRaw(status).firestoreValue,
            note = note.trim()
        ).normalizedCopy()
    }

    companion object {
        fun hidden(): RoomEditorState {
            return RoomEditorState(
                visible = false,
                isEditMode = false,
                id = "",
                code = "",
                name = "",
                location = "",
                rows = ComputerRoom.DEFAULT_ROWS.toString(),
                columns = ComputerRoom.DEFAULT_COLUMNS.toString(),
                status = ComputerRoom.RoomStatus.Active.firestoreValue,
                note = ""
            )
        }

        fun forCreate(): RoomEditorState {
            return hidden().copy(
                visible = true,
                isEditMode = false
            )
        }

        fun forEdit(room: ComputerRoom): RoomEditorState {
            val safeRoom = room.normalizedCopy()

            return RoomEditorState(
                visible = true,
                isEditMode = true,
                id = safeRoom.safeId,
                code = safeRoom.displayCode,
                name = safeRoom.displayName,
                location = safeRoom.displayLocation,
                rows = safeRoom.normalizedRows.toString(),
                columns = safeRoom.normalizedColumns.toString(),
                status = safeRoom.statusEnum.firestoreValue,
                note = safeRoom.note
            )
        }
    }
}

private enum class RoomAction {
    SetActive,
    SetMaintenance,
    Lock,
    Delete
}

private data class RoomConfirmState(
    val action: RoomAction,
    val room: ComputerRoom,
    val title: String,
    val message: String,
    val confirmText: String,
    val danger: Boolean
)

private data class LocalToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun buildRoomConfirmState(
    action: RoomAction,
    room: ComputerRoom
): RoomConfirmState {
    val roomName = room.displayName

    return when (action) {
        RoomAction.SetActive -> {
            RoomConfirmState(
                action = action,
                room = room,
                title = "Mở hoạt động phòng",
                message = "Xác nhận chuyển $roomName sang trạng thái đang hoạt động?",
                confirmText = "Mở hoạt động",
                danger = false
            )
        }

        RoomAction.SetMaintenance -> {
            RoomConfirmState(
                action = action,
                room = room,
                title = "Chuyển bảo trì",
                message = "$roomName sẽ tạm dừng sử dụng để bảo trì.",
                confirmText = "Bảo trì",
                danger = false
            )
        }

        RoomAction.Lock -> {
            RoomConfirmState(
                action = action,
                room = room,
                title = "Khóa phòng máy",
                message = "$roomName sẽ không thể dùng cho booking hoặc phân máy.",
                confirmText = "Khóa",
                danger = true
            )
        }

        RoomAction.Delete -> {
            RoomConfirmState(
                action = action,
                room = room,
                title = "Xóa phòng máy",
                message = "Phòng $roomName sẽ bị xóa khỏi Firestore. Máy tính thuộc phòng này không bị xóa ở bước này.",
                confirmText = "Xóa",
                danger = true
            )
        }
    }
}

private fun iconForRoomStatus(
    status: ComputerRoom.RoomStatus
): ImageVector {
    return when (status) {
        ComputerRoom.RoomStatus.Active -> Icons.Default.CheckCircle
        ComputerRoom.RoomStatus.Maintenance -> Icons.Default.Settings
        ComputerRoom.RoomStatus.Locked -> Icons.Default.Block
    }
}

private fun toneForRoomStatus(
    status: ComputerRoom.RoomStatus
): AppBadgeTone {
    return when (status) {
        ComputerRoom.RoomStatus.Active -> AppBadgeTone.Success
        ComputerRoom.RoomStatus.Maintenance -> AppBadgeTone.Warning
        ComputerRoom.RoomStatus.Locked -> AppBadgeTone.Neutral
    }
}

private fun statusBackground(
    status: ComputerRoom.RoomStatus
): Color {
    return when (status) {
        ComputerRoom.RoomStatus.Active -> AppColors.GreenSoft
        ComputerRoom.RoomStatus.Maintenance -> AppColors.AmberSoft
        ComputerRoom.RoomStatus.Locked -> Color(0xFFF3F7FB)
    }
}

private fun statusColor(
    status: ComputerRoom.RoomStatus
): Color {
    return when (status) {
        ComputerRoom.RoomStatus.Active -> AppColors.Green
        ComputerRoom.RoomStatus.Maintenance -> AppColors.Amber
        ComputerRoom.RoomStatus.Locked -> AppColors.Muted
    }
}

private fun localToneColors(
    tone: AppBadgeTone
): LocalToneColors {
    return when (tone) {
        AppBadgeTone.Info -> LocalToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> LocalToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> LocalToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> LocalToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Neutral -> LocalToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )

        AppBadgeTone.Purple -> LocalToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )
    }
}