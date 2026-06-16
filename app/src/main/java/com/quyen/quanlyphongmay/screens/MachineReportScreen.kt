@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.quyen.quanlyphongmay.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quyen.quanlyphongmay.model.Computer
import com.quyen.quanlyphongmay.model.MachineReport
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.MachineReportRepository
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.AppGradientButton
import com.quyen.quanlyphongmay.ui.design.AppOutlinedButton
import com.quyen.quanlyphongmay.ui.design.AppSectionHeader
import com.quyen.quanlyphongmay.ui.design.EmptyState
import com.quyen.quanlyphongmay.ui.design.StatCard
import com.quyen.quanlyphongmay.ui.design.StatusBadge

@Composable
fun MachineReportScreen(
    currentUser: User? = null,
    onShowMessage: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val safeUser = currentUser?.normalizedCopy() ?: User().normalizedCopy()
    val repository = remember {
        MachineReportRepository()
    }
    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var reports by remember {
        mutableStateOf<List<MachineReport>>(emptyList())
    }

    var computers by remember {
        mutableStateOf<List<Computer>>(emptyList())
    }

    var loadingReports by remember {
        mutableStateOf(false)
    }

    var loadingComputers by remember {
        mutableStateOf(false)
    }

    var saving by remember {
        mutableStateOf(false)
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    var keyword by rememberSaveable {
        mutableStateOf("")
    }

    var selectedStatus by remember {
        mutableStateOf<MachineReport.MachineReportStatus?>(null)
    }

    var selectedComputer by remember {
        mutableStateOf<Computer?>(null)
    }

    var reportTitle by rememberSaveable {
        mutableStateOf("")
    }

    var reportDescription by rememberSaveable {
        mutableStateOf("")
    }

    var selectedErrorType by remember {
        mutableStateOf(MachineReport.MachineReportErrorType.Hardware)
    }

    var selectedPriority by remember {
        mutableStateOf(MachineReport.MachineReportPriority.Normal)
    }

    var selectedReportAction by remember {
        mutableStateOf<MachineReportAction?>(null)
    }

    var actionNote by rememberSaveable {
        mutableStateOf("")
    }

    fun loadReports() {
        loadingReports = true

        repository.getMachineReports(
            currentUser = safeUser
        ) { success, message, result ->
            loadingReports = false

            if (success) {
                reports = result
            }

            if (!message.isNullOrBlank()) {
                onShowMessage(message)
            }
        }
    }

    fun loadComputers() {
        loadingComputers = true

        firestore.collection(Computer.COLLECTION)
            .orderBy(Computer.FIELD_ROOM_CODE, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                loadingComputers = false

                computers = snapshot.documents.mapNotNull { document ->
                    document.toObject(Computer::class.java)
                        ?.copy(id = document.id)
                        ?.normalizedCopy()
                }
            }
            .addOnFailureListener {
                loadingComputers = false
                onShowMessage("Không thể tải danh sách máy tính để báo lỗi.")
            }
    }

    fun resetForm() {
        selectedComputer = null
        reportTitle = ""
        reportDescription = ""
        selectedErrorType = MachineReport.MachineReportErrorType.Hardware
        selectedPriority = MachineReport.MachineReportPriority.Normal
    }

    fun submitReport() {
        val targetComputer = selectedComputer

        if (targetComputer == null) {
            onShowMessage("Vui lòng chọn máy tính cần báo lỗi.")
            return
        }

        saving = true

        repository.createMachineReport(
            computer = targetComputer,
            currentUser = safeUser,
            title = reportTitle,
            description = reportDescription,
            errorType = selectedErrorType,
            priority = selectedPriority
        ) { success, message, _ ->
            saving = false

            if (success) {
                resetForm()
                reloadKey++
            }

            onShowMessage(message ?: if (success) "Đã gửi báo lỗi máy." else "Không thể gửi báo lỗi máy.")
        }
    }

    fun handleReportAction(
        action: MachineReportAction
    ) {
        saving = true

        when (action.type) {
            MachineReportActionType.StartProcessing -> {
                repository.startProcessingReport(
                    report = action.report,
                    currentUser = safeUser,
                    note = actionNote
                ) { success, message, _ ->
                    saving = false
                    selectedReportAction = null
                    actionNote = ""
                    if (success) {
                        reloadKey++
                    }
                    onShowMessage(message ?: "Đã cập nhật báo lỗi.")
                }
            }

            MachineReportActionType.Resolve -> {
                repository.resolveReport(
                    report = action.report,
                    currentUser = safeUser,
                    resolvedNote = actionNote
                ) { success, message, _ ->
                    saving = false
                    if (success) {
                        selectedReportAction = null
                        actionNote = ""
                        reloadKey++
                    }
                    onShowMessage(message ?: "Đã cập nhật báo lỗi.")
                }
            }

            MachineReportActionType.Cancel -> {
                repository.cancelReport(
                    report = action.report,
                    currentUser = safeUser,
                    reason = actionNote
                ) { success, message, _ ->
                    saving = false
                    if (success) {
                        selectedReportAction = null
                        actionNote = ""
                        reloadKey++
                    }
                    onShowMessage(message ?: "Đã hủy báo lỗi.")
                }
            }

            MachineReportActionType.Delete -> {
                repository.deleteReport(
                    report = action.report,
                    currentUser = safeUser
                ) { success, message ->
                    saving = false
                    selectedReportAction = null
                    actionNote = ""
                    if (success) {
                        reloadKey++
                    }
                    onShowMessage(message ?: "Đã xóa báo lỗi.")
                }
            }
        }
    }

    LaunchedEffect(
        safeUser.uid,
        safeUser.email,
        reloadKey
    ) {
        loadReports()
        loadComputers()
    }

    val visibleReports = remember(
        reports,
        keyword,
        selectedStatus
    ) {
        repository.filterReportsByKeyword(
            reports = repository.filterReportsByStatus(
                reports = reports,
                status = selectedStatus
            ),
            keyword = keyword
        )
    }

    val pendingCount = remember(reports) {
        MachineReportRepository.pendingCount(reports)
    }

    val processingCount = remember(reports) {
        MachineReportRepository.processingCount(reports)
    }

    val resolvedCount = remember(reports) {
        MachineReportRepository.resolvedCount(reports)
    }

    val canProcess = remember(safeUser.uid, safeUser.role, safeUser.status) {
        repository.canCurrentUserProcessReport(safeUser)
    }

    val canCreate = remember(safeUser.uid, safeUser.role, safeUser.status) {
        repository.canCurrentUserCreateReport(safeUser) && !safeUser.isAdmin
    }

    if (selectedReportAction != null) {
        MachineReportActionDialog(
            action = selectedReportAction,
            note = actionNote,
            saving = saving,
            onNoteChange = {
                actionNote = it
            },
            onDismiss = {
                if (!saving) {
                    selectedReportAction = null
                    actionNote = ""
                }
            },
            onConfirm = { action ->
                handleReportAction(action)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = 0.dp,
            bottom = 18.dp
        )
    ) {
        if (!safeUser.isStudent) {
            item {
                MachineReportHeroCard(
                    currentUser = safeUser,
                    reportCount = reports.size,
                    pendingCount = pendingCount,
                    processingCount = processingCount,
                    resolvedCount = resolvedCount,
                    loading = loadingReports,
                    onRefresh = {
                        reloadKey++
                    }
                )
            }

            item {
                MachineReportProblemMapCard(
                    computers = computers,
                    reports = reports
                )
            }
        }

        if (!safeUser.isApproved) {
            item {
                EmptyState(
                    title = "Tài khoản chưa được duyệt",
                    message = "Bạn cần được quản trị viên duyệt tài khoản trước khi sử dụng chức năng báo lỗi máy.",
                    icon = Icons.Default.ErrorOutline
                )
            }

            return@LazyColumn
        }

        if (canCreate) {
            item {
                MachineReportCreateCard(
                    currentUser = safeUser,
                    computers = computers,
                    loadingComputers = loadingComputers,
                    selectedComputer = selectedComputer,
                    title = reportTitle,
                    description = reportDescription,
                    selectedErrorType = selectedErrorType,
                    selectedPriority = selectedPriority,
                    saving = saving,
                    onSelectedComputerChange = {
                        selectedComputer = it
                    },
                    onTitleChange = {
                        reportTitle = it
                    },
                    onDescriptionChange = {
                        reportDescription = it
                    },
                    onErrorTypeChange = {
                        selectedErrorType = it
                    },
                    onPriorityChange = {
                        selectedPriority = it
                    },
                    onSubmit = {
                        submitReport()
                    },
                    onReset = {
                        resetForm()
                    }
                )
            }
        }

        if (!safeUser.isStudent) {
            item {
                MachineReportFilterCard(
                    keyword = keyword,
                    selectedStatus = selectedStatus,
                    onKeywordChange = {
                        keyword = it
                    },
                    onSelectedStatusChange = {
                        selectedStatus = it
                    },
                    onClear = {
                        keyword = ""
                        selectedStatus = null
                    }
                )
            }

            if (loadingReports) {
                item {
                    MachineReportLoadingCard()
                }
            } else if (visibleReports.isEmpty()) {
                item {
                    EmptyState(
                        title = "Chưa có báo lỗi phù hợp",
                        message = "Danh sách báo lỗi máy đang trống hoặc không khớp bộ lọc hiện tại.",
                        icon = Icons.Default.ReportProblem
                    )
                }
            } else {
                items(
                    items = visibleReports,
                    key = { report -> report.safeId.ifBlank { report.hashCode().toString() } }
                ) { report ->
                    MachineReportItemCard(
                        report = report,
                        canProcess = canProcess,
                        currentUser = safeUser,
                        onAction = { actionType ->
                            selectedReportAction = MachineReportAction(
                                report = report,
                                type = actionType
                            )
                            actionNote = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineReportHeroCard(
    currentUser: User,
    reportCount: Int,
    pendingCount: Int,
    processingCount: Int,
    resolvedCount: Int,
    loading: Boolean,
    onRefresh: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(23.dp),
                    color = AppColors.RoseSoft,
                    border = BorderStroke(
                        width = 1.dp,
                        color = AppColors.Rose.copy(alpha = 0.18f)
                    ),
                    shadowElevation = 3.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = AppColors.Rose,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.size(13.dp))

                AppSectionHeader(
                    title = "Báo lỗi máy",
                    subtitle = if (currentUser.isAdmin || currentUser.isTeacher) {
                        "Theo dõi và xử lý các lỗi máy tính trong phòng máy."
                    } else {
                        "Gửi báo lỗi máy tính và theo dõi tình trạng xử lý."
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
                    MachineReportSmallButton(
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
                    title = "Tổng",
                    value = reportCount.toString(),
                    icon = Icons.Default.ReportProblem,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Chờ",
                    value = pendingCount.toString(),
                    icon = Icons.Default.ErrorOutline,
                    tone = AppBadgeTone.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Xử lý",
                    value = processingCount.toString(),
                    icon = Icons.Default.Build,
                    tone = AppBadgeTone.Purple,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Xong",
                    value = resolvedCount.toString(),
                    icon = Icons.Default.TaskAlt,
                    tone = AppBadgeTone.Success,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



private fun computerHasMachineProblem(
    computer: Computer,
    activeComputerIds: Set<String>
): Boolean {
    val safeComputer = computer.normalizedCopy()

    return safeComputer.safeId in activeComputerIds ||
            safeComputer.isBroken ||
            safeComputer.isMaintenance ||
            safeComputer.isLocked
}

@Composable
private fun MachineReportProblemMapCard(
    computers: List<Computer>,
    reports: List<MachineReport>
) {
    val activeReports = remember(reports) {
        reports
            .map { it.normalizedCopy() }
            .filter { report ->
                report.isPending || report.isProcessing
            }
    }

    val activeComputerIds = remember(activeReports) {
        activeReports
            .map { report -> report.safeComputerId }
            .filter { it.isNotBlank() }
            .toSet()
    }

    val normalizedComputers = remember(computers) {
        computers
            .map { it.normalizedCopy() }
            .sortedWith(
                compareBy<Computer> { it.displayRoomCode }
                    .thenBy { it.normalizedRow }
                    .thenBy { it.normalizedColumn }
                    .thenBy { it.displayCode }
            )
    }

    val roomGroups = remember(normalizedComputers) {
        normalizedComputers
            .groupBy { computer ->
                computer.safeRoomId.ifBlank {
                    computer.displayRoomCode.ifBlank {
                        computer.displayRoomName
                    }
                }
            }
            .toSortedMap()
    }

    var selectedRoomKey by rememberSaveable {
        mutableStateOf("")
    }

    val firstRoomKey = remember(roomGroups) {
        roomGroups.keys.firstOrNull().orEmpty()
    }

    LaunchedEffect(firstRoomKey, selectedRoomKey) {
        if (selectedRoomKey.isBlank() && firstRoomKey.isNotBlank()) {
            selectedRoomKey = firstRoomKey
        }
    }

    val currentRoomKey = selectedRoomKey.ifBlank { firstRoomKey }
    val roomComputers = remember(roomGroups, currentRoomKey) {
        roomGroups[currentRoomKey].orEmpty()
    }

    val maxRow = remember(roomComputers) {
        roomComputers.maxOfOrNull { it.normalizedRow }?.coerceAtLeast(1) ?: 1
    }

    val maxColumn = remember(roomComputers) {
        roomComputers.maxOfOrNull { it.normalizedColumn }?.coerceAtLeast(1) ?: 1
    }

    val computerBySeat = remember(roomComputers) {
        roomComputers.associateBy { computer ->
            computer.normalizedRow to computer.normalizedColumn
        }
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 9.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppSectionHeader(
                title = "Sơ đồ máy đang lỗi",
                subtitle = "Máy màu đỏ là máy đang có báo lỗi chờ xử lý hoặc đang xử lý."
            )

            if (roomGroups.isEmpty()) {
                EmptyState(
                    title = "Chưa có sơ đồ máy",
                    message = "Hệ thống chưa có dữ liệu máy tính để hiển thị sơ đồ lỗi.",
                    icon = Icons.Default.Computer
                )
                return@Column
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roomGroups.forEach { roomEntry ->
                    val roomComputersInEntry = roomEntry.value
                    val roomName = roomComputersInEntry.firstOrNull()?.displayRoomCode
                        ?.ifBlank { roomComputersInEntry.firstOrNull()?.displayRoomName.orEmpty() }
                        .orEmpty()
                        .ifBlank { "Phòng máy" }
                    val brokenCount = roomComputersInEntry.count { computer ->
                        computerHasMachineProblem(
                            computer = computer,
                            activeComputerIds = activeComputerIds
                        )
                    }

                    MachineReportChoiceChip(
                        text = "$roomName • $brokenCount lỗi",
                        selected = currentRoomKey == roomEntry.key,
                        tone = if (brokenCount > 0) AppBadgeTone.Error else AppBadgeTone.Info,
                        onClick = {
                            selectedRoomKey = roomEntry.key
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFFF8FBFF),
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Border
                ),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Column(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (rowIndex in 1..maxRow) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (columnIndex in 1..maxColumn) {
                                    val computer = computerBySeat[rowIndex to columnIndex]
                                    MachineReportProblemCell(
                                        computer = computer,
                                        hasProblem = computer != null && computerHasMachineProblem(
                                            computer = computer,
                                            activeComputerIds = activeComputerIds
                                        ),
                                        modifier = Modifier.width(72.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MachineReportLegend(
                            text = "Đang lỗi",
                            tone = AppBadgeTone.Error
                        )

                        MachineReportLegend(
                            text = "Bình thường",
                            tone = AppBadgeTone.Success
                        )

                        MachineReportLegend(
                            text = "Trống",
                            tone = AppBadgeTone.Neutral
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MachineReportProblemCell(
    computer: Computer?,
    hasProblem: Boolean,
    modifier: Modifier = Modifier
) {
    val background = when {
        hasProblem -> Color(0xFFFFDDE5)
        computer != null -> AppColors.GreenSoft
        else -> Color.White
    }

    val border = when {
        hasProblem -> AppColors.Rose.copy(alpha = 0.72f)
        computer != null -> AppColors.Green.copy(alpha = 0.20f)
        else -> AppColors.Border
    }

    val content = when {
        hasProblem -> AppColors.Rose
        computer != null -> AppColors.Green
        else -> AppColors.Muted
    }

    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(
            width = if (hasProblem) 2.dp else 1.dp,
            color = border
        ),
        shadowElevation = if (hasProblem) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = if (hasProblem) Icons.Default.ReportProblem else Icons.Default.Computer,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = computer?.compactSeatLabel ?: "Trống",
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MachineReportCreateCard(
    currentUser: User,
    computers: List<Computer>,
    loadingComputers: Boolean,
    selectedComputer: Computer?,
    title: String,
    description: String,
    selectedErrorType: MachineReport.MachineReportErrorType,
    selectedPriority: MachineReport.MachineReportPriority,
    saving: Boolean,
    onSelectedComputerChange: (Computer) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onErrorTypeChange: (MachineReport.MachineReportErrorType) -> Unit,
    onPriorityChange: (MachineReport.MachineReportPriority) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 9.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppSectionHeader(
                title = if (currentUser.isStudent) "Báo lỗi máy tính" else "Tạo báo lỗi",
                subtitle = if (currentUser.isStudent) {
                    "Chọn máy trực tiếp trên sơ đồ phòng để báo lỗi nhanh và chính xác."
                } else {
                    "Chọn máy, mô tả lỗi và gửi cho người phụ trách xử lý."
                }
            )

            MachineReportComputerMapPicker(
                computers = computers,
                loading = loadingComputers,
                selectedComputer = selectedComputer,
                onSelectedComputerChange = onSelectedComputerChange
            )

            MachineReportTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "Tiêu đề lỗi",
                placeholder = "Ví dụ: Máy không lên màn hình",
                icon = Icons.Default.ReportProblem,
                singleLine = true
            )

            MachineReportTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Mô tả chi tiết",
                placeholder = "Mô tả hiện tượng lỗi, thời điểm xảy ra, thao tác đã thử...",
                icon = Icons.Default.Info,
                singleLine = false,
                minLines = 3
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MachineReportSmallTitle(
                    title = "Loại lỗi"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MachineReport.errorTypeOptions().forEach { option ->
                        MachineReportChoiceChip(
                            text = option.vietnameseName,
                            selected = selectedErrorType == option,
                            tone = toneForErrorType(option),
                            onClick = {
                                onErrorTypeChange(option)
                            }
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MachineReportSmallTitle(
                    title = "Mức độ ưu tiên"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MachineReport.priorityOptions().forEach { option ->
                        MachineReportChoiceChip(
                            text = option.vietnameseName,
                            selected = selectedPriority == option,
                            tone = toneForPriority(option),
                            onClick = {
                                onPriorityChange(option)
                            }
                        )
                    }
                }
            }

            AppGradientButton(
                text = if (saving) "Đang gửi..." else "Gửi báo lỗi máy",
                onClick = onSubmit,
                leadingIcon = Icons.Default.Save,
                modifier = Modifier.fillMaxWidth()
            )

            AppOutlinedButton(
                text = "Xóa nội dung nhập",
                onClick = onReset,
                leadingIcon = Icons.Default.Cancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MachineReportComputerMapPicker(
    computers: List<Computer>,
    loading: Boolean,
    selectedComputer: Computer?,
    onSelectedComputerChange: (Computer) -> Unit
) {
    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    var selectedRoomKey by rememberSaveable {
        mutableStateOf("")
    }

    val normalizedComputers = remember(computers) {
        computers
            .map { it.normalizedCopy() }
            .sortedWith(
                compareBy<Computer> { it.displayRoomCode }
                    .thenBy { it.normalizedRow }
                    .thenBy { it.normalizedColumn }
                    .thenBy { it.displayCode }
            )
    }

    val roomGroups = remember(normalizedComputers) {
        normalizedComputers
            .groupBy { computer ->
                computer.safeRoomId.ifBlank {
                    computer.displayRoomCode.ifBlank {
                        computer.displayRoomName
                    }
                }
            }
            .toSortedMap()
    }

    val firstRoomKey = remember(roomGroups) {
        roomGroups.keys.firstOrNull().orEmpty()
    }

    LaunchedEffect(firstRoomKey, selectedRoomKey) {
        if (selectedRoomKey.isBlank() && firstRoomKey.isNotBlank()) {
            selectedRoomKey = firstRoomKey
        }
    }

    val currentRoomKey = selectedComputer?.safeRoomId?.takeIf { it.isNotBlank() }
        ?: selectedRoomKey.ifBlank { firstRoomKey }

    val roomComputers = remember(roomGroups, currentRoomKey, searchText) {
        val keyword = searchText.trim()

        roomGroups[currentRoomKey]
            .orEmpty()
            .filter { computer ->
                computer.matchesKeyword(keyword)
            }
    }

    val allRoomComputers = remember(roomGroups, currentRoomKey) {
        roomGroups[currentRoomKey].orEmpty()
    }

    val maxRow = remember(allRoomComputers) {
        allRoomComputers.maxOfOrNull { it.normalizedRow }?.coerceAtLeast(1) ?: 1
    }

    val maxColumn = remember(allRoomComputers) {
        allRoomComputers.maxOfOrNull { it.normalizedColumn }?.coerceAtLeast(1) ?: 1
    }

    val computerBySeat = remember(roomComputers) {
        roomComputers.associateBy { computer ->
            computer.normalizedRow to computer.normalizedColumn
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MachineReportSmallTitle(
            title = "Chọn máy theo sơ đồ phòng"
        )

        MachineReportTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = "Tìm máy",
            placeholder = "Mã máy, tên máy, phòng, hàng/cột...",
            icon = Icons.Default.Search,
            singleLine = true
        )

        if (selectedComputer != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = AppColors.GreenSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Green.copy(alpha = 0.18f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Green,
                        modifier = Modifier.size(21.dp)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "${selectedComputer.displayCode} • ${selectedComputer.displayRoomName} • ${selectedComputer.seatLabel}",
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

        if (loading) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Border
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 3.dp,
                        color = AppColors.Blue
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Text(
                        text = "Đang tải sơ đồ máy tính...",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            return
        }

        if (roomGroups.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Border
                )
            ) {
                Text(
                    text = "Chưa có máy tính trong hệ thống để báo lỗi.",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(14.dp)
                )
            }
            return
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            roomGroups.forEach { roomEntry ->
                val roomComputersInEntry = roomEntry.value
                val roomName = roomComputersInEntry.firstOrNull()?.displayRoomCode
                    ?.ifBlank { roomComputersInEntry.firstOrNull()?.displayRoomName.orEmpty() }
                    .orEmpty()
                    .ifBlank { "Phòng máy" }

                MachineReportChoiceChip(
                    text = "$roomName (${roomComputersInEntry.size})",
                    selected = currentRoomKey == roomEntry.key,
                    tone = AppBadgeTone.Info,
                    onClick = {
                        selectedRoomKey = roomEntry.key
                    }
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFFF8FBFF),
            border = BorderStroke(
                width = 1.dp,
                color = AppColors.Border
            ),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = AppColors.BlueDeep,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Sơ đồ chọn máy",
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${roomComputers.size}/${allRoomComputers.size} máy",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }

                Column(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (rowIndex in 1..maxRow) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (columnIndex in 1..maxColumn) {
                                val computer = computerBySeat[rowIndex to columnIndex]
                                MachineReportSeatCell(
                                    computer = computer,
                                    row = rowIndex,
                                    column = columnIndex,
                                    selected = computer != null &&
                                            selectedComputer?.safeId == computer.safeId,
                                    onClick = {
                                        if (computer != null) {
                                            onSelectedComputerChange(computer)
                                        }
                                    },
                                    modifier = Modifier.width(72.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MachineReportLegend(
                        text = "Có máy",
                        tone = AppBadgeTone.Info
                    )

                    MachineReportLegend(
                        text = "Đã chọn",
                        tone = AppBadgeTone.Success
                    )

                    MachineReportLegend(
                        text = "Trống",
                        tone = AppBadgeTone.Neutral
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineReportSeatCell(
    computer: Computer?,
    row: Int,
    column: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> AppColors.GreenSoft
        computer != null -> AppColors.BlueSoft
        else -> Color.White
    }

    val border = when {
        selected -> AppColors.Green.copy(alpha = 0.45f)
        computer != null -> AppColors.Blue.copy(alpha = 0.22f)
        else -> AppColors.Border
    }

    val content = when {
        selected -> AppColors.Green
        computer != null -> AppColors.BlueDeep
        else -> AppColors.Muted
    }

    Surface(
        modifier = modifier
            .height(64.dp)
            .clickable(
                enabled = computer != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = border
        ),
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Computer,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = computer?.compactSeatLabel ?: "H$row-C$column",
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MachineReportLegend(
    text: String,
    tone: AppBadgeTone
) {
    val colors = toneColors(tone)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
        border = BorderStroke(
            width = 1.dp,
            color = colors.border
        )
    ) {
        Text(
            text = text,
            color = colors.content,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 5.dp
            )
        )
    }
}

@Composable
private fun MachineReportFilterCard(
    keyword: String,
    selectedStatus: MachineReport.MachineReportStatus?,
    onKeywordChange: (String) -> Unit,
    onSelectedStatusChange: (MachineReport.MachineReportStatus?) -> Unit,
    onClear: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 9.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppSectionHeader(
                title = "Danh sách báo lỗi",
                subtitle = "Tìm kiếm theo máy, phòng, người báo lỗi, mô tả hoặc trạng thái."
            )

            MachineReportTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                label = "Tìm báo lỗi",
                placeholder = "Máy, phòng, mô tả, người báo lỗi...",
                icon = Icons.Default.Search,
                singleLine = true
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MachineReportChoiceChip(
                    text = "Tất cả",
                    selected = selectedStatus == null,
                    tone = AppBadgeTone.Info,
                    onClick = {
                        onSelectedStatusChange(null)
                    }
                )

                MachineReport.statusOptions().forEach { option ->
                    MachineReportChoiceChip(
                        text = option.vietnameseName,
                        selected = selectedStatus == option,
                        tone = toneForStatus(option),
                        onClick = {
                            onSelectedStatusChange(option)
                        }
                    )
                }
            }

            AppOutlinedButton(
                text = "Xóa bộ lọc",
                onClick = onClear,
                leadingIcon = Icons.Default.FilterAlt,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MachineReportItemCard(
    report: MachineReport,
    canProcess: Boolean,
    currentUser: User,
    onAction: (MachineReportActionType) -> Unit
) {
    val safeReport = report.normalizedCopy()

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 8.dp
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
                    shape = RoundedCornerShape(19.dp),
                    color = toneColors(toneForStatus(safeReport.statusEnum)).background,
                    border = BorderStroke(
                        width = 1.dp,
                        color = toneColors(toneForStatus(safeReport.statusEnum)).border
                    )
                ) {
                    Icon(
                        imageVector = iconForStatus(safeReport.statusEnum),
                        contentDescription = null,
                        tint = toneColors(toneForStatus(safeReport.statusEnum)).content,
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(modifier = Modifier.size(11.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = safeReport.displayTitle,
                        color = AppColors.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Text(
                        text = "${safeReport.displayComputerCode} • ${safeReport.displayRoomName} • ${safeReport.seatLabel}",
                        color = AppColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp
                        )
                    )
                }

                StatusBadge(
                    text = safeReport.statusNameVi,
                    tone = toneForStatus(safeReport.statusEnum)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF8FBFF),
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Border
                )
            ) {
                Column(
                    modifier = Modifier.padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    MachineReportInfoLine(
                        icon = Icons.Default.ReportProblem,
                        label = "Mô tả",
                        value = safeReport.displayDescription
                    )

                    MachineReportInfoLine(
                        icon = Icons.Default.Tune,
                        label = "Loại lỗi",
                        value = "${safeReport.errorTypeNameVi} • Ưu tiên ${safeReport.priorityNameVi}"
                    )

                    MachineReportInfoLine(
                        icon = Icons.Default.Info,
                        label = "Người báo",
                        value = safeReport.displayReporterName
                    )

                    if (safeReport.handledById.isNotBlank() || safeReport.handledByEmail.isNotBlank()) {
                        MachineReportInfoLine(
                            icon = Icons.Default.Verified,
                            label = "Người xử lý",
                            value = safeReport.displayHandlerName
                        )
                    }

                    if (safeReport.adminNote.isNotBlank()) {
                        MachineReportInfoLine(
                            icon = Icons.Default.Info,
                            label = "Ghi chú",
                            value = safeReport.adminNote
                        )
                    }

                    if (safeReport.resolvedNote.isNotBlank()) {
                        MachineReportInfoLine(
                            icon = Icons.Default.TaskAlt,
                            label = "Kết quả",
                            value = safeReport.resolvedNote
                        )
                    }

                    MachineReportInfoLine(
                        icon = Icons.Default.CheckCircle,
                        label = "Thời gian",
                        value = MachineReportRepository.formatCreatedTime(safeReport)
                    )
                }
            }

            MachineReportActionRow(
                report = safeReport,
                canProcess = canProcess,
                currentUser = currentUser,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun MachineReportActionRow(
    report: MachineReport,
    canProcess: Boolean,
    currentUser: User,
    onAction: (MachineReportActionType) -> Unit
) {
    val safeUser = currentUser.normalizedCopy()
    val canCancel = safeUser.isAdmin ||
            safeUser.isTeacher ||
            report.reporterId == safeUser.uid ||
            report.reporterEmail.equals(safeUser.email, ignoreCase = true)

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (canProcess && report.isPending) {
            MachineReportSmallButton(
                text = "Tiếp nhận",
                icon = Icons.Default.Build,
                tone = AppBadgeTone.Purple,
                onClick = {
                    onAction(MachineReportActionType.StartProcessing)
                }
            )
        }

        if (canProcess && (report.isPending || report.isProcessing)) {
            MachineReportSmallButton(
                text = "Hoàn tất",
                icon = Icons.Default.TaskAlt,
                tone = AppBadgeTone.Success,
                onClick = {
                    onAction(MachineReportActionType.Resolve)
                }
            )
        }

        if (canCancel && (report.isPending || report.isProcessing)) {
            MachineReportSmallButton(
                text = "Hủy",
                icon = Icons.Default.Cancel,
                tone = AppBadgeTone.Error,
                onClick = {
                    onAction(MachineReportActionType.Cancel)
                }
            )
        }

        if (safeUser.isAdmin) {
            MachineReportSmallButton(
                text = "Xóa",
                icon = Icons.Default.ErrorOutline,
                tone = AppBadgeTone.Error,
                onClick = {
                    onAction(MachineReportActionType.Delete)
                }
            )
        }
    }
}

@Composable
private fun MachineReportActionDialog(
    action: MachineReportAction?,
    note: String,
    saving: Boolean,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (MachineReportAction) -> Unit
) {
    if (action == null) {
        return
    }

    val title = when (action.type) {
        MachineReportActionType.StartProcessing -> "Tiếp nhận báo lỗi"
        MachineReportActionType.Resolve -> "Hoàn tất xử lý"
        MachineReportActionType.Cancel -> "Hủy báo lỗi"
        MachineReportActionType.Delete -> "Xóa báo lỗi"
    }

    val message = when (action.type) {
        MachineReportActionType.StartProcessing -> "Nhập ghi chú nếu cần. Báo lỗi sẽ chuyển sang trạng thái đang xử lý."
        MachineReportActionType.Resolve -> "Nhập kết quả xử lý. Nội dung này bắt buộc khi hoàn tất báo lỗi."
        MachineReportActionType.Cancel -> "Nhập lý do hủy nếu cần."
        MachineReportActionType.Delete -> "Báo lỗi sẽ bị xóa khỏi Firestore. Thao tác này không thể hoàn tác."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                if (action.type != MachineReportActionType.Delete) {
                    MachineReportTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        label = "Ghi chú",
                        placeholder = "Nhập ghi chú xử lý...",
                        icon = Icons.Default.Info,
                        singleLine = false,
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    onConfirm(action)
                }
            ) {
                Text(
                    text = if (saving) "Đang xử lý..." else "Xác nhận",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) {
                Text(
                    text = "Đóng",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun MachineReportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    singleLine: Boolean,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}

@Composable
private fun MachineReportComputerChip(
    computer: Computer,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        AppColors.BlueSoft
    } else {
        Color.White
    }

    val border = if (selected) {
        AppColors.Blue.copy(alpha = 0.22f)
    } else {
        AppColors.Border
    }

    Surface(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(999.dp),
        color = background,
        border = BorderStroke(
            width = 1.dp,
            color = border
        ),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = if (selected) AppColors.BlueDeep else AppColors.Muted,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                text = "${computer.displayCode} • ${computer.displayRoomCode.ifBlank { computer.displayRoomName }}",
                color = if (selected) AppColors.BlueDeep else AppColors.Text,
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
private fun MachineReportChoiceChip(
    text: String,
    selected: Boolean,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = toneColors(tone)

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
                .height(40.dp)
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.size(5.dp))
            }

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
private fun MachineReportInfoLine(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Muted,
            modifier = Modifier.size(17.dp)
        )

        Spacer(modifier = Modifier.size(7.dp))

        Text(
            text = "$label: ",
            color = AppColors.Ink,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        )

        Text(
            text = value,
            color = AppColors.Muted,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 17.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MachineReportSmallButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = toneColors(tone)

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
        ),
        shadowElevation = 1.dp
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

@Composable
private fun MachineReportSmallTitle(
    title: String
) {
    Text(
        text = title,
        color = AppColors.Ink,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp
        )
    )
}

@Composable
private fun MachineReportLoadingCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = AppColors.Blue
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "Đang tải danh sách báo lỗi máy...",
                color = AppColors.Muted,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Immutable
private data class MachineReportAction(
    val report: MachineReport,
    val type: MachineReportActionType
)

private enum class MachineReportActionType {
    StartProcessing,
    Resolve,
    Cancel,
    Delete
}

@Immutable
private data class ToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private fun toneColors(
    tone: AppBadgeTone
): ToneColors {
    return when (tone) {
        AppBadgeTone.Info -> ToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.20f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> ToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.20f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> ToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.22f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> ToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.20f),
            content = AppColors.Rose
        )

        AppBadgeTone.Purple -> ToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.20f),
            content = AppColors.Purple
        )

        AppBadgeTone.Neutral -> ToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )
    }
}

private fun toneForStatus(
    status: MachineReport.MachineReportStatus
): AppBadgeTone {
    return when (status) {
        MachineReport.MachineReportStatus.Pending -> AppBadgeTone.Warning
        MachineReport.MachineReportStatus.Processing -> AppBadgeTone.Purple
        MachineReport.MachineReportStatus.Resolved -> AppBadgeTone.Success
        MachineReport.MachineReportStatus.Cancelled -> AppBadgeTone.Error
    }
}

private fun toneForPriority(
    priority: MachineReport.MachineReportPriority
): AppBadgeTone {
    return when (priority) {
        MachineReport.MachineReportPriority.Low -> AppBadgeTone.Neutral
        MachineReport.MachineReportPriority.Normal -> AppBadgeTone.Info
        MachineReport.MachineReportPriority.High -> AppBadgeTone.Warning
        MachineReport.MachineReportPriority.Critical -> AppBadgeTone.Error
    }
}

private fun toneForErrorType(
    errorType: MachineReport.MachineReportErrorType
): AppBadgeTone {
    return when (errorType) {
        MachineReport.MachineReportErrorType.Hardware -> AppBadgeTone.Error
        MachineReport.MachineReportErrorType.Software -> AppBadgeTone.Purple
        MachineReport.MachineReportErrorType.Network -> AppBadgeTone.Info
        MachineReport.MachineReportErrorType.Peripheral -> AppBadgeTone.Warning
        MachineReport.MachineReportErrorType.Performance -> AppBadgeTone.Warning
        MachineReport.MachineReportErrorType.Other -> AppBadgeTone.Neutral
    }
}

private fun iconForStatus(
    status: MachineReport.MachineReportStatus
): ImageVector {
    return when (status) {
        MachineReport.MachineReportStatus.Pending -> Icons.Default.ErrorOutline
        MachineReport.MachineReportStatus.Processing -> Icons.Default.Build
        MachineReport.MachineReportStatus.Resolved -> Icons.Default.TaskAlt
        MachineReport.MachineReportStatus.Cancelled -> Icons.Default.Cancel
    }
}
