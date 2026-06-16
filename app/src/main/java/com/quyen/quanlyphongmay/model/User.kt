package com.quyen.quanlyphongmay.model

import com.google.firebase.Timestamp
import com.quyen.quanlyphongmay.core.AppAuthContract

data class User(
    val id: String = "",
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val name: String = "",
    val identifier: String = "",
    val studentCode: String = "",
    val teacherCode: String = "",
    val role: String = AppAuthContract.Role.Student.firestoreValue,
    val status: String = AppAuthContract.Status.Pending.firestoreValue,
    val className: String = "",
    val department: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val approvedAt: Timestamp? = null,
    val approvedBy: String = "",
    val rejectedAt: Timestamp? = null,
    val rejectedBy: String = "",
    val rejectedReason: String = "",
    val lockedAt: Timestamp? = null,
    val lockedBy: String = "",
    val unlockedAt: Timestamp? = null,
    val unlockedBy: String = "",
    val lastLoginAt: Timestamp? = null
) {

    val safeId: String
        get() = when {
            id.isNotBlank() -> id
            uid.isNotBlank() -> uid
            else -> ""
        }

    val displayName: String
        get() = when {
            fullName.isNotBlank() -> fullName
            name.isNotBlank() -> name
            email.isNotBlank() -> email.substringBefore("@")
            else -> "Người dùng"
        }

    val displayIdentifier: String
        get() {
            val normalizedRole = AppAuthContract.Role.fromRaw(role)

            return when {
                identifier.isNotBlank() -> identifier
                normalizedRole == AppAuthContract.Role.Student && studentCode.isNotBlank() -> studentCode
                normalizedRole == AppAuthContract.Role.Teacher && teacherCode.isNotBlank() -> teacherCode
                else -> ""
            }
        }

    val displayDepartment: String
        get() {
            val normalizedRole = AppAuthContract.Role.fromRaw(role)

            return when {
                normalizedRole == AppAuthContract.Role.Student && className.isNotBlank() -> className
                department.isNotBlank() -> department
                className.isNotBlank() -> className
                else -> ""
            }
        }

    val roleEnum: AppAuthContract.Role
        get() = AppAuthContract.resolveRoleForEmail(
            email = email,
            rawRole = role
        )

    val statusEnum: AppAuthContract.Status
        get() = AppAuthContract.resolveStatusForEmail(
            email = email,
            rawStatus = status
        )

    val roleValue: String
        get() = roleEnum.firestoreValue

    val statusValue: String
        get() = statusEnum.firestoreValue

    val roleNameVi: String
        get() = roleEnum.vietnameseName

    val statusNameVi: String
        get() = statusEnum.vietnameseName

    val isSystemAdmin: Boolean
        get() = AppAuthContract.isSystemAdminEmail(email)

    val isAdmin: Boolean
        get() = roleEnum == AppAuthContract.Role.Admin

    val isTeacher: Boolean
        get() = roleEnum == AppAuthContract.Role.Teacher

    val isStudent: Boolean
        get() = roleEnum == AppAuthContract.Role.Student

    val isPending: Boolean
        get() = statusEnum == AppAuthContract.Status.Pending

    val isApproved: Boolean
        get() = statusEnum == AppAuthContract.Status.Approved

    val isRejected: Boolean
        get() = statusEnum == AppAuthContract.Status.Rejected

    val isLocked: Boolean
        get() = statusEnum == AppAuthContract.Status.Locked

    val canAccessApp: Boolean
        get() = AppAuthContract.canUserAccessApp(
            email = email,
            rawStatus = status
        )

    val canManageUsers: Boolean
        get() = AppAuthContract.canManageUsers(roleValue)

    val canApproveAccounts: Boolean
        get() = AppAuthContract.canApproveAccounts(roleValue)

    val canManageRooms: Boolean
        get() = AppAuthContract.canManageRooms(roleValue)

    val canManageComputers: Boolean
        get() = AppAuthContract.canManageComputers(roleValue)

    val canCreateBooking: Boolean
        get() = AppAuthContract.canCreateBooking(roleValue)

    val canApproveBookings: Boolean
        get() = AppAuthContract.canApproveBookings(roleValue)

    val canSendSystemNotification: Boolean
        get() = AppAuthContract.canSendSystemNotification(roleValue)

    val canSendClassNotification: Boolean
        get() = AppAuthContract.canSendClassNotification(roleValue)

    val canViewAllSystemSchedule: Boolean
        get() = AppAuthContract.canViewAllSystemSchedule(roleValue)

    val canExportData: Boolean
        get() = AppAuthContract.canExportData(roleValue)

    fun normalizedCopy(): User {
        val normalizedRole = AppAuthContract.normalizeRoleForFirestore(
            email = email,
            rawRole = role
        )

        val normalizedStatus = AppAuthContract.normalizeStatusForFirestore(
            email = email,
            rawStatus = status
        )

        val normalizedId = safeId
        val normalizedName = displayName
        val normalizedIdentifier = displayIdentifier
        val normalizedDepartment = displayDepartment

        return copy(
            id = normalizedId,
            uid = if (uid.isNotBlank()) uid else normalizedId,
            email = AppAuthContract.normalizeEmail(email),
            fullName = if (fullName.isNotBlank()) fullName else normalizedName,
            name = if (name.isNotBlank()) name else normalizedName,
            identifier = normalizedIdentifier,
            role = normalizedRole,
            status = normalizedStatus,
            department = normalizedDepartment,
            className = if (AppAuthContract.Role.fromRaw(normalizedRole) == AppAuthContract.Role.Student) {
                if (className.isNotBlank()) className else normalizedDepartment
            } else {
                className
            },
            studentCode = if (AppAuthContract.Role.fromRaw(normalizedRole) == AppAuthContract.Role.Student) {
                if (studentCode.isNotBlank()) studentCode else normalizedIdentifier
            } else {
                studentCode
            },
            teacherCode = if (AppAuthContract.Role.fromRaw(normalizedRole) == AppAuthContract.Role.Teacher) {
                if (teacherCode.isNotBlank()) teacherCode else normalizedIdentifier
            } else {
                teacherCode
            }
        )
    }

    fun toFirestoreMapForMerge(): Map<String, Any?> {
        val normalizedUser = normalizedCopy()

        return linkedMapOf(
            AppAuthContract.UserFields.ID to normalizedUser.id,
            AppAuthContract.UserFields.UID to normalizedUser.uid,
            AppAuthContract.UserFields.EMAIL to normalizedUser.email,
            AppAuthContract.UserFields.FULL_NAME to normalizedUser.fullName,
            AppAuthContract.UserFields.NAME to normalizedUser.name,
            AppAuthContract.UserFields.IDENTIFIER to normalizedUser.identifier,
            AppAuthContract.UserFields.STUDENT_CODE to normalizedUser.studentCode,
            AppAuthContract.UserFields.TEACHER_CODE to normalizedUser.teacherCode,
            AppAuthContract.UserFields.ROLE to normalizedUser.role,
            AppAuthContract.UserFields.STATUS to normalizedUser.status,
            AppAuthContract.UserFields.CLASS_NAME to normalizedUser.className,
            AppAuthContract.UserFields.DEPARTMENT to normalizedUser.department,
            AppAuthContract.UserFields.PHONE to normalizedUser.phone,
            "avatarUrl" to normalizedUser.avatarUrl
        )
    }

    companion object {

        fun empty(): User {
            return User()
        }

        fun systemAdmin(
            uid: String,
            email: String = AppAuthContract.SYSTEM_ADMIN_EMAIL
        ): User {
            return User(
                id = uid,
                uid = uid,
                email = AppAuthContract.normalizeEmail(email),
                fullName = "Quản trị viên",
                name = "Quản trị viên",
                identifier = "SYSTEM_ADMIN",
                role = AppAuthContract.Role.Admin.firestoreValue,
                status = AppAuthContract.Status.Approved.firestoreValue,
                department = "Hệ thống"
            )
        }

        fun fromRegisterInput(
            uid: String,
            email: String,
            fullName: String,
            identifier: String,
            role: String,
            department: String,
            phone: String
        ): User {
            val normalizedEmail = AppAuthContract.normalizeEmail(email)
            val normalizedRole = AppAuthContract.normalizeRoleForFirestore(
                email = normalizedEmail,
                rawRole = role
            )

            val normalizedStatus = AppAuthContract.normalizeStatusForFirestore(
                email = normalizedEmail,
                rawStatus = if (AppAuthContract.isSystemAdminEmail(normalizedEmail)) {
                    AppAuthContract.Status.Approved.firestoreValue
                } else {
                    AppAuthContract.Status.Pending.firestoreValue
                }
            )

            return User(
                id = uid,
                uid = uid,
                email = normalizedEmail,
                fullName = fullName.trim(),
                name = fullName.trim(),
                identifier = identifier.trim(),
                studentCode = if (AppAuthContract.Role.fromRaw(normalizedRole) == AppAuthContract.Role.Student) {
                    identifier.trim()
                } else {
                    ""
                },
                teacherCode = if (AppAuthContract.Role.fromRaw(normalizedRole) == AppAuthContract.Role.Teacher) {
                    identifier.trim()
                } else {
                    ""
                },
                role = normalizedRole,
                status = normalizedStatus,
                className = if (AppAuthContract.Role.fromRaw(normalizedRole) == AppAuthContract.Role.Student) {
                    department.trim()
                } else {
                    ""
                },
                department = department.trim(),
                phone = phone.trim()
            )
        }
    }
}