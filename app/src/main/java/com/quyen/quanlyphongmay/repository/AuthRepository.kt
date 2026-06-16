package com.quyen.quanlyphongmay.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quyen.quanlyphongmay.model.User
import java.util.Locale

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository(firestore)
) {
    private val userCollection = firestore.collection(USER_COLLECTION_NAME)

    fun getCurrentFirebaseUser() = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase(Locale.getDefault())
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank()) {
            callback(false, "Vui lòng nhập email.", null)
            return
        }

        if (cleanPassword.isBlank()) {
            callback(false, "Vui lòng nhập mật khẩu.", null)
            return
        }

        auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid.orEmpty()

                if (uid.isBlank()) {
                    callback(false, "Không tìm thấy tài khoản đăng nhập.", null)
                    return@addOnSuccessListener
                }

                getUserByUid(uid = uid) { success, message, user ->
                    if (!success || user == null) {
                        callback(false, message ?: "Không thể tải thông tin tài khoản.", null)
                        return@getUserByUid
                    }

                    val safeUser = user.normalizedCopy()

                    if (!safeUser.isApproved) {
                        callback(
                            false,
                            when {
                                safeUser.isPending -> "Tài khoản đang chờ quản trị viên duyệt."
                                safeUser.isRejected -> "Tài khoản đã bị từ chối. Vui lòng liên hệ quản trị viên."
                                else -> "Tài khoản chưa được kích hoạt."
                            },
                            safeUser
                        )
                        return@getUserByUid
                    }

                    callback(true, "Đăng nhập thành công.", safeUser)
                }
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeAuthError(exception), null)
            }
    }

    fun register(
        user: User,
        password: String,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val safeInputUser = user.normalizedCopy()
        val cleanEmail = safeInputUser.email.trim().lowercase(Locale.getDefault())
        val cleanPassword = password.trim()

        val validationMessage = validateRegisterInput(
            user = safeInputUser,
            password = cleanPassword
        )

        if (validationMessage != null) {
            callback(false, validationMessage, null)
            return
        }

        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid.orEmpty()

                if (uid.isBlank()) {
                    callback(false, "Không tạo được mã tài khoản.", null)
                    return@addOnSuccessListener
                }

                val registeredUser = safeInputUser.copy(
                    uid = uid,
                    email = cleanEmail,
                    status = STATUS_PENDING,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                ).normalizedCopy()

                saveUser(
                    user = registeredUser,
                    includeCreatedAt = true
                ) { saveSuccess, saveMessage, savedUser ->
                    if (!saveSuccess || savedUser == null) {
                        callback(false, saveMessage ?: "Không thể lưu thông tin tài khoản.", null)
                        return@saveUser
                    }

                    notificationRepository.createAccountRegisteredNotificationForAdmin(
                        registeredUser = savedUser
                    ) { _, _, _ -> }

                    callback(
                        true,
                        "Đăng ký thành công. Tài khoản đã được gửi cho quản trị viên duyệt.",
                        savedUser
                    )
                }
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeAuthError(exception), null)
            }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        role: String,
        identifier: String,
        department: String,
        phone: String,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val draftUser = User(
            uid = "",
            email = email.trim().lowercase(Locale.getDefault()),
            fullName = fullName.trim(),
            role = role.trim(),
            status = STATUS_PENDING,
            identifier = identifier.trim(),
            department = department.trim(),
            phone = phone.trim(),
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ).normalizedCopy()

        register(
            user = draftUser,
            password = password,
            callback = callback
        )
    }

    fun getCurrentUser(
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val uid = auth.currentUser?.uid.orEmpty()

        if (uid.isBlank()) {
            callback(false, "Chưa đăng nhập.", null)
            return
        }

        getUserByUid(
            uid = uid,
            callback = callback
        )
    }

    fun getUserByUid(
        uid: String,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val cleanUid = uid.trim()

        if (cleanUid.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản.", null)
            return
        }

        userCollection
            .document(cleanUid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    callback(false, "Không tìm thấy hồ sơ tài khoản.", null)
                    return@addOnSuccessListener
                }

                val user = documentToUserSafe(
                    documentId = document.id,
                    data = document.data.orEmpty()
                )

                if (user == null) {
                    callback(false, "Dữ liệu tài khoản không hợp lệ.", null)
                } else {
                    callback(true, null, user.normalizedCopy())
                }
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception), null)
            }
    }

    fun getUserByEmail(
        email: String,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase(Locale.getDefault())

        if (cleanEmail.isBlank()) {
            callback(false, "Vui lòng nhập email.", null)
            return
        }

        userCollection
            .whereEqualTo("email", cleanEmail)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val document = snapshot.documents.firstOrNull()

                if (document == null) {
                    callback(false, "Không tìm thấy tài khoản.", null)
                    return@addOnSuccessListener
                }

                val user = documentToUserSafe(
                    documentId = document.id,
                    data = document.data.orEmpty()
                )

                if (user == null) {
                    callback(false, "Dữ liệu tài khoản không hợp lệ.", null)
                } else {
                    callback(true, null, user.normalizedCopy())
                }
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception), null)
            }
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            callback(false, "Bạn chưa đăng nhập.")
            return
        }

        val cleanNewPassword = newPassword.trim()

        if (cleanNewPassword.length < 6) {
            callback(false, "Mật khẩu mới phải có ít nhất 6 ký tự.")
            return
        }

        firebaseUser.updatePassword(cleanNewPassword)
            .addOnSuccessListener {
                callback(true, "Đổi mật khẩu thành công.")
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeAuthError(exception))
            }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        updatePassword(
            currentPassword = currentPassword,
            newPassword = newPassword,
            callback = callback
        )
    }

    fun changePassword(
        newPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        updatePassword(
            currentPassword = "",
            newPassword = newPassword,
            callback = callback
        )
    }

    fun sendPasswordResetEmail(
        email: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase(Locale.getDefault())

        if (cleanEmail.isBlank()) {
            callback(false, "Vui lòng nhập email.")
            return
        }

        auth.sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {
                callback(true, "Đã gửi email đặt lại mật khẩu.")
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeAuthError(exception))
            }
    }

    fun approveUser(
        targetUser: User,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeTargetUser = targetUser.normalizedCopy()
        val safeAdminUser = adminUser.normalizedCopy()

        if (!safeAdminUser.isAdmin) {
            callback(false, "Bạn không có quyền duyệt tài khoản.")
            return
        }

        if (safeTargetUser.uid.isBlank()) {
            callback(false, "Không tìm thấy tài khoản cần duyệt.")
            return
        }

        val approvedUser = safeTargetUser.copy(
            status = STATUS_APPROVED,
            updatedAt = Timestamp.now()
        ).normalizedCopy()

        val updateMap = mutableMapOf<String, Any?>(
            "status" to STATUS_APPROVED,
            "approvedById" to safeAdminUser.uid,
            "approvedByEmail" to safeAdminUser.email,
            "approvedByName" to safeAdminUser.displayName,
            "approvedAt" to Timestamp.now(),
            "rejectReason" to "",
            "updatedAt" to Timestamp.now()
        )

        userCollection
            .document(safeTargetUser.uid)
            .set(updateMap, SetOptions.merge())
            .addOnSuccessListener {
                notificationRepository.createAccountApprovedNotification(
                    approvedUser = approvedUser,
                    adminUser = safeAdminUser
                ) { _, _, _ -> }

                callback(true, "Đã duyệt tài khoản. Người dùng sẽ nhận thông báo.")
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception))
            }
    }

    fun rejectUser(
        targetUser: User,
        adminUser: User,
        reason: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeTargetUser = targetUser.normalizedCopy()
        val safeAdminUser = adminUser.normalizedCopy()

        if (!safeAdminUser.isAdmin) {
            callback(false, "Bạn không có quyền từ chối tài khoản.")
            return
        }

        if (safeTargetUser.uid.isBlank()) {
            callback(false, "Không tìm thấy tài khoản cần từ chối.")
            return
        }

        val cleanReason = reason.trim().ifBlank {
            "Tài khoản chưa đủ điều kiện duyệt."
        }

        val rejectedUser = safeTargetUser.copy(
            status = STATUS_REJECTED,
            updatedAt = Timestamp.now()
        ).normalizedCopy()

        val updateMap = mutableMapOf<String, Any?>(
            "status" to STATUS_REJECTED,
            "approvedById" to safeAdminUser.uid,
            "approvedByEmail" to safeAdminUser.email,
            "approvedByName" to safeAdminUser.displayName,
            "approvedAt" to Timestamp.now(),
            "rejectReason" to cleanReason,
            "updatedAt" to Timestamp.now()
        )

        userCollection
            .document(safeTargetUser.uid)
            .set(updateMap, SetOptions.merge())
            .addOnSuccessListener {
                notificationRepository.createAccountRejectedNotification(
                    rejectedUser = rejectedUser,
                    adminUser = safeAdminUser,
                    reason = cleanReason
                ) { _, _, _ -> }

                callback(true, "Đã từ chối tài khoản. Người dùng sẽ nhận thông báo.")
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception))
            }
    }

    fun updateUserProfile(
        user: User,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val safeUser = user.normalizedCopy()

        if (safeUser.uid.isBlank()) {
            callback(false, "Không tìm thấy tài khoản cần cập nhật.", null)
            return
        }

        val updatedUser = safeUser.copy(
            updatedAt = Timestamp.now()
        ).normalizedCopy()

        saveUser(
            user = updatedUser,
            includeCreatedAt = false,
            callback = callback
        )
    }

    fun saveUser(
        user: User,
        includeCreatedAt: Boolean,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        val safeUser = user.normalizedCopy()

        if (safeUser.uid.isBlank()) {
            callback(false, "Không tìm thấy mã tài khoản.", null)
            return
        }

        val map = userToMap(
            user = safeUser,
            includeCreatedAt = includeCreatedAt
        )

        userCollection
            .document(safeUser.uid)
            .set(
                map,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                callback(true, "Đã lưu tài khoản.", safeUser)
            }
            .addOnFailureListener { exception ->
                callback(false, normalizeFirestoreError(exception), null)
            }
    }

    private fun validateRegisterInput(
        user: User,
        password: String
    ): String? {
        val safeUser = user.normalizedCopy()

        return when {
            safeUser.email.isBlank() -> {
                "Vui lòng nhập email."
            }

            !safeUser.email.contains("@") -> {
                "Email không hợp lệ."
            }

            password.length < 6 -> {
                "Mật khẩu phải có ít nhất 6 ký tự."
            }

            safeUser.displayName.isBlank() -> {
                "Vui lòng nhập họ tên."
            }

            safeUser.role.isBlank() -> {
                "Vui lòng chọn vai trò."
            }

            safeUser.isStudent && safeUser.displayIdentifier.isBlank() -> {
                "Vui lòng nhập mã số sinh viên."
            }

            safeUser.isTeacher && safeUser.displayIdentifier.isBlank() -> {
                "Vui lòng nhập mã giảng viên."
            }

            safeUser.isStudent && safeUser.displayDepartment.isBlank() -> {
                "Vui lòng nhập lớp."
            }

            safeUser.isTeacher && safeUser.displayDepartment.isBlank() -> {
                "Vui lòng nhập khoa hoặc bộ môn."
            }

            else -> null
        }
    }

    private fun documentToUserSafe(
        documentId: String,
        data: Map<String, Any>
    ): User? {
        return try {
            User(
                uid = readString(data["uid"]).ifBlank { documentId },
                email = readString(data["email"]),
                fullName = readFirstNonBlankString(
                    data = data,
                    keys = listOf(
                        "fullName",
                        "name",
                        "displayName",
                        "hoTen",
                        "hoten"
                    )
                ),
                role = readString(data["role"]),
                status = readString(data["status"]),
                identifier = readFirstNonBlankString(
                    data = data,
                    keys = listOf(
                        "identifier",
                        "studentCode",
                        "studentId",
                        "teacherCode",
                        "teacherId",
                        "mssv",
                        "msgv",
                        "code"
                    )
                ),
                department = readFirstNonBlankString(
                    data = data,
                    keys = listOf(
                        "department",
                        "className",
                        "class",
                        "lop",
                        "studentClass",
                        "unit"
                    )
                ),
                phone = readString(data["phone"]),
                createdAt = readTimestamp(data["createdAt"]),
                updatedAt = readTimestamp(data["updatedAt"])
            ).normalizedCopy()
        } catch (exception: Exception) {
            null
        }
    }

    private fun userToMap(
        user: User,
        includeCreatedAt: Boolean
    ): MutableMap<String, Any?> {
        val safeUser = user.normalizedCopy()

        val map = mutableMapOf<String, Any?>(
            "uid" to safeUser.uid,
            "email" to safeUser.email,
            "fullName" to safeUser.displayName,
            "name" to safeUser.displayName,
            "displayName" to safeUser.displayName,
            "role" to safeUser.role,
            "status" to safeUser.status,
            "identifier" to safeUser.displayIdentifier,
            "department" to safeUser.displayDepartment,
            "phone" to safeUser.phone,
            "updatedAt" to Timestamp.now()
        )

        if (safeUser.isStudent) {
            map["studentCode"] = safeUser.displayIdentifier
            map["studentId"] = safeUser.displayIdentifier
            map["mssv"] = safeUser.displayIdentifier
            map["className"] = safeUser.displayDepartment
            map["studentClass"] = safeUser.displayDepartment
        }

        if (safeUser.isTeacher) {
            map["teacherCode"] = safeUser.displayIdentifier
            map["teacherId"] = safeUser.displayIdentifier
            map["msgv"] = safeUser.displayIdentifier
            map["unit"] = safeUser.displayDepartment
        }

        if (includeCreatedAt) {
            map["createdAt"] = safeUser.createdAt ?: Timestamp.now()
        }

        return map
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

    private fun normalizeAuthError(
        exception: Exception
    ): String {
        val code = (exception as? FirebaseAuthException)?.errorCode.orEmpty()
        val raw = exception.message.orEmpty().lowercase(Locale.getDefault())

        return when {
            code == "ERROR_INVALID_EMAIL" || raw.contains("badly formatted") -> {
                "Email không hợp lệ."
            }

            code == "ERROR_USER_NOT_FOUND" || raw.contains("no user record") -> {
                "Tài khoản không tồn tại."
            }

            code == "ERROR_WRONG_PASSWORD" || code == "ERROR_INVALID_CREDENTIAL" || raw.contains("password is invalid") -> {
                "Email hoặc mật khẩu không đúng."
            }

            code == "ERROR_EMAIL_ALREADY_IN_USE" || raw.contains("already in use") -> {
                "Email này đã được đăng ký."
            }

            code == "ERROR_WEAK_PASSWORD" || raw.contains("password should be at least") -> {
                "Mật khẩu phải có ít nhất 6 ký tự."
            }

            code == "ERROR_NETWORK_REQUEST_FAILED" || raw.contains("network") -> {
                "Không thể kết nối mạng. Vui lòng thử lại."
            }

            raw.contains("requires recent login") -> {
                "Phiên đăng nhập đã cũ. Vui lòng đăng xuất và đăng nhập lại."
            }

            else -> {
                "Không thể xác thực tài khoản. Vui lòng thử lại."
            }
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

    companion object {
        private const val USER_COLLECTION_NAME = "users"
        private const val STATUS_PENDING = "pending"
        private const val STATUS_APPROVED = "approved"
        private const val STATUS_REJECTED = "rejected"
    }
}