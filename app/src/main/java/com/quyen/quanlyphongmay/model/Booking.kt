package com.quyen.quanlyphongmay.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Booking(
    val id: String = "",
    val roomId: String = "",
    val roomCode: String = "",
    val roomName: String = "",
    val teacherId: String = "",
    val teacherEmail: String = "",
    val teacherName: String = "",
    val className: String = "",
    val studentCount: Int = DEFAULT_STUDENT_COUNT,
    val subjectName: String = "",
    val lessonTitle: String = "",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val dateKey: String = "",
    val weekKey: String = "",
    val status: String = BookingStatus.Pending.firestoreValue,
    val note: String = "",
    val rejectReason: String = "",
    val assignedSeatCount: Int = 0,
    val unassignedSeatCount: Int = 0,
    val seatAssignments: List<BookingSeatAssignment> = emptyList(),
    val approvedById: String = "",
    val approvedByEmail: String = "",
    val approvedByName: String = "",
    val approvedAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val safeId: String
        get() = id.trim()

    val safeRoomId: String
        get() = roomId.trim()

    val displayRoomCode: String
        get() = roomCode.trim().ifBlank { "Chưa rõ phòng" }

    val displayRoomName: String
        get() = roomName.trim().ifBlank { displayRoomCode }

    val displayTeacherName: String
        get() = teacherName.trim().ifBlank { teacherEmail.trim().ifBlank { "Giảng viên" } }

    val displayTeacherEmail: String
        get() = teacherEmail.trim()

    val displayClassName: String
        get() = className.trim().ifBlank { "Chưa nhập lớp" }

    val normalizedStudentCount: Int
        get() = studentCount.coerceIn(MIN_STUDENT_COUNT, MAX_STUDENT_COUNT)

    val displayStudentCount: String
        get() = "$normalizedStudentCount sinh viên"

    val displaySubjectName: String
        get() = subjectName.trim().ifBlank { "Chưa nhập học phần" }

    val displayLessonTitle: String
        get() = lessonTitle.trim().ifBlank { displaySubjectName }

    val displayNote: String
        get() = note.trim()

    val statusEnum: BookingStatus
        get() = BookingStatus.fromRaw(status)

    val statusNameVi: String
        get() = statusEnum.vietnameseName

    val isPending: Boolean
        get() = statusEnum == BookingStatus.Pending

    val isApproved: Boolean
        get() = statusEnum == BookingStatus.Approved

    val isRejected: Boolean
        get() = statusEnum == BookingStatus.Rejected

    val isCancelled: Boolean
        get() = statusEnum == BookingStatus.Cancelled

    val startDate: Date?
        get() = startTime?.toDate()

    val endDate: Date?
        get() = endTime?.toDate()

    val safeDateKey: String
        get() = dateKey.trim().ifBlank {
            startDate?.let { formatDateKey(it) }.orEmpty()
        }

    val safeWeekKey: String
        get() = weekKey.trim().ifBlank {
            startDate?.let { formatWeekKey(it) }.orEmpty()
        }

    val displayDate: String
        get() = startDate?.let { formatVietnameseDate(it) } ?: "Chưa chọn ngày"

    val displayTimeRange: String
        get() {
            val start = startDate
            val end = endDate

            if (start == null || end == null) {
                return "Chưa chọn giờ"
            }

            return "${formatTime(start)} - ${formatTime(end)}"
        }

    val displayScheduleLine: String
        get() = "$displayDate • $displayTimeRange"

    val normalizedAssignments: List<BookingSeatAssignment>
        get() = seatAssignments
            .map { assignment -> assignment.normalizedCopy() }
            .filter { assignment -> assignment.seatLabel.isNotBlank() }

    val normalizedAssignedSeatCount: Int
        get() = if (assignedSeatCount > 0) {
            assignedSeatCount
        } else {
            normalizedAssignments.size
        }.coerceAtLeast(0)

    val normalizedUnassignedSeatCount: Int
        get() {
            val explicitValue = unassignedSeatCount.coerceAtLeast(0)
            if (explicitValue > 0) {
                return explicitValue
            }

            return (normalizedStudentCount - normalizedAssignedSeatCount).coerceAtLeast(0)
        }

    val assignmentSummary: String
        get() {
            return when {
                normalizedAssignedSeatCount <= 0 -> {
                    "Chưa phân chỗ ngồi"
                }

                normalizedUnassignedSeatCount > 0 -> {
                    "Đã phân $normalizedAssignedSeatCount chỗ • còn ${normalizedUnassignedSeatCount} sinh viên chưa có tài khoản hoặc chưa đủ máy"
                }

                else -> {
                    "Đã phân đủ $normalizedAssignedSeatCount chỗ"
                }
            }
        }

    val searchText: String
        get() = listOf(
            safeId,
            displayRoomCode,
            displayRoomName,
            displayTeacherName,
            displayTeacherEmail,
            displayClassName,
            normalizedStudentCount.toString(),
            displaySubjectName,
            displayLessonTitle,
            statusNameVi,
            statusEnum.firestoreValue,
            displayDate,
            displayTimeRange,
            displayNote,
            assignmentSummary
        ).joinToString(" ").lowercase(Locale.getDefault())

    fun normalizedCopy(): Booking {
        val cleanStart = startTime
        val cleanEnd = endTime
        val generatedDateKey = cleanStart?.toDate()?.let { formatDateKey(it) }.orEmpty()
        val generatedWeekKey = cleanStart?.toDate()?.let { formatWeekKey(it) }.orEmpty()

        val cleanAssignments = seatAssignments
            .map { assignment -> assignment.normalizedCopy() }
            .filter { assignment -> assignment.seatLabel.isNotBlank() }

        val cleanStudentCount = studentCount.coerceIn(MIN_STUDENT_COUNT, MAX_STUDENT_COUNT)
        val cleanAssignedCount = if (assignedSeatCount > 0) assignedSeatCount else cleanAssignments.size
        val cleanUnassignedCount = if (unassignedSeatCount > 0) {
            unassignedSeatCount
        } else {
            cleanStudentCount - cleanAssignedCount
        }.coerceAtLeast(0)

        return copy(
            id = id.trim(),
            roomId = roomId.trim(),
            roomCode = roomCode.trim(),
            roomName = roomName.trim(),
            teacherId = teacherId.trim(),
            teacherEmail = teacherEmail.trim().lowercase(Locale.getDefault()),
            teacherName = teacherName.trim(),
            className = className.trim(),
            studentCount = cleanStudentCount,
            subjectName = subjectName.trim(),
            lessonTitle = lessonTitle.trim(),
            startTime = cleanStart,
            endTime = cleanEnd,
            dateKey = dateKey.trim().ifBlank { generatedDateKey },
            weekKey = weekKey.trim().ifBlank { generatedWeekKey },
            status = statusEnum.firestoreValue,
            note = note.trim(),
            rejectReason = rejectReason.trim(),
            assignedSeatCount = cleanAssignedCount,
            unassignedSeatCount = cleanUnassignedCount,
            seatAssignments = cleanAssignments,
            approvedById = approvedById.trim(),
            approvedByEmail = approvedByEmail.trim().lowercase(Locale.getDefault()),
            approvedByName = approvedByName.trim(),
            approvedAt = approvedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun matchesKeyword(keyword: String): Boolean {
        val cleanKeyword = keyword.trim().lowercase(Locale.getDefault())

        if (cleanKeyword.isBlank()) {
            return true
        }

        return searchText.contains(cleanKeyword)
    }

    fun overlapsWith(other: Booking): Boolean {
        val thisStart = startDate ?: return false
        val thisEnd = endDate ?: return false
        val otherStart = other.startDate ?: return false
        val otherEnd = other.endDate ?: return false

        if (safeRoomId.isBlank() || other.safeRoomId.isBlank()) {
            return false
        }

        if (safeRoomId != other.safeRoomId) {
            return false
        }

        if (safeId.isNotBlank() && safeId == other.safeId) {
            return false
        }

        return thisStart.before(otherEnd) && thisEnd.after(otherStart)
    }

    fun canBeApproved(): Boolean {
        return isPending && startDate != null && endDate != null && safeRoomId.isNotBlank()
    }

    fun canBeRejected(): Boolean {
        return isPending
    }

    fun canBeCancelledBy(user: User): Boolean {
        val currentUser = user.normalizedCopy()

        return when {
            currentUser.isAdmin -> isPending || isApproved
            currentUser.isTeacher -> {
                val sameTeacher = teacherEmail.equals(currentUser.email, ignoreCase = true) ||
                        teacherId.isNotBlank() && teacherId == currentUser.uid

                sameTeacher && isPending
            }

            else -> false
        }
    }

    fun assignmentForUser(user: User): BookingSeatAssignment? {
        val safeUser = user.normalizedCopy()

        if (!safeUser.isStudent) {
            return null
        }

        val userEmail = safeUser.email.trim().lowercase(Locale.getDefault())
        val userCode = safeUser.displayIdentifier.trim().lowercase(Locale.getDefault())
        val userClass = safeUser.displayDepartment.trim().lowercase(Locale.getDefault())

        return normalizedAssignments.firstOrNull { assignment ->
            val assignmentEmail = assignment.studentEmail.trim().lowercase(Locale.getDefault())
            val assignmentCode = assignment.studentCode.trim().lowercase(Locale.getDefault())
            val assignmentClass = assignment.className.trim().lowercase(Locale.getDefault())

            assignmentEmail.isNotBlank() && assignmentEmail == userEmail ||
                    assignmentCode.isNotBlank() && assignmentCode == userCode ||
                    assignmentClass.isNotBlank() && userClass.isNotBlank() && assignmentClass == userClass && assignment.studentEmail.isBlank()
        }
    }

    fun isVisibleForStudent(user: User): Boolean {
        val safeUser = user.normalizedCopy()

        if (!safeUser.isStudent || !isApproved) {
            return false
        }

        val userClass = safeUser.displayDepartment.trim().lowercase(Locale.getDefault())
        val bookingClass = displayClassName.trim().lowercase(Locale.getDefault())

        return userClass.isNotBlank() && bookingClass.isNotBlank() && userClass == bookingClass
    }

    companion object {
        const val COLLECTION_NAME = "bookings"

        const val MIN_BOOKING_MINUTES = 15
        const val MAX_BOOKING_HOURS = 8

        const val MIN_STUDENT_COUNT = 1
        const val MAX_STUDENT_COUNT = 300
        const val DEFAULT_STUDENT_COUNT = 1

        fun newDraft(
            teacher: User,
            room: ComputerRoom,
            className: String,
            studentCount: Int,
            subjectName: String,
            lessonTitle: String,
            startDate: Date,
            endDate: Date,
            note: String
        ): Booking {
            val safeTeacher = teacher.normalizedCopy()
            val safeRoom = room.normalizedCopy()

            return Booking(
                id = "",
                roomId = safeRoom.safeId,
                roomCode = safeRoom.displayCode,
                roomName = safeRoom.displayName,
                teacherId = safeTeacher.uid,
                teacherEmail = safeTeacher.email,
                teacherName = safeTeacher.displayName,
                className = className.trim(),
                studentCount = studentCount.coerceIn(MIN_STUDENT_COUNT, MAX_STUDENT_COUNT),
                subjectName = subjectName.trim(),
                lessonTitle = lessonTitle.trim(),
                startTime = Timestamp(startDate),
                endTime = Timestamp(endDate),
                dateKey = formatDateKey(startDate),
                weekKey = formatWeekKey(startDate),
                status = BookingStatus.Pending.firestoreValue,
                note = note.trim(),
                rejectReason = "",
                assignedSeatCount = 0,
                unassignedSeatCount = studentCount.coerceIn(MIN_STUDENT_COUNT, MAX_STUDENT_COUNT),
                seatAssignments = emptyList(),
                approvedById = "",
                approvedByEmail = "",
                approvedByName = "",
                approvedAt = null,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).normalizedCopy()
        }

        fun newDraft(
            teacher: User,
            room: ComputerRoom,
            className: String,
            subjectName: String,
            lessonTitle: String,
            startDate: Date,
            endDate: Date,
            note: String
        ): Booking {
            return newDraft(
                teacher = teacher,
                room = room,
                className = className,
                studentCount = DEFAULT_STUDENT_COUNT,
                subjectName = subjectName,
                lessonTitle = lessonTitle,
                startDate = startDate,
                endDate = endDate,
                note = note
            )
        }

        fun empty(): Booking {
            return Booking()
        }

        fun statusOptions(): List<BookingStatus> {
            return listOf(
                BookingStatus.Pending,
                BookingStatus.Approved,
                BookingStatus.Rejected,
                BookingStatus.Cancelled
            )
        }

        fun generateLocalId(): String {
            return UUID.randomUUID().toString()
        }

        fun formatDateKey(date: Date): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }

        fun formatWeekKey(date: Date): String {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = date
            val year = calendar.get(Calendar.YEAR)
            val week = calendar.get(Calendar.WEEK_OF_YEAR).toString().padStart(2, '0')
            return "$year-W$week"
        }

        fun formatVietnameseDate(date: Date): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }

        fun formatTime(date: Date): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }

        fun isValidTimeRange(startDate: Date?, endDate: Date?): Boolean {
            if (startDate == null || endDate == null) {
                return false
            }

            if (!endDate.after(startDate)) {
                return false
            }

            val durationMillis = endDate.time - startDate.time
            val durationMinutes = durationMillis / 60_000L
            val durationHours = durationMinutes / 60.0

            return durationMinutes >= MIN_BOOKING_MINUTES && durationHours <= MAX_BOOKING_HOURS
        }

        fun buildTimeRangeError(startDate: Date?, endDate: Date?): String? {
            if (startDate == null || endDate == null) {
                return "Vui lòng chọn đầy đủ ngày giờ bắt đầu và kết thúc."
            }

            if (!endDate.after(startDate)) {
                return "Giờ kết thúc phải sau giờ bắt đầu."
            }

            val durationMillis = endDate.time - startDate.time
            val durationMinutes = durationMillis / 60_000L
            val durationHours = durationMinutes / 60.0

            return when {
                durationMinutes < MIN_BOOKING_MINUTES -> {
                    "Thời lượng booking tối thiểu là $MIN_BOOKING_MINUTES phút."
                }

                durationHours > MAX_BOOKING_HOURS -> {
                    "Thời lượng booking tối đa là $MAX_BOOKING_HOURS giờ."
                }

                else -> null
            }
        }

        fun buildStudentCountError(studentCount: Int?): String? {
            if (studentCount == null) {
                return "Vui lòng nhập số lượng sinh viên."
            }

            return when {
                studentCount < MIN_STUDENT_COUNT -> {
                    "Số lượng sinh viên phải từ $MIN_STUDENT_COUNT trở lên."
                }

                studentCount > MAX_STUDENT_COUNT -> {
                    "Số lượng sinh viên tối đa là $MAX_STUDENT_COUNT."
                }

                else -> null
            }
        }
    }
}

data class BookingSeatAssignment(
    val studentId: String = "",
    val studentEmail: String = "",
    val studentName: String = "",
    val studentCode: String = "",
    val className: String = "",
    val roomId: String = "",
    val roomCode: String = "",
    val computerId: String = "",
    val computerCode: String = "",
    val computerName: String = "",
    val row: Int = 0,
    val column: Int = 0,
    val seatLabel: String = "",
    val assignedAt: Timestamp? = null
) {
    val normalizedRow: Int
        get() = row.coerceAtLeast(0)

    val normalizedColumn: Int
        get() = column.coerceAtLeast(0)

    val displayStudentName: String
        get() = studentName.trim().ifBlank {
            studentEmail.trim().ifBlank {
                studentCode.trim().ifBlank {
                    "Sinh viên chưa có tài khoản"
                }
            }
        }

    val displayComputerCode: String
        get() = computerCode.trim().ifBlank { computerName.trim().ifBlank { "Máy chưa rõ" } }

    val displayRoomCode: String
        get() = roomCode.trim().ifBlank { "Phòng chưa rõ" }

    val displaySeatLabel: String
        get() = seatLabel.trim().ifBlank {
            if (normalizedRow > 0 && normalizedColumn > 0) {
                "Hàng $normalizedRow • Cột $normalizedColumn"
            } else {
                "Chưa có vị trí"
            }
        }

    fun normalizedCopy(): BookingSeatAssignment {
        val cleanRow = row.coerceAtLeast(0)
        val cleanColumn = column.coerceAtLeast(0)

        return copy(
            studentId = studentId.trim(),
            studentEmail = studentEmail.trim().lowercase(Locale.getDefault()),
            studentName = studentName.trim(),
            studentCode = studentCode.trim(),
            className = className.trim(),
            roomId = roomId.trim(),
            roomCode = roomCode.trim(),
            computerId = computerId.trim(),
            computerCode = computerCode.trim(),
            computerName = computerName.trim(),
            row = cleanRow,
            column = cleanColumn,
            seatLabel = seatLabel.trim().ifBlank {
                if (cleanRow > 0 && cleanColumn > 0) {
                    "Hàng $cleanRow • Cột $cleanColumn"
                } else {
                    ""
                }
            },
            assignedAt = assignedAt
        )
    }

    companion object {
        fun fromComputer(
            student: User?,
            className: String,
            roomId: String,
            roomCode: String,
            computerId: String,
            computerCode: String,
            computerName: String,
            row: Int,
            column: Int
        ): BookingSeatAssignment {
            val safeStudent = student?.normalizedCopy()
            val cleanRow = row.coerceAtLeast(0)
            val cleanColumn = column.coerceAtLeast(0)

            return BookingSeatAssignment(
                studentId = safeStudent?.uid.orEmpty(),
                studentEmail = safeStudent?.email.orEmpty(),
                studentName = safeStudent?.displayName.orEmpty(),
                studentCode = safeStudent?.displayIdentifier.orEmpty(),
                className = className.trim(),
                roomId = roomId.trim(),
                roomCode = roomCode.trim(),
                computerId = computerId.trim(),
                computerCode = computerCode.trim(),
                computerName = computerName.trim(),
                row = cleanRow,
                column = cleanColumn,
                seatLabel = if (cleanRow > 0 && cleanColumn > 0) {
                    "Hàng $cleanRow • Cột $cleanColumn"
                } else {
                    ""
                },
                assignedAt = Timestamp.now()
            ).normalizedCopy()
        }
    }
}

enum class BookingStatus(
    val firestoreValue: String,
    val vietnameseName: String
) {
    Pending(
        firestoreValue = "pending",
        vietnameseName = "Chờ duyệt"
    ),

    Approved(
        firestoreValue = "approved",
        vietnameseName = "Đã duyệt"
    ),

    Rejected(
        firestoreValue = "rejected",
        vietnameseName = "Từ chối"
    ),

    Cancelled(
        firestoreValue = "cancelled",
        vietnameseName = "Đã hủy"
    );

    companion object {
        fun fromRaw(raw: String?): BookingStatus {
            val value = raw.orEmpty().trim().lowercase(Locale.getDefault())

            return when (value) {
                "pending",
                "wait",
                "waiting",
                "cho_duyet",
                "chờ duyệt",
                "dang_cho",
                "đang chờ" -> Pending

                "approved",
                "approve",
                "accepted",
                "accept",
                "duyet",
                "da_duyet",
                "đã duyệt" -> Approved

                "rejected",
                "reject",
                "deny",
                "denied",
                "tu_choi",
                "từ chối" -> Rejected

                "cancelled",
                "canceled",
                "cancel",
                "huy",
                "da_huy",
                "đã hủy" -> Cancelled

                else -> Pending
            }
        }
    }
}

enum class BookingAction {
    Create,
    Approve,
    Reject,
    Cancel,
    Delete,
    AssignSeats,
    NotifyStudents
}