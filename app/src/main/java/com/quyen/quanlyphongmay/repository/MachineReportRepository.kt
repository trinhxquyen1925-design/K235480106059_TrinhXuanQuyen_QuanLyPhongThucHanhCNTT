package com.quyen.quanlyphongmay.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quyen.quanlyphongmay.model.Computer
import com.quyen.quanlyphongmay.model.MachineReport
import com.quyen.quanlyphongmay.model.User
import java.util.Date
import java.util.Locale

class MachineReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getMachineReports(
        currentUser: User,
        callback: (Boolean, String?, List<MachineReport>) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isApproved) {
            callback(
                false,
                "Tài khoản chưa được duyệt nên không thể xem báo lỗi máy.",
                emptyList()
            )
            return
        }

        firestore.collection(MachineReport.COLLECTION)
            .orderBy(
                MachineReport.FIELD_CREATED_AT,
                Query.Direction.DESCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->
                val allReports = snapshot.documents.mapNotNull { document ->
                    document.toMachineReport()
                }

                val visibleReports = filterReportsByUser(
                    currentUser = safeUser,
                    reports = allReports
                )

                callback(
                    true,
                    "Đã tải danh sách báo lỗi máy.",
                    visibleReports
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    normalizeFirestoreError(exception),
                    emptyList()
                )
            }
    }

    fun getReportsByComputer(
        currentUser: User,
        computerId: String,
        callback: (Boolean, String?, List<MachineReport>) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()
        val safeComputerId = computerId.trim()

        if (!safeUser.isApproved) {
            callback(
                false,
                "Tài khoản chưa được duyệt nên không thể xem báo lỗi máy.",
                emptyList()
            )
            return
        }

        if (safeComputerId.isBlank()) {
            callback(
                false,
                "Không tìm thấy máy tính cần xem báo lỗi.",
                emptyList()
            )
            return
        }

        firestore.collection(MachineReport.COLLECTION)
            .whereEqualTo(
                MachineReport.FIELD_COMPUTER_ID,
                safeComputerId
            )
            .orderBy(
                MachineReport.FIELD_CREATED_AT,
                Query.Direction.DESCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->
                val reports = snapshot.documents
                    .mapNotNull { document -> document.toMachineReport() }
                    .let { reports ->
                        filterReportsByUser(
                            currentUser = safeUser,
                            reports = reports
                        )
                    }

                callback(
                    true,
                    "Đã tải báo lỗi của máy.",
                    reports
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    normalizeFirestoreError(exception),
                    emptyList()
                )
            }
    }

    fun createMachineReport(
        computer: Computer,
        currentUser: User,
        title: String,
        description: String,
        errorType: MachineReport.MachineReportErrorType,
        priority: MachineReport.MachineReportPriority,
        callback: (Boolean, String?, MachineReport?) -> Unit
    ) {
        val safeComputer = computer.normalizedCopy()
        val safeUser = currentUser.normalizedCopy()
        val safeTitle = title.trim()
        val safeDescription = description.trim()

        if (!safeUser.isApproved) {
            callback(
                false,
                "Tài khoản chưa được duyệt nên không thể báo lỗi máy.",
                null
            )
            return
        }

        if (safeComputer.safeId.isBlank()) {
            callback(
                false,
                "Vui lòng chọn máy tính cần báo lỗi.",
                null
            )
            return
        }

        if (safeTitle.isBlank()) {
            callback(
                false,
                "Vui lòng nhập tiêu đề lỗi.",
                null
            )
            return
        }

        if (safeDescription.length < MIN_DESCRIPTION_LENGTH) {
            callback(
                false,
                "Vui lòng mô tả lỗi rõ hơn ít nhất $MIN_DESCRIPTION_LENGTH ký tự.",
                null
            )
            return
        }

        val document = firestore.collection(MachineReport.COLLECTION).document()

        val draft = MachineReport.createDraft(
            computer = safeComputer,
            reporter = safeUser,
            title = safeTitle,
            description = safeDescription,
            errorType = errorType,
            priority = priority
        ).copy(
            id = document.id
        ).normalizedCopy()

        document
            .set(
                machineReportToMap(
                    report = draft,
                    includeCreatedAt = true,
                    includeUpdatedAt = true
                )
            )
            .addOnSuccessListener {
                callback(
                    true,
                    "Đã gửi báo lỗi máy. Quản trị viên/giáo viên có thể theo dõi và xử lý.",
                    draft
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    normalizeFirestoreError(exception),
                    null
                )
            }
    }

    fun createMachineReportByComputerId(
        computerId: String,
        currentUser: User,
        title: String,
        description: String,
        errorType: MachineReport.MachineReportErrorType,
        priority: MachineReport.MachineReportPriority,
        callback: (Boolean, String?, MachineReport?) -> Unit
    ) {
        val safeComputerId = computerId.trim()

        if (safeComputerId.isBlank()) {
            callback(
                false,
                "Vui lòng chọn máy tính cần báo lỗi.",
                null
            )
            return
        }

        firestore.collection(Computer.COLLECTION)
            .document(safeComputerId)
            .get()
            .addOnSuccessListener { document ->
                val computer = document.toObject(Computer::class.java)
                    ?.copy(id = document.id)
                    ?.normalizedCopy()

                if (computer == null) {
                    callback(
                        false,
                        "Không tìm thấy máy tính cần báo lỗi.",
                        null
                    )
                    return@addOnSuccessListener
                }

                createMachineReport(
                    computer = computer,
                    currentUser = currentUser,
                    title = title,
                    description = description,
                    errorType = errorType,
                    priority = priority,
                    callback = callback
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    normalizeFirestoreError(exception),
                    null
                )
            }
    }

    fun updateReportStatus(
        report: MachineReport,
        currentUser: User,
        targetStatus: MachineReport.MachineReportStatus,
        adminNote: String = "",
        resolvedNote: String = "",
        callback: (Boolean, String?, MachineReport?) -> Unit
    ) {
        val safeReport = report.normalizedCopy()
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isAdmin && !safeUser.isTeacher) {
            callback(
                false,
                "Chỉ quản trị viên hoặc giáo viên được cập nhật trạng thái báo lỗi.",
                null
            )
            return
        }

        if (safeReport.safeId.isBlank()) {
            callback(
                false,
                "Không tìm thấy báo lỗi cần cập nhật.",
                null
            )
            return
        }

        if (safeReport.isResolved || safeReport.isCancelled) {
            callback(
                false,
                "Báo lỗi này đã kết thúc nên không thể cập nhật tiếp.",
                null
            )
            return
        }

        val now = Timestamp.now()
        val cleanAdminNote = adminNote.trim()
        val cleanResolvedNote = resolvedNote.trim()

        val updatedReport = safeReport.copy(
            status = targetStatus.firestoreValue,
            handledById = safeUser.uid,
            handledByEmail = safeUser.email,
            handledByName = safeUser.displayName.ifBlank { safeUser.email },
            adminNote = cleanAdminNote.ifBlank { safeReport.adminNote },
            resolvedNote = cleanResolvedNote.ifBlank { safeReport.resolvedNote },
            updatedAt = now,
            resolvedAt = if (targetStatus == MachineReport.MachineReportStatus.Resolved) {
                now
            } else {
                safeReport.resolvedAt
            }
        ).normalizedCopy()

        firestore.collection(MachineReport.COLLECTION)
            .document(updatedReport.safeId)
            .update(
                machineReportUpdateMap(
                    report = updatedReport,
                    targetStatus = targetStatus
                )
            )
            .addOnSuccessListener {
                callback(
                    true,
                    statusSuccessMessage(targetStatus),
                    updatedReport
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    normalizeFirestoreError(exception),
                    null
                )
            }
    }

    fun startProcessingReport(
        report: MachineReport,
        currentUser: User,
        note: String = "",
        callback: (Boolean, String?, MachineReport?) -> Unit
    ) {
        updateReportStatus(
            report = report,
            currentUser = currentUser,
            targetStatus = MachineReport.MachineReportStatus.Processing,
            adminNote = note,
            callback = callback
        )
    }

    fun resolveReport(
        report: MachineReport,
        currentUser: User,
        resolvedNote: String,
        callback: (Boolean, String?, MachineReport?) -> Unit
    ) {
        val safeResolvedNote = resolvedNote.trim()

        if (safeResolvedNote.isBlank()) {
            callback(
                false,
                "Vui lòng nhập ghi chú xử lý trước khi hoàn tất.",
                null
            )
            return
        }

        updateReportStatus(
            report = report,
            currentUser = currentUser,
            targetStatus = MachineReport.MachineReportStatus.Resolved,
            resolvedNote = safeResolvedNote,
            callback = callback
        )
    }

    fun cancelReport(
        report: MachineReport,
        currentUser: User,
        reason: String,
        callback: (Boolean, String?, MachineReport?) -> Unit
    ) {
        val safeReport = report.normalizedCopy()
        val safeUser = currentUser.normalizedCopy()
        val safeReason = reason.trim()

        if (safeReport.safeId.isBlank()) {
            callback(
                false,
                "Không tìm thấy báo lỗi cần hủy.",
                null
            )
            return
        }

        val canCancel = safeUser.isAdmin ||
                safeUser.isTeacher ||
                safeReport.reporterId == safeUser.uid ||
                safeReport.reporterEmail.equals(safeUser.email, ignoreCase = true)

        if (!canCancel) {
            callback(
                false,
                "Bạn không có quyền hủy báo lỗi này.",
                null
            )
            return
        }

        if (!safeReport.canEditByReporter && !safeUser.isAdmin && !safeUser.isTeacher) {
            callback(
                false,
                "Báo lỗi đã được tiếp nhận nên sinh viên không thể hủy.",
                null
            )
            return
        }

        updateReportStatus(
            report = safeReport,
            currentUser = safeUser,
            targetStatus = MachineReport.MachineReportStatus.Cancelled,
            adminNote = safeReason.ifBlank { "Đã hủy báo lỗi." },
            callback = callback
        )
    }

    fun deleteReport(
        report: MachineReport,
        currentUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        val safeReport = report.normalizedCopy()
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isAdmin) {
            callback(
                false,
                "Chỉ quản trị viên được xóa báo lỗi máy."
            )
            return
        }

        if (safeReport.safeId.isBlank()) {
            callback(
                false,
                "Không tìm thấy báo lỗi cần xóa."
            )
            return
        }

        firestore.collection(MachineReport.COLLECTION)
            .document(safeReport.safeId)
            .delete()
            .addOnSuccessListener {
                callback(
                    true,
                    "Đã xóa báo lỗi máy."
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    normalizeFirestoreError(exception)
                )
            }
    }

    fun filterReportsByStatus(
        reports: List<MachineReport>,
        status: MachineReport.MachineReportStatus?
    ): List<MachineReport> {
        if (status == null) {
            return reports.map { it.normalizedCopy() }
        }

        return reports
            .map { it.normalizedCopy() }
            .filter { it.statusEnum == status }
    }

    fun filterReportsByKeyword(
        reports: List<MachineReport>,
        keyword: String
    ): List<MachineReport> {
        val safeKeyword = keyword.trim()

        if (safeKeyword.isBlank()) {
            return reports.map { it.normalizedCopy() }
        }

        return reports
            .map { it.normalizedCopy() }
            .filter { it.matchesKeyword(safeKeyword) }
    }

    fun canCurrentUserCreateReport(
        currentUser: User
    ): Boolean {
        val safeUser = currentUser.normalizedCopy()

        return safeUser.isApproved
    }

    fun canCurrentUserProcessReport(
        currentUser: User
    ): Boolean {
        val safeUser = currentUser.normalizedCopy()

        return safeUser.isApproved && (safeUser.isAdmin || safeUser.isTeacher)
    }

    private fun filterReportsByUser(
        currentUser: User,
        reports: List<MachineReport>
    ): List<MachineReport> {
        val safeUser = currentUser.normalizedCopy()

        if (safeUser.isAdmin || safeUser.isTeacher) {
            return reports.map { it.normalizedCopy() }
        }

        return reports
            .map { it.normalizedCopy() }
            .filter { report ->
                report.reporterId == safeUser.uid ||
                        report.reporterEmail.equals(
                            safeUser.email,
                            ignoreCase = true
                        )
            }
    }

    private fun DocumentSnapshot.toMachineReport(): MachineReport? {
        val report = toObject(MachineReport::class.java)
            ?.copy(id = id)
            ?.normalizedCopy()

        return report
    }

    private fun machineReportToMap(
        report: MachineReport,
        includeCreatedAt: Boolean,
        includeUpdatedAt: Boolean
    ): Map<String, Any?> {
        val safeReport = report.normalizedCopy()
        val data = linkedMapOf<String, Any?>(
            MachineReport.FIELD_ID to safeReport.safeId,
            MachineReport.FIELD_COMPUTER_ID to safeReport.safeComputerId,
            MachineReport.FIELD_COMPUTER_CODE to safeReport.displayComputerCode,
            MachineReport.FIELD_COMPUTER_NAME to safeReport.displayComputerName,
            MachineReport.FIELD_ROOM_ID to safeReport.safeRoomId,
            MachineReport.FIELD_ROOM_CODE to safeReport.displayRoomCode,
            MachineReport.FIELD_ROOM_NAME to safeReport.displayRoomName,
            MachineReport.FIELD_ROW to safeReport.normalizedRow,
            MachineReport.FIELD_COLUMN to safeReport.normalizedColumn,
            MachineReport.FIELD_TITLE to safeReport.displayTitle,
            MachineReport.FIELD_DESCRIPTION to safeReport.displayDescription,
            MachineReport.FIELD_ERROR_TYPE to safeReport.errorTypeEnum.firestoreValue,
            MachineReport.FIELD_PRIORITY to safeReport.priorityEnum.firestoreValue,
            MachineReport.FIELD_STATUS to safeReport.statusEnum.firestoreValue,
            MachineReport.FIELD_REPORTER_ID to safeReport.reporterId,
            MachineReport.FIELD_REPORTER_EMAIL to safeReport.reporterEmail,
            MachineReport.FIELD_REPORTER_NAME to safeReport.displayReporterName,
            MachineReport.FIELD_REPORTER_ROLE to safeReport.reporterRole,
            MachineReport.FIELD_HANDLED_BY_ID to safeReport.handledById,
            MachineReport.FIELD_HANDLED_BY_EMAIL to safeReport.handledByEmail,
            MachineReport.FIELD_HANDLED_BY_NAME to safeReport.handledByName,
            MachineReport.FIELD_ADMIN_NOTE to safeReport.adminNote,
            MachineReport.FIELD_RESOLVED_NOTE to safeReport.resolvedNote,
            MachineReport.FIELD_RESOLVED_AT to safeReport.resolvedAt
        )

        if (includeCreatedAt) {
            data[MachineReport.FIELD_CREATED_AT] = Timestamp.now()
        } else {
            data[MachineReport.FIELD_CREATED_AT] = safeReport.createdAt
        }

        if (includeUpdatedAt) {
            data[MachineReport.FIELD_UPDATED_AT] = Timestamp.now()
        } else {
            data[MachineReport.FIELD_UPDATED_AT] = safeReport.updatedAt
        }

        return data
    }

    private fun machineReportUpdateMap(
        report: MachineReport,
        targetStatus: MachineReport.MachineReportStatus
    ): Map<String, Any?> {
        val safeReport = report.normalizedCopy()
        val data = linkedMapOf<String, Any?>(
            MachineReport.FIELD_STATUS to targetStatus.firestoreValue,
            MachineReport.FIELD_HANDLED_BY_ID to safeReport.handledById,
            MachineReport.FIELD_HANDLED_BY_EMAIL to safeReport.handledByEmail,
            MachineReport.FIELD_HANDLED_BY_NAME to safeReport.handledByName,
            MachineReport.FIELD_ADMIN_NOTE to safeReport.adminNote,
            MachineReport.FIELD_RESOLVED_NOTE to safeReport.resolvedNote,
            MachineReport.FIELD_UPDATED_AT to Timestamp.now()
        )

        if (targetStatus == MachineReport.MachineReportStatus.Resolved) {
            data[MachineReport.FIELD_RESOLVED_AT] = Timestamp.now()
        }

        return data
    }

    private fun statusSuccessMessage(
        status: MachineReport.MachineReportStatus
    ): String {
        return when (status) {
            MachineReport.MachineReportStatus.Pending -> "Đã chuyển báo lỗi về trạng thái chờ xử lý."
            MachineReport.MachineReportStatus.Processing -> "Đã tiếp nhận và bắt đầu xử lý báo lỗi."
            MachineReport.MachineReportStatus.Resolved -> "Đã hoàn tất xử lý báo lỗi."
            MachineReport.MachineReportStatus.Cancelled -> "Đã hủy báo lỗi máy."
        }
    }

    private fun normalizeFirestoreError(
        exception: Exception
    ): String {
        val raw = exception.message.orEmpty().lowercase(Locale.getDefault())

        return when {
            raw.contains("permission") ||
                    raw.contains("missing or insufficient permissions") -> {
                "Không có quyền truy cập dữ liệu báo lỗi máy. Kiểm tra Firestore Rules."
            }

            raw.contains("failed_precondition") ||
                    raw.contains("index") -> {
                "Firestore cần tạo index cho truy vấn báo lỗi máy."
            }

            raw.contains("network") ||
                    raw.contains("unavailable") ||
                    raw.contains("deadline") -> {
                "Không thể kết nối Firestore. Vui lòng kiểm tra mạng."
            }

            else -> {
                "Không thể xử lý báo lỗi máy. Vui lòng thử lại."
            }
        }
    }

    companion object {
        private const val MIN_DESCRIPTION_LENGTH = 8

        fun formatCreatedTime(
            report: MachineReport
        ): String {
            val date = report.createdAt?.toDate() ?: Date()

            return android.text.format.DateFormat
                .format("dd/MM/yyyy HH:mm", date)
                .toString()
        }

        fun pendingCount(
            reports: List<MachineReport>
        ): Int {
            return reports.count { it.normalizedCopy().isPending }
        }

        fun processingCount(
            reports: List<MachineReport>
        ): Int {
            return reports.count { it.normalizedCopy().isProcessing }
        }

        fun resolvedCount(
            reports: List<MachineReport>
        ): Int {
            return reports.count { it.normalizedCopy().isResolved }
        }

        fun cancelledCount(
            reports: List<MachineReport>
        ): Int {
            return reports.count { it.normalizedCopy().isCancelled }
        }
    }
}
