package com.quyen.quanlyphongmay.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quyen.quanlyphongmay.core.AppAuthContract
import com.quyen.quanlyphongmay.model.User
import java.util.Locale

class UserManagementRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val usersCollection = firestore.collection(
        AppAuthContract.FirestoreCollections.USERS
    )

    data class UserFilter(
        val keyword: String = "",
        val role: String = "",
        val status: String = ""
    )

    data class UsersResult(
        val users: List<User> = emptyList(),
        val total: Int = 0,
        val pendingCount: Int = 0,
        val approvedCount: Int = 0,
        val rejectedCount: Int = 0,
        val lockedCount: Int = 0,
        val studentCount: Int = 0,
        val teacherCount: Int = 0,
        val adminCount: Int = 0
    )

    fun getUsers(
        filter: UserFilter = UserFilter(),
        callback: (Boolean, String?, UsersResult) -> Unit
    ) {
        usersCollection
            .orderBy(
                AppAuthContract.UserFields.CREATED_AT,
                Query.Direction.DESCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->
                val rawUsers = snapshot.documents.mapNotNull { document ->
                    document.toSafeUser()
                }

                val normalizedUsers = rawUsers.map { user ->
                    user.normalizedCopy()
                }

                val filteredUsers = applyFilter(
                    users = normalizedUsers,
                    filter = filter
                )

                callback(
                    true,
                    null,
                    buildUsersResult(filteredUsers)
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message),
                    UsersResult()
                )
            }
    }

    fun getPendingUsers(
        callback: (Boolean, String?, List<User>) -> Unit
    ) {
        usersCollection
            .whereEqualTo(
                AppAuthContract.UserFields.STATUS,
                AppAuthContract.Status.Pending.firestoreValue
            )
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents
                    .mapNotNull { document -> document.toSafeUser() }
                    .map { user -> user.normalizedCopy() }
                    .filter { user -> user.isPending }
                    .sortedWith(
                        compareBy<User> { it.createdAt?.seconds ?: 0L }
                            .thenBy { it.displayName.lowercase(Locale.getDefault()) }
                    )

                callback(true, null, users)
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message),
                    emptyList()
                )
            }
    }

    fun getUserById(
        userId: String,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val cleanUserId = userId.trim()

        if (cleanUserId.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản.", null)
            return
        }

        usersCollection
            .document(cleanUserId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toSafeUser()?.normalizedCopy()

                if (user == null) {
                    callback(false, "Không tìm thấy tài khoản.", null)
                } else {
                    callback(true, null, user)
                }
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message),
                    null
                )
            }
    }

    fun approveUser(
        targetUser: User,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val safeTarget = targetUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val targetId = safeTarget.safeId

        if (targetId.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản cần duyệt.")
            return
        }

        if (safeTarget.isSystemAdmin) {
            callback(false, "Không thể thay đổi tài khoản hệ thống.")
            return
        }

        val patch = linkedMapOf<String, Any>(
            AppAuthContract.UserFields.STATUS to AppAuthContract.Status.Approved.firestoreValue,
            AppAuthContract.UserFields.ROLE to AppAuthContract.normalizeRoleForFirestore(
                email = safeTarget.email,
                rawRole = safeTarget.role
            ),
            AppAuthContract.UserFields.UPDATED_AT to FieldValue.serverTimestamp(),
            AppAuthContract.UserFields.APPROVED_AT to FieldValue.serverTimestamp(),
            AppAuthContract.UserFields.APPROVED_BY to safeAdmin.safeId.ifBlank { safeAdmin.email },
            "rejectedAt" to FieldValue.delete(),
            "rejectedBy" to FieldValue.delete(),
            "rejectedReason" to FieldValue.delete(),
            "lockedAt" to FieldValue.delete(),
            "lockedBy" to FieldValue.delete()
        )

        usersCollection
            .document(targetId)
            .update(patch)
            .addOnSuccessListener {
                callback(true, "Đã duyệt tài khoản.")
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun rejectUser(
        targetUser: User,
        adminUser: User,
        reason: String = "",
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val safeTarget = targetUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val targetId = safeTarget.safeId

        if (targetId.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản cần từ chối.")
            return
        }

        if (safeTarget.isSystemAdmin) {
            callback(false, "Không thể thay đổi tài khoản hệ thống.")
            return
        }

        val patch = linkedMapOf<String, Any>(
            AppAuthContract.UserFields.STATUS to AppAuthContract.Status.Rejected.firestoreValue,
            AppAuthContract.UserFields.UPDATED_AT to FieldValue.serverTimestamp(),
            "rejectedAt" to FieldValue.serverTimestamp(),
            "rejectedBy" to safeAdmin.safeId.ifBlank { safeAdmin.email },
            "rejectedReason" to reason.trim().ifBlank { "Tài khoản không đủ điều kiện phê duyệt." },
            AppAuthContract.UserFields.APPROVED_AT to FieldValue.delete(),
            AppAuthContract.UserFields.APPROVED_BY to FieldValue.delete()
        )

        usersCollection
            .document(targetId)
            .update(patch)
            .addOnSuccessListener {
                callback(true, "Đã từ chối tài khoản.")
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun lockUser(
        targetUser: User,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val safeTarget = targetUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val targetId = safeTarget.safeId

        if (targetId.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản cần khóa.")
            return
        }

        if (safeTarget.isSystemAdmin) {
            callback(false, "Không thể khóa tài khoản hệ thống.")
            return
        }

        val patch = linkedMapOf<String, Any>(
            AppAuthContract.UserFields.STATUS to AppAuthContract.Status.Locked.firestoreValue,
            AppAuthContract.UserFields.UPDATED_AT to FieldValue.serverTimestamp(),
            AppAuthContract.UserFields.LOCKED_AT to FieldValue.serverTimestamp(),
            AppAuthContract.UserFields.LOCKED_BY to safeAdmin.safeId.ifBlank { safeAdmin.email }
        )

        usersCollection
            .document(targetId)
            .update(patch)
            .addOnSuccessListener {
                callback(true, "Đã khóa tài khoản.")
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun unlockUser(
        targetUser: User,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val safeTarget = targetUser.normalizedCopy()
        val safeAdmin = adminUser.normalizedCopy()
        val targetId = safeTarget.safeId

        if (targetId.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản cần mở khóa.")
            return
        }

        if (safeTarget.isSystemAdmin) {
            callback(false, "Không thể thay đổi tài khoản hệ thống.")
            return
        }

        val statusAfterUnlock = if (safeTarget.isRejected) {
            AppAuthContract.Status.Rejected.firestoreValue
        } else {
            AppAuthContract.Status.Approved.firestoreValue
        }

        val patch = linkedMapOf<String, Any>(
            AppAuthContract.UserFields.STATUS to statusAfterUnlock,
            AppAuthContract.UserFields.UPDATED_AT to FieldValue.serverTimestamp(),
            "unlockedAt" to FieldValue.serverTimestamp(),
            "unlockedBy" to safeAdmin.safeId.ifBlank { safeAdmin.email },
            AppAuthContract.UserFields.LOCKED_AT to FieldValue.delete(),
            AppAuthContract.UserFields.LOCKED_BY to FieldValue.delete()
        )

        usersCollection
            .document(targetId)
            .update(patch)
            .addOnSuccessListener {
                callback(true, "Đã mở khóa tài khoản.")
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun deleteUserDocument(
        targetUser: User,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val safeTarget = targetUser.normalizedCopy()
        val targetId = safeTarget.safeId

        if (targetId.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản cần xóa.")
            return
        }

        if (safeTarget.isSystemAdmin) {
            callback(false, "Không thể xóa tài khoản hệ thống.")
            return
        }

        usersCollection
            .document(targetId)
            .delete()
            .addOnSuccessListener {
                callback(true, "Đã xóa hồ sơ tài khoản.")
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun normalizeExistingUsers(
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        usersCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                var changedCount = 0

                snapshot.documents.forEach { document ->
                    val user = document.toSafeUser() ?: return@forEach
                    val normalizedUser = user.normalizedCopy()
                    val documentId = document.id

                    val normalizedRole = AppAuthContract.normalizeRoleForFirestore(
                        email = normalizedUser.email,
                        rawRole = normalizedUser.role
                    )

                    val normalizedStatus = AppAuthContract.normalizeStatusForFirestore(
                        email = normalizedUser.email,
                        rawStatus = normalizedUser.status
                    )

                    val patch = linkedMapOf<String, Any>(
                        AppAuthContract.UserFields.ID to normalizedUser.safeId.ifBlank { documentId },
                        AppAuthContract.UserFields.UID to normalizedUser.uid.ifBlank { documentId },
                        AppAuthContract.UserFields.EMAIL to AppAuthContract.normalizeEmail(normalizedUser.email),
                        AppAuthContract.UserFields.FULL_NAME to normalizedUser.displayName,
                        AppAuthContract.UserFields.NAME to normalizedUser.displayName,
                        AppAuthContract.UserFields.IDENTIFIER to normalizedUser.displayIdentifier,
                        AppAuthContract.UserFields.ROLE to normalizedRole,
                        AppAuthContract.UserFields.STATUS to normalizedStatus,
                        AppAuthContract.UserFields.DEPARTMENT to normalizedUser.displayDepartment,
                        AppAuthContract.UserFields.UPDATED_AT to FieldValue.serverTimestamp()
                    )

                    batch.update(
                        usersCollection.document(documentId),
                        patch
                    )
                    changedCount++
                }

                if (changedCount == 0) {
                    callback(true, "Không có tài khoản cần chuẩn hóa.")
                    return@addOnSuccessListener
                }

                batch.commit()
                    .addOnSuccessListener {
                        callback(true, "Đã chuẩn hóa $changedCount tài khoản.")
                    }
                    .addOnFailureListener { exception ->
                        callback(
                            false,
                            AppAuthContract.normalizeAuthMessage(exception.message)
                        )
                    }
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    private fun applyFilter(
        users: List<User>,
        filter: UserFilter
    ): List<User> {
        val keyword = filter.keyword
            .trim()
            .lowercase(Locale.getDefault())

        val role = filter.role.trim()
        val status = filter.status.trim()

        return users.filter { user ->
            val matchesKeyword = if (keyword.isBlank()) {
                true
            } else {
                user.displayName.lowercase(Locale.getDefault()).contains(keyword) ||
                        user.email.lowercase(Locale.getDefault()).contains(keyword) ||
                        user.displayIdentifier.lowercase(Locale.getDefault()).contains(keyword) ||
                        user.displayDepartment.lowercase(Locale.getDefault()).contains(keyword) ||
                        user.phone.lowercase(Locale.getDefault()).contains(keyword)
            }

            val matchesRole = if (role.isBlank()) {
                true
            } else {
                user.roleEnum == AppAuthContract.Role.fromRaw(role)
            }

            val matchesStatus = if (status.isBlank()) {
                true
            } else {
                user.statusEnum == AppAuthContract.Status.fromRaw(status)
            }

            matchesKeyword && matchesRole && matchesStatus
        }
    }

    private fun buildUsersResult(
        users: List<User>
    ): UsersResult {
        return UsersResult(
            users = users,
            total = users.size,
            pendingCount = users.count { it.isPending },
            approvedCount = users.count { it.isApproved },
            rejectedCount = users.count { it.isRejected },
            lockedCount = users.count { it.isLocked },
            studentCount = users.count { it.isStudent },
            teacherCount = users.count { it.isTeacher },
            adminCount = users.count { it.isAdmin }
        )
    }

    private fun DocumentSnapshot.toSafeUser(): User? {
        if (!exists()) {
            return null
        }

        val firestoreUser = toObject(User::class.java) ?: return null

        val fixedId = firestoreUser.id.ifBlank {
            firestoreUser.uid.ifBlank {
                id
            }
        }

        val fixedUid = firestoreUser.uid.ifBlank {
            fixedId
        }

        val fixedEmail = AppAuthContract.normalizeEmail(
            firestoreUser.email.ifBlank {
                getString(AppAuthContract.UserFields.EMAIL).orEmpty()
            }
        )

        val fixedFullName = firestoreUser.fullName.ifBlank {
            firestoreUser.name.ifBlank {
                getString(AppAuthContract.UserFields.FULL_NAME)
                    ?: getString(AppAuthContract.UserFields.NAME)
                    ?: ""
            }
        }

        val fixedIdentifier = firestoreUser.identifier.ifBlank {
            getString(AppAuthContract.UserFields.IDENTIFIER)
                ?: getString(AppAuthContract.UserFields.STUDENT_CODE)
                ?: getString(AppAuthContract.UserFields.TEACHER_CODE)
                ?: ""
        }

        val fixedDepartment = firestoreUser.department.ifBlank {
            firestoreUser.className.ifBlank {
                getString(AppAuthContract.UserFields.DEPARTMENT)
                    ?: getString(AppAuthContract.UserFields.CLASS_NAME)
                    ?: ""
            }
        }

        val fixedRole = AppAuthContract.normalizeRoleForFirestore(
            email = fixedEmail,
            rawRole = firestoreUser.role.ifBlank {
                getString(AppAuthContract.UserFields.ROLE).orEmpty()
            }
        )

        val fixedStatus = AppAuthContract.normalizeStatusForFirestore(
            email = fixedEmail,
            rawStatus = firestoreUser.status.ifBlank {
                getString(AppAuthContract.UserFields.STATUS).orEmpty()
            }
        )

        return firestoreUser.copy(
            id = fixedId,
            uid = fixedUid,
            email = fixedEmail,
            fullName = fixedFullName,
            name = firestoreUser.name.ifBlank { fixedFullName },
            identifier = fixedIdentifier,
            role = fixedRole,
            status = fixedStatus,
            department = fixedDepartment,
            className = firestoreUser.className.ifBlank {
                if (AppAuthContract.Role.fromRaw(fixedRole) == AppAuthContract.Role.Student) {
                    fixedDepartment
                } else {
                    ""
                }
            },
            studentCode = firestoreUser.studentCode.ifBlank {
                if (AppAuthContract.Role.fromRaw(fixedRole) == AppAuthContract.Role.Student) {
                    fixedIdentifier
                } else {
                    ""
                }
            },
            teacherCode = firestoreUser.teacherCode.ifBlank {
                if (AppAuthContract.Role.fromRaw(fixedRole) == AppAuthContract.Role.Teacher) {
                    fixedIdentifier
                } else {
                    ""
                }
            }
        )
    }
}