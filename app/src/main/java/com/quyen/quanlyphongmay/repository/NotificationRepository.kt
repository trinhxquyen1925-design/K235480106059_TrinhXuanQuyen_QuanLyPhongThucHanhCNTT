package com.quyen.quanlyphongmay.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quyen.quanlyphongmay.model.User
import java.text.SimpleDateFormat
import java.util.Locale
import com.quyen.quanlyphongmay.model.Booking

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = AppNotificationType.System.firestoreValue,
    val targetRoute: String = "notifications",
    val targetUserIds: List<String> = emptyList(),
    val targetEmails: List<String> = emptyList(),
    val targetRoles: List<String> = emptyList(),
    val targetClassName: String = "",
    val readByUserIds: List<String> = emptyList(),
    val readByEmails: List<String> = emptyList(),
    val createdById: String = "",
    val createdByEmail: String = "",
    val createdByName: String = "",
    val createdByRole: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val safeId: String
        get() = id.trim()

    val typeEnum: AppNotificationType
        get() = AppNotificationType.fromRaw(type)

    val displayTimeText: String
        get() {
            val date = createdAt?.toDate() ?: return "Vừa xong"
            val now = System.currentTimeMillis()
            val diff = now - date.time

            if (diff < 60_000L) {
                return "Vừa xong"
            }

            if (diff < 3_600_000L) {
                val minute = (diff / 60_000L).coerceAtLeast(1L)
                return "$minute phút trước"
            }

            if (diff < 86_400_000L) {
                val hour = (diff / 3_600_000L).coerceAtLeast(1L)
                return "$hour giờ trước"
            }

            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        }

    fun normalizedCopy(): AppNotification {
        return copy(
            id = id.trim(),
            title = title.trim().ifBlank { "Thông báo" },
            message = message.trim(),
            type = AppNotificationType.fromRaw(type).firestoreValue,
            targetRoute = targetRoute.trim().ifBlank { "notifications" },
            targetUserIds = targetUserIds.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            targetEmails = targetEmails.map { it.trim().lowercase(Locale.getDefault()) }.filter { it.isNotBlank() }.distinct(),
            targetRoles = targetRoles.map { normalizeRole(it) }.filter { it.isNotBlank() }.distinct(),
            targetClassName = targetClassName.trim(),
            readByUserIds = readByUserIds.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            readByEmails = readByEmails.map { it.trim().lowercase(Locale.getDefault()) }.filter { it.isNotBlank() }.distinct(),
            createdById = createdById.trim(),
            createdByEmail = createdByEmail.trim().lowercase(Locale.getDefault()),
            createdByName = createdByName.trim(),
            createdByRole = normalizeRole(createdByRole)
        )
    }

    fun isVisibleTo(user: User): Boolean {
        val safeUser = user.normalizedCopy()
        val safeNotification = normalizedCopy()

        if (safeUser.uid.isBlank() && safeUser.email.isBlank()) {
            return false
        }

        val hasDirectTarget = safeNotification.targetUserIds.isNotEmpty() ||
                safeNotification.targetEmails.isNotEmpty()

        val matchedUserId = safeUser.uid.isNotBlank() &&
                safeNotification.targetUserIds.contains(safeUser.uid)

        val matchedEmail = safeUser.email.isNotBlank() &&
                safeNotification.targetEmails.contains(safeUser.email.lowercase(Locale.getDefault()))

        val matchedRole = safeNotification.targetRoles.isEmpty() ||
                safeNotification.targetRoles.contains(normalizeRole(safeUser.role))

        val matchedClass = safeNotification.targetClassName.isBlank() ||
                safeNotification.targetClassName.equals(
                    safeUser.displayDepartment,
                    ignoreCase = true
                )

        return if (hasDirectTarget) {
            matchedUserId || matchedEmail || (matchedRole && matchedClass)
        } else {
            matchedRole && matchedClass
        }
    }

    fun isReadBy(user: User): Boolean {
        val safeUser = user.normalizedCopy()
        val safeNotification = normalizedCopy()

        val readById = safeUser.uid.isNotBlank() &&
                safeNotification.readByUserIds.contains(safeUser.uid)

        val readByEmail = safeUser.email.isNotBlank() &&
                safeNotification.readByEmails.contains(safeUser.email.lowercase(Locale.getDefault()))

        return readById || readByEmail
    }

    fun matchesKeyword(keyword: String): Boolean {
        val cleanKeyword = keyword.trim().lowercase(Locale.getDefault())

        if (cleanKeyword.isBlank()) {
            return true
        }

        val source = listOf(
            title,
            message,
            type,
            targetRoute,
            targetClassName,
            createdByEmail,
            createdByName,
            createdByRole
        ).joinToString(" ").lowercase(Locale.getDefault())

        return source.contains(cleanKeyword)
    }

    companion object {
        const val COLLECTION = "notifications"

        const val FIELD_ID = "id"
        const val FIELD_TITLE = "title"
        const val FIELD_MESSAGE = "message"
        const val FIELD_TYPE = "type"
        const val FIELD_TARGET_ROUTE = "targetRoute"
        const val FIELD_TARGET_USER_IDS = "targetUserIds"
        const val FIELD_TARGET_EMAILS = "targetEmails"
        const val FIELD_TARGET_ROLES = "targetRoles"
        const val FIELD_TARGET_CLASS_NAME = "targetClassName"
        const val FIELD_READ_BY_USER_IDS = "readByUserIds"
        const val FIELD_READ_BY_EMAILS = "readByEmails"
        const val FIELD_CREATED_BY_ID = "createdById"
        const val FIELD_CREATED_BY_EMAIL = "createdByEmail"
        const val FIELD_CREATED_BY_NAME = "createdByName"
        const val FIELD_CREATED_BY_ROLE = "createdByRole"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"

        fun normalizeRole(raw: String): String {
            val value = raw.trim().lowercase(Locale.getDefault())

            return when (value) {
                "admin",
                "administrator",
                "quan_tri",
                "quản trị",
                "quan tri",
                "qtv" -> "admin"

                "teacher",
                "giaovien",
                "giao_vien",
                "giáo viên",
                "giangvien",
                "giang_vien",
                "giảng viên" -> "teacher"

                "student",
                "sinhvien",
                "sinh_vien",
                "sinh viên" -> "student"

                else -> value
            }
        }
    }
}

enum class AppNotificationType(
    val firestoreValue: String,
    val vietnameseName: String
) {
    Booking(
        firestoreValue = "booking",
        vietnameseName = "Booking"
    ),

    Seat(
        firestoreValue = "seat",
        vietnameseName = "Chỗ ngồi"
    ),

    Account(
        firestoreValue = "account",
        vietnameseName = "Tài khoản"
    ),

    Computer(
        firestoreValue = "computer",
        vietnameseName = "Máy tính"
    ),

    Room(
        firestoreValue = "room",
        vietnameseName = "Phòng máy"
    ),

    System(
        firestoreValue = "system",
        vietnameseName = "Hệ thống"
    );

    companion object {
        fun fromRaw(raw: String?): AppNotificationType {
            val value = raw
                .orEmpty()
                .trim()
                .lowercase(Locale.getDefault())

            return when (value) {
                "booking",
                "book",
                "lich",
                "lịch",
                "schedule" -> Booking

                "seat",
                "seating",
                "cho_ngoi",
                "chỗ ngồi",
                "seat_map" -> Seat

                "account",
                "user",
                "tai_khoan",
                "tài khoản" -> Account

                "computer",
                "machine",
                "may_tinh",
                "máy tính" -> Computer

                "room",
                "phong",
                "phong_may",
                "phòng máy" -> Room

                else -> System
            }
        }
    }
}

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val notificationCollection = firestore.collection(AppNotification.COLLECTION)

    data class NotificationFilter(
        val keyword: String = "",
        val onlyUnread: Boolean = false,
        val type: String = ""
    )

    data class NotificationResult(
        val notifications: List<AppNotification> = emptyList(),
        val total: Int = 0,
        val unreadCount: Int = 0,
        val bookingCount: Int = 0,
        val accountCount: Int = 0,
        val systemCount: Int = 0
    )

    fun getNotifications(
        currentUser: User,
        filter: NotificationFilter = NotificationFilter(),
        callback: (Boolean, String?, NotificationResult) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        if (safeUser.uid.isBlank() && safeUser.email.isBlank()) {
            callback(false, "Không tìm thấy tài khoản hiện tại.", NotificationResult())
            return
        }

        notificationCollection
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val notifications = snapshot.documents
                        .mapNotNull { document ->
                            documentToNotificationSafe(
                                documentId = document.id,
                                data = document.data.orEmpty()
                            )
                        }
                        .map { it.normalizedCopy() }
                        .filter { notification ->
                            notification.isVisibleTo(safeUser)
                        }
                        .filter { notification ->
                            if (filter.type.trim().isBlank()) {
                                true
                            } else {
                                notification.typeEnum == AppNotificationType.fromRaw(filter.type)
                            }
                        }
                        .filter { notification ->
                            notification.matchesKeyword(filter.keyword)
                        }
                        .filter { notification ->
                            if (filter.onlyUnread) {
                                !notification.isReadBy(safeUser)
                            } else {
                                true
                            }
                        }
                        .sortedWith(
                            compareByDescending<AppNotification> { notification ->
                                notification.createdAt?.toDate()?.time ?: 0L
                            }.thenByDescending { notification ->
                                notification.safeId
                            }
                        )

                    callback(
                        true,
                        null,
                        buildNotificationResult(
                            user = safeUser,
                            notifications = notifications
                        )
                    )
                } catch (exception: Exception) {
                    callback(false, normalizeFirestoreError(exception), NotificationResult())
                }
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception), NotificationResult())
            }
    }

    fun createSystemNotification(
        title: String,
        message: String,
        targetRoles: List<String>,
        targetUserIds: List<String> = emptyList(),
        targetEmails: List<String> = emptyList(),
        targetClassName: String = "",
        targetRoute: String = "notifications",
        createdBy: User,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeUser = createdBy.normalizedCopy()
        val safeTargetRoles = targetRoles
            .map { AppNotification.normalizeRole(it) }
            .filter { it.isNotBlank() }
            .distinct()

        if (!safeUser.isAdmin && !safeUser.isTeacher) {
            callback(false, "Bạn không có quyền tạo thông báo.", null)
            return
        }

        if (safeUser.isTeacher && safeTargetRoles.any { it != "student" }) {
            callback(false, "Giáo viên chỉ được gửi thông báo đến sinh viên.", null)
            return
        }

        if (title.trim().isBlank()) {
            callback(false, "Vui lòng nhập tiêu đề thông báo.", null)
            return
        }

        if (message.trim().isBlank()) {
            callback(false, "Vui lòng nhập nội dung thông báo.", null)
            return
        }

        if (
            safeTargetRoles.isEmpty() &&
            targetUserIds.none { it.trim().isNotBlank() } &&
            targetEmails.none { it.trim().isNotBlank() }
        ) {
            callback(false, "Vui lòng chọn người nhận thông báo.", null)
            return
        }

        createNotification(
            notification = AppNotification(
                title = title,
                message = message,
                type = AppNotificationType.System.firestoreValue,
                targetRoute = targetRoute,
                targetRoles = safeTargetRoles,
                targetUserIds = targetUserIds,
                targetEmails = targetEmails,
                targetClassName = targetClassName,
                createdById = safeUser.uid,
                createdByEmail = safeUser.email,
                createdByName = safeUser.displayName.ifBlank { safeUser.email },
                createdByRole = safeUser.role
            ),
            callback = callback
        )
    }

    fun createAccountRegisteredNotificationForAdmin(
        registeredUser: User,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeUser = registeredUser.normalizedCopy()

        createNotification(
            notification = AppNotification(
                title = "Tài khoản mới chờ duyệt",
                message = "${safeUser.displayName.ifBlank { safeUser.email }} vừa đăng ký tài khoản ${safeUser.roleNameVi}.",
                type = AppNotificationType.Account.firestoreValue,
                targetRoute = "account_approval",
                targetRoles = listOf("admin"),
                createdById = safeUser.uid,
                createdByEmail = safeUser.email,
                createdByName = safeUser.displayName.ifBlank { safeUser.email },
                createdByRole = safeUser.role
            ),
            callback = callback
        )
    }

    fun createAccountApprovedNotification(
        approvedUser: User,
        adminUser: User,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeUser = approvedUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()

        createNotification(
            notification = AppNotification(
                title = "Tài khoản đã được duyệt",
                message = "Tài khoản của bạn đã được quản trị viên duyệt. Bạn có thể sử dụng hệ thống.",
                type = AppNotificationType.Account.firestoreValue,
                targetRoute = "home",
                targetUserIds = listOf(safeUser.uid),
                targetEmails = listOf(safeUser.email),
                createdById = safeAdmin.uid,
                createdByEmail = safeAdmin.email,
                createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
                createdByRole = safeAdmin.role
            ),
            callback = callback
        )
    }

    fun createAccountRejectedNotification(
        rejectedUser: User,
        adminUser: User,
        reason: String,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeUser = rejectedUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val cleanReason = reason.trim().ifBlank { "Tài khoản chưa đủ điều kiện duyệt." }

        createNotification(
            notification = AppNotification(
                title = "Tài khoản bị từ chối",
                message = "Tài khoản của bạn đã bị từ chối. Lý do: $cleanReason",
                type = AppNotificationType.Account.firestoreValue,
                targetRoute = "notifications",
                targetUserIds = listOf(safeUser.uid),
                targetEmails = listOf(safeUser.email),
                createdById = safeAdmin.uid,
                createdByEmail = safeAdmin.email,
                createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
                createdByRole = safeAdmin.role
            ),
            callback = callback
        )
    }

    fun createBookingCreatedNotification(
        bookingId: String,
        bookingTitle: String,
        className: String,
        roomCode: String,
        teacherUser: User,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeTeacher = teacherUser.normalizedCopy()

        createNotification(
            notification = AppNotification(
                title = "Booking mới chờ duyệt",
                message = "${safeTeacher.displayName.ifBlank { safeTeacher.email }} vừa tạo booking ${bookingTitle.trim().ifBlank { bookingId }} cho lớp ${className.trim().ifBlank { "chưa rõ" }} tại phòng ${roomCode.trim().ifBlank { "chưa rõ" }}.",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "booking_approval",
                targetRoles = listOf("admin"),
                createdById = safeTeacher.uid,
                createdByEmail = safeTeacher.email,
                createdByName = safeTeacher.displayName.ifBlank { safeTeacher.email },
                createdByRole = safeTeacher.role
            ),
            callback = callback
        )
    }

    fun createBookingApprovedNotifications(
        bookingId: String,
        bookingTitle: String,
        className: String,
        roomCode: String,
        teacherUser: User,
        studentUsers: List<User>,
        adminUser: User,
        callback: (Boolean, String?, List<AppNotification>) -> Unit
    ) {
        val safeTeacher = teacherUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val safeStudents = studentUsers.map { it.normalizedCopy() }

        val targets = mutableListOf<Pair<List<String>, List<String>>>()

        if (safeTeacher.uid.isNotBlank() || safeTeacher.email.isNotBlank()) {
            targets.add(
                listOf(safeTeacher.uid) to listOf(safeTeacher.email)
            )
        }

        safeStudents.forEach { student ->
            targets.add(
                listOf(student.uid) to listOf(student.email)
            )
        }

        createManyNotifications(
            drafts = targets.map { target ->
                AppNotification(
                    title = "Booking đã được duyệt",
                    message = "Booking ${bookingTitle.trim().ifBlank { bookingId }} của lớp ${className.trim().ifBlank { "chưa rõ" }} tại phòng ${roomCode.trim().ifBlank { "chưa rõ" }} đã được duyệt.",
                    type = AppNotificationType.Booking.firestoreValue,
                    targetRoute = "schedule",
                    targetUserIds = target.first,
                    targetEmails = target.second,
                    createdById = safeAdmin.uid,
                    createdByEmail = safeAdmin.email,
                    createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
                    createdByRole = safeAdmin.role
                )
            },
            callback = callback
        )
    }

    fun createBookingRejectedNotification(
        bookingId: String,
        bookingTitle: String,
        className: String,
        teacherUser: User,
        adminUser: User,
        reason: String,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeTeacher = teacherUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val cleanReason = reason.trim().ifBlank { "Booking chưa đủ điều kiện duyệt." }

        createNotification(
            notification = AppNotification(
                title = "Booking bị từ chối",
                message = "Booking ${bookingTitle.trim().ifBlank { bookingId }} của lớp ${className.trim().ifBlank { "chưa rõ" }} bị từ chối. Lý do: $cleanReason",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "bookings",
                targetUserIds = listOf(safeTeacher.uid),
                targetEmails = listOf(safeTeacher.email),
                createdById = safeAdmin.uid,
                createdByEmail = safeAdmin.email,
                createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
                createdByRole = safeAdmin.role
            ),
            callback = callback
        )
    }

    fun createBookingCancelledNotification(
        bookingId: String,
        bookingTitle: String,
        className: String,
        teacherUser: User,
        cancelledBy: User,
        reason: String,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeTeacher = teacherUser.normalizedCopy()
        val safeCancelledBy = cancelledBy.normalizedCopy()
        val cleanReason = reason.trim().ifBlank { "Booking đã bị hủy." }

        createNotification(
            notification = AppNotification(
                title = "Booking bị hủy",
                message = "Booking ${bookingTitle.trim().ifBlank { bookingId }} của lớp ${className.trim().ifBlank { "chưa rõ" }} đã bị hủy. Lý do: $cleanReason",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "bookings",
                targetRoles = if (safeCancelledBy.isTeacher) listOf("admin") else emptyList(),
                targetUserIds = if (safeCancelledBy.isTeacher) emptyList() else listOf(safeTeacher.uid),
                targetEmails = if (safeCancelledBy.isTeacher) emptyList() else listOf(safeTeacher.email),
                createdById = safeCancelledBy.uid,
                createdByEmail = safeCancelledBy.email,
                createdByName = safeCancelledBy.displayName.ifBlank { safeCancelledBy.email },
                createdByRole = safeCancelledBy.role
            ),
            callback = callback
        )
    }

    fun createSeatAssignedNotification(
        title: String,
        message: String,
        studentUser: User,
        teacherUser: User,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeStudent = studentUser.normalizedCopy()
        val safeTeacher = teacherUser.normalizedCopy()

        createNotification(
            notification = AppNotification(
                title = title.trim().ifBlank { "Đã phân chỗ ngồi" },
                message = message.trim().ifBlank { "Bạn đã được phân chỗ ngồi trong phòng máy." },
                type = AppNotificationType.Seat.firestoreValue,
                targetRoute = "schedule",
                targetUserIds = listOf(safeStudent.uid),
                targetEmails = listOf(safeStudent.email),
                createdById = safeTeacher.uid,
                createdByEmail = safeTeacher.email,
                createdByName = safeTeacher.displayName.ifBlank { safeTeacher.email },
                createdByRole = safeTeacher.role
            ),
            callback = callback
        )
    }
    fun createBookingCreatedNotification(
        booking: Booking,
        teacher: User,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()
        val safeTeacher = teacher.normalizedCopy()

        createNotification(
            notification = AppNotification(
                title = "Booking mới chờ duyệt",
                message = "${safeTeacher.displayName.ifBlank { safeTeacher.email }} đã gửi booking phòng ${safeBooking.displayRoomCode} cho lớp ${safeBooking.displayClassName} vào ${safeBooking.displayScheduleLine}.",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "booking_approval",
                targetRoles = listOf("admin"),
                createdById = safeTeacher.uid,
                createdByEmail = safeTeacher.email,
                createdByName = safeTeacher.displayName.ifBlank { safeTeacher.email },
                createdByRole = safeTeacher.role
            ),
            callback = callback
        )
    }

    fun createBookingApprovedNotifications(
        booking: Booking,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()

        val teacherNotification = AppNotification(
            title = "Booking đã được duyệt",
            message = "Booking phòng ${safeBooking.displayRoomCode} cho lớp ${safeBooking.displayClassName} vào ${safeBooking.displayScheduleLine} đã được duyệt. Mở lịch để xem sơ đồ chỗ ngồi.",
            type = AppNotificationType.Booking.firestoreValue,
            targetRoute = "bookings",
            targetRoles = listOf("teacher"),
            targetUserIds = listOf(safeBooking.teacherId).filter { it.isNotBlank() },
            targetEmails = listOf(safeBooking.displayTeacherEmail).filter { it.isNotBlank() },
            createdById = safeAdmin.uid,
            createdByEmail = safeAdmin.email,
            createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
            createdByRole = safeAdmin.role
        )

        val studentNotification = AppNotification(
            title = "Bạn có lịch phòng máy",
            message = "Lớp ${safeBooking.displayClassName} có lịch học tại phòng ${safeBooking.displayRoomCode} vào ${safeBooking.displayScheduleLine}. Mở lịch để xem chỗ ngồi của bạn.",
            type = AppNotificationType.Booking.firestoreValue,
            targetRoute = "schedule",
            targetRoles = listOf("student"),
            targetClassName = safeBooking.displayClassName,
            createdById = safeAdmin.uid,
            createdByEmail = safeAdmin.email,
            createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
            createdByRole = safeAdmin.role
        )

        createManyNotifications(
            drafts = listOf(
                teacherNotification,
                studentNotification
            )
        ) { success, message, _ ->
            callback(success, message)
        }
    }

    fun createBookingRejectedNotification(
        booking: Booking,
        adminUser: User,
        reason: String,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val cleanReason = reason.trim().ifBlank {
            "Booking chưa đủ điều kiện duyệt."
        }

        createNotification(
            notification = AppNotification(
                title = "Booking bị từ chối",
                message = "Booking phòng ${safeBooking.displayRoomCode} cho lớp ${safeBooking.displayClassName} đã bị từ chối. Lý do: $cleanReason",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "bookings",
                targetRoles = listOf("teacher"),
                targetUserIds = listOf(safeBooking.teacherId).filter { it.isNotBlank() },
                targetEmails = listOf(safeBooking.displayTeacherEmail).filter { it.isNotBlank() },
                createdById = safeAdmin.uid,
                createdByEmail = safeAdmin.email,
                createdByName = safeAdmin.displayName.ifBlank { "Quản trị viên" },
                createdByRole = safeAdmin.role
            ),
            callback = callback
        )
    }

    fun createBookingCancelledNotification(
        booking: Booking,
        currentUser: User,
        reason: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeBooking = booking.normalizedCopy()
        val safeUser = currentUser.normalizedCopy()
        val cleanReason = reason.trim().ifBlank {
            "Booking đã bị hủy."
        }

        val notification = if (safeUser.isTeacher) {
            AppNotification(
                title = "Booking đã bị hủy",
                message = "Booking phòng ${safeBooking.displayRoomCode} cho lớp ${safeBooking.displayClassName} vào ${safeBooking.displayScheduleLine} đã bị giáo viên hủy. Lý do: $cleanReason",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "booking_approval",
                targetRoles = listOf("admin"),
                createdById = safeUser.uid,
                createdByEmail = safeUser.email,
                createdByName = safeUser.displayName.ifBlank { safeUser.email },
                createdByRole = safeUser.role
            )
        } else {
            AppNotification(
                title = "Booking đã bị hủy",
                message = "Booking phòng ${safeBooking.displayRoomCode} cho lớp ${safeBooking.displayClassName} vào ${safeBooking.displayScheduleLine} đã bị hủy. Lý do: $cleanReason",
                type = AppNotificationType.Booking.firestoreValue,
                targetRoute = "bookings",
                targetRoles = listOf("teacher"),
                targetUserIds = listOf(safeBooking.teacherId).filter { it.isNotBlank() },
                targetEmails = listOf(safeBooking.displayTeacherEmail).filter { it.isNotBlank() },
                createdById = safeUser.uid,
                createdByEmail = safeUser.email,
                createdByName = safeUser.displayName.ifBlank { safeUser.email },
                createdByRole = safeUser.role
            )
        }

        createNotification(
            notification = notification
        ) { success, message, _ ->
            callback(success, message)
        }
    }
    fun markNotificationRead(
        notification: AppNotification,
        currentUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeNotification = notification.normalizedCopy()
        val safeUser = currentUser.normalizedCopy()

        if (safeNotification.safeId.isBlank()) {
            callback(false, "Không tìm thấy thông báo.")
            return
        }

        val newReadByUserIds = safeNotification.readByUserIds
            .plus(safeUser.uid)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val newReadByEmails = safeNotification.readByEmails
            .plus(safeUser.email)
            .map { it.trim().lowercase(Locale.getDefault()) }
            .filter { it.isNotBlank() }
            .distinct()

        notificationCollection
            .document(safeNotification.safeId)
            .set(
                mapOf(
                    AppNotification.FIELD_READ_BY_USER_IDS to newReadByUserIds,
                    AppNotification.FIELD_READ_BY_EMAILS to newReadByEmails,
                    AppNotification.FIELD_UPDATED_AT to Timestamp.now()
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                callback(true, "Đã đánh dấu đã đọc.")
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception))
            }
    }

    fun markAllRead(
        currentUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        getNotifications(
            currentUser = safeUser,
            filter = NotificationFilter(onlyUnread = true)
        ) { success, message, result ->
            if (!success) {
                callback(false, message ?: "Không thể tải thông báo.")
                return@getNotifications
            }

            if (result.notifications.isEmpty()) {
                callback(true, "Không có thông báo chưa đọc.")
                return@getNotifications
            }

            val batch = firestore.batch()

            result.notifications.forEach { notification ->
                val safeNotification = notification.normalizedCopy()

                val newReadByUserIds = safeNotification.readByUserIds
                    .plus(safeUser.uid)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

                val newReadByEmails = safeNotification.readByEmails
                    .plus(safeUser.email)
                    .map { it.trim().lowercase(Locale.getDefault()) }
                    .filter { it.isNotBlank() }
                    .distinct()

                batch.set(
                    notificationCollection.document(safeNotification.safeId),
                    mapOf(
                        AppNotification.FIELD_READ_BY_USER_IDS to newReadByUserIds,
                        AppNotification.FIELD_READ_BY_EMAILS to newReadByEmails,
                        AppNotification.FIELD_UPDATED_AT to Timestamp.now()
                    ),
                    SetOptions.merge()
                )
            }

            batch
                .commit()
                .addOnSuccessListener {
                    callback(true, "Đã đánh dấu tất cả thông báo là đã đọc.")
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception))
                }
        }
    }

    fun createNotification(
        notification: AppNotification,
        callback: (Boolean, String?, AppNotification?) -> Unit
    ) {
        val draft = notification.normalizedCopy()

        if (draft.title.isBlank()) {
            callback(false, "Vui lòng nhập tiêu đề thông báo.", null)
            return
        }

        if (draft.message.isBlank()) {
            callback(false, "Vui lòng nhập nội dung thông báo.", null)
            return
        }

        val documentRef = if (draft.safeId.isNotBlank()) {
            notificationCollection.document(draft.safeId)
        } else {
            notificationCollection.document()
        }

        val now = Timestamp.now()
        val notificationToSave = draft.copy(
            id = documentRef.id,
            createdAt = draft.createdAt ?: now,
            updatedAt = now
        ).normalizedCopy()

        documentRef
            .set(
                notificationToMap(notificationToSave),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                callback(true, "Đã tạo thông báo.", notificationToSave)
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception), null)
            }
    }

    private fun createManyNotifications(
        drafts: List<AppNotification>,
        callback: (Boolean, String?, List<AppNotification>) -> Unit
    ) {
        val safeDrafts = drafts.map { it.normalizedCopy() }

        if (safeDrafts.isEmpty()) {
            callback(true, "Không có thông báo cần tạo.", emptyList())
            return
        }

        val batch = firestore.batch()
        val now = Timestamp.now()
        val savedNotifications = mutableListOf<AppNotification>()

        safeDrafts.forEach { draft ->
            val documentRef = notificationCollection.document()
            val notification = draft.copy(
                id = documentRef.id,
                createdAt = draft.createdAt ?: now,
                updatedAt = now
            ).normalizedCopy()

            batch.set(
                documentRef,
                notificationToMap(notification),
                SetOptions.merge()
            )

            savedNotifications.add(notification)
        }

        batch
            .commit()
            .addOnSuccessListener {
                callback(true, "Đã tạo ${savedNotifications.size} thông báo.", savedNotifications)
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception), emptyList())
            }
    }

    private fun buildNotificationResult(
        user: User,
        notifications: List<AppNotification>
    ): NotificationResult {
        val safeNotifications = notifications.map { it.normalizedCopy() }

        return NotificationResult(
            notifications = safeNotifications,
            total = safeNotifications.size,
            unreadCount = safeNotifications.count { !it.isReadBy(user) },
            bookingCount = safeNotifications.count { it.typeEnum == AppNotificationType.Booking || it.typeEnum == AppNotificationType.Seat },
            accountCount = safeNotifications.count { it.typeEnum == AppNotificationType.Account },
            systemCount = safeNotifications.count { it.typeEnum == AppNotificationType.System }
        )
    }

    private fun documentToNotificationSafe(
        documentId: String,
        data: Map<String, Any>
    ): AppNotification? {
        return try {
            AppNotification(
                id = readString(data[AppNotification.FIELD_ID]).ifBlank { documentId },
                title = readString(data[AppNotification.FIELD_TITLE]).ifBlank {
                    readString(data["subject"]).ifBlank { "Thông báo" }
                },
                message = readString(data[AppNotification.FIELD_MESSAGE]).ifBlank {
                    readString(data["content"])
                },
                type = readString(data[AppNotification.FIELD_TYPE]).ifBlank {
                    AppNotificationType.System.firestoreValue
                },
                targetRoute = readString(data[AppNotification.FIELD_TARGET_ROUTE]).ifBlank {
                    readString(data["route"]).ifBlank { "notifications" }
                },
                targetUserIds = readStringList(data[AppNotification.FIELD_TARGET_USER_IDS]),
                targetEmails = readStringList(data[AppNotification.FIELD_TARGET_EMAILS]),
                targetRoles = readStringList(data[AppNotification.FIELD_TARGET_ROLES]).ifEmpty {
                    readStringList(data["roles"])
                },
                targetClassName = readString(data[AppNotification.FIELD_TARGET_CLASS_NAME]).ifBlank {
                    readString(data["className"])
                },
                readByUserIds = readStringList(data[AppNotification.FIELD_READ_BY_USER_IDS]),
                readByEmails = readStringList(data[AppNotification.FIELD_READ_BY_EMAILS]),
                createdById = readString(data[AppNotification.FIELD_CREATED_BY_ID]),
                createdByEmail = readString(data[AppNotification.FIELD_CREATED_BY_EMAIL]),
                createdByName = readString(data[AppNotification.FIELD_CREATED_BY_NAME]).ifBlank {
                    readString(data["senderName"])
                },
                createdByRole = readString(data[AppNotification.FIELD_CREATED_BY_ROLE]),
                createdAt = readTimestamp(data[AppNotification.FIELD_CREATED_AT]),
                updatedAt = readTimestamp(data[AppNotification.FIELD_UPDATED_AT])
            ).normalizedCopy()
        } catch (exception: Exception) {
            null
        }
    }

    private fun notificationToMap(
        notification: AppNotification
    ): MutableMap<String, Any?> {
        val safeNotification = notification.normalizedCopy()

        return mutableMapOf(
            AppNotification.FIELD_ID to safeNotification.safeId,
            AppNotification.FIELD_TITLE to safeNotification.title,
            AppNotification.FIELD_MESSAGE to safeNotification.message,
            AppNotification.FIELD_TYPE to safeNotification.typeEnum.firestoreValue,
            AppNotification.FIELD_TARGET_ROUTE to safeNotification.targetRoute,
            AppNotification.FIELD_TARGET_USER_IDS to safeNotification.targetUserIds,
            AppNotification.FIELD_TARGET_EMAILS to safeNotification.targetEmails,
            AppNotification.FIELD_TARGET_ROLES to safeNotification.targetRoles,
            AppNotification.FIELD_TARGET_CLASS_NAME to safeNotification.targetClassName,
            AppNotification.FIELD_READ_BY_USER_IDS to safeNotification.readByUserIds,
            AppNotification.FIELD_READ_BY_EMAILS to safeNotification.readByEmails,
            AppNotification.FIELD_CREATED_BY_ID to safeNotification.createdById,
            AppNotification.FIELD_CREATED_BY_EMAIL to safeNotification.createdByEmail,
            AppNotification.FIELD_CREATED_BY_NAME to safeNotification.createdByName,
            AppNotification.FIELD_CREATED_BY_ROLE to safeNotification.createdByRole,
            AppNotification.FIELD_CREATED_AT to (safeNotification.createdAt ?: Timestamp.now()),
            AppNotification.FIELD_UPDATED_AT to Timestamp.now()
        )
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

    private fun readStringList(
        value: Any?
    ): List<String> {
        return when (value) {
            null -> emptyList()

            is List<*> -> {
                value.mapNotNull { item ->
                    readString(item).takeIf { it.isNotBlank() }
                }
            }

            is Array<*> -> {
                value.mapNotNull { item ->
                    readString(item).takeIf { it.isNotBlank() }
                }
            }

            is String -> {
                value.split(
                    ",",
                    ";",
                    "|"
                ).map { it.trim() }.filter { it.isNotBlank() }
            }

            else -> {
                listOf(readString(value)).filter { it.isNotBlank() }
            }
        }
    }

    private fun readTimestamp(
        value: Any?
    ): Timestamp? {
        return when (value) {
            null -> null
            is Timestamp -> value
            is java.util.Date -> Timestamp(value)
            is Long -> Timestamp(java.util.Date(value))
            is Int -> Timestamp(java.util.Date(value.toLong()))
            is Double -> Timestamp(java.util.Date(value.toLong()))
            is String -> {
                val millis = value.trim().toLongOrNull()
                if (millis != null) {
                    Timestamp(java.util.Date(millis))
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