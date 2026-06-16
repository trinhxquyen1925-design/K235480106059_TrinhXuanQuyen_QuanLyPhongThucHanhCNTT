package com.quyen.quanlyphongmay.core

import java.util.Locale

object AppAuthContract {

    const val SYSTEM_ADMIN_EMAIL = "k235480106059@tnut.edu.vn"

    object FirestoreCollections {
        const val USERS = "users"
        const val ROOMS = "rooms"
        const val COMPUTERS = "computers"
        const val BOOKINGS = "bookings"
        const val NOTIFICATIONS = "notifications"
        const val MACHINE_REPORTS = "machineReports"
    }

    object UserFields {
        const val ID = "id"
        const val UID = "uid"
        const val EMAIL = "email"
        const val FULL_NAME = "fullName"
        const val NAME = "name"
        const val IDENTIFIER = "identifier"
        const val STUDENT_CODE = "studentCode"
        const val TEACHER_CODE = "teacherCode"
        const val ROLE = "role"
        const val STATUS = "status"
        const val CLASS_NAME = "className"
        const val DEPARTMENT = "department"
        const val PHONE = "phone"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val APPROVED_AT = "approvedAt"
        const val APPROVED_BY = "approvedBy"
        const val LOCKED_AT = "lockedAt"
        const val LOCKED_BY = "lockedBy"
    }

    enum class Role(
        val firestoreValue: String,
        val vietnameseName: String,
        val priority: Int
    ) {
        Student(
            firestoreValue = "Student",
            vietnameseName = "Sinh viên",
            priority = 10
        ),
        Teacher(
            firestoreValue = "Teacher",
            vietnameseName = "Giáo viên",
            priority = 20
        ),
        Admin(
            firestoreValue = "Admin",
            vietnameseName = "Quản trị viên",
            priority = 30
        );

        companion object {
            fun fromRaw(raw: String?): Role {
                val normalized = raw.normalizeKey()

                return when (normalized) {
                    "student",
                    "sinhvien",
                    "sinh_vien",
                    "sv" -> Student

                    "teacher",
                    "giaovien",
                    "giao_vien",
                    "gv" -> Teacher

                    "admin",
                    "administrator",
                    "quantrivien",
                    "quan_tri_vien",
                    "qtv" -> Admin

                    else -> Student
                }
            }

            fun firestoreValueOf(raw: String?): String {
                return fromRaw(raw).firestoreValue
            }

            fun vietnameseNameOf(raw: String?): String {
                return fromRaw(raw).vietnameseName
            }

            fun canRegister(role: Role): Boolean {
                return role == Student || role == Teacher
            }
        }
    }

    enum class Status(
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
        Locked(
            firestoreValue = "locked",
            vietnameseName = "Đã khóa"
        );

        companion object {
            fun fromRaw(raw: String?): Status {
                val normalized = raw.normalizeKey()

                return when (normalized) {
                    "pending",
                    "waiting",
                    "wait",
                    "choduyet",
                    "cho_duyet",
                    "chờduyệt" -> Pending

                    "approved",
                    "approve",
                    "active",
                    "accepted",
                    "daduyet",
                    "da_duyet",
                    "đãduyệt" -> Approved

                    "rejected",
                    "reject",
                    "denied",
                    "tuchoi",
                    "tu_choi",
                    "từchối" -> Rejected

                    "locked",
                    "lock",
                    "blocked",
                    "disabled",
                    "khoa",
                    "khoá",
                    "dakhoa",
                    "da_khoa",
                    "đãkhóa",
                    "đãkhoá" -> Locked

                    else -> Pending
                }
            }

            fun firestoreValueOf(raw: String?): String {
                return fromRaw(raw).firestoreValue
            }

            fun vietnameseNameOf(raw: String?): String {
                return fromRaw(raw).vietnameseName
            }

            fun canAccessApp(raw: String?): Boolean {
                return fromRaw(raw) == Approved
            }
        }
    }

    fun normalizeEmail(email: String?): String {
        return email.orEmpty()
            .trim()
            .lowercase(Locale.getDefault())
    }

    fun isSystemAdminEmail(email: String?): Boolean {
        return normalizeEmail(email) == SYSTEM_ADMIN_EMAIL
    }

    fun resolveRoleForEmail(
        email: String?,
        rawRole: String?
    ): Role {
        return if (isSystemAdminEmail(email)) {
            Role.Admin
        } else {
            Role.fromRaw(rawRole)
        }
    }

    fun resolveStatusForEmail(
        email: String?,
        rawStatus: String?
    ): Status {
        return if (isSystemAdminEmail(email)) {
            Status.Approved
        } else {
            Status.fromRaw(rawStatus)
        }
    }

    fun normalizeRoleForFirestore(
        email: String?,
        rawRole: String?
    ): String {
        return resolveRoleForEmail(
            email = email,
            rawRole = rawRole
        ).firestoreValue
    }

    fun normalizeStatusForFirestore(
        email: String?,
        rawStatus: String?
    ): String {
        return resolveStatusForEmail(
            email = email,
            rawStatus = rawStatus
        ).firestoreValue
    }

    fun roleNameVi(rawRole: String?): String {
        return Role.vietnameseNameOf(rawRole)
    }

    fun statusNameVi(rawStatus: String?): String {
        return Status.vietnameseNameOf(rawStatus)
    }

    fun canUserAccessApp(
        email: String?,
        rawStatus: String?
    ): Boolean {
        return resolveStatusForEmail(
            email = email,
            rawStatus = rawStatus
        ) == Status.Approved
    }

    fun canManageUsers(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canApproveAccounts(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canManageRooms(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canManageComputers(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canApproveBookings(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canCreateBooking(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Teacher
    }

    fun canSendClassNotification(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Teacher
    }

    fun canSendSystemNotification(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canViewAllSystemSchedule(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun canExportData(rawRole: String?): Boolean {
        return Role.fromRaw(rawRole) == Role.Admin
    }

    fun normalizeAuthMessage(message: String?): String {
        val raw = message.orEmpty().trim()
        val lower = raw.lowercase(Locale.getDefault())

        return when {
            raw.isBlank() -> "Không thể hoàn thành thao tác."

            lower.contains("password is invalid") ||
                    lower.contains("invalid credential") ||
                    lower.contains("wrong-password") ||
                    lower.contains("invalid-credential") -> {
                "Email hoặc mật khẩu chưa chính xác."
            }

            lower.contains("there is no user record") ||
                    lower.contains("user-not-found") -> {
                "Tài khoản không tồn tại."
            }

            lower.contains("network") ||
                    lower.contains("unavailable") ||
                    lower.contains("timeout") -> {
                "Không thể kết nối dữ liệu. Vui lòng thử lại."
            }

            lower.contains("badly formatted") ||
                    lower.contains("invalid-email") -> {
                "Email không đúng định dạng."
            }

            lower.contains("already in use") ||
                    lower.contains("email-already-in-use") -> {
                "Email đã được sử dụng."
            }

            lower.contains("weak-password") -> {
                "Mật khẩu quá yếu."
            }

            lower.contains("too-many-requests") -> {
                "Bạn thao tác quá nhiều lần. Vui lòng thử lại sau."
            }

            lower.contains("permission") ||
                    lower.contains("permission-denied") -> {
                "Bạn không có quyền thực hiện thao tác này."
            }

            lower.contains("failed_precondition") ||
                    lower.contains("failed-precondition") ||
                    lower.contains("index") -> {
                "Dữ liệu cần được cấu hình thêm chỉ mục. Vui lòng kiểm tra Firestore."
            }

            lower.contains("firestore") -> {
                "Không thể tải dữ liệu hệ thống."
            }

            else -> raw
        }
    }

    fun buildAccountBlockedMessage(
        email: String?,
        rawStatus: String?
    ): String {
        if (isSystemAdminEmail(email)) {
            return ""
        }

        return when (Status.fromRaw(rawStatus)) {
            Status.Pending -> "Tài khoản đang chờ phê duyệt."
            Status.Rejected -> "Tài khoản của bạn đã bị từ chối."
            Status.Locked -> "Tài khoản của bạn đang bị khóa."
            Status.Approved -> ""
        }
    }

    private fun String?.normalizeKey(): String {
        return this.orEmpty()
            .trim()
            .lowercase(Locale.getDefault())
            .replace(" ", "")
            .replace("-", "_")
            .replace(".", "_")
    }
}