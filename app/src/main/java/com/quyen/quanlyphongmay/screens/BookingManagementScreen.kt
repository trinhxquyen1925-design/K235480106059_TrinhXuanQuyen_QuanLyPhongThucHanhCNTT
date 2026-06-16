package com.quyen.quanlyphongmay.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Computer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.model.Booking
import com.quyen.quanlyphongmay.model.BookingSeatAssignment
import com.quyen.quanlyphongmay.model.BookingStatus
import com.quyen.quanlyphongmay.model.ComputerRoom
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.BookingRepository
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BookingManagementScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    onlyPending: Boolean = false,
    openCreatePanel: Boolean = false,
    bookingRepository: BookingRepository = BookingRepository(),
    roomRepository: ComputerRoomRepository = ComputerRoomRepository(),
    onShowMessage: (String) -> Unit = {}
) {
    val user = currentUser.normalizedCopy()

    if (!user.isAdmin && !user.isTeacher && !user.isStudent) {
        PermissionDeniedCard(
            modifier = modifier.fillMaxWidth(),
            message = "Tài khoản không có quyền truy cập booking."
        )
        return
    }

    var keyword by rememberSaveable { mutableStateOf("") }
    var selectedRoomId by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable {
        mutableStateOf(if (onlyPending) BookingStatus.Pending.firestoreValue else "")
    }

    var selectedDateKey by rememberSaveable {
        mutableStateOf(if (onlyPending) "" else BookingDateHelper.todayDateText())
    }

    var weekStartKey by rememberSaveable {
        mutableStateOf(BookingDateHelper.weekStartKey(Date()))
    }

    var loadingBookings by remember { mutableStateOf(true) }
    var loadingWeekBookings by remember { mutableStateOf(true) }
    var loadingRooms by remember { mutableStateOf(true) }
    var actionLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var rooms by remember { mutableStateOf<List<ComputerRoom>>(emptyList()) }
    var bookingResult by remember { mutableStateOf(BookingRepository.BookingResult()) }
    var weekBookingResult by remember { mutableStateOf(BookingRepository.BookingResult()) }
    var reloadKey by remember { mutableIntStateOf(0) }

    var formState by remember {
        mutableStateOf<BookingFormState>(BookingFormState.hidden())
    }

    var confirmState by remember {
        mutableStateOf<BookingConfirmState?>(null)
    }

    fun reloadRooms() {
        loadingRooms = true

        roomRepository.getRooms { success, message, result ->
            loadingRooms = false

            if (success) {
                rooms = result.rooms.map { room -> room.normalizedCopy() }

                if (formState.visible && formState.roomId.isBlank()) {
                    formState = formState.copy(
                        roomId = rooms.firstOrNull()?.safeId.orEmpty()
                    )
                }
            } else {
                rooms = emptyList()
                errorMessage = message ?: "Không thể tải danh sách phòng máy."
            }
        }
    }

    fun reloadBookings() {
        loadingBookings = true
        errorMessage = ""

        bookingRepository.getBookings(
            currentUser = user,
            filter = BookingRepository.BookingFilter(
                keyword = keyword,
                roomId = selectedRoomId,
                status = selectedStatus,
                dateKey = if (onlyPending) "" else selectedDateKey,
                onlyMine = user.isTeacher && !user.isAdmin,
                onlyApprovedForSchedule = user.isStudent
            )
        ) { success, message, result ->
            loadingBookings = false

            if (success) {
                bookingResult = result.copy(
                    bookings = result.bookings.map { booking -> booking.normalizedCopy() }
                )
            } else {
                bookingResult = BookingRepository.BookingResult()
                errorMessage = message ?: "Không thể tải danh sách booking."
            }
        }
    }

    fun reloadWeekBookings() {
        if (onlyPending) {
            weekBookingResult = BookingRepository.BookingResult()
            loadingWeekBookings = false
            return
        }

        loadingWeekBookings = true

        val weekStart = BookingDateHelper.parseDateKey(weekStartKey) ?: Date()
        val weekKey = Booking.formatWeekKey(weekStart)

        bookingRepository.getBookings(
            currentUser = user,
            filter = BookingRepository.BookingFilter(
                roomId = selectedRoomId,
                status = selectedStatus,
                weekKey = weekKey,
                onlyMine = user.isTeacher && !user.isAdmin,
                onlyApprovedForSchedule = user.isStudent
            )
        ) { success, _, result ->
            loadingWeekBookings = false

            weekBookingResult = if (success) {
                result.copy(
                    bookings = result.bookings.map { booking -> booking.normalizedCopy() }
                )
            } else {
                BookingRepository.BookingResult()
            }
        }
    }

    fun openCreateForm() {
        if (!user.isTeacher && !user.isAdmin) {
            errorMessage = "Sinh viên không có quyền booking phòng."
            return
        }

        formState = BookingFormState.forCreate(
            roomId = rooms.firstOrNull()?.safeId.orEmpty(),
            dateText = if (selectedDateKey.isNotBlank()) selectedDateKey else BookingDateHelper.todayDateText()
        )

        errorMessage = ""
    }

    fun closeForm() {
        if (!actionLoading) {
            formState = BookingFormState.hidden()
            errorMessage = ""
        }
    }

    fun createBooking() {
        val validationMessage = formState.validate(rooms)

        if (validationMessage != null) {
            errorMessage = validationMessage
            return
        }

        val room = rooms.firstOrNull { room -> room.safeId == formState.roomId }

        if (room == null) {
            errorMessage = "Vui lòng chọn phòng máy hợp lệ."
            return
        }

        val startDate = formState.startDateTime()
        val endDate = formState.endDateTime()

        if (startDate == null || endDate == null) {
            errorMessage = "Ngày hoặc giờ booking chưa hợp lệ."
            return
        }

        actionLoading = true
        errorMessage = ""

        bookingRepository.createBooking(
            teacher = user,
            room = room,
            className = formState.className,
            studentCount = formState.studentCountValue(),
            subjectName = formState.subjectName,
            lessonTitle = formState.lessonTitle,
            startDate = startDate,
            endDate = endDate,
            note = formState.note
        ) { success, message, _ ->
            actionLoading = false

            if (success) {
                formState = BookingFormState.hidden()
                selectedDateKey = Booking.formatDateKey(startDate)
                weekStartKey = BookingDateHelper.weekStartKey(startDate)
                onShowMessage(message ?: "Đã gửi booking phòng.")
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể tạo booking phòng."
            }
        }
    }

    fun runConfirmAction(state: BookingConfirmState) {
        actionLoading = true
        errorMessage = ""

        val callback: (Boolean, String?) -> Unit = { success, message ->
            actionLoading = false
            confirmState = null

            if (success) {
                onShowMessage(message ?: "Đã cập nhật booking.")
                reloadKey++
            } else {
                errorMessage = message ?: "Không thể cập nhật booking."
            }
        }

        when (state.action) {
            BookingUiAction.Approve -> {
                bookingRepository.approveBooking(
                    booking = state.booking,
                    adminUser = user,
                    callback = callback
                )
            }

            BookingUiAction.Reject -> {
                bookingRepository.rejectBooking(
                    booking = state.booking,
                    adminUser = user,
                    reason = state.reason.ifBlank { "Không đủ điều kiện duyệt." },
                    callback = callback
                )
            }

            BookingUiAction.Cancel -> {
                bookingRepository.cancelBooking(
                    booking = state.booking,
                    currentUser = user,
                    reason = state.reason.ifBlank { "Booking đã được hủy." },
                    callback = callback
                )
            }

            BookingUiAction.Delete -> {
                bookingRepository.deleteBooking(
                    booking = state.booking,
                    adminUser = user,
                    callback = callback
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadRooms()

        if (openCreatePanel && (user.isTeacher || user.isAdmin)) {
            formState = BookingFormState.forCreate(
                roomId = "",
                dateText = selectedDateKey.ifBlank { BookingDateHelper.todayDateText() }
            )
        }
    }

    LaunchedEffect(reloadKey, keyword, selectedRoomId, selectedStatus, selectedDateKey) {
        reloadBookings()
    }

    LaunchedEffect(reloadKey, selectedRoomId, selectedStatus, weekStartKey) {
        reloadWeekBookings()
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
            BookingHeaderCard(
                user = user,
                result = bookingResult,
                loadingRooms = loadingRooms,
                onlyPending = onlyPending,
                selectedDateKey = selectedDateKey,
                onRefresh = {
                    reloadRooms()
                    reloadKey++
                },
                onCreateClick = {
                    openCreateForm()
                }
            )
        }

        if (!onlyPending) {
            item {
                RealWeekCalendarCard(
                    weekStartKey = weekStartKey,
                    selectedDateKey = selectedDateKey,
                    bookingResult = weekBookingResult,
                    loading = loadingWeekBookings,
                    onPreviousWeek = {
                        val previous = BookingDateHelper.addDays(
                            date = BookingDateHelper.parseDateKey(weekStartKey) ?: Date(),
                            days = -7
                        )
                        weekStartKey = BookingDateHelper.weekStartKey(previous)
                        selectedDateKey = BookingDateHelper.formatDateKey(previous)
                    },
                    onToday = {
                        val today = Date()
                        weekStartKey = BookingDateHelper.weekStartKey(today)
                        selectedDateKey = BookingDateHelper.formatDateKey(today)
                    },
                    onNextWeek = {
                        val next = BookingDateHelper.addDays(
                            date = BookingDateHelper.parseDateKey(weekStartKey) ?: Date(),
                            days = 7
                        )
                        weekStartKey = BookingDateHelper.weekStartKey(next)
                        selectedDateKey = BookingDateHelper.formatDateKey(next)
                    },
                    onSelectDate = { dateKey ->
                        selectedDateKey = dateKey
                    }
                )
            }
        }

        item {
            BookingFilterCard(
                keyword = keyword,
                onKeywordChange = { keyword = it },
                rooms = rooms,
                selectedRoomId = selectedRoomId,
                onRoomChange = { selectedRoomId = it },
                selectedStatus = selectedStatus,
                onStatusChange = {
                    if (!onlyPending) {
                        selectedStatus = it
                    }
                },
                onlyPending = onlyPending,
                onClear = {
                    keyword = ""
                    selectedRoomId = ""
                    selectedStatus = if (onlyPending) BookingStatus.Pending.firestoreValue else ""
                    if (!onlyPending) {
                        selectedDateKey = BookingDateHelper.todayDateText()
                        weekStartKey = BookingDateHelper.weekStartKey(Date())
                    }
                }
            )
        }

        if (formState.visible) {
            item {
                BookingCreateFormCard(
                    state = formState,
                    rooms = rooms,
                    loading = actionLoading,
                    onStateChange = { formState = it },
                    onSubmit = { createBooking() },
                    onCancel = { closeForm() }
                )
            }
        }

        item {
            ErrorBanner(
                message = errorMessage,
                visible = errorMessage.isNotBlank()
            )
        }

        if (loadingBookings) {
            item {
                LoadingStateCard(
                    message = "Đang tải danh sách booking",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            if (bookingResult.bookings.isEmpty()) {
                item {
                    EmptyState(
                        title = emptyTitleFor(user, onlyPending),
                        message = emptyMessageFor(user, onlyPending, selectedDateKey),
                        icon = Icons.Default.EventAvailable,
                        actionText = if (user.isTeacher || user.isAdmin) "Tạo booking" else "",
                        onActionClick = if (user.isTeacher || user.isAdmin) {
                            { openCreateForm() }
                        } else {
                            null
                        }
                    )
                }
            } else {
                itemsIndexed(
                    items = bookingResult.bookings,
                    key = { index, booking ->
                        val safeBooking = booking.normalizedCopy()
                        val baseKey = safeBooking.safeId
                            .ifBlank { "${safeBooking.safeRoomId}_${safeBooking.safeDateKey}_${safeBooking.displayTimeRange}" }
                            .ifBlank { "booking" }

                        "$baseKey-$index"
                    }
                ) { _, booking ->
                    BookingItemCard(
                        user = user,
                        booking = booking,
                        onAction = { action, target ->
                            confirmState = buildBookingConfirmState(
                                action = action,
                                booking = target
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingHeaderCard(
    user: User,
    result: BookingRepository.BookingResult,
    loadingRooms: Boolean,
    onlyPending: Boolean,
    selectedDateKey: String,
    onRefresh: () -> Unit,
    onCreateClick: () -> Unit
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
                        text = bookingHeaderTitle(user, onlyPending),
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
                            bookingHeaderSubtitle(user, onlyPending, selectedDateKey)
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

                SmallBookingButton(
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
                MiniBookingStat(
                    title = "Tổng",
                    value = result.total.toString(),
                    icon = Icons.Default.EventAvailable,
                    tone = AppBadgeTone.Info,
                    modifier = Modifier.weight(1f)
                )

                MiniBookingStat(
                    title = "Chờ",
                    value = result.pendingCount.toString(),
                    icon = Icons.Default.Timer,
                    tone = AppBadgeTone.Warning,
                    modifier = Modifier.weight(1f)
                )

                MiniBookingStat(
                    title = "Duyệt",
                    value = result.approvedCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    tone = AppBadgeTone.Success,
                    modifier = Modifier.weight(1f)
                )

                MiniBookingStat(
                    title = "Từ chối",
                    value = result.rejectedCount.toString(),
                    icon = Icons.Default.ErrorOutline,
                    tone = AppBadgeTone.Error,
                    modifier = Modifier.weight(1f)
                )
            }

            if (user.isTeacher || user.isAdmin) {
                AppGradientButton(
                    text = "Tạo booking phòng",
                    onClick = onCreateClick,
                    leadingIcon = Icons.Default.Add,
                    height = 52.dp
                )
            }
        }
    }
}

@Composable
private fun RealWeekCalendarCard(
    weekStartKey: String,
    selectedDateKey: String,
    bookingResult: BookingRepository.BookingResult,
    loading: Boolean,
    onPreviousWeek: () -> Unit,
    onToday: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDate: (String) -> Unit
) {
    val weekStart = BookingDateHelper.parseDateKey(weekStartKey) ?: Date()
    val weekDays = BookingDateHelper.weekDates(weekStart)
    val todayKey = BookingDateHelper.todayDateText()
    val weekRangeText = "${BookingDateHelper.formatDisplayDayMonth(weekDays.first())} - ${BookingDateHelper.formatDisplayDayMonth(weekDays.last())}"

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppSectionHeader(
                title = "Lịch tuần",
                subtitle = if (loading) {
                    "Đang tải lịch tuần..."
                } else {
                    "Tuần $weekRangeText"
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallBookingButton(
                    text = "Tuần trước",
                    icon = Icons.Default.NavigateBefore,
                    tone = AppBadgeTone.Neutral,
                    onClick = onPreviousWeek
                )

                SmallBookingButton(
                    text = "Hôm nay",
                    icon = Icons.Default.Today,
                    tone = AppBadgeTone.Info,
                    onClick = onToday
                )

                SmallBookingButton(
                    text = "Tuần sau",
                    icon = Icons.Default.NavigateNext,
                    tone = AppBadgeTone.Neutral,
                    onClick = onNextWeek
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekDays.forEach { date ->
                    val dateKey = BookingDateHelper.formatDateKey(date)
                    val selected = dateKey == selectedDateKey
                    val isToday = dateKey == todayKey
                    val dayBookings = bookingResult.bookings.filter { booking ->
                        booking.safeDateKey == dateKey
                    }

                    DayCell(
                        modifier = Modifier.weight(1f),
                        date = date,
                        selected = selected,
                        isToday = isToday,
                        bookingCount = dayBookings.size,
                        approvedCount = dayBookings.count { booking -> booking.isApproved },
                        onClick = {
                            onSelectDate(dateKey)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    date: Date,
    selected: Boolean,
    isToday: Boolean,
    bookingCount: Int,
    approvedCount: Int,
    onClick: () -> Unit
) {
    val tone = when {
        selected -> AppBadgeTone.Info
        isToday -> AppBadgeTone.Success
        else -> AppBadgeTone.Neutral
    }

    val colors = bookingToneColors(tone)

    Surface(
        modifier = modifier.clickable(
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
        shadowElevation = if (selected) 6.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = BookingDateHelper.dayOfWeekVi(date),
                color = colors.content,
                maxLines = 1,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = BookingDateHelper.dayNumber(date),
                color = AppColors.Ink,
                maxLines = 1,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(5.dp))

            Surface(
                modifier = Modifier.size(if (bookingCount > 0) 7.dp else 4.dp),
                shape = RoundedCornerShape(999.dp),
                color = when {
                    approvedCount > 0 -> AppColors.Green
                    bookingCount > 0 -> AppColors.Amber
                    isToday -> colors.content.copy(alpha = 0.55f)
                    else -> AppColors.Border.copy(alpha = 0.45f)
                }
            ) {}

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    bookingCount > 0 -> "$bookingCount tiết"
                    isToday -> "Hôm nay"
                    else -> "Trống"
                },
                color = if (bookingCount > 0 || isToday) colors.content else AppColors.Muted,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun MiniBookingStat(
    title: String,
    value: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    modifier: Modifier = Modifier
) {
    val colors = bookingToneColors(tone)

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
                overflow = TextOverflow.Ellipsis,
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
private fun BookingFilterCard(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    rooms: List<ComputerRoom>,
    selectedRoomId: String,
    onRoomChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    onlyPending: Boolean,
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
                placeholder = "Lớp, môn học, phòng, giáo viên",
                leadingIcon = Icons.Default.Search,
                minHeight = 56.dp,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            BookingSmallTitle("Phòng máy")

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookingChip(
                    text = "Tất cả",
                    value = "",
                    selectedValue = selectedRoomId,
                    icon = Icons.Default.FilterAlt,
                    onSelected = onRoomChange
                )

                rooms.forEach { room ->
                    BookingChip(
                        text = room.displayCode,
                        value = room.safeId,
                        selectedValue = selectedRoomId,
                        icon = Icons.Default.MeetingRoom,
                        onSelected = onRoomChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BookingSmallTitle("Trạng thái")

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!onlyPending) {
                    BookingChip(
                        text = "Tất cả",
                        value = "",
                        selectedValue = selectedStatus,
                        icon = Icons.Default.FilterAlt,
                        onSelected = onStatusChange
                    )
                }

                Booking.statusOptions().forEach { status ->
                    BookingChip(
                        text = status.vietnameseName,
                        value = status.firestoreValue,
                        selectedValue = selectedStatus,
                        icon = iconForBookingStatus(status),
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
private fun BookingCreateFormCard(
    state: BookingFormState,
    rooms: List<ComputerRoom>,
    loading: Boolean,
    onStateChange: (BookingFormState) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 10.dp
    ) {
        Column {
            AppSectionHeader(
                title = "Tạo booking phòng",
                subtitle = "Số lượng sinh viên không được lớn hơn số máy khả dụng trong phòng."
            )

            Spacer(modifier = Modifier.height(14.dp))

            BookingFormSection(
                title = "Phòng và lớp học",
                subtitle = "Booking sẽ hiển thị cho sinh viên đã đăng ký đúng lớp này."
            ) {
                BookingRoomPicker(
                    rooms = rooms,
                    selectedRoomId = state.roomId,
                    onSelected = { roomId ->
                        onStateChange(state.copy(roomId = roomId))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledBookingInput(
                    title = "Lớp học",
                    description = "Nhập đúng tên lớp để sinh viên trong lớp thấy lịch"
                ) {
                    AppTextField(
                        value = state.className,
                        onValueChange = {
                            onStateChange(state.copy(className = it))
                        },
                        label = "Lớp học",
                        placeholder = "Ví dụ: KTPM-K23A",
                        leadingIcon = Icons.Default.Groups,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LabeledBookingInput(
                    title = "Số lượng sinh viên",
                    description = "Không được lớn hơn số máy khả dụng trong phòng đã chọn"
                ) {
                    AppTextField(
                        value = state.studentCountText,
                        onValueChange = { value ->
                            val clean = value.filter { char -> char.isDigit() }.take(3)
                            onStateChange(state.copy(studentCountText = clean))
                        },
                        label = "Số lượng sinh viên",
                        placeholder = "Ví dụ: 35",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LabeledBookingInput(
                    title = "Học phần",
                    description = "Tên môn hoặc học phần thực hành"
                ) {
                    AppTextField(
                        value = state.subjectName,
                        onValueChange = {
                            onStateChange(state.copy(subjectName = it))
                        },
                        label = "Học phần",
                        placeholder = "Ví dụ: Lập trình Android",
                        leadingIcon = Icons.Default.Info,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LabeledBookingInput(
                    title = "Nội dung buổi học",
                    description = "Nội dung hiển thị trong lịch của sinh viên"
                ) {
                    AppTextField(
                        value = state.lessonTitle,
                        onValueChange = {
                            onStateChange(state.copy(lessonTitle = it))
                        },
                        label = "Nội dung buổi học",
                        placeholder = "Ví dụ: Thực hành Firebase Authentication",
                        leadingIcon = Icons.Default.Info,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BookingFormSection(
                title = "Thời gian booking",
                subtitle = "Nhập đúng định dạng ngày và giờ để hệ thống kiểm tra trùng lịch."
            ) {
                LabeledBookingInput(
                    title = "Ngày",
                    description = "Định dạng năm-tháng-ngày"
                ) {
                    AppTextField(
                        value = state.dateText,
                        onValueChange = {
                            onStateChange(state.copy(dateText = it))
                        },
                        label = "Ngày",
                        placeholder = "Ví dụ: 2026-06-01",
                        leadingIcon = Icons.Default.CalendarMonth,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LabeledBookingInput(
                    title = "Giờ bắt đầu",
                    description = "Định dạng 24 giờ"
                ) {
                    AppTextField(
                        value = state.startTimeText,
                        onValueChange = {
                            onStateChange(state.copy(startTimeText = it))
                        },
                        label = "Giờ bắt đầu",
                        placeholder = "Ví dụ: 07:00",
                        leadingIcon = Icons.Default.Timer,
                        minHeight = 60.dp,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LabeledBookingInput(
                    title = "Giờ kết thúc",
                    description = "Phải sau giờ bắt đầu"
                ) {
                    AppTextField(
                        value = state.endTimeText,
                        onValueChange = {
                            onStateChange(state.copy(endTimeText = it))
                        },
                        label = "Giờ kết thúc",
                        placeholder = "Ví dụ: 09:30",
                        leadingIcon = Icons.Default.Timer,
                        minHeight = 60.dp,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BookingFormSection(
                title = "Ghi chú",
                subtitle = "Thông tin thêm cho quản trị viên khi duyệt booking."
            ) {
                AppTextField(
                    value = state.note,
                    onValueChange = {
                        onStateChange(state.copy(note = it))
                    },
                    label = "Ghi chú",
                    placeholder = "Ví dụ: Cần phòng có đủ máy cho nhóm thực hành",
                    leadingIcon = Icons.Default.Info,
                    singleLine = false,
                    minHeight = 76.dp,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppGradientButton(
                text = "Gửi booking",
                onClick = onSubmit,
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

@Composable
private fun LabeledBookingInput(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = AppColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = description,
            color = AppColors.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        )

        Spacer(modifier = Modifier.height(7.dp))

        content()
    }
}

@Composable
private fun BookingFormSection(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookingRoomPicker(
    rooms: List<ComputerRoom>,
    selectedRoomId: String,
    onSelected: (String) -> Unit
) {
    Column {
        BookingSmallTitle("Phòng máy")

        Spacer(modifier = Modifier.height(8.dp))

        if (rooms.isEmpty()) {
            EmptyState(
                title = "Chưa có phòng máy",
                message = "Cần tạo phòng máy trước khi booking.",
                icon = Icons.Default.MeetingRoom
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rooms.forEach { room ->
                    BookingChip(
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

@Composable
private fun BookingItemCard(
    user: User,
    booking: Booking,
    onAction: (BookingUiAction, Booking) -> Unit
) {
    val safeBooking = booking.normalizedCopy()
    val seatAssignment = safeBooking.assignmentForUser(user)

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
                    color = bookingStatusBackground(safeBooking.statusEnum),
                    modifier = Modifier
                        .height(52.dp)
                        .width(52.dp)
                ) {
                    Icon(
                        imageVector = iconForBookingStatus(safeBooking.statusEnum),
                        contentDescription = null,
                        tint = bookingStatusColor(safeBooking.statusEnum),
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = safeBooking.displayLessonTitle,
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
                        text = "${safeBooking.displayClassName} • ${safeBooking.displayRoomCode} • ${safeBooking.displayStudentCount}",
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
                    text = safeBooking.statusNameVi,
                    tone = toneForBookingStatus(safeBooking.statusEnum)
                )

                StatusBadge(
                    text = safeBooking.displayRoomCode,
                    tone = AppBadgeTone.Info
                )
            }

            BookingInfoLine(
                icon = Icons.Default.CalendarMonth,
                title = "Thời gian",
                value = safeBooking.displayScheduleLine
            )

            BookingInfoLine(
                icon = Icons.Default.Person,
                title = "Giảng viên",
                value = safeBooking.displayTeacherName
            )

            BookingInfoLine(
                icon = Icons.Default.Groups,
                title = "Lớp / sĩ số",
                value = "${safeBooking.displayClassName} • ${safeBooking.displayStudentCount}"
            )

            BookingInfoLine(
                icon = Icons.Default.Info,
                title = "Học phần",
                value = safeBooking.displaySubjectName
            )

            if (safeBooking.isApproved) {
                if (user.isStudent) {
                    BookingInfoLine(
                        icon = Icons.Default.CheckCircle,
                        title = "Chỗ ngồi",
                        value = seatAssignment?.let { assignment ->
                            "${assignment.displaySeatLabel} • ${assignment.displayComputerCode}"
                        } ?: "Bạn thuộc lớp này nhưng chưa có chỗ riêng. Liên hệ giáo viên hoặc quản trị viên."
                    )
                } else {
                    BookingInfoLine(
                        icon = Icons.Default.CheckCircle,
                        title = "Phân chỗ",
                        value = safeBooking.assignmentSummary
                    )
                }

                BookingSeatMapCard(
                    user = user,
                    booking = safeBooking
                )
            }

            if (safeBooking.displayNote.isNotBlank()) {
                BookingInfoLine(
                    icon = Icons.Default.Info,
                    title = "Ghi chú",
                    value = safeBooking.displayNote
                )
            }

            BookingActionRow(
                user = user,
                booking = safeBooking,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun BookingSeatMapCard(
    user: User,
    booking: Booking
) {
    val safeBooking = booking.normalizedCopy()
    val assignments = safeBooking.normalizedAssignments
    val myAssignment = safeBooking.assignmentForUser(user)

    if (!safeBooking.isApproved) {
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, AppColors.BorderSoft),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppSectionHeader(
                title = if (user.isStudent) "Sơ đồ chỗ ngồi của bạn" else "Sơ đồ chỗ ngồi lớp",
                subtitle = seatMapSubtitle(
                    user = user,
                    booking = safeBooking,
                    myAssignment = myAssignment
                )
            )

            if (assignments.isEmpty()) {
                EmptySeatMapMessage(
                    booking = safeBooking
                )
            } else {
                SeatMapGrid(
                    user = user,
                    assignments = assignments,
                    myAssignment = myAssignment
                )

                SeatMapLegend(
                    user = user
                )

                if (!user.isStudent) {
                    SeatAssignmentList(
                        assignments = assignments,
                        unassignedCount = safeBooking.normalizedUnassignedSeatCount
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySeatMapMessage(
    booking: Booking
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.70f),
        border = BorderStroke(1.dp, AppColors.BorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = AppColors.Muted,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Chưa có sơ đồ chỗ ngồi",
                color = AppColors.Ink,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (booking.normalizedUnassignedSeatCount > 0) {
                    "Booking đã duyệt nhưng chưa có sinh viên có tài khoản trong lớp để phân chỗ riêng."
                } else {
                    "Chưa có dữ liệu phân chỗ cho buổi học này."
                },
                color = AppColors.Muted,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun SeatMapGrid(
    user: User,
    assignments: List<BookingSeatAssignment>,
    myAssignment: BookingSeatAssignment?
) {
    val cleanAssignments = assignments
        .map { assignment -> assignment.normalizedCopy() }
        .filter { assignment -> assignment.normalizedRow > 0 && assignment.normalizedColumn > 0 }

    val maxRow = cleanAssignments.maxOfOrNull { assignment -> assignment.normalizedRow } ?: 1
    val maxColumn = cleanAssignments.maxOfOrNull { assignment -> assignment.normalizedColumn } ?: 1
    val assignmentMap = cleanAssignments.associateBy { assignment ->
        "${assignment.normalizedRow}_${assignment.normalizedColumn}"
    }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.widthIn(min = 320.dp),
            shape = RoundedCornerShape(18.dp),
            color = AppColors.BlueSoft.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, AppColors.Blue.copy(alpha = 0.14f))
        ) {
            Text(
                text = "BẢNG / MÀN CHIẾU",
                modifier = Modifier.padding(vertical = 9.dp),
                color = AppColors.BlueDeep,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
        }

        for (rowIndex in 1..maxRow) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(width = 34.dp, height = 54.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, AppColors.BorderSoft)
                ) {
                    Text(
                        text = "H$rowIndex",
                        modifier = Modifier.padding(top = 17.dp),
                        color = AppColors.Muted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    )
                }

                for (columnIndex in 1..maxColumn) {
                    val assignment = assignmentMap["${rowIndex}_${columnIndex}"]
                    SeatCell(
                        user = user,
                        assignment = assignment,
                        isMine = isSameSeat(assignment, myAssignment)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatCell(
    user: User,
    assignment: BookingSeatAssignment?,
    isMine: Boolean
) {
    val occupied = assignment != null

    val background = when {
        isMine -> AppColors.BlueSoft
        occupied -> AppColors.GreenSoft
        else -> Color(0xFFF3F7FB)
    }

    val border = when {
        isMine -> AppColors.Blue
        occupied -> AppColors.Green.copy(alpha = 0.28f)
        else -> AppColors.Border
    }

    val content = when {
        isMine -> AppColors.BlueDeep
        occupied -> AppColors.Green
        else -> AppColors.Muted
    }

    val mainText = when {
        assignment == null -> "Trống"
        else -> assignment.displayComputerCode
    }

    val subText = when {
        assignment == null -> ""
        isMine -> "Chỗ của bạn"
        user.isStudent -> "Đã có"
        else -> assignment.displayStudentName
    }

    Surface(
        modifier = Modifier.size(width = 90.dp, height = 72.dp),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(
            width = if (isMine) 2.dp else 1.dp,
            color = border
        ),
        shadowElevation = if (isMine) 8.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = mainText,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )

            if (subText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subText,
                    color = if (isMine) AppColors.BlueDeep else AppColors.Muted,
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatMapLegend(
    user: User
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (user.isStudent) {
            StatusBadge(
                text = "Ô xanh dương là chỗ của bạn",
                tone = AppBadgeTone.Info
            )
        } else {
            StatusBadge(
                text = "Xanh lá: đã phân sinh viên",
                tone = AppBadgeTone.Success
            )

            StatusBadge(
                text = "Xanh dương: chỗ đang được chọn",
                tone = AppBadgeTone.Info
            )
        }
    }
}

@Composable
private fun SeatAssignmentList(
    assignments: List<BookingSeatAssignment>,
    unassignedCount: Int
) {
    val cleanAssignments = assignments
        .map { assignment -> assignment.normalizedCopy() }
        .sortedWith(
            compareBy<BookingSeatAssignment>(
                { it.normalizedRow },
                { it.normalizedColumn },
                { it.displayStudentName.lowercase(Locale.getDefault()) }
            )
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        BookingSmallTitle("Danh sách phân chỗ")

        cleanAssignments.forEach { assignment ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, AppColors.BorderSoft)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = AppColors.Blue,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = assignment.displayStudentName,
                            color = AppColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        )

                        Text(
                            text = "${assignment.displaySeatLabel} • ${assignment.displayComputerCode}",
                            color = AppColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        if (unassignedCount > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.AmberSoft,
                border = BorderStroke(1.dp, AppColors.Amber.copy(alpha = 0.18f))
            ) {
                Text(
                    text = "Còn $unassignedCount sinh viên chưa có tài khoản nên chưa có chỗ riêng.",
                    modifier = Modifier.padding(10.dp),
                    color = AppColors.Amber,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BookingInfoLine(
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
                modifier = Modifier.width(84.dp)
            )

            Text(
                text = value,
                color = AppColors.Text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookingActionRow(
    user: User,
    booking: Booking,
    onAction: (BookingUiAction, Booking) -> Unit
) {
    val safeUser = user.normalizedCopy()
    val safeBooking = booking.normalizedCopy()

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (safeUser.isAdmin && safeBooking.isPending) {
            SmallBookingButton(
                text = "Duyệt",
                icon = Icons.Default.CheckCircle,
                tone = AppBadgeTone.Success,
                onClick = {
                    onAction(BookingUiAction.Approve, safeBooking)
                }
            )

            SmallBookingButton(
                text = "Từ chối",
                icon = Icons.Default.ErrorOutline,
                tone = AppBadgeTone.Error,
                onClick = {
                    onAction(BookingUiAction.Reject, safeBooking)
                }
            )
        }

        if (safeBooking.canBeCancelledBy(safeUser)) {
            SmallBookingButton(
                text = "Hủy",
                icon = Icons.Default.ErrorOutline,
                tone = AppBadgeTone.Warning,
                onClick = {
                    onAction(BookingUiAction.Cancel, safeBooking)
                }
            )
        }

        if (safeUser.isAdmin) {
            SmallBookingButton(
                text = "Xóa",
                icon = Icons.Default.Delete,
                tone = AppBadgeTone.Error,
                onClick = {
                    onAction(BookingUiAction.Delete, safeBooking)
                }
            )
        }
    }
}

@Composable
private fun SmallBookingButton(
    text: String,
    icon: ImageVector,
    tone: AppBadgeTone,
    onClick: () -> Unit
) {
    val colors = bookingToneColors(tone)

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
private fun BookingChip(
    text: String,
    value: String,
    selectedValue: String,
    icon: ImageVector,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    val colors = if (selected) {
        bookingToneColors(AppBadgeTone.Info)
    } else {
        BookingLocalToneColors(
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
private fun BookingSmallTitle(
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

private data class BookingFormState(
    val visible: Boolean,
    val roomId: String,
    val className: String,
    val studentCountText: String,
    val subjectName: String,
    val lessonTitle: String,
    val dateText: String,
    val startTimeText: String,
    val endTimeText: String,
    val note: String
) {
    fun validate(rooms: List<ComputerRoom>): String? {
        val room = rooms.firstOrNull { it.safeId == roomId }
        val startDate = startDateTime()
        val endDate = endDateTime()
        val count = studentCountText.trim().toIntOrNull()

        return when {
            roomId.trim().isBlank() -> "Vui lòng chọn phòng máy."
            room == null -> "Phòng máy không hợp lệ."
            className.trim().isBlank() -> "Vui lòng nhập lớp học."
            Booking.buildStudentCountError(count) != null -> Booking.buildStudentCountError(count)
            subjectName.trim().isBlank() -> "Vui lòng nhập học phần."
            lessonTitle.trim().isBlank() -> "Vui lòng nhập nội dung buổi học."
            dateText.trim().isBlank() -> "Vui lòng nhập ngày booking."
            startTimeText.trim().isBlank() -> "Vui lòng nhập giờ bắt đầu."
            endTimeText.trim().isBlank() -> "Vui lòng nhập giờ kết thúc."
            startDate == null -> "Ngày hoặc giờ bắt đầu không đúng định dạng."
            endDate == null -> "Ngày hoặc giờ kết thúc không đúng định dạng."
            !Booking.isValidTimeRange(startDate, endDate) -> Booking.buildTimeRangeError(startDate, endDate)
            else -> null
        }
    }

    fun studentCountValue(): Int {
        return studentCountText.trim().toIntOrNull()
            ?.coerceIn(Booking.MIN_STUDENT_COUNT, Booking.MAX_STUDENT_COUNT)
            ?: Booking.DEFAULT_STUDENT_COUNT
    }

    fun startDateTime(): Date? {
        return parseDateTime(
            dateText = dateText,
            timeText = startTimeText
        )
    }

    fun endDateTime(): Date? {
        return parseDateTime(
            dateText = dateText,
            timeText = endTimeText
        )
    }

    companion object {
        fun hidden(): BookingFormState {
            return BookingFormState(
                visible = false,
                roomId = "",
                className = "",
                studentCountText = "35",
                subjectName = "",
                lessonTitle = "",
                dateText = BookingDateHelper.todayDateText(),
                startTimeText = "07:00",
                endTimeText = "09:30",
                note = ""
            )
        }

        fun forCreate(
            roomId: String,
            dateText: String
        ): BookingFormState {
            return hidden().copy(
                visible = true,
                roomId = roomId.trim(),
                dateText = dateText.trim().ifBlank { BookingDateHelper.todayDateText() }
            )
        }
    }
}

private enum class BookingUiAction {
    Approve,
    Reject,
    Cancel,
    Delete
}

private data class BookingConfirmState(
    val action: BookingUiAction,
    val booking: Booking,
    val title: String,
    val message: String,
    val confirmText: String,
    val reason: String = "",
    val danger: Boolean
)

private data class BookingLocalToneColors(
    val background: Color,
    val border: Color,
    val content: Color
)

private object BookingDateHelper {
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val displayDayMonthFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    fun todayDateText(): String {
        return formatDateKey(Date())
    }

    fun formatDateKey(date: Date): String {
        return dateKeyFormat.format(date)
    }

    fun parseDateKey(dateKey: String): Date? {
        return try {
            dateKeyFormat.isLenient = false
            dateKeyFormat.parse(dateKey.trim())
        } catch (exception: Exception) {
            null
        }
    }

    fun weekStartKey(date: Date): String {
        return formatDateKey(startOfWeek(date))
    }

    fun startOfWeek(date: Date): Date {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = date
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun weekDates(weekStart: Date): List<Date> {
        return (0..6).map { day ->
            addDays(weekStart, day)
        }
    }

    fun addDays(
        date: Date,
        days: Int
    ): Date {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }

    fun dayOfWeekVi(date: Date): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = date

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            Calendar.SATURDAY -> "T7"
            Calendar.SUNDAY -> "CN"
            else -> ""
        }
    }

    fun dayNumber(date: Date): String {
        return dayNumberFormat.format(date)
    }

    fun formatDisplayDayMonth(date: Date): String {
        return displayDayMonthFormat.format(date)
    }

    fun formatLongDate(dateKey: String): String {
        val date = parseDateKey(dateKey) ?: return "ngày đã chọn"
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}

private fun buildBookingConfirmState(
    action: BookingUiAction,
    booking: Booking
): BookingConfirmState {
    val safeBooking = booking.normalizedCopy()
    val titleLine = "${safeBooking.displayClassName} • ${safeBooking.displayRoomCode}"

    return when (action) {
        BookingUiAction.Approve -> {
            BookingConfirmState(
                action = action,
                booking = safeBooking,
                title = "Duyệt booking phòng",
                message = "Xác nhận duyệt booking $titleLine vào ${safeBooking.displayScheduleLine}? Hệ thống sẽ phân chỗ ngồi ngẫu nhiên cho sinh viên đã có tài khoản trong lớp.",
                confirmText = "Duyệt",
                danger = false
            )
        }

        BookingUiAction.Reject -> {
            BookingConfirmState(
                action = action,
                booking = safeBooking,
                title = "Từ chối booking",
                message = "Booking $titleLine sẽ bị từ chối.",
                confirmText = "Từ chối",
                reason = "Không đủ điều kiện duyệt.",
                danger = true
            )
        }

        BookingUiAction.Cancel -> {
            BookingConfirmState(
                action = action,
                booking = safeBooking,
                title = "Hủy booking",
                message = "Xác nhận hủy booking $titleLine?",
                confirmText = "Hủy",
                reason = "Booking đã được hủy.",
                danger = true
            )
        }

        BookingUiAction.Delete -> {
            BookingConfirmState(
                action = action,
                booking = safeBooking,
                title = "Xóa booking",
                message = "Booking $titleLine sẽ bị xóa khỏi Firestore.",
                confirmText = "Xóa",
                danger = true
            )
        }
    }
}

private fun parseDateTime(
    dateText: String,
    timeText: String
): Date? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        formatter.isLenient = false
        formatter.parse("${dateText.trim()} ${timeText.trim()}")
    } catch (exception: Exception) {
        null
    }
}

private fun emptyTitleFor(
    user: User,
    onlyPending: Boolean
): String {
    return when {
        onlyPending -> "Không có booking chờ duyệt"
        user.isTeacher -> "Chưa có booking trong ngày"
        user.isStudent -> "Chưa có lịch phòng máy"
        else -> "Chưa có booking"
    }
}

private fun emptyMessageFor(
    user: User,
    onlyPending: Boolean,
    selectedDateKey: String
): String {
    val selectedDateText = if (selectedDateKey.isNotBlank()) {
        BookingDateHelper.formatLongDate(selectedDateKey)
    } else {
        "ngày đã chọn"
    }

    return when {
        onlyPending -> "Hiện tại chưa có booking nào cần phê duyệt."
        user.isTeacher -> "Ngày $selectedDateText chưa có booking nào. Bấm Tạo booking phòng để gửi yêu cầu sử dụng phòng máy."
        user.isStudent -> "Ngày $selectedDateText chưa có lịch phòng máy đã được duyệt cho lớp của bạn."
        else -> "Ngày $selectedDateText chưa có booking nào trong hệ thống."
    }
}

private fun bookingHeaderTitle(
    user: User,
    onlyPending: Boolean
): String {
    return when {
        onlyPending -> "Duyệt booking"
        user.isAdmin -> "Quản lý lịch phòng"
        user.isTeacher -> "Lịch booking của tôi"
        else -> "Lịch phòng máy"
    }
}

private fun bookingHeaderSubtitle(
    user: User,
    onlyPending: Boolean,
    selectedDateKey: String
): String {
    if (onlyPending) {
        return "Duyệt, từ chối và theo dõi booking phòng đang chờ xử lý."
    }

    val dateText = BookingDateHelper.formatLongDate(selectedDateKey)

    return when {
        user.isAdmin -> "Theo dõi booking và lịch phòng máy trong ngày $dateText."
        user.isTeacher -> "Tạo booking phòng và xem toàn bộ sơ đồ chỗ ngồi lớp trong ngày $dateText."
        else -> "Xem lịch phòng máy và chỗ ngồi của bạn trong ngày $dateText."
    }
}

private fun seatMapSubtitle(
    user: User,
    booking: Booking,
    myAssignment: BookingSeatAssignment?
): String {
    return when {
        user.isStudent && myAssignment != null -> {
            "Bạn ngồi tại ${myAssignment.displaySeatLabel} • ${myAssignment.displayComputerCode}."
        }

        user.isStudent -> {
            "Bạn thuộc lớp ${booking.displayClassName}, nhưng chưa có chỗ riêng trong dữ liệu phân chỗ."
        }

        else -> {
            "Lớp ${booking.displayClassName} • ${booking.displayStudentCount} • ${booking.assignmentSummary}."
        }
    }
}

private fun isSameSeat(
    first: BookingSeatAssignment?,
    second: BookingSeatAssignment?
): Boolean {
    if (first == null || second == null) {
        return false
    }

    val cleanFirst = first.normalizedCopy()
    val cleanSecond = second.normalizedCopy()

    return cleanFirst.computerId.isNotBlank() &&
            cleanFirst.computerId == cleanSecond.computerId ||
            cleanFirst.normalizedRow == cleanSecond.normalizedRow &&
            cleanFirst.normalizedColumn == cleanSecond.normalizedColumn &&
            cleanFirst.displayComputerCode == cleanSecond.displayComputerCode
}

private fun iconForBookingStatus(
    status: BookingStatus
): ImageVector {
    return when (status) {
        BookingStatus.Pending -> Icons.Default.Timer
        BookingStatus.Approved -> Icons.Default.CheckCircle
        BookingStatus.Rejected -> Icons.Default.ErrorOutline
        BookingStatus.Cancelled -> Icons.Default.ErrorOutline
    }
}

private fun toneForBookingStatus(
    status: BookingStatus
): AppBadgeTone {
    return when (status) {
        BookingStatus.Pending -> AppBadgeTone.Warning
        BookingStatus.Approved -> AppBadgeTone.Success
        BookingStatus.Rejected -> AppBadgeTone.Error
        BookingStatus.Cancelled -> AppBadgeTone.Neutral
    }
}

private fun bookingStatusBackground(
    status: BookingStatus
): Color {
    return when (status) {
        BookingStatus.Pending -> AppColors.AmberSoft
        BookingStatus.Approved -> AppColors.GreenSoft
        BookingStatus.Rejected -> AppColors.RoseSoft
        BookingStatus.Cancelled -> Color(0xFFF3F7FB)
    }
}

private fun bookingStatusColor(
    status: BookingStatus
): Color {
    return when (status) {
        BookingStatus.Pending -> AppColors.Amber
        BookingStatus.Approved -> AppColors.Green
        BookingStatus.Rejected -> AppColors.Rose
        BookingStatus.Cancelled -> AppColors.Muted
    }
}

private fun bookingToneColors(
    tone: AppBadgeTone
): BookingLocalToneColors {
    return when (tone) {
        AppBadgeTone.Info -> BookingLocalToneColors(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> BookingLocalToneColors(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> BookingLocalToneColors(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> BookingLocalToneColors(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Neutral -> BookingLocalToneColors(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )

        AppBadgeTone.Purple -> BookingLocalToneColors(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )
    }
}