package com.quyen.quanlyphongmay.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Workspaces
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.model.Computer
import com.quyen.quanlyphongmay.model.ComputerRoom
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.ComputerRepository
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
import com.quyen.quanlyphongmay.ui.design.StatusBadge

@Composable
fun ComputerManagementScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    computerRepository: ComputerRepository = ComputerRepository(),
    roomRepository: ComputerRoomRepository = ComputerRoomRepository(),
    onShowMessage: (String) -> Unit = {}
) {
    val adminUser = currentUser.normalizedCopy()

    if (!adminUser.isAdmin) {
        PermissionDeniedCard(
            modifier = modifier.fillMaxWidth(),
            message = "Chỉ quản trị viên mới có quyền quản lý máy tính."
        )
        return
    }

    var keyword by rememberSaveable { mutableStateOf("") }
    var selectedRoomId by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf("") }

    var loadingComputers by remember { mutableStateOf(true) }
    var loadingRooms by remember { mutableStateOf(true) }
    var actionLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var computerResult by remember { mutableStateOf(ComputerRepository.ComputerResult()) }
    var rooms by remember { mutableStateOf<List<ComputerRoom>>(emptyList()) }

    var reloadKey by remember { mutableIntStateOf(0) }
    var panelState by remember { mutableStateOf(ComputerPanelState.hidden()) }
    var confirmState by remember { mutableStateOf<ComputerConfirmState?>(null) }

    fun selectedRoomFromFilter(): ComputerRoom? {
        return rooms.firstOrNull { room -> room.safeId == selectedRoomId }
    }

    fun selectedRoomFromPanel(): ComputerRoom? {
        val roomId = when (panelState.mode) {
            ComputerPanelMode.Single -> panelState.single.roomId
            ComputerPanelMode.BulkCreate -> panelState.bulkCreate.roomId
            ComputerPanelMode.BulkDelete -> panelState.bulkDelete.roomId
        }

        return rooms.firstOrNull { room -> room.safeId == roomId }
    }

    fun computersInRoom(roomId: String): List<Computer> {
        return computerResult.computers
            .map { computer -> computer.normalizedCopy() }
            .filter { computer -> computer.safeRoomId == roomId }
    }

    fun existingSeatKeys(roomId: String): Set<Pair<Int, Int>> {
        return computersInRoom(roomId)
            .map { computer -> computer.normalizedRow to computer.normalizedColumn }
            .toSet()
    }

    fun reloadRooms() {
        loadingRooms = true

        roomRepository.getRooms { success, message, result ->
            loadingRooms = false

            if (success) {
                rooms = result.rooms.map { room -> room.normalizedCopy() }
            } else {
                rooms = emptyList()
                errorMessage = message ?: "Không thể tải danh sách phòng máy."
            }
        }
    }

    fun reloadComputers() {
        loadingComputers = true
        errorMessage = ""

        computerRepository.getComputers(
            filter = ComputerRepository.ComputerFilter(
                keyword = keyword,
                roomId = selectedRoomId,
                status = selectedStatus
            )
        ) { success, message, result ->
            loadingComputers = false

            if (success) {
                computerResult = result.copy(
                    computers = result.computers.map { computer -> computer.normalizedCopy() }
                )
            } else {
                computerResult = ComputerRepository.ComputerResult()
                errorMessage = message ?: "Không thể tải danh sách máy tính."
            }
        }
    }

    fun openPanel() {
        val room = selectedRoomFromFilter()
        val roomId = room?.safeId.orEmpty()
        val roomCode = room?.displayCode.orEmpty()
        val roomSeats = room?.allSeatKeys().orEmpty()
        val existingSeats = existingSeatKeys(roomId)

        panelState = ComputerPanelState.forCreate(
            roomId = roomId,
            roomCode = roomCode,
            selectedSeats = roomSeats.filterNot { seat -> existingSeats.contains(seat) }.toSet()
        )

        errorMessage = ""
    }

    fun openEditPanel(computer: Computer) {
        panelState = ComputerPanelState.forEdit(computer)
        errorMessage = ""
    }

    fun closePanel() {
        if (!actionLoading) {
            panelState = ComputerPanelState.hidden()
            errorMessage = ""
        }
    }

    fun saveSingleComputer() {
        val validationMessage = panelState.single.validate(rooms)

        if (validationMessage != null) {
            errorMessage = validationMessage
            return
        }

        val room = rooms.firstOrNull { room -> room.safeId == panelState.single.roomId }

        if (room == null) {
            errorMessage = "Vui lòng chọn phòng máy hợp lệ."
            return
        }

        actionLoading = true
        errorMessage = ""

        val computer = panelState.single.toComputer(room)

        if (panelState.isEditMode) {
            computerRepository.updateComputer(
                computer = computer,
                adminUser = adminUser
            ) { success, message, _ ->
                actionLoading = false

                if (success) {
                    panelState = ComputerPanelState.hidden()
                    onShowMessage(message ?: "Đã cập nhật máy tính.")
                    reloadKey++
                } else {
                    errorMessage = message ?: "Không thể cập nhật máy tính."
                }
            }
        } else {
            computerRepository.createComputer(
                computer = computer,
                adminUser = adminUser
            ) { success, message, _ ->
                actionLoading = false

                if (success) {
                    panelState = ComputerPanelState.hidden()
                    onShowMessage(message ?: "Đã thêm máy tính.")
                    reloadKey++
                } else {
                    errorMessage = message ?: "Không thể thêm máy tính."
                }
            }
        }
    }

    fun saveBulkCreate() {
        val room = selectedRoomFromPanel()
        val existingSeats = if (room != null) existingSeatKeys(room.safeId) else emptySet()

        val validationMessage = panelState.bulkCreate.validate(
            rooms = rooms,
            existingSeatKeys = existingSeats
        )

        if (validationMessage != null) {
            errorMessage = validationMessage
            return
        }

        if (room == null) {
            errorMessage = "Vui lòng chọn phòng máy hợp lệ."
            return
        }

        val selectedSeats = panelState.bulkCreate.selectedSeats.sortedWith(
            compareBy<Pair<Int, Int>>({ it.first }, { it.second })
        )

        actionLoading = true
        errorMessage = ""

        var createdCount = 0
        val startIndex = panelState.bulkCreate.startIndex.trim().toIntOrNull() ?: 1

        fun createNext(index: Int) {
            if (index >= selectedSeats.size) {
                actionLoading = false
                panelState = ComputerPanelState.hidden()
                onShowMessage("Đã tạo $createdCount máy tính.")
                reloadKey++
                return
            }

            val seat = selectedSeats[index]
            val runningIndex = startIndex + index
            val code = buildBulkComputerCode(panelState.bulkCreate.prefix, runningIndex)

            val computer = Computer(
                id = "",
                roomId = room.safeId,
                roomCode = room.displayCode,
                roomName = room.displayName,
                code = code,
                name = "Máy $code",
                row = seat.first,
                column = seat.second,
                status = Computer.ComputerStatus.Available.firestoreValue,
                cpu = panelState.bulkCreate.cpu.trim(),
                ram = panelState.bulkCreate.ram.trim(),
                storage = panelState.bulkCreate.storage.trim(),
                monitor = panelState.bulkCreate.monitor.trim(),
                ipAddress = "",
                gpu = panelState.bulkCreate.gpu.trim(),
                operatingSystem = panelState.bulkCreate.operatingSystem.trim(),
                note = panelState.bulkCreate.note.trim()
            ).normalizedCopy()

            computerRepository.createComputer(
                computer = computer,
                adminUser = adminUser
            ) { success, message, _ ->
                if (success) {
                    createdCount++
                    createNext(index + 1)
                } else {
                    actionLoading = false
                    errorMessage = message ?: "Không thể tạo máy tại vị trí H${seat.first}-C${seat.second}."
                }
            }
        }

        createNext(0)
    }

    fun saveBulkDelete() {
        val room = selectedRoomFromPanel()
        val selectedSeats = panelState.bulkDelete.selectedSeats

        if (room == null) {
            errorMessage = "Vui lòng chọn phòng máy hợp lệ."
            return
        }

        if (selectedSeats.isEmpty()) {
            errorMessage = "Vui lòng chọn ít nhất một máy cần xóa trên sơ đồ."
            return
        }

        val targetComputers = computersInRoom(room.safeId).filter { computer ->
            selectedSeats.contains(computer.normalizedRow to computer.normalizedColumn)
        }

        if (targetComputers.isEmpty()) {
            errorMessage = "Không tìm thấy máy cần xóa ở các vị trí đã chọn."
            return
        }

        actionLoading = true
        errorMessage = ""

        var deletedCount = 0

        fun deleteNext(index: Int) {
            if (index >= targetComputers.size) {
                actionLoading = false
                panelState = ComputerPanelState.hidden()
                onShowMessage("Đã xóa $deletedCount máy tính.")
                reloadKey++
                return
            }

            val computer = targetComputers[index]

            computerRepository.deleteComputer(
                computer = computer,
                adminUser = adminUser
            ) { success, message ->
                if (success) {
                    deletedCount++
                    deleteNext(index + 1)
                } else {
                    actionLoading = false
                    errorMessage = message ?: "Không thể xóa ${computer.displayName}."
                }
            }
        }

        deleteNext(0)
    }

    fun runConfirmAction(state: ComputerConfirmState) {
        actionLoading = true
        errorMessage = ""

        val callback: (Boolean, String?) -> Unit = { success, message ->
            actionLoading = false
            confirmState = null

            if (success) {
                onShowMessage(message ?: "Đã cập nhật máy tính.")
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể cập nhật máy tính."
            }
        }

        when (state.action) {
            ComputerAction.SetAvailable -> {
                computerRepository.setComputerAvailable(
                    computer = state.computer,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            ComputerAction.SetInUse -> {
                computerRepository.setComputerInUse(
                    computer = state.computer,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            ComputerAction.SetBroken -> {
                computerRepository.setComputerBroken(
                    computer = state.computer,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            ComputerAction.SetMaintenance -> {
                computerRepository.setComputerMaintenance(
                    computer = state.computer,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            ComputerAction.Lock -> {
                computerRepository.lockComputer(
                    computer = state.computer,
                    adminUser = adminUser,
                    callback = callback
                )
            }

            ComputerAction.Delete -> {
                computerRepository.deleteComputer(
                    computer = state.computer,
                    adminUser = adminUser,
                    callback = callback
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadRooms()
    }

    LaunchedEffect(reloadKey, keyword, selectedRoomId, selectedStatus) {
        reloadComputers()
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
                runConfirmAction(state)
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
            ComputerHeaderCard(
                result = computerResult,
                roomCount = rooms.size,
                loadingRooms = loadingRooms,
                onRefresh = {
                    reloadRooms()
                    reloadKey++
                },
                onAdd = { openPanel() }
            )
        }

        item {
            ComputerFilterCard(
                keyword = keyword,
                onKeywordChange = { keyword = it },
                rooms = rooms,
                selectedRoomId = selectedRoomId,
                onRoomChange = { selectedRoomId = it },
                selectedStatus = selectedStatus,
                onStatusChange = { selectedStatus = it },
                onClear = {
                    keyword = ""
                    selectedRoomId = ""
                    selectedStatus = ""
                }
            )
        }

        if (panelState.visible) {
            item {
                ComputerPanel(
                    state = panelState,
                    rooms = rooms,
                    computers = computerResult.computers,
                    existingSeatKeys = selectedRoomFromPanel()?.let { room -> existingSeatKeys(room.safeId) }.orEmpty(),
                    loading = actionLoading,
                    onStateChange = { panelState = it },
                    onSaveSingle = { saveSingleComputer() },
                    onSaveBulkCreate = { saveBulkCreate() },
                    onSaveBulkDelete = { saveBulkDelete() },
                    onCancel = { closePanel() }
                )
            }
        }

        item {
            ErrorBanner(
                message = errorMessage,
                visible = errorMessage.isNotBlank()
            )
        }

        if (loadingComputers) {
            item {
                LoadingStateCard(
                    message = "Đang tải danh sách máy tính",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            if (computerResult.computers.isEmpty()) {
                item {
                    EmptyState(
                        title = "Chưa có máy tính",
                        message = "Bấm Thêm máy tính để thêm từng máy, thêm hàng loạt hoặc chọn vị trí trên sơ đồ.",
                        icon = Icons.Default.Computer,
                        actionText = "Thêm máy tính",
                        onActionClick = { openPanel() }
                    )
                }
            } else {
                itemsIndexed(
                    items = computerResult.computers,
                    key = { index, computer ->
                        val safeComputer = computer.normalizedCopy()
                        val baseKey = safeComputer.safeId
                            .ifBlank { safeComputer.displayCode }
                            .ifBlank { safeComputer.displayName }
                            .ifBlank { "computer" }

                        "${baseKey}_${safeComputer.safeRoomId}_${safeComputer.normalizedRow}_${safeComputer.normalizedColumn}_$index"
                    }
                ) { _, computer ->
                    ComputerItemCard(
                        computer = computer,
                        onEdit = { openEditPanel(computer) },
                        onAction = { action, target ->
                            confirmState = buildComputerConfirmState(
                                action = action,
                                computer = target
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComputerHeaderCard(
    result: ComputerRepository.ComputerResult,
    roomCount: Int,
    loadingRooms: Boolean,
    onRefresh: () -> Unit,
    onAdd: () -> Unit
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
                        text = "Tổng quan máy tính",
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = if (loadingRooms) {
                            "Đang tải phòng máy..."
                        } else {
                            "$roomCount phòng máy • quản lý máy theo phòng"
                        },
                        color = AppColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }

                SmallComputerButton(
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
                MiniComputerStat(
                    title = "Tổng",
                    value = result.total.toString(),
                    icon = Icons.Default.Computer,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )

                MiniComputerStat(
                    title = "Rảnh",
                    value = result.availableCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    tone = AppBadgeTone.Success,
                    modifier = Modifier.weight(1f)
                )

                MiniComputerStat(
                    title = "Dùng",
                    value = result.inUseCount.toString(),
                    icon = Icons.Default.Keyboard,
                    tone = AppBadgeTone.Warning,
                    modifier = Modifier.weight(1f)
                )

                MiniComputerStat(
                    title = "Hỏng",
                    value = result.brokenCount.toString(),
                    icon = Icons.Default.ErrorOutline,
                    tone = AppBadgeTone.Error,
                    modifier = Modifier.weight(1f)
                )
            }

            AppGradientButton(
                text = "Thêm / xóa máy tính",
                onClick = onAdd,
                leadingIcon = Icons.Default.Add,
                height = 52.dp
            )
        }
    }
}

@Composable
private fun MiniComputerStat(
    title: String,
    value: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    modifier: Modifier = Modifier
) {
    val colors = computerToneColors(tone)

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
private fun ComputerFilterCard(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    rooms: List<ComputerRoom>,
    selectedRoomId: String,
    onRoomChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    onClear: () -> Unit
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
                placeholder = "Mã máy, tên máy, phòng, vị trí, cấu hình",
                leadingIcon = Icons.Default.Search,
                minHeight = 56.dp,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComputerSmallTitle("Phòng máy")

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComputerChip(
                    text = "Tất cả",
                    value = "",
                    selectedValue = selectedRoomId,
                    icon = Icons.Default.FilterAlt,
                    onSelected = onRoomChange
                )

                rooms.forEach { room ->
                    ComputerChip(
                        text = room.displayCode,
                        value = room.safeId,
                        selectedValue = selectedRoomId,
                        icon = Icons.Default.MeetingRoom,
                        onSelected = onRoomChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ComputerSmallTitle("Trạng thái máy")

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComputerChip(
                    text = "Tất cả",
                    value = "",
                    selectedValue = selectedStatus,
                    icon = Icons.Default.FilterAlt,
                    onSelected = onStatusChange
                )

                Computer.statusOptions().forEach { status ->
                    ComputerChip(
                        text = status.vietnameseName,
                        value = status.firestoreValue,
                        selectedValue = selectedStatus,
                        icon = iconForComputerStatus(status),
                        onSelected = onStatusChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppOutlinedButton(
                text = "Xóa bộ lọc",
                onClick = onClear,
                leadingIcon = Icons.Default.Refresh,
                height = 46.dp
            )
        }
    }
}

@Composable
private fun ComputerPanel(
    state: ComputerPanelState,
    rooms: List<ComputerRoom>,
    computers: List<Computer>,
    existingSeatKeys: Set<Pair<Int, Int>>,
    loading: Boolean,
    onStateChange: (ComputerPanelState) -> Unit,
    onSaveSingle: () -> Unit,
    onSaveBulkCreate: () -> Unit,
    onSaveBulkDelete: () -> Unit,
    onCancel: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 10.dp
    ) {
        Column {
            AppSectionHeader(
                title = if (state.isEditMode) "Sửa máy tính" else "Thao tác máy tính",
                subtitle = if (state.isEditMode) {
                    "Cập nhật thông tin máy tính đang chọn."
                } else {
                    "Thêm một máy, thêm hàng loạt hoặc xóa hàng loạt theo sơ đồ."
                }
            )

            if (!state.isEditMode) {
                Spacer(modifier = Modifier.height(14.dp))

                PanelModeSelector(
                    selectedMode = state.mode,
                    onModeChange = { mode ->
                        val roomId = when (mode) {
                            ComputerPanelMode.Single -> state.single.roomId
                            ComputerPanelMode.BulkCreate -> state.bulkCreate.roomId
                            ComputerPanelMode.BulkDelete -> state.bulkDelete.roomId
                        }

                        val room = rooms.firstOrNull { it.safeId == roomId }
                        val selectedSeats = room?.allSeatKeys()
                            ?.filterNot { seat -> existingSeatKeys.contains(seat) }
                            ?.toSet()
                            .orEmpty()

                        onStateChange(
                            state.copy(
                                mode = mode,
                                bulkCreate = state.bulkCreate.copy(
                                    selectedSeats = if (mode == ComputerPanelMode.BulkCreate) selectedSeats else state.bulkCreate.selectedSeats
                                )
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (state.mode) {
                ComputerPanelMode.Single -> {
                    SingleComputerForm(
                        state = state.single,
                        rooms = rooms,
                        loading = loading,
                        isEditMode = state.isEditMode,
                        onStateChange = { single ->
                            onStateChange(state.copy(single = single))
                        },
                        onSave = onSaveSingle,
                        onCancel = onCancel
                    )
                }

                ComputerPanelMode.BulkCreate -> {
                    BulkCreateForm(
                        state = state.bulkCreate,
                        rooms = rooms,
                        existingSeatKeys = existingSeatKeys,
                        loading = loading,
                        onStateChange = { bulk ->
                            onStateChange(state.copy(bulkCreate = bulk))
                        },
                        onSave = onSaveBulkCreate,
                        onCancel = onCancel
                    )
                }

                ComputerPanelMode.BulkDelete -> {
                    BulkDeleteForm(
                        state = state.bulkDelete,
                        rooms = rooms,
                        computers = computers,
                        loading = loading,
                        onStateChange = { bulk ->
                            onStateChange(state.copy(bulkDelete = bulk))
                        },
                        onSave = onSaveBulkDelete,
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelModeSelector(
    selectedMode: ComputerPanelMode,
    onModeChange: (ComputerPanelMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeButton(
                text = "Thêm 1 máy",
                icon = Icons.Default.Computer,
                selected = selectedMode == ComputerPanelMode.Single,
                onClick = { onModeChange(ComputerPanelMode.Single) },
                modifier = Modifier.weight(1f)
            )

            ModeButton(
                text = "Thêm hàng loạt",
                icon = Icons.Default.ViewModule,
                selected = selectedMode == ComputerPanelMode.BulkCreate,
                onClick = { onModeChange(ComputerPanelMode.BulkCreate) },
                modifier = Modifier.weight(1f)
            )
        }

        ModeButton(
            text = "Xóa máy hàng loạt theo sơ đồ",
            icon = Icons.Default.Delete,
            selected = selectedMode == ComputerPanelMode.BulkDelete,
            onClick = { onModeChange(ComputerPanelMode.BulkDelete) },
            modifier = Modifier.fillMaxWidth(),
            danger = true
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    val colors = when {
        selected && danger -> computerToneColors(AppBadgeTone.Error)
        selected -> computerToneColors(AppBadgeTone.Info)
        else -> ComputerLocalToneColors(
            background = AppColors.CardSolid,
            border = AppColors.BorderSoft,
            content = AppColors.Muted
        )
    }

    Surface(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(20.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = if (selected) 5.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.height(19.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = text,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun SingleComputerForm(
    state: SingleComputerState,
    rooms: List<ComputerRoom>,
    loading: Boolean,
    isEditMode: Boolean,
    onStateChange: (SingleComputerState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        FormSectionCard(
            title = "Thông tin máy tính",
            subtitle = "Nhập mã máy, tên máy, phòng và vị trí hàng/cột."
        ) {
            ComputerRoomPicker(
                rooms = rooms,
                selectedRoomId = state.roomId,
                onSelected = { roomId ->
                    onStateChange(state.copy(roomId = roomId))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.code,
                onValueChange = { onStateChange(state.copy(code = it)) },
                label = "Mã máy",
                placeholder = "VD: A203-01",
                leadingIcon = Icons.Default.Computer,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.name,
                onValueChange = { onStateChange(state.copy(name = it)) },
                label = "Tên máy",
                placeholder = "VD: Máy A203-01",
                leadingIcon = Icons.Default.Computer,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppTextField(
                    value = state.row,
                    onValueChange = {
                        onStateChange(state.copy(row = it.filter { char -> char.isDigit() }))
                    },
                    label = "Hàng",
                    placeholder = "1",
                    leadingIcon = Icons.Default.ViewModule,
                    minHeight = 60.dp,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                AppTextField(
                    value = state.column,
                    onValueChange = {
                        onStateChange(state.copy(column = it.filter { char -> char.isDigit() }))
                    },
                    label = "Cột",
                    placeholder = "1",
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

            ComputerStatusPicker(
                selectedStatus = state.status,
                onSelected = { onStateChange(state.copy(status = it.firestoreValue)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MachineSpecFields(
            cpu = state.cpu,
            ram = state.ram,
            storage = state.storage,
            monitor = state.monitor,
            gpu = state.gpu,
            operatingSystem = state.operatingSystem,
            note = state.note,
            onCpuChange = { onStateChange(state.copy(cpu = it)) },
            onRamChange = { onStateChange(state.copy(ram = it)) },
            onStorageChange = { onStateChange(state.copy(storage = it)) },
            onMonitorChange = { onStateChange(state.copy(monitor = it)) },
            onGpuChange = { onStateChange(state.copy(gpu = it)) },
            onOperatingSystemChange = { onStateChange(state.copy(operatingSystem = it)) },
            onNoteChange = { onStateChange(state.copy(note = it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppGradientButton(
            text = if (isEditMode) "Lưu thay đổi" else "Thêm máy tính",
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

@Composable
private fun BulkCreateForm(
    state: BulkCreateState,
    rooms: List<ComputerRoom>,
    existingSeatKeys: Set<Pair<Int, Int>>,
    loading: Boolean,
    onStateChange: (BulkCreateState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val selectedRoom = rooms.firstOrNull { room -> room.safeId == state.roomId }

    Column {
        FormSectionCard(
            title = "Thông tin tạo máy",
            subtitle = "Chọn phòng, tiền tố mã máy và số thứ tự bắt đầu."
        ) {
            ComputerRoomPicker(
                rooms = rooms,
                selectedRoomId = state.roomId,
                onSelected = { roomId ->
                    val room = rooms.firstOrNull { it.safeId == roomId }
                    val seats = room?.allSeatKeys()
                        ?.filterNot { seat -> existingSeatKeys.contains(seat) }
                        ?.toSet()
                        .orEmpty()

                    onStateChange(
                        state.copy(
                            roomId = roomId,
                            prefix = state.prefix.ifBlank { room?.displayCode.orEmpty() },
                            selectedSeats = seats
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedRoom != null) {
                StatusBadge(
                    text = "${selectedRoom.normalizedRows} hàng • ${selectedRoom.normalizedColumns} cột • đã chọn ${state.selectedSeats.size}",
                    tone = AppBadgeTone.Info
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            AppTextField(
                value = state.prefix,
                onValueChange = { onStateChange(state.copy(prefix = it)) },
                label = "Tiền tố mã máy",
                placeholder = "VD: A203",
                leadingIcon = Icons.Default.Computer,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.startIndex,
                onValueChange = {
                    onStateChange(state.copy(startIndex = it.filter { char -> char.isDigit() }))
                },
                label = "Số bắt đầu",
                placeholder = "1",
                leadingIcon = Icons.Default.Workspaces,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MachineSpecFields(
            cpu = state.cpu,
            ram = state.ram,
            storage = state.storage,
            monitor = state.monitor,
            gpu = state.gpu,
            operatingSystem = state.operatingSystem,
            note = state.note,
            onCpuChange = { onStateChange(state.copy(cpu = it)) },
            onRamChange = { onStateChange(state.copy(ram = it)) },
            onStorageChange = { onStateChange(state.copy(storage = it)) },
            onMonitorChange = { onStateChange(state.copy(monitor = it)) },
            onGpuChange = { onStateChange(state.copy(gpu = it)) },
            onOperatingSystemChange = { onStateChange(state.copy(operatingSystem = it)) },
            onNoteChange = { onStateChange(state.copy(note = it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedRoom != null) {
            BulkCreateSeatMap(
                room = selectedRoom,
                selectedSeats = state.selectedSeats,
                existingSeatKeys = existingSeatKeys,
                onToggleSeat = { seat ->
                    val newSeats = if (state.selectedSeats.contains(seat)) {
                        state.selectedSeats - seat
                    } else {
                        state.selectedSeats + seat
                    }

                    onStateChange(state.copy(selectedSeats = newSeats))
                },
                onSelectAllAvailable = {
                    val seats = selectedRoom.allSeatKeys()
                        .filterNot { seat -> existingSeatKeys.contains(seat) }
                        .toSet()

                    onStateChange(state.copy(selectedSeats = seats))
                },
                onClear = {
                    onStateChange(state.copy(selectedSeats = emptySet()))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppGradientButton(
            text = "Tạo ${state.selectedSeats.size} máy đã chọn",
            onClick = onSave,
            loading = loading,
            enabled = !loading,
            leadingIcon = Icons.Default.ViewModule,
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

@Composable
private fun BulkDeleteForm(
    state: BulkDeleteState,
    rooms: List<ComputerRoom>,
    computers: List<Computer>,
    loading: Boolean,
    onStateChange: (BulkDeleteState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val selectedRoom = rooms.firstOrNull { room -> room.safeId == state.roomId }
    val roomComputers = computers.map { it.normalizedCopy() }.filter { it.safeRoomId == state.roomId }
    val computerBySeat = roomComputers.associateBy { computer -> computer.normalizedRow to computer.normalizedColumn }

    Column {
        FormSectionCard(
            title = "Xóa máy hàng loạt",
            subtitle = "Chọn phòng, sau đó chọn các máy cần xóa trên sơ đồ."
        ) {
            ComputerRoomPicker(
                rooms = rooms,
                selectedRoomId = state.roomId,
                onSelected = { roomId ->
                    onStateChange(
                        state.copy(
                            roomId = roomId,
                            selectedSeats = emptySet()
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusBadge(
                text = "Đã chọn ${state.selectedSeats.size} máy để xóa",
                tone = AppBadgeTone.Error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedRoom != null) {
            BulkDeleteSeatMap(
                room = selectedRoom,
                computerBySeat = computerBySeat,
                selectedSeats = state.selectedSeats,
                onToggleSeat = { seat ->
                    val newSeats = if (state.selectedSeats.contains(seat)) {
                        state.selectedSeats - seat
                    } else {
                        state.selectedSeats + seat
                    }

                    onStateChange(state.copy(selectedSeats = newSeats))
                },
                onSelectAllExisting = {
                    onStateChange(state.copy(selectedSeats = computerBySeat.keys.toSet()))
                },
                onClear = {
                    onStateChange(state.copy(selectedSeats = emptySet()))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppGradientButton(
            text = "Xóa ${state.selectedSeats.size} máy đã chọn",
            onClick = onSave,
            loading = loading,
            enabled = !loading,
            leadingIcon = Icons.Default.Delete,
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

@Composable
private fun FormSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, AppColors.BorderSoft),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            AppSectionHeader(
                title = title,
                subtitle = subtitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun MachineSpecFields(
    cpu: String,
    ram: String,
    storage: String,
    monitor: String,
    gpu: String,
    operatingSystem: String,
    note: String,
    onCpuChange: (String) -> Unit,
    onRamChange: (String) -> Unit,
    onStorageChange: (String) -> Unit,
    onMonitorChange: (String) -> Unit,
    onGpuChange: (String) -> Unit,
    onOperatingSystemChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    FormSectionCard(
        title = "Cấu hình áp dụng",
        subtitle = "Nhập thông tin phần cứng rõ ràng: CPU, RAM, ổ cứng, màn hình, GPU và hệ điều hành."
    ) {
        AppTextField(
            value = cpu,
            onValueChange = onCpuChange,
            label = "CPU",
            placeholder = "VD: Intel Core i5-12400",
            leadingIcon = Icons.Default.Computer,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppTextField(
                value = ram,
                onValueChange = onRamChange,
                label = "RAM",
                placeholder = "VD: 16GB DDR4",
                leadingIcon = Icons.Default.Storage,
                minHeight = 60.dp,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )

            AppTextField(
                value = storage,
                onValueChange = onStorageChange,
                label = "Ổ cứng",
                placeholder = "VD: SSD 512GB",
                leadingIcon = Icons.Default.Storage,
                minHeight = 60.dp,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = monitor,
            onValueChange = onMonitorChange,
            label = "Màn hình",
            placeholder = "VD: 24 inch Full HD",
            leadingIcon = Icons.Default.Computer,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = gpu,
            onValueChange = onGpuChange,
            label = "GPU",
            placeholder = "VD: RTX 3060 / GTX 1650 / Intel UHD",
            leadingIcon = Icons.Default.Info,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = operatingSystem,
            onValueChange = onOperatingSystemChange,
            label = "Hệ điều hành",
            placeholder = "VD: Windows 11 Pro / Ubuntu 22.04",
            leadingIcon = Icons.Default.Info,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = note,
            onValueChange = onNoteChange,
            label = "Ghi chú",
            placeholder = "VD: Cấu hình phòng thực hành Android",
            leadingIcon = Icons.Default.Info,
            singleLine = false,
            minHeight = 76.dp,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}

@Composable
private fun BulkCreateSeatMap(
    room: ComputerRoom,
    selectedSeats: Set<Pair<Int, Int>>,
    existingSeatKeys: Set<Pair<Int, Int>>,
    onToggleSeat: (Pair<Int, Int>) -> Unit,
    onSelectAllAvailable: () -> Unit,
    onClear: () -> Unit
) {
    SeatMapFrame(
        title = "Chọn vị trí máy cần tạo",
        subtitle = "Vị trí đỏ đã có máy, vị trí xanh sẽ được tạo máy mới.",
        primaryText = "Chọn tất cả trống",
        primaryIcon = Icons.Default.CheckCircle,
        onPrimary = onSelectAllAvailable,
        secondaryText = "Bỏ chọn",
        secondaryIcon = Icons.Default.Refresh,
        onSecondary = onClear
    ) {
        RoomSeatGrid(
            room = room,
            cellContent = { row, column ->
                val seat = row to column
                val occupied = existingSeatKeys.contains(seat)
                val selected = selectedSeats.contains(seat)

                SeatCell(
                    label = "H$row-C$column",
                    icon = if (occupied) Icons.Default.Block else Icons.Default.Computer,
                    tone = when {
                        occupied -> AppBadgeTone.Error
                        selected -> AppBadgeTone.Success
                        else -> AppBadgeTone.Neutral
                    },
                    selected = selected,
                    enabled = !occupied,
                    onClick = { onToggleSeat(seat) }
                )
            }
        )
    }
}

@Composable
private fun BulkDeleteSeatMap(
    room: ComputerRoom,
    computerBySeat: Map<Pair<Int, Int>, Computer>,
    selectedSeats: Set<Pair<Int, Int>>,
    onToggleSeat: (Pair<Int, Int>) -> Unit,
    onSelectAllExisting: () -> Unit,
    onClear: () -> Unit
) {
    SeatMapFrame(
        title = "Chọn máy cần xóa",
        subtitle = "Chỉ vị trí đang có máy mới chọn được. Máy được chọn sẽ có màu xanh dương.",
        primaryText = "Chọn tất cả máy",
        primaryIcon = Icons.Default.CheckCircle,
        onPrimary = onSelectAllExisting,
        secondaryText = "Bỏ chọn",
        secondaryIcon = Icons.Default.Refresh,
        onSecondary = onClear
    ) {
        RoomSeatGrid(
            room = room,
            cellContent = { row, column ->
                val seat = row to column
                val computer = computerBySeat[seat]
                val hasComputer = computer != null
                val selected = selectedSeats.contains(seat)

                SeatCell(
                    label = computer?.displayCode ?: "Trống",
                    icon = if (hasComputer) Icons.Default.Computer else Icons.Default.ViewModule,
                    tone = when {
                        selected -> AppBadgeTone.Info
                        hasComputer -> AppBadgeTone.Error
                        else -> AppBadgeTone.Neutral
                    },
                    selected = selected,
                    enabled = hasComputer,
                    onClick = { onToggleSeat(seat) }
                )
            }
        )
    }
}

@Composable
private fun SeatMapFrame(
    title: String,
    subtitle: String,
    primaryText: String,
    primaryIcon: ImageVector,
    onPrimary: () -> Unit,
    secondaryText: String,
    secondaryIcon: ImageVector,
    onSecondary: () -> Unit,
    content: @Composable () -> Unit
) {
    FormSectionCard(
        title = title,
        subtitle = subtitle
    ) {
        ScreenMiniBanner()

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppOutlinedButton(
                text = primaryText,
                onClick = onPrimary,
                leadingIcon = primaryIcon,
                height = 44.dp,
                modifier = Modifier.weight(1f)
            )

            AppOutlinedButton(
                text = secondaryText,
                onClick = onSecondary,
                leadingIcon = secondaryIcon,
                height = 44.dp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        content()

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(text = "Sẽ tạo", tone = AppBadgeTone.Success)
            StatusBadge(text = "Chọn xóa", tone = AppBadgeTone.Info)
            StatusBadge(text = "Đã có máy", tone = AppBadgeTone.Error)
            StatusBadge(text = "Trống", tone = AppBadgeTone.Neutral)
        }
    }
}

@Composable
private fun RoomSeatGrid(
    room: ComputerRoom,
    cellContent: @Composable (row: Int, column: Int) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier.horizontalScroll(horizontalScrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 1..room.normalizedRows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeatRowLabel(row)

                for (column in 1..room.normalizedColumns) {
                    cellContent(row, column)
                }
            }
        }
    }
}

@Composable
private fun ScreenMiniBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AppColors.BlueSoft,
        border = BorderStroke(1.dp, AppColors.Blue.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BẢNG GIẢNG / MÀN CHIẾU",
                color = AppColors.BlueDeep,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.Cyan,
                                AppColors.Blue,
                                AppColors.Purple
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun SeatRowLabel(
    row: Int
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, AppColors.BorderSoft)
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "H$row",
                color = AppColors.Muted,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun SeatCell(
    label: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = if (tone == AppBadgeTone.Neutral) {
        ComputerLocalToneColors(
            background = Color(0xFFF6F9FD),
            border = AppColors.BorderSoft,
            content = AppColors.Muted
        )
    } else {
        computerToneColors(tone)
    }

    Surface(
        modifier = Modifier
            .width(74.dp)
            .height(54.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = colors.background,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = colors.border
        ),
        shadowElevation = if (selected) 7.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
                color = if (tone == AppBadgeTone.Neutral) AppColors.Muted else colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComputerRoomPicker(
    rooms: List<ComputerRoom>,
    selectedRoomId: String,
    onSelected: (String) -> Unit
) {
    Column {
        ComputerSmallTitle("Phòng máy")

        Spacer(modifier = Modifier.height(8.dp))

        if (rooms.isEmpty()) {
            EmptyState(
                title = "Chưa có phòng máy",
                message = "Cần tạo phòng máy trước khi thêm máy tính.",
                icon = Icons.Default.MeetingRoom
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rooms.forEach { room ->
                    ComputerChip(
                        text = room.displayCode,
                        value = room.safeId,
                        selectedValue = selectedRoomId,
                        icon = Icons.Default.MeetingRoom,
                        onSelected = onSelected
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComputerStatusPicker(
    selectedStatus: String,
    onSelected: (Computer.ComputerStatus) -> Unit
) {
    Column {
        ComputerSmallTitle("Trạng thái")

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Computer.statusOptions().forEach { status ->
                ComputerChip(
                    text = status.vietnameseName,
                    value = status.firestoreValue,
                    selectedValue = selectedStatus,
                    icon = iconForComputerStatus(status),
                    onSelected = { onSelected(status) }
                )
            }
        }
    }
}

@Composable
private fun ComputerItemCard(
    computer: Computer,
    onEdit: (Computer) -> Unit,
    onAction: (ComputerAction, Computer) -> Unit
) {
    val safeComputer = computer.normalizedCopy()

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
                    color = statusBackground(safeComputer.statusEnum),
                    modifier = Modifier
                        .height(52.dp)
                        .width(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = statusColor(safeComputer.statusEnum),
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = safeComputer.displayName,
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
                        text = "${safeComputer.displayCode} • ${safeComputer.displayRoomName} • ${safeComputer.seatLabel}",
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
                    text = safeComputer.statusNameVi,
                    tone = toneForComputerStatus(safeComputer.statusEnum)
                )

                StatusBadge(
                    text = safeComputer.displayRoomCode.ifBlank { "Chưa rõ phòng" },
                    tone = AppBadgeTone.Info
                )

                StatusBadge(
                    text = safeComputer.compactSeatLabel,
                    tone = AppBadgeTone.Purple
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComputerInfoTile(
                    title = "CPU",
                    value = safeComputer.cpu.ifBlank { "Chưa cập nhật" },
                    icon = Icons.Default.Computer,
                    modifier = Modifier.weight(1f)
                )

                ComputerInfoTile(
                    title = "RAM",
                    value = safeComputer.ram.ifBlank { "Chưa cập nhật" },
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComputerInfoTile(
                    title = "Ổ cứng",
                    value = safeComputer.storage.ifBlank { "Chưa cập nhật" },
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f)
                )

                ComputerInfoTile(
                    title = "Màn hình",
                    value = safeComputer.monitor.ifBlank { "Chưa cập nhật" },
                    icon = Icons.Default.Computer,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComputerInfoTile(
                    title = "GPU",
                    value = safeComputer.displayGpu,
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )

                ComputerInfoTile(
                    title = "Hệ điều hành",
                    value = safeComputer.displayOperatingSystem,
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
            }

            if (safeComputer.note.isNotBlank()) {
                ComputerInfoLine(
                    icon = Icons.Default.Info,
                    title = "Ghi chú",
                    value = safeComputer.note
                )
            }

            ComputerActionRow(
                computer = safeComputer,
                onEdit = onEdit,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun ComputerInfoTile(
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
private fun ComputerInfoLine(
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
private fun ComputerActionRow(
    computer: Computer,
    onEdit: (Computer) -> Unit,
    onAction: (ComputerAction, Computer) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallComputerButton(
            text = "Sửa",
            icon = Icons.Default.Edit,
            tone = AppBadgeTone.Info,
            onClick = { onEdit(computer) }
        )

        if (!computer.isAvailable) {
            SmallComputerButton(
                text = "Rảnh",
                icon = Icons.Default.CheckCircle,
                tone = AppBadgeTone.Success,
                onClick = { onAction(ComputerAction.SetAvailable, computer) }
            )
        }

        if (computer.isAvailable) {
            SmallComputerButton(
                text = "Đang dùng",
                icon = Icons.Default.Keyboard,
                tone = AppBadgeTone.Warning,
                onClick = { onAction(ComputerAction.SetInUse, computer) }
            )
        }

        if (!computer.isBroken) {
            SmallComputerButton(
                text = "Hỏng",
                icon = Icons.Default.ErrorOutline,
                tone = AppBadgeTone.Error,
                onClick = { onAction(ComputerAction.SetBroken, computer) }
            )
        }

        if (!computer.isMaintenance) {
            SmallComputerButton(
                text = "Bảo trì",
                icon = Icons.Default.Build,
                tone = AppBadgeTone.Warning,
                onClick = { onAction(ComputerAction.SetMaintenance, computer) }
            )
        }

        if (!computer.isLocked) {
            SmallComputerButton(
                text = "Khóa",
                icon = Icons.Default.Block,
                tone = AppBadgeTone.Neutral,
                onClick = { onAction(ComputerAction.Lock, computer) }
            )
        }

        SmallComputerButton(
            text = "Xóa",
            icon = Icons.Default.Delete,
            tone = AppBadgeTone.Error,
            onClick = { onAction(ComputerAction.Delete, computer) }
        )
    }
}

@Composable
private fun SmallComputerButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = computerToneColors(tone)

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
                .defaultMinSize(minHeight = 34.dp)
                .padding(horizontal = 11.dp, vertical = 8.dp),
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
private fun ComputerChip(
    text: String,
    value: String,
    selectedValue: String,
    icon: ImageVector,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    val colors = if (selected) {
        computerToneColors(AppBadgeTone.Info)
    } else {
        ComputerLocalToneColors(
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
private fun ComputerSmallTitle(
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

private data class ComputerPanelState(
    val visible: Boolean,
    val isEditMode: Boolean,
    val mode: ComputerPanelMode,
    val single: SingleComputerState,
    val bulkCreate: BulkCreateState,
    val bulkDelete: BulkDeleteState
) {
    companion object {
        fun hidden(): ComputerPanelState {
            return ComputerPanelState(
                visible = false,
                isEditMode = false,
                mode = ComputerPanelMode.Single,
                single = SingleComputerState.empty(),
                bulkCreate = BulkCreateState.empty(),
                bulkDelete = BulkDeleteState.empty()
            )
        }

        fun forCreate(
            roomId: String,
            roomCode: String,
            selectedSeats: Set<Pair<Int, Int>>
        ): ComputerPanelState {
            val cleanRoomId = roomId.trim()

            return ComputerPanelState(
                visible = true,
                isEditMode = false,
                mode = ComputerPanelMode.Single,
                single = SingleComputerState.empty().copy(roomId = cleanRoomId),
                bulkCreate = BulkCreateState.empty().copy(
                    roomId = cleanRoomId,
                    prefix = roomCode.trim(),
                    selectedSeats = selectedSeats
                ),
                bulkDelete = BulkDeleteState.empty().copy(roomId = cleanRoomId)
            )
        }

        fun forEdit(computer: Computer): ComputerPanelState {
            return ComputerPanelState(
                visible = true,
                isEditMode = true,
                mode = ComputerPanelMode.Single,
                single = SingleComputerState.fromComputer(computer),
                bulkCreate = BulkCreateState.empty(),
                bulkDelete = BulkDeleteState.empty()
            )
        }
    }
}

private enum class ComputerPanelMode {
    Single,
    BulkCreate,
    BulkDelete
}

private data class SingleComputerState(
    val id: String,
    val roomId: String,
    val code: String,
    val name: String,
    val row: String,
    val column: String,
    val status: String,
    val cpu: String,
    val ram: String,
    val storage: String,
    val monitor: String,
    val gpu: String,
    val operatingSystem: String,
    val note: String
) {
    fun validate(rooms: List<ComputerRoom>): String? {
        val cleanRow = row.trim().toIntOrNull()
        val cleanColumn = column.trim().toIntOrNull()
        val room = rooms.firstOrNull { it.safeId == roomId }

        return when {
            roomId.trim().isBlank() -> "Vui lòng chọn phòng máy."
            room == null -> "Phòng máy không hợp lệ."
            code.trim().isBlank() -> "Vui lòng nhập mã máy."
            name.trim().isBlank() -> "Vui lòng nhập tên máy."
            cleanRow == null -> "Hàng máy không hợp lệ."
            cleanColumn == null -> "Cột máy không hợp lệ."
            cleanRow !in Computer.MIN_ROW..Computer.MAX_ROW -> "Hàng máy phải từ ${Computer.MIN_ROW} đến ${Computer.MAX_ROW}."
            cleanColumn !in Computer.MIN_COLUMN..Computer.MAX_COLUMN -> "Cột máy phải từ ${Computer.MIN_COLUMN} đến ${Computer.MAX_COLUMN}."
            !room.hasValidSeatPosition(cleanRow, cleanColumn) -> "Vị trí máy vượt quá sơ đồ ${room.normalizedRows} hàng x ${room.normalizedColumns} cột của phòng."
            else -> null
        }
    }

    fun toComputer(room: ComputerRoom): Computer {
        val safeRoom = room.normalizedCopy()

        return Computer(
            id = id.trim(),
            roomId = safeRoom.safeId,
            roomCode = safeRoom.displayCode,
            roomName = safeRoom.displayName,
            code = code.trim(),
            name = name.trim(),
            row = row.trim().toIntOrNull() ?: Computer.DEFAULT_ROW,
            column = column.trim().toIntOrNull() ?: Computer.DEFAULT_COLUMN,
            status = Computer.ComputerStatus.fromRaw(status).firestoreValue,
            cpu = cpu.trim(),
            ram = ram.trim(),
            storage = storage.trim(),
            monitor = monitor.trim(),
            ipAddress = "",
            gpu = gpu.trim(),
            operatingSystem = operatingSystem.trim(),
            note = note.trim()
        ).normalizedCopy()
    }

    companion object {
        fun empty(): SingleComputerState {
            return SingleComputerState(
                id = "",
                roomId = "",
                code = "",
                name = "",
                row = Computer.DEFAULT_ROW.toString(),
                column = Computer.DEFAULT_COLUMN.toString(),
                status = Computer.ComputerStatus.Available.firestoreValue,
                cpu = "",
                ram = "",
                storage = "",
                monitor = "",
                gpu = "",
                operatingSystem = "",
                note = ""
            )
        }

        fun fromComputer(computer: Computer): SingleComputerState {
            val safeComputer = computer.normalizedCopy()

            return SingleComputerState(
                id = safeComputer.safeId,
                roomId = safeComputer.safeRoomId,
                code = safeComputer.displayCode,
                name = safeComputer.displayName,
                row = safeComputer.normalizedRow.toString(),
                column = safeComputer.normalizedColumn.toString(),
                status = safeComputer.statusEnum.firestoreValue,
                cpu = safeComputer.cpu,
                ram = safeComputer.ram,
                storage = safeComputer.storage,
                monitor = safeComputer.monitor,
                gpu = safeComputer.displayGpu.takeIf { it != "Chưa nhập GPU" } ?: "",
                operatingSystem = safeComputer.operatingSystem,
                note = safeComputer.note
            )
        }
    }
}

private data class BulkCreateState(
    val roomId: String,
    val prefix: String,
    val startIndex: String,
    val cpu: String,
    val ram: String,
    val storage: String,
    val monitor: String,
    val gpu: String,
    val operatingSystem: String,
    val note: String,
    val selectedSeats: Set<Pair<Int, Int>>
) {
    fun validate(
        rooms: List<ComputerRoom>,
        existingSeatKeys: Set<Pair<Int, Int>>
    ): String? {
        val room = rooms.firstOrNull { it.safeId == roomId }
        val cleanStartIndex = startIndex.trim().toIntOrNull()

        return when {
            roomId.trim().isBlank() -> "Vui lòng chọn phòng máy."
            room == null -> "Phòng máy không hợp lệ."
            prefix.trim().isBlank() -> "Vui lòng nhập tiền tố mã máy."
            cleanStartIndex == null || cleanStartIndex <= 0 -> "Số bắt đầu không hợp lệ."
            selectedSeats.isEmpty() -> "Vui lòng chọn vị trí máy trên sơ đồ."
            selectedSeats.any { seat -> existingSeatKeys.contains(seat) } -> "Có vị trí đã tồn tại máy. Vui lòng bỏ chọn vị trí bị khóa."
            selectedSeats.any { seat -> !room.hasValidSeatPosition(seat.first, seat.second) } -> "Có vị trí vượt quá sơ đồ phòng máy."
            else -> null
        }
    }

    companion object {
        fun empty(): BulkCreateState {
            return BulkCreateState(
                roomId = "",
                prefix = "",
                startIndex = "1",
                cpu = "",
                ram = "",
                storage = "",
                monitor = "",
                gpu = "",
                operatingSystem = "",
                note = "",
                selectedSeats = emptySet()
            )
        }
    }
}

private data class BulkDeleteState(
    val roomId: String,
    val selectedSeats: Set<Pair<Int, Int>>
) {
    companion object {
        fun empty(): BulkDeleteState {
            return BulkDeleteState(
                roomId = "",
                selectedSeats = emptySet()
            )
        }
    }
}

private enum class ComputerAction {
    SetAvailable,
    SetInUse,
    SetBroken,
    SetMaintenance,
    Lock,
    Delete
}

private data class ComputerConfirmState(
    val action: ComputerAction,
    val computer: Computer,
    val title: String,
    val message: String,
    val confirmText: String,
    val danger: Boolean
)

private data class ComputerLocalToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun ComputerRoom.allSeatKeys(): List<Pair<Int, Int>> {
    val safeRoom = normalizedCopy()
    val seats = mutableListOf<Pair<Int, Int>>()

    for (row in 1..safeRoom.normalizedRows) {
        for (column in 1..safeRoom.normalizedColumns) {
            seats.add(row to column)
        }
    }

    return seats
}

private fun buildBulkComputerCode(
    prefix: String,
    index: Int
): String {
    val cleanPrefix = prefix.trim().uppercase()
    val cleanIndex = if (index <= 0) 1 else index
    return "$cleanPrefix-${cleanIndex.toString().padStart(2, '0')}"
}

private fun buildBulkIpAddress(
    prefix: String,
    index: Int
): String {
    val cleanPrefix = prefix.trim()

    if (cleanPrefix.isBlank()) {
        return ""
    }

    return if (cleanPrefix.endsWith(".")) {
        "$cleanPrefix$index"
    } else {
        cleanPrefix
    }
}

private fun buildComputerConfirmState(
    action: ComputerAction,
    computer: Computer
): ComputerConfirmState {
    val computerName = computer.displayName

    return when (action) {
        ComputerAction.SetAvailable -> {
            ComputerConfirmState(
                action = action,
                computer = computer,
                title = "Chuyển máy về trạng thái rảnh",
                message = "Xác nhận chuyển $computerName sang trạng thái rảnh?",
                confirmText = "Chuyển rảnh",
                danger = false
            )
        }

        ComputerAction.SetInUse -> {
            ComputerConfirmState(
                action = action,
                computer = computer,
                title = "Chuyển máy sang đang dùng",
                message = "Xác nhận chuyển $computerName sang trạng thái đang dùng?",
                confirmText = "Đang dùng",
                danger = false
            )
        }

        ComputerAction.SetBroken -> {
            ComputerConfirmState(
                action = action,
                computer = computer,
                title = "Báo máy hỏng",
                message = "$computerName sẽ được chuyển sang trạng thái hỏng.",
                confirmText = "Báo hỏng",
                danger = true
            )
        }

        ComputerAction.SetMaintenance -> {
            ComputerConfirmState(
                action = action,
                computer = computer,
                title = "Chuyển bảo trì",
                message = "$computerName sẽ được chuyển sang trạng thái bảo trì.",
                confirmText = "Bảo trì",
                danger = false
            )
        }

        ComputerAction.Lock -> {
            ComputerConfirmState(
                action = action,
                computer = computer,
                title = "Khóa máy tính",
                message = "$computerName sẽ không thể phân cho lịch học hoặc booking.",
                confirmText = "Khóa",
                danger = true
            )
        }

        ComputerAction.Delete -> {
            ComputerConfirmState(
                action = action,
                computer = computer,
                title = "Xóa máy tính",
                message = "$computerName sẽ bị xóa khỏi Firestore.",
                confirmText = "Xóa",
                danger = true
            )
        }
    }
}

private fun iconForComputerStatus(
    status: Computer.ComputerStatus
): ImageVector {
    return when (status) {
        Computer.ComputerStatus.Available -> Icons.Default.CheckCircle
        Computer.ComputerStatus.InUse -> Icons.Default.Keyboard
        Computer.ComputerStatus.Broken -> Icons.Default.ErrorOutline
        Computer.ComputerStatus.Maintenance -> Icons.Default.Build
        Computer.ComputerStatus.Locked -> Icons.Default.Block
    }
}

private fun toneForComputerStatus(
    status: Computer.ComputerStatus
): AppBadgeTone {
    return when (status) {
        Computer.ComputerStatus.Available -> AppBadgeTone.Success
        Computer.ComputerStatus.InUse -> AppBadgeTone.Warning
        Computer.ComputerStatus.Broken -> AppBadgeTone.Error
        Computer.ComputerStatus.Maintenance -> AppBadgeTone.Warning
        Computer.ComputerStatus.Locked -> AppBadgeTone.Neutral
    }
}

private fun statusBackground(
    status: Computer.ComputerStatus
): Color {
    return when (status) {
        Computer.ComputerStatus.Available -> AppColors.GreenSoft
        Computer.ComputerStatus.InUse -> AppColors.AmberSoft
        Computer.ComputerStatus.Broken -> AppColors.RoseSoft
        Computer.ComputerStatus.Maintenance -> AppColors.AmberSoft
        Computer.ComputerStatus.Locked -> Color(0xFFF3F7FB)
    }
}

private fun statusColor(
    status: Computer.ComputerStatus
): Color {
    return when (status) {
        Computer.ComputerStatus.Available -> AppColors.Green
        Computer.ComputerStatus.InUse -> AppColors.Amber
        Computer.ComputerStatus.Broken -> AppColors.Rose
        Computer.ComputerStatus.Maintenance -> AppColors.Amber
        Computer.ComputerStatus.Locked -> AppColors.Muted
    }
}

private fun computerToneColors(
    tone: AppBadgeTone
): ComputerLocalToneColors {
    return when (tone) {
        AppBadgeTone.Info -> ComputerLocalToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> ComputerLocalToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> ComputerLocalToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> ComputerLocalToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Neutral -> ComputerLocalToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )

        AppBadgeTone.Purple -> ComputerLocalToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )
    }
}