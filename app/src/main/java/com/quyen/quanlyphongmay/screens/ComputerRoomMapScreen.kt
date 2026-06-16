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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.quyen.quanlyphongmay.ui.design.AppSectionHeader
import com.quyen.quanlyphongmay.ui.design.AppTextField
import com.quyen.quanlyphongmay.ui.design.ConfirmDialog
import com.quyen.quanlyphongmay.ui.design.EmptyState
import com.quyen.quanlyphongmay.ui.design.ErrorBanner
import com.quyen.quanlyphongmay.ui.design.LoadingStateCard
import com.quyen.quanlyphongmay.ui.design.StatusBadge

@Composable
fun ComputerRoomMapScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    initialRoomId: String = "",
    roomRepository: ComputerRoomRepository = ComputerRoomRepository(),
    computerRepository: ComputerRepository = ComputerRepository(),
    onShowMessage: (String) -> Unit = {}
) {
    val user = currentUser.normalizedCopy()

    var keyword by rememberSaveable {
        mutableStateOf("")
    }

    var selectedRoomId by rememberSaveable {
        mutableStateOf(initialRoomId.trim())
    }

    var rooms by remember {
        mutableStateOf<List<ComputerRoom>>(emptyList())
    }

    var computers by remember {
        mutableStateOf<List<Computer>>(emptyList())
    }

    var loadingRooms by remember {
        mutableStateOf(true)
    }

    var loadingComputers by remember {
        mutableStateOf(false)
    }

    var actionLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    var selectedComputer by remember {
        mutableStateOf<Computer?>(null)
    }

    var confirmState by remember {
        mutableStateOf<MapConfirmState?>(null)
    }

    val selectedRoom = rooms.firstOrNull { room ->
        room.safeId == selectedRoomId
    }

    val filteredComputers = remember(computers, keyword) {
        val cleanKeyword = keyword.trim()

        if (cleanKeyword.isBlank()) {
            computers
        } else {
            computers.filter { computer ->
                computer.matchesKeyword(cleanKeyword)
            }
        }
    }

    val computerByPosition = remember(filteredComputers) {
        filteredComputers.associateBy { computer ->
            computer.normalizedRow to computer.normalizedColumn
        }
    }

    val mapStats = remember(computers) {
        buildMapStats(computers)
    }

    fun loadRooms() {
        loadingRooms = true
        errorMessage = ""

        roomRepository.getRooms { success, message, result ->
            loadingRooms = false

            if (success) {
                val safeRooms = result.rooms.map { room ->
                    room.normalizedCopy()
                }

                rooms = safeRooms

                val currentRoomExists = safeRooms.any { room ->
                    room.safeId == selectedRoomId
                }

                if (!currentRoomExists) {
                    selectedRoomId = safeRooms.firstOrNull()?.safeId.orEmpty()
                }

                if (safeRooms.isEmpty()) {
                    computers = emptyList()
                    selectedComputer = null
                }
            } else {
                rooms = emptyList()
                computers = emptyList()
                selectedRoomId = ""
                selectedComputer = null
                errorMessage = message ?: "Không thể tải danh sách phòng máy."
            }
        }
    }

    fun loadComputersForRoom(roomId: String) {
        val cleanRoomId = roomId.trim()

        if (cleanRoomId.isBlank()) {
            computers = emptyList()
            selectedComputer = null
            loadingComputers = false
            return
        }

        loadingComputers = true
        errorMessage = ""

        computerRepository.getComputersByRoom(
            roomId = cleanRoomId
        ) { success, message, result ->
            loadingComputers = false

            if (success) {
                val safeComputers = result.map { computer ->
                    computer.normalizedCopy()
                }

                computers = safeComputers

                selectedComputer = selectedComputer?.let { current ->
                    safeComputers.firstOrNull { computer ->
                        computer.safeId == current.safeId
                    }
                }
            } else {
                computers = emptyList()
                selectedComputer = null
                errorMessage = message ?: "Không thể tải sơ đồ máy tính."
            }
        }
    }

    fun runStatusAction(state: MapConfirmState) {
        if (!user.isAdmin) {
            errorMessage = "Bạn không có quyền thực hiện thao tác này."
            confirmState = null
            return
        }

        actionLoading = true
        errorMessage = ""

        val callback: (Boolean, String?) -> Unit = { success, message ->
            actionLoading = false
            confirmState = null

            if (success) {
                onShowMessage(message ?: "Đã cập nhật trạng thái máy.")
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể cập nhật trạng thái máy."
            }
        }

        when (state.action) {
            MapComputerAction.SetAvailable -> {
                computerRepository.setComputerAvailable(
                    computer = state.computer,
                    adminUser = user,
                    callback = callback
                )
            }

            MapComputerAction.SetInUse -> {
                computerRepository.setComputerInUse(
                    computer = state.computer,
                    adminUser = user,
                    callback = callback
                )
            }

            MapComputerAction.SetBroken -> {
                computerRepository.setComputerBroken(
                    computer = state.computer,
                    adminUser = user,
                    callback = callback
                )
            }

            MapComputerAction.SetMaintenance -> {
                computerRepository.setComputerMaintenance(
                    computer = state.computer,
                    adminUser = user,
                    callback = callback
                )
            }

            MapComputerAction.Lock -> {
                computerRepository.lockComputer(
                    computer = state.computer,
                    adminUser = user,
                    callback = callback
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        loadRooms()
    }

    LaunchedEffect(selectedRoomId, reloadKey) {
        loadComputersForRoom(selectedRoomId)
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
                runStatusAction(state)
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
            MapHeaderCard(
                selectedRoom = selectedRoom,
                roomCount = rooms.size,
                stats = mapStats,
                loadingRooms = loadingRooms,
                loadingComputers = loadingComputers,
                onRefresh = {
                    loadRooms()
                    reloadKey++
                }
            )
        }

        item {
            RoomMapFilterCard(
                keyword = keyword,
                onKeywordChange = {
                    keyword = it
                },
                rooms = rooms,
                selectedRoomId = selectedRoomId,
                onRoomSelected = { roomId ->
                    selectedRoomId = roomId
                    selectedComputer = null
                    keyword = ""
                }
            )
        }

        item {
            ErrorBanner(
                message = errorMessage,
                visible = errorMessage.isNotBlank()
            )
        }

        when {
            loadingRooms || loadingComputers -> {
                item {
                    LoadingStateCard(
                        message = if (loadingRooms) {
                            "Đang tải phòng máy"
                        } else {
                            "Đang tải sơ đồ máy tính"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            rooms.isEmpty() -> {
                item {
                    EmptyState(
                        title = "Chưa có phòng máy",
                        message = "Cần tạo phòng máy trước khi xem sơ đồ máy tính.",
                        icon = Icons.Default.MeetingRoom
                    )
                }
            }

            selectedRoom == null -> {
                item {
                    EmptyState(
                        title = "Chưa chọn phòng máy",
                        message = "Chọn một phòng máy để xem sơ đồ hàng/cột.",
                        icon = Icons.Default.GridView
                    )
                }
            }

            else -> {
                item {
                    RoomCinemaMapCard(
                        room = selectedRoom,
                        computerByPosition = computerByPosition,
                        selectedComputer = selectedComputer,
                        onComputerClick = { computer ->
                            selectedComputer = computer
                        }
                    )
                }

                item {
                    MapLegendCard()
                }

                item {
                    SelectedComputerDetailCard(
                        user = user,
                        computer = selectedComputer,
                        onAction = { action, computer ->
                            confirmState = buildMapConfirmState(
                                action = action,
                                computer = computer
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapHeaderCard(
    selectedRoom: ComputerRoom?,
    roomCount: Int,
    stats: MapStats,
    loadingRooms: Boolean,
    loadingComputers: Boolean,
    onRefresh: () -> Unit
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
                        text = "Sơ đồ phòng máy",
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = when {
                            loadingRooms -> "Đang tải danh sách phòng máy..."
                            loadingComputers -> "Đang tải sơ đồ máy tính..."
                            selectedRoom != null -> "${selectedRoom.displayName} • ${selectedRoom.normalizedRows} hàng x ${selectedRoom.normalizedColumns} cột"
                            else -> "$roomCount phòng máy"
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

                MapSmallButton(
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
                MiniMapStat(
                    title = "Tổng",
                    value = stats.total.toString(),
                    icon = Icons.Default.Computer,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )

                MiniMapStat(
                    title = "Rảnh",
                    value = stats.available.toString(),
                    icon = Icons.Default.CheckCircle,
                    tone = AppBadgeTone.Success,
                    modifier = Modifier.weight(1f)
                )

                MiniMapStat(
                    title = "Dùng",
                    value = stats.inUse.toString(),
                    icon = Icons.Default.Keyboard,
                    tone = AppBadgeTone.Warning,
                    modifier = Modifier.weight(1f)
                )

                MiniMapStat(
                    title = "Hỏng",
                    value = stats.broken.toString(),
                    icon = Icons.Default.ErrorOutline,
                    tone = AppBadgeTone.Error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniMapStat(
    title: String,
    value: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    modifier: Modifier = Modifier
) {
    val colors = mapToneColors(tone)

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
private fun RoomMapFilterCard(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    rooms: List<ComputerRoom>,
    selectedRoomId: String,
    onRoomSelected: (String) -> Unit
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
                label = "Tìm máy trên sơ đồ",
                placeholder = "Mã máy, tên máy, vị trí, trạng thái",
                leadingIcon = Icons.Default.Search,
                minHeight = 56.dp,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Phòng máy",
                color = AppColors.Text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (rooms.isEmpty()) {
                EmptyState(
                    title = "Chưa có phòng máy",
                    message = "Hãy tạo phòng máy ở mục Quản lý phòng máy.",
                    icon = Icons.Default.MeetingRoom
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rooms.forEach { room ->
                        MapFilterChip(
                            text = room.displayCode,
                            value = room.safeId,
                            selectedValue = selectedRoomId,
                            icon = Icons.Default.MeetingRoom,
                            onSelected = onRoomSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCinemaMapCard(
    room: ComputerRoom,
    computerByPosition: Map<Pair<Int, Int>, Computer>,
    selectedComputer: Computer?,
    onComputerClick: (Computer) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppSectionHeader(
                title = room.displayName,
                subtitle = "${room.displayCode} • ${room.capacity} vị trí • ${room.statusNameVi}"
            )

            ScreenBanner()

            Column(
                modifier = Modifier.horizontalScroll(horizontalScrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 1..room.normalizedRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RowLabel(row = row)

                        for (column in 1..room.normalizedColumns) {
                            val computer = computerByPosition[row to column]

                            ComputerSeatCell(
                                column = column,
                                computer = computer,
                                selected = computer != null && selectedComputer?.safeId == computer.safeId,
                                onClick = {
                                    if (computer != null) {
                                        onComputerClick(computer)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AppColors.BlueSoft,
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Blue.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BẢNG GIẢNG / MÀN CHIẾU",
                color = AppColors.BlueDeep,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
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
private fun RowLabel(
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
                .height(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "H$row",
                color = AppColors.Muted,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun ComputerSeatCell(
    column: Int,
    computer: Computer?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (computer == null) {
        MapLocalToneColors(
            background = Color(0xFFF6F9FD),
            border = AppColors.BorderSoft,
            content = AppColors.Muted
        )
    } else {
        mapToneColors(toneForMapComputerStatus(computer.statusEnum))
    }

    val borderColor = if (selected) {
        AppColors.Blue
    } else {
        colors.border
    }

    Surface(
        modifier = Modifier
            .width(72.dp)
            .height(54.dp)
            .clickable(
                enabled = computer != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = colors.background,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (selected) 8.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (computer == null) {
                    Icons.Default.ViewModule
                } else {
                    Icons.Default.Computer
                },
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = computer?.displayCode ?: "Trống",
                color = if (computer == null) AppColors.Muted else AppColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.5.sp
                )
            )

            Text(
                text = "C$column",
                color = AppColors.Muted,
                maxLines = 1,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MapLegendCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 6.dp
    ) {
        Column {
            Text(
                text = "Chú thích trạng thái",
                color = AppColors.Text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(text = "Rảnh", tone = AppBadgeTone.Success)
                StatusBadge(text = "Đang dùng", tone = AppBadgeTone.Warning)
                StatusBadge(text = "Hỏng", tone = AppBadgeTone.Error)
                StatusBadge(text = "Bảo trì", tone = AppBadgeTone.Warning)
                StatusBadge(text = "Đã khóa", tone = AppBadgeTone.Neutral)
                StatusBadge(text = "Trống vị trí", tone = AppBadgeTone.Neutral)
            }
        }
    }
}

@Composable
private fun SelectedComputerDetailCard(
    user: User,
    computer: Computer?,
    onAction: (MapComputerAction, Computer) -> Unit
) {
    if (computer == null) {
        EmptyState(
            title = "Chọn một máy trên sơ đồ",
            message = "Thông tin chi tiết của máy sẽ hiển thị tại đây.",
            icon = Icons.Default.Computer
        )
        return
    }

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
                    tone = toneForMapComputerStatus(safeComputer.statusEnum)
                )

                StatusBadge(
                    text = safeComputer.compactSeatLabel,
                    tone = AppBadgeTone.Purple
                )
            }

            ComputerDetailLine(
                icon = Icons.Default.Computer,
                title = "CPU",
                value = safeComputer.cpu.ifBlank { "Chưa cập nhật" }
            )

            ComputerDetailLine(
                icon = Icons.Default.Storage,
                title = "RAM",
                value = safeComputer.ram.ifBlank { "Chưa cập nhật" }
            )

            ComputerDetailLine(
                icon = Icons.Default.Info,
                title = "IP",
                value = safeComputer.ipAddress.ifBlank { "Chưa cập nhật" }
            )

            if (safeComputer.note.isNotBlank()) {
                ComputerDetailLine(
                    icon = Icons.Default.Info,
                    title = "Ghi chú",
                    value = safeComputer.note
                )
            }

            if (user.isAdmin) {
                AdminMapActionPanel(
                    computer = safeComputer,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun ComputerDetailLine(
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
                modifier = Modifier.width(82.dp)
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
private fun AdminMapActionPanel(
    computer: Computer,
    onAction: (MapComputerAction, Computer) -> Unit
) {
    Column {
        Text(
            text = "Thao tác quản trị",
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
            if (!computer.isAvailable) {
                MapSmallButton(
                    text = "Rảnh",
                    icon = Icons.Default.CheckCircle,
                    tone = AppBadgeTone.Success,
                    onClick = {
                        onAction(MapComputerAction.SetAvailable, computer)
                    }
                )
            }

            if (computer.isAvailable) {
                MapSmallButton(
                    text = "Đang dùng",
                    icon = Icons.Default.Keyboard,
                    tone = AppBadgeTone.Warning,
                    onClick = {
                        onAction(MapComputerAction.SetInUse, computer)
                    }
                )
            }

            if (!computer.isBroken) {
                MapSmallButton(
                    text = "Hỏng",
                    icon = Icons.Default.ErrorOutline,
                    tone = AppBadgeTone.Error,
                    onClick = {
                        onAction(MapComputerAction.SetBroken, computer)
                    }
                )
            }

            if (!computer.isMaintenance) {
                MapSmallButton(
                    text = "Bảo trì",
                    icon = Icons.Default.Build,
                    tone = AppBadgeTone.Warning,
                    onClick = {
                        onAction(MapComputerAction.SetMaintenance, computer)
                    }
                )
            }

            if (!computer.isLocked) {
                MapSmallButton(
                    text = "Khóa",
                    icon = Icons.Default.Block,
                    tone = AppBadgeTone.Neutral,
                    onClick = {
                        onAction(MapComputerAction.Lock, computer)
                    }
                )
            }
        }
    }
}

@Composable
private fun MapSmallButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = mapToneColors(tone)

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
                .defaultMinSize(minHeight = 36.dp)
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
private fun MapFilterChip(
    text: String,
    value: String,
    selectedValue: String,
    icon: ImageVector,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    val colors = if (selected) {
        mapToneColors(AppBadgeTone.Info)
    } else {
        MapLocalToneColors(
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

private data class MapStats(
    val total: Int = 0,
    val available: Int = 0,
    val inUse: Int = 0,
    val broken: Int = 0,
    val maintenance: Int = 0,
    val locked: Int = 0
)

private data class MapLocalToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private enum class MapComputerAction {
    SetAvailable,
    SetInUse,
    SetBroken,
    SetMaintenance,
    Lock
}

private data class MapConfirmState(
    val action: MapComputerAction,
    val computer: Computer,
    val title: String,
    val message: String,
    val confirmText: String,
    val danger: Boolean
)

private fun buildMapStats(
    computers: List<Computer>
): MapStats {
    return MapStats(
        total = computers.size,
        available = computers.count { it.isAvailable },
        inUse = computers.count { it.isInUse },
        broken = computers.count { it.isBroken },
        maintenance = computers.count { it.isMaintenance },
        locked = computers.count { it.isLocked }
    )
}

private fun buildMapConfirmState(
    action: MapComputerAction,
    computer: Computer
): MapConfirmState {
    val computerName = computer.displayName

    return when (action) {
        MapComputerAction.SetAvailable -> {
            MapConfirmState(
                action = action,
                computer = computer,
                title = "Chuyển máy về trạng thái rảnh",
                message = "Xác nhận chuyển $computerName sang trạng thái rảnh?",
                confirmText = "Chuyển rảnh",
                danger = false
            )
        }

        MapComputerAction.SetInUse -> {
            MapConfirmState(
                action = action,
                computer = computer,
                title = "Chuyển máy sang đang dùng",
                message = "Xác nhận chuyển $computerName sang trạng thái đang dùng?",
                confirmText = "Đang dùng",
                danger = false
            )
        }

        MapComputerAction.SetBroken -> {
            MapConfirmState(
                action = action,
                computer = computer,
                title = "Báo máy hỏng",
                message = "$computerName sẽ được chuyển sang trạng thái hỏng.",
                confirmText = "Báo hỏng",
                danger = true
            )
        }

        MapComputerAction.SetMaintenance -> {
            MapConfirmState(
                action = action,
                computer = computer,
                title = "Chuyển bảo trì",
                message = "$computerName sẽ được chuyển sang trạng thái bảo trì.",
                confirmText = "Bảo trì",
                danger = false
            )
        }

        MapComputerAction.Lock -> {
            MapConfirmState(
                action = action,
                computer = computer,
                title = "Khóa máy tính",
                message = "$computerName sẽ không thể phân cho lịch học hoặc booking.",
                confirmText = "Khóa",
                danger = true
            )
        }
    }
}

private fun toneForMapComputerStatus(
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

private fun mapToneColors(
    tone: AppBadgeTone
): MapLocalToneColors {
    return when (tone) {
        AppBadgeTone.Info -> MapLocalToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> MapLocalToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> MapLocalToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> MapLocalToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Neutral -> MapLocalToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )

        AppBadgeTone.Purple -> MapLocalToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )
    }
}