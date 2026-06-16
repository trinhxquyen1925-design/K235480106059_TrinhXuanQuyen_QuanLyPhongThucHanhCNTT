package com.quyen.quanlyphongmay.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quyen.quanlyphongmay.model.Booking
import com.quyen.quanlyphongmay.model.BookingSeatAssignment
import com.quyen.quanlyphongmay.model.BookingStatus
import com.quyen.quanlyphongmay.model.ComputerRoom
import com.quyen.quanlyphongmay.model.User
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class BookingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository(firestore)
) {
    private val bookingCollection = firestore.collection(Booking.COLLECTION_NAME)
    private val userCollection = firestore.collection("users")
    private val computerCollection = firestore.collection("computers")

    data class BookingFilter(
        val keyword: String = "",
        val roomId: String = "",
        val teacherEmail: String = "",
        val status: String = "",
        val dateKey: String = "",
        val weekKey: String = "",
        val onlyMine: Boolean = false,
        val onlyApprovedForSchedule: Boolean = false
    )

    data class BookingResult(
        val bookings: List<Booking> = emptyList(),
        val total: Int = 0,
        val pendingCount: Int = 0,
        val approvedCount: Int = 0,
        val rejectedCount: Int = 0,
        val cancelledCount: Int = 0
    )

    private data class SeatStudentCandidate(
        val uid: String,
        val email: String,
        val name: String,
        val studentCode: String,
        val className: String
    )

    private data class SeatComputerCandidate(
        val id: String,
        val code: String,
        val name: String,
        val roomId: String,
        val roomCode: String,
        val row: Int,
        val column: Int,
        val status: String
    )

    fun getBookings(
        currentUser: User,
        filter: BookingFilter = BookingFilter(),
        callback: (Boolean, String?, BookingResult) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        try {
            bookingCollection
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val allBookings = snapshot.documents.mapNotNull { document ->
                            documentToBookingSafe(
                                documentId = document.id,
                                data = document.data.orEmpty()
                            )
                        }

                        val visibleBookings = allBookings.filter { booking ->
                            canUserSeeBooking(
                                user = safeUser,
                                booking = booking
                            )
                        }

                        val filteredBookings = visibleBookings
                            .filter { booking ->
                                val safeBooking = booking.normalizedCopy()

                                val keywordMatched = filter.keyword.trim().isBlank() ||
                                        safeBooking.matchesKeyword(filter.keyword)

                                val roomMatched = filter.roomId.trim().isBlank() ||
                                        safeBooking.safeRoomId == filter.roomId.trim()

                                val teacherMatched = filter.teacherEmail.trim().isBlank() ||
                                        safeBooking.displayTeacherEmail.equals(
                                            filter.teacherEmail.trim(),
                                            ignoreCase = true
                                        )

                                val statusMatched = filter.status.trim().isBlank() ||
                                        safeBooking.statusEnum.firestoreValue == BookingStatus.fromRaw(filter.status).firestoreValue

                                val dateMatched = filter.dateKey.trim().isBlank() ||
                                        safeBooking.safeDateKey == filter.dateKey.trim()

                                val weekMatched = filter.weekKey.trim().isBlank() ||
                                        safeBooking.safeWeekKey == filter.weekKey.trim()

                                val mineMatched = !filter.onlyMine ||
                                        safeBooking.displayTeacherEmail.equals(
                                            safeUser.email,
                                            ignoreCase = true
                                        ) ||
                                        safeBooking.teacherId.isNotBlank() && safeBooking.teacherId == safeUser.uid

                                val approvedMatched = !filter.onlyApprovedForSchedule ||
                                        safeBooking.isApproved

                                keywordMatched &&
                                        roomMatched &&
                                        teacherMatched &&
                                        statusMatched &&
                                        dateMatched &&
                                        weekMatched &&
                                        mineMatched &&
                                        approvedMatched
                            }
                            .sortedWith(
                                compareBy<Booking>(
                                    { it.startDate?.time ?: Long.MAX_VALUE },
                                    { it.displayRoomCode.lowercase(Locale.getDefault()) },
                                    { it.displayClassName.lowercase(Locale.getDefault()) }
                                )
                            )

                        callback(
                            true,
                            null,
                            buildBookingResult(filteredBookings)
                        )
                    } catch (exception: Exception) {
                        callback(
                            false,
                            normalizeFirestoreError(exception),
                            BookingResult()
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        BookingResult()
                    )
                }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                BookingResult()
            )
        }
    }

    fun getPendingBookings(
        currentUser: User,
        callback: (Boolean, String?, BookingResult) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isAdmin) {
            callback(
                false,
                "Bạn không có quyền duyệt booking.",
                BookingResult()
            )
            return
        }

        getBookings(
            currentUser = safeUser,
            filter = BookingFilter(
                status = BookingStatus.Pending.firestoreValue
            ),
            callback = callback
        )
    }

    fun getScheduleBookings(
        currentUser: User,
        dateKey: String = "",
        weekKey: String = "",
        callback: (Boolean, String?, BookingResult) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        val filter = when {
            safeUser.isAdmin -> {
                BookingFilter(
                    dateKey = dateKey,
                    weekKey = weekKey,
                    onlyApprovedForSchedule = true
                )
            }

            safeUser.isTeacher -> {
                BookingFilter(
                    dateKey = dateKey,
                    weekKey = weekKey,
                    onlyMine = true,
                    onlyApprovedForSchedule = false
                )
            }

            else -> {
                BookingFilter(
                    dateKey = dateKey,
                    weekKey = weekKey,
                    onlyApprovedForSchedule = true
                )
            }
        }

        getBookings(
            currentUser = safeUser,
            filter = filter,
            callback = callback
        )
    }

    fun createBooking(
        booking: Booking,
        teacher: User,
        callback: (Boolean, String?, Booking?) -> Unit
    ) {
        val safeTeacher = teacher.normalizedCopy()

        if (!safeTeacher.isTeacher && !safeTeacher.isAdmin) {
            callback(
                false,
                "Chỉ giáo viên mới có quyền tạo booking phòng.",
                null
            )
            return
        }

        val normalizedBooking = booking.normalizedCopy()

        val validationMessage = validateBookingBeforeCreate(
            booking = normalizedBooking,
            teacher = safeTeacher
        )

        if (validationMessage != null) {
            callback(false, validationMessage, null)
            return
        }

        validateRoomCapacityForBooking(
            booking = normalizedBooking
        ) { capacitySuccess, capacityMessage ->
            if (!capacitySuccess) {
                callback(
                    false,
                    capacityMessage ?: "Số lượng sinh viên vượt quá số máy khả dụng trong phòng.",
                    null
                )
                return@validateRoomCapacityForBooking
            }

            checkBookingConflict(
                targetBooking = normalizedBooking,
                includePending = true
            ) { success, message, hasConflict ->
                if (!success) {
                    callback(
                        false,
                        message ?: "Không thể kiểm tra trùng lịch phòng.",
                        null
                    )
                    return@checkBookingConflict
                }

                if (hasConflict) {
                    callback(
                        false,
                        "Phòng máy đã có booking trùng thời gian. Vui lòng chọn thời gian khác.",
                        null
                    )
                    return@checkBookingConflict
                }

                try {
                    val documentRef = if (normalizedBooking.safeId.isNotBlank()) {
                        bookingCollection.document(normalizedBooking.safeId)
                    } else {
                        bookingCollection.document()
                    }

                    val bookingToSave = normalizedBooking.copy(
                        id = documentRef.id,
                        teacherId = safeTeacher.uid,
                        teacherEmail = safeTeacher.email,
                        teacherName = safeTeacher.displayName,
                        status = BookingStatus.Pending.firestoreValue,
                        assignedSeatCount = 0,
                        unassignedSeatCount = normalizedBooking.normalizedStudentCount,
                        seatAssignments = emptyList(),
                        createdAt = normalizedBooking.createdAt ?: Timestamp.now(),
                        updatedAt = Timestamp.now()
                    ).normalizedCopy()

                    documentRef
                        .set(
                            bookingToMap(
                                booking = bookingToSave,
                                includeCreatedAt = true
                            ),
                            SetOptions.merge()
                        )
                        .addOnSuccessListener {
                            notificationRepository.createBookingCreatedNotification(
                                booking = bookingToSave,
                                teacher = safeTeacher
                            ) { _, _, _ -> }

                            callback(
                                true,
                                "Đã gửi booking phòng. Admin sẽ nhận thông báo để duyệt.",
                                bookingToSave
                            )
                        }
                        .addOnFailureListener { exception ->
                            callback(
                                false,
                                normalizeFirestoreError(exception),
                                null
                            )
                        }
                } catch (exception: Exception) {
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        null
                    )
                }
            }
        }
    }

    fun createBooking(
        teacher: User,
        room: ComputerRoom,
        className: String,
        studentCount: Int,
        subjectName: String,
        lessonTitle: String,
        startDate: Date,
        endDate: Date,
        note: String,
        callback: (Boolean, String?, Booking?) -> Unit
    ) {
        val booking = Booking.newDraft(
            teacher = teacher,
            room = room,
            className = className,
            studentCount = studentCount,
            subjectName = subjectName,
            lessonTitle = lessonTitle,
            startDate = startDate,
            endDate = endDate,
            note = note
        )

        createBooking(
            booking = booking,
            teacher = teacher,
            callback = callback
        )
    }

    fun createBooking(
        teacher: User,
        room: ComputerRoom,
        className: String,
        subjectName: String,
        lessonTitle: String,
        startDate: Date,
        endDate: Date,
        note: String,
        callback: (Boolean, String?, Booking?) -> Unit
    ) {
        createBooking(
            teacher = teacher,
            room = room,
            className = className,
            studentCount = Booking.DEFAULT_STUDENT_COUNT,
            subjectName = subjectName,
            lessonTitle = lessonTitle,
            startDate = startDate,
            endDate = endDate,
            note = note,
            callback = callback
        )
    }

    fun approveBooking(
        booking: Booking,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeAdmin = adminUser.normalizedCopy()
        val safeBooking = booking.normalizedCopy()

        if (!safeAdmin.isAdmin) {
            callback(false, "Bạn không có quyền duyệt booking.")
            return
        }

        if (!safeBooking.canBeApproved()) {
            callback(false, "Booking này không đủ điều kiện để duyệt.")
            return
        }

        validateRoomCapacityForBooking(
            booking = safeBooking
        ) { capacitySuccess, capacityMessage ->
            if (!capacitySuccess) {
                callback(
                    false,
                    capacityMessage ?: "Không thể duyệt vì số lượng sinh viên vượt quá số máy khả dụng trong phòng."
                )
                return@validateRoomCapacityForBooking
            }

            checkBookingConflict(
                targetBooking = safeBooking,
                includePending = false
            ) { success, message, hasConflict ->
                if (!success) {
                    callback(
                        false,
                        message ?: "Không thể kiểm tra trùng lịch phòng."
                    )
                    return@checkBookingConflict
                }

                if (hasConflict) {
                    callback(
                        false,
                        "Không thể duyệt vì phòng đã có lịch được duyệt trùng thời gian."
                    )
                    return@checkBookingConflict
                }

                buildSeatAssignmentsForBooking(
                    booking = safeBooking
                ) { assignmentSuccess, assignmentMessage, assignments, unassignedCount ->
                    if (!assignmentSuccess) {
                        callback(
                            false,
                            assignmentMessage ?: "Không thể phân chỗ ngồi cho booking."
                        )
                        return@buildSeatAssignmentsForBooking
                    }

                    val approvedBookingForNotification = safeBooking.copy(
                        status = BookingStatus.Approved.firestoreValue,
                        seatAssignments = assignments,
                        assignedSeatCount = assignments.size,
                        unassignedSeatCount = unassignedCount,
                        approvedById = safeAdmin.uid,
                        approvedByEmail = safeAdmin.email,
                        approvedByName = safeAdmin.displayName,
                        approvedAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    ).normalizedCopy()

                    updateBookingStatus(
                        booking = safeBooking,
                        adminUser = safeAdmin,
                        status = BookingStatus.Approved,
                        rejectReason = "",
                        seatAssignments = assignments,
                        assignedSeatCount = assignments.size,
                        unassignedSeatCount = unassignedCount,
                        successMessage = buildApproveSuccessMessage(
                            assignedCount = assignments.size,
                            unassignedCount = unassignedCount
                        ),
                        afterSuccess = {
                            notificationRepository.createBookingApprovedNotifications(
                                booking = approvedBookingForNotification,
                                adminUser = safeAdmin
                            ) { _, _ -> }
                        },
                        callback = callback
                    )
                }
            }
        }
    }

    fun rejectBooking(
        booking: Booking,
        adminUser: User,
        reason: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeAdmin = adminUser.normalizedCopy()
        val safeBooking = booking.normalizedCopy()

        if (!safeAdmin.isAdmin) {
            callback(false, "Bạn không có quyền từ chối booking.")
            return
        }

        if (!safeBooking.canBeRejected()) {
            callback(false, "Chỉ booking chờ duyệt mới có thể từ chối.")
            return
        }

        val rejectedBookingForNotification = safeBooking.copy(
            status = BookingStatus.Rejected.firestoreValue,
            rejectReason = reason.trim().ifBlank { "Không đủ điều kiện duyệt." },
            approvedById = safeAdmin.uid,
            approvedByEmail = safeAdmin.email,
            approvedByName = safeAdmin.displayName,
            approvedAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ).normalizedCopy()

        updateBookingStatus(
            booking = safeBooking,
            adminUser = safeAdmin,
            status = BookingStatus.Rejected,
            rejectReason = reason.trim(),
            seatAssignments = emptyList(),
            assignedSeatCount = 0,
            unassignedSeatCount = safeBooking.normalizedStudentCount,
            successMessage = "Đã từ chối booking phòng. Giáo viên sẽ nhận thông báo.",
            afterSuccess = {
                notificationRepository.createBookingRejectedNotification(
                    booking = rejectedBookingForNotification,
                    adminUser = safeAdmin,
                    reason = reason
                ) { _, _, _ -> }
            },
            callback = callback
        )
    }

    fun cancelBooking(
        booking: Booking,
        currentUser: User,
        reason: String = "",
        callback: (Boolean, String?) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()
        val safeBooking = booking.normalizedCopy()

        if (!safeBooking.canBeCancelledBy(safeUser)) {
            callback(false, "Bạn không có quyền hủy booking này.")
            return
        }

        val cancelledBookingForNotification = safeBooking.copy(
            status = BookingStatus.Cancelled.firestoreValue,
            rejectReason = reason.trim().ifBlank { "Booking đã được hủy." },
            updatedAt = Timestamp.now()
        ).normalizedCopy()

        updateBookingStatus(
            booking = safeBooking,
            adminUser = safeUser,
            status = BookingStatus.Cancelled,
            rejectReason = reason.trim(),
            seatAssignments = safeBooking.normalizedAssignments,
            assignedSeatCount = safeBooking.normalizedAssignedSeatCount,
            unassignedSeatCount = safeBooking.normalizedUnassignedSeatCount,
            successMessage = "Đã hủy booking phòng. Admin sẽ nhận thông báo.",
            afterSuccess = {
                notificationRepository.createBookingCancelledNotification(
                    booking = cancelledBookingForNotification,
                    currentUser = safeUser,
                    reason = reason
                ) { _, _ -> }
            },
            callback = callback
        )
    }

    fun deleteBooking(
        booking: Booking,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeAdmin = adminUser.normalizedCopy()
        val safeBooking = booking.normalizedCopy()

        if (!safeAdmin.isAdmin) {
            callback(false, "Bạn không có quyền xóa booking.")
            return
        }

        if (safeBooking.safeId.isBlank()) {
            callback(false, "Không tìm thấy booking để xóa.")
            return
        }

        try {
            bookingCollection
                .document(safeBooking.safeId)
                .delete()
                .addOnSuccessListener {
                    callback(true, "Đã xóa booking.")
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception))
                }
        } catch (exception: Exception) {
            callback(false, normalizeFirestoreError(exception))
        }
    }

    fun checkBookingConflict(
        targetBooking: Booking,
        includePending: Boolean,
        callback: (Boolean, String?, Boolean) -> Unit
    ) {
        val safeTarget = targetBooking.normalizedCopy()

        if (safeTarget.safeRoomId.isBlank()) {
            callback(false, "Vui lòng chọn phòng máy.", false)
            return
        }

        if (!Booking.isValidTimeRange(safeTarget.startDate, safeTarget.endDate)) {
            callback(
                false,
                Booking.buildTimeRangeError(safeTarget.startDate, safeTarget.endDate),
                false
            )
            return
        }

        try {
            bookingCollection
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val bookings = snapshot.documents.mapNotNull { document ->
                            documentToBookingSafe(
                                documentId = document.id,
                                data = document.data.orEmpty()
                            )
                        }

                        val hasConflict = bookings.any { booking ->
                            val safeBooking = booking.normalizedCopy()

                            val statusAllowed = if (includePending) {
                                safeBooking.isPending || safeBooking.isApproved
                            } else {
                                safeBooking.isApproved
                            }

                            statusAllowed && safeBooking.overlapsWith(safeTarget)
                        }

                        callback(true, null, hasConflict)
                    } catch (exception: Exception) {
                        callback(
                            false,
                            normalizeFirestoreError(exception),
                            false
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        false
                    )
                }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                false
            )
        }
    }

    private fun validateRoomCapacityForBooking(
        booking: Booking,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()

        if (safeBooking.safeRoomId.isBlank()) {
            callback(false, "Vui lòng chọn phòng máy.")
            return
        }

        loadComputersForRoom(
            roomId = safeBooking.safeRoomId
        ) { success, message, computers ->
            if (!success) {
                callback(
                    false,
                    message ?: "Không thể kiểm tra số máy trong phòng."
                )
                return@loadComputersForRoom
            }

            val assignableCount = computers.count { computer ->
                isComputerAssignable(computer.status)
            }

            if (assignableCount <= 0) {
                callback(
                    false,
                    "Phòng ${safeBooking.displayRoomCode} chưa có máy khả dụng. Vui lòng thêm máy hoặc chọn phòng khác."
                )
                return@loadComputersForRoom
            }

            if (safeBooking.normalizedStudentCount > assignableCount) {
                callback(
                    false,
                    "Số lượng sinh viên (${safeBooking.normalizedStudentCount}) không được lớn hơn số máy khả dụng của phòng ${safeBooking.displayRoomCode} ($assignableCount máy)."
                )
                return@loadComputersForRoom
            }

            callback(true, null)
        }
    }

    private fun updateBookingStatus(
        booking: Booking,
        adminUser: User,
        status: BookingStatus,
        rejectReason: String,
        seatAssignments: List<BookingSeatAssignment>,
        assignedSeatCount: Int,
        unassignedSeatCount: Int,
        successMessage: String,
        afterSuccess: () -> Unit,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()
        val safeUser = adminUser.normalizedCopy()

        if (safeBooking.safeId.isBlank()) {
            callback(false, "Không tìm thấy booking để cập nhật.")
            return
        }

        try {
            val now = Timestamp.now()

            val cleanAssignments = seatAssignments
                .map { assignment -> assignment.normalizedCopy() }
                .filter { assignment -> assignment.seatLabel.isNotBlank() }

            val cleanAssignedCount = assignedSeatCount.coerceAtLeast(0)
            val cleanUnassignedCount = unassignedSeatCount.coerceAtLeast(0)

            val updateMap = mutableMapOf<String, Any?>(
                "status" to status.firestoreValue,
                "assignedSeatCount" to cleanAssignedCount,
                "unassignedSeatCount" to cleanUnassignedCount,
                "seatAssignments" to cleanAssignments.map { assignment -> seatAssignmentToMap(assignment) },
                "updatedAt" to now
            )

            when (status) {
                BookingStatus.Approved -> {
                    updateMap["approvedById"] = safeUser.uid
                    updateMap["approvedByEmail"] = safeUser.email
                    updateMap["approvedByName"] = safeUser.displayName
                    updateMap["approvedAt"] = now
                    updateMap["rejectReason"] = ""
                }

                BookingStatus.Rejected -> {
                    updateMap["approvedById"] = safeUser.uid
                    updateMap["approvedByEmail"] = safeUser.email
                    updateMap["approvedByName"] = safeUser.displayName
                    updateMap["approvedAt"] = now
                    updateMap["rejectReason"] = rejectReason.ifBlank { "Không đủ điều kiện duyệt." }
                }

                BookingStatus.Cancelled -> {
                    updateMap["rejectReason"] = rejectReason.ifBlank { "Booking đã được hủy." }
                }

                BookingStatus.Pending -> {
                    updateMap["rejectReason"] = ""
                }
            }

            bookingCollection
                .document(safeBooking.safeId)
                .set(
                    updateMap,
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    afterSuccess()
                    callback(true, successMessage)
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception))
                }
        } catch (exception: Exception) {
            callback(false, normalizeFirestoreError(exception))
        }
    }

    private fun buildSeatAssignmentsForBooking(
        booking: Booking,
        callback: (Boolean, String?, List<BookingSeatAssignment>, Int) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()

        if (safeBooking.safeRoomId.isBlank()) {
            callback(
                false,
                "Không tìm thấy phòng máy để phân chỗ.",
                emptyList(),
                safeBooking.normalizedStudentCount
            )
            return
        }

        loadComputersForRoom(
            roomId = safeBooking.safeRoomId
        ) { computerSuccess, computerMessage, computers ->
            if (!computerSuccess) {
                callback(
                    false,
                    computerMessage ?: "Không thể tải danh sách máy tính trong phòng.",
                    emptyList(),
                    safeBooking.normalizedStudentCount
                )
                return@loadComputersForRoom
            }

            val availableComputers = computers
                .filter { computer -> isComputerAssignable(computer.status) }
                .sortedWith(
                    compareBy<SeatComputerCandidate>(
                        { it.row },
                        { it.column },
                        { it.code.lowercase(Locale.getDefault()) }
                    )
                )

            if (availableComputers.size < safeBooking.normalizedStudentCount) {
                callback(
                    false,
                    "Không thể phân chỗ vì phòng ${safeBooking.displayRoomCode} chỉ có ${availableComputers.size} máy khả dụng, nhỏ hơn ${safeBooking.normalizedStudentCount} sinh viên.",
                    emptyList(),
                    safeBooking.normalizedStudentCount
                )
                return@loadComputersForRoom
            }

            loadStudentsForClass(
                className = safeBooking.displayClassName
            ) { studentSuccess, studentMessage, students ->
                if (!studentSuccess) {
                    callback(
                        false,
                        studentMessage ?: "Không thể tải danh sách sinh viên trong lớp.",
                        emptyList(),
                        safeBooking.normalizedStudentCount
                    )
                    return@loadStudentsForClass
                }

                val cleanStudents = students
                    .distinctBy { student ->
                        student.email.ifBlank { student.studentCode }.ifBlank { student.uid }
                    }
                    .filter { student ->
                        student.className.equals(
                            safeBooking.displayClassName,
                            ignoreCase = true
                        )
                    }

                val randomComputers = availableComputers.shuffled(Random(System.currentTimeMillis()))

                val assignmentLimit = minOf(
                    safeBooking.normalizedStudentCount,
                    cleanStudents.size,
                    randomComputers.size
                )

                val assignments = (0 until assignmentLimit).map { index ->
                    val student = cleanStudents[index]
                    val computer = randomComputers[index]

                    BookingSeatAssignment(
                        studentId = student.uid,
                        studentEmail = student.email,
                        studentName = student.name,
                        studentCode = student.studentCode,
                        className = safeBooking.displayClassName,
                        roomId = safeBooking.safeRoomId,
                        roomCode = safeBooking.displayRoomCode,
                        computerId = computer.id,
                        computerCode = computer.code,
                        computerName = computer.name,
                        row = computer.row,
                        column = computer.column,
                        seatLabel = buildSeatLabel(
                            row = computer.row,
                            column = computer.column,
                            computerCode = computer.code
                        ),
                        assignedAt = Timestamp.now()
                    ).normalizedCopy()
                }

                val unassignedCount = (safeBooking.normalizedStudentCount - assignments.size).coerceAtLeast(0)

                callback(
                    true,
                    null,
                    assignments,
                    unassignedCount
                )
            }
        }
    }

    private fun loadStudentsForClass(
        className: String,
        callback: (Boolean, String?, List<SeatStudentCandidate>) -> Unit
    ) {
        val cleanClassName = className.trim()

        if (cleanClassName.isBlank() || cleanClassName == "Chưa nhập lớp") {
            callback(true, null, emptyList())
            return
        }

        try {
            userCollection
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val students = snapshot.documents.mapNotNull { document ->
                            val data = document.data.orEmpty()
                            val role = readString(data["role"]).lowercase(Locale.getDefault())
                            val status = readString(data["status"]).lowercase(Locale.getDefault())

                            val studentClass = readFirstNonBlankString(
                                data = data,
                                keys = listOf(
                                    "className",
                                    "class",
                                    "lop",
                                    "studentClass",
                                    "department",
                                    "unit"
                                )
                            )

                            val isStudentRole = role == "student" ||
                                    role == "sinhvien" ||
                                    role == "sinh_vien" ||
                                    role == "sinh viên" ||
                                    role == "sv"

                            val isApproved = status == "approved" ||
                                    status == "đã duyệt" ||
                                    status == "da_duyet" ||
                                    status == "active" ||
                                    status == "enabled"

                            if (isStudentRole && isApproved && studentClass.equals(cleanClassName, ignoreCase = true)) {
                                SeatStudentCandidate(
                                    uid = readString(data["uid"]).ifBlank { document.id },
                                    email = readString(data["email"]).lowercase(Locale.getDefault()),
                                    name = readFirstNonBlankString(
                                        data = data,
                                        keys = listOf(
                                            "name",
                                            "fullName",
                                            "displayName",
                                            "hoTen",
                                            "hoten"
                                        )
                                    ).ifBlank { readString(data["email"]) },
                                    studentCode = readFirstNonBlankString(
                                        data = data,
                                        keys = listOf(
                                            "studentCode",
                                            "studentId",
                                            "code",
                                            "identifier",
                                            "mssv"
                                        )
                                    ),
                                    className = studentClass
                                )
                            } else {
                                null
                            }
                        }

                        callback(true, null, students)
                    } catch (exception: Exception) {
                        callback(false, normalizeFirestoreError(exception), emptyList())
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception), emptyList())
                }
        } catch (exception: Exception) {
            callback(false, normalizeFirestoreError(exception), emptyList())
        }
    }

    private fun loadComputersForRoom(
        roomId: String,
        callback: (Boolean, String?, List<SeatComputerCandidate>) -> Unit
    ) {
        val cleanRoomId = roomId.trim()

        if (cleanRoomId.isBlank()) {
            callback(true, null, emptyList())
            return
        }

        try {
            computerCollection
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val computers = snapshot.documents.mapNotNull { document ->
                            val data = document.data.orEmpty()
                            val computerRoomId = readString(data["roomId"])

                            if (computerRoomId != cleanRoomId) {
                                null
                            } else {
                                SeatComputerCandidate(
                                    id = readString(data["id"]).ifBlank { document.id },
                                    code = readFirstNonBlankString(
                                        data = data,
                                        keys = listOf(
                                            "code",
                                            "computerCode",
                                            "machineCode",
                                            "name"
                                        )
                                    ).ifBlank { document.id },
                                    name = readFirstNonBlankString(
                                        data = data,
                                        keys = listOf(
                                            "name",
                                            "computerName",
                                            "machineName",
                                            "code"
                                        )
                                    ).ifBlank { document.id },
                                    roomId = computerRoomId,
                                    roomCode = readFirstNonBlankString(
                                        data = data,
                                        keys = listOf(
                                            "roomCode",
                                            "roomName"
                                        )
                                    ),
                                    row = resolveComputerRow(data),
                                    column = resolveComputerColumn(data),
                                    status = readString(data["status"])
                                )
                            }
                        }

                        callback(true, null, computers)
                    } catch (exception: Exception) {
                        callback(false, normalizeFirestoreError(exception), emptyList())
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception), emptyList())
                }
        } catch (exception: Exception) {
            callback(false, normalizeFirestoreError(exception), emptyList())
        }
    }

    private fun canUserSeeBooking(
        user: User,
        booking: Booking
    ): Boolean {
        val safeUser = user.normalizedCopy()
        val safeBooking = booking.normalizedCopy()

        return when {
            safeUser.isAdmin -> true

            safeUser.isTeacher -> {
                safeBooking.displayTeacherEmail.equals(
                    safeUser.email,
                    ignoreCase = true
                ) ||
                        safeBooking.teacherId.isNotBlank() && safeBooking.teacherId == safeUser.uid
            }

            safeUser.isStudent -> {
                safeBooking.isVisibleForStudent(safeUser)
            }

            else -> false
        }
    }

    private fun validateBookingBeforeCreate(
        booking: Booking,
        teacher: User
    ): String? {
        val safeTeacher = teacher.normalizedCopy()
        val safeBooking = booking.normalizedCopy()

        return when {
            !safeTeacher.isTeacher && !safeTeacher.isAdmin -> {
                "Chỉ giáo viên mới có quyền tạo booking phòng."
            }

            safeBooking.safeRoomId.isBlank() -> {
                "Vui lòng chọn phòng máy."
            }

            safeBooking.displayClassName == "Chưa nhập lớp" -> {
                "Vui lòng nhập lớp học."
            }

            Booking.buildStudentCountError(safeBooking.normalizedStudentCount) != null -> {
                Booking.buildStudentCountError(safeBooking.normalizedStudentCount)
            }

            safeBooking.displaySubjectName == "Chưa nhập học phần" -> {
                "Vui lòng nhập học phần."
            }

            safeBooking.displayLessonTitle == "Chưa nhập học phần" -> {
                "Vui lòng nhập nội dung buổi học."
            }

            !Booking.isValidTimeRange(safeBooking.startDate, safeBooking.endDate) -> {
                Booking.buildTimeRangeError(safeBooking.startDate, safeBooking.endDate)
            }

            else -> null
        }
    }

    private fun buildBookingResult(
        bookings: List<Booking>
    ): BookingResult {
        val safeBookings = bookings.map { booking ->
            booking.normalizedCopy()
        }

        return BookingResult(
            bookings = safeBookings,
            total = safeBookings.size,
            pendingCount = safeBookings.count { it.isPending },
            approvedCount = safeBookings.count { it.isApproved },
            rejectedCount = safeBookings.count { it.isRejected },
            cancelledCount = safeBookings.count { it.isCancelled }
        )
    }

    private fun documentToBookingSafe(
        documentId: String,
        data: Map<String, Any>
    ): Booking? {
        return try {
            Booking(
                id = readString(data["id"]).ifBlank { documentId },
                roomId = readString(data["roomId"]),
                roomCode = readString(data["roomCode"]),
                roomName = readString(data["roomName"]),
                teacherId = readString(data["teacherId"]),
                teacherEmail = readString(data["teacherEmail"]),
                teacherName = readString(data["teacherName"]),
                className = readString(data["className"]),
                studentCount = readInt(data["studentCount"]).let { count ->
                    if (count <= 0) Booking.DEFAULT_STUDENT_COUNT else count
                },
                subjectName = readString(data["subjectName"]),
                lessonTitle = readString(data["lessonTitle"]),
                startTime = readTimestamp(data["startTime"]),
                endTime = readTimestamp(data["endTime"]),
                dateKey = readString(data["dateKey"]),
                weekKey = readString(data["weekKey"]),
                status = BookingStatus.fromRaw(readString(data["status"])).firestoreValue,
                note = readString(data["note"]),
                rejectReason = readString(data["rejectReason"]),
                assignedSeatCount = readInt(data["assignedSeatCount"]),
                unassignedSeatCount = readInt(data["unassignedSeatCount"]),
                seatAssignments = readSeatAssignments(data["seatAssignments"]),
                approvedById = readString(data["approvedById"]),
                approvedByEmail = readString(data["approvedByEmail"]),
                approvedByName = readString(data["approvedByName"]),
                approvedAt = readTimestamp(data["approvedAt"]),
                createdAt = readTimestamp(data["createdAt"]),
                updatedAt = readTimestamp(data["updatedAt"])
            ).normalizedCopy()
        } catch (exception: Exception) {
            null
        }
    }

    private fun bookingToMap(
        booking: Booking,
        includeCreatedAt: Boolean
    ): MutableMap<String, Any?> {
        val safeBooking = booking.normalizedCopy()

        val map = mutableMapOf<String, Any?>(
            "id" to safeBooking.safeId,
            "roomId" to safeBooking.safeRoomId,
            "roomCode" to safeBooking.displayRoomCode,
            "roomName" to safeBooking.displayRoomName,
            "teacherId" to safeBooking.teacherId,
            "teacherEmail" to safeBooking.displayTeacherEmail,
            "teacherName" to safeBooking.displayTeacherName,
            "className" to safeBooking.displayClassName,
            "studentCount" to safeBooking.normalizedStudentCount,
            "subjectName" to safeBooking.displaySubjectName,
            "lessonTitle" to safeBooking.displayLessonTitle,
            "startTime" to safeBooking.startTime,
            "endTime" to safeBooking.endTime,
            "dateKey" to safeBooking.safeDateKey,
            "weekKey" to safeBooking.safeWeekKey,
            "status" to safeBooking.statusEnum.firestoreValue,
            "note" to safeBooking.displayNote,
            "rejectReason" to safeBooking.rejectReason,
            "assignedSeatCount" to safeBooking.normalizedAssignedSeatCount,
            "unassignedSeatCount" to safeBooking.normalizedUnassignedSeatCount,
            "seatAssignments" to safeBooking.normalizedAssignments.map { assignment ->
                seatAssignmentToMap(assignment)
            },
            "approvedById" to safeBooking.approvedById,
            "approvedByEmail" to safeBooking.approvedByEmail,
            "approvedByName" to safeBooking.approvedByName,
            "approvedAt" to safeBooking.approvedAt,
            "updatedAt" to Timestamp.now()
        )

        if (includeCreatedAt) {
            map["createdAt"] = safeBooking.createdAt ?: Timestamp.now()
        }

        return map
    }

    private fun seatAssignmentToMap(
        assignment: BookingSeatAssignment
    ): Map<String, Any?> {
        val safeAssignment = assignment.normalizedCopy()

        return mapOf(
            "studentId" to safeAssignment.studentId,
            "studentEmail" to safeAssignment.studentEmail,
            "studentName" to safeAssignment.studentName,
            "studentCode" to safeAssignment.studentCode,
            "className" to safeAssignment.className,
            "roomId" to safeAssignment.roomId,
            "roomCode" to safeAssignment.roomCode,
            "computerId" to safeAssignment.computerId,
            "computerCode" to safeAssignment.computerCode,
            "computerName" to safeAssignment.computerName,
            "row" to safeAssignment.normalizedRow,
            "column" to safeAssignment.normalizedColumn,
            "seatLabel" to safeAssignment.displaySeatLabel,
            "assignedAt" to (safeAssignment.assignedAt ?: Timestamp.now())
        )
    }

    private fun readSeatAssignments(
        value: Any?
    ): List<BookingSeatAssignment> {
        val rawList = value as? List<*> ?: return emptyList()

        return rawList.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null

            BookingSeatAssignment(
                studentId = readString(map["studentId"]),
                studentEmail = readString(map["studentEmail"]),
                studentName = readString(map["studentName"]),
                studentCode = readString(map["studentCode"]),
                className = readString(map["className"]),
                roomId = readString(map["roomId"]),
                roomCode = readString(map["roomCode"]),
                computerId = readString(map["computerId"]),
                computerCode = readString(map["computerCode"]),
                computerName = readString(map["computerName"]),
                row = readInt(map["row"]),
                column = readInt(map["column"]),
                seatLabel = readString(map["seatLabel"]),
                assignedAt = readTimestamp(map["assignedAt"])
            ).normalizedCopy()
        }
    }

    private fun buildSeatLabel(
        row: Int,
        column: Int,
        computerCode: String
    ): String {
        val cleanRow = row.coerceAtLeast(0)
        val cleanColumn = column.coerceAtLeast(0)
        val cleanCode = computerCode.trim()

        return when {
            cleanRow > 0 && cleanColumn > 0 && cleanCode.isNotBlank() -> {
                "Hàng $cleanRow • Cột $cleanColumn • $cleanCode"
            }

            cleanRow > 0 && cleanColumn > 0 -> {
                "Hàng $cleanRow • Cột $cleanColumn"
            }

            cleanCode.isNotBlank() -> {
                cleanCode
            }

            else -> {
                "Chỗ ngồi đã phân"
            }
        }
    }

    private fun isComputerAssignable(
        status: String
    ): Boolean {
        val cleanStatus = status.trim().lowercase(Locale.getDefault())

        return cleanStatus.isBlank() ||
                cleanStatus == "available" ||
                cleanStatus == "free" ||
                cleanStatus == "idle" ||
                cleanStatus == "ready" ||
                cleanStatus == "normal" ||
                cleanStatus == "rảnh" ||
                cleanStatus == "san_sang" ||
                cleanStatus == "sẵn sàng" ||
                cleanStatus == "hoat_dong" ||
                cleanStatus == "hoạt động"
    }

    private fun buildApproveSuccessMessage(
        assignedCount: Int,
        unassignedCount: Int
    ): String {
        return when {
            assignedCount <= 0 -> {
                "Đã duyệt booking. Giáo viên và sinh viên liên quan sẽ nhận thông báo."
            }

            unassignedCount > 0 -> {
                "Đã duyệt booking và phân $assignedCount chỗ. Còn $unassignedCount sinh viên chưa có tài khoản. Giáo viên và sinh viên liên quan sẽ nhận thông báo."
            }

            else -> {
                "Đã duyệt booking và phân đủ $assignedCount chỗ ngồi. Giáo viên và sinh viên liên quan sẽ nhận thông báo."
            }
        }
    }

    private fun resolveComputerRow(
        data: Map<String, Any>
    ): Int {
        val value = readFirstPositiveInt(
            data = data,
            keys = listOf(
                "row",
                "seatRow",
                "positionRow",
                "mapRow"
            )
        )

        return value.coerceAtLeast(1)
    }

    private fun resolveComputerColumn(
        data: Map<String, Any>
    ): Int {
        val value = readFirstPositiveInt(
            data = data,
            keys = listOf(
                "column",
                "col",
                "seatColumn",
                "positionColumn",
                "mapColumn"
            )
        )

        return value.coerceAtLeast(1)
    }

    private fun readFirstPositiveInt(
        data: Map<String, Any>,
        keys: List<String>
    ): Int {
        for (key in keys) {
            val value = readInt(data[key])
            if (value > 0) {
                return value
            }
        }

        return 1
    }

    private fun readFirstNonBlankString(
        data: Map<String, Any>,
        keys: List<String>
    ): String {
        for (key in keys) {
            val value = readString(data[key])
            if (value.isNotBlank()) {
                return value
            }
        }

        return ""
    }

    private fun readString(
        value: Any?
    ): String {
        return when (value) {
            null -> ""
            is String -> value.trim()
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> value.toString().trim()
        }
    }

    private fun readInt(
        value: Any?
    ): Int {
        return when (value) {
            null -> 0
            is Int -> value
            is Long -> value.toInt()
            is Double -> value.toInt()
            is Float -> value.toInt()
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun readTimestamp(
        value: Any?
    ): Timestamp? {
        return when (value) {
            null -> null
            is Timestamp -> value
            is Date -> Timestamp(value)
            is Long -> Timestamp(Date(value))
            is Int -> Timestamp(Date(value.toLong()))
            is Double -> Timestamp(Date(value.toLong()))
            is String -> {
                val millis = value.trim().toLongOrNull()
                if (millis != null) {
                    Timestamp(Date(millis))
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun normalizeFirestoreError(
        exception: Exception
    ): String {
        val raw = exception.message.orEmpty().lowercase(Locale.getDefault())

        return when {
            raw.contains("permission") ||
                    raw.contains("missing or insufficient permissions") -> {
                "Bạn không có quyền thực hiện thao tác này."
            }

            raw.contains("network") ||
                    raw.contains("unavailable") ||
                    raw.contains("deadline") -> {
                "Không thể kết nối dữ liệu. Vui lòng thử lại."
            }

            raw.contains("failed_precondition") ||
                    raw.contains("index") -> {
                "Truy vấn dữ liệu chưa sẵn sàng. Vui lòng thử lại sau."
            }

            else -> {
                "Không thể hoàn thành thao tác. Vui lòng thử lại."
            }
        }
    }
}