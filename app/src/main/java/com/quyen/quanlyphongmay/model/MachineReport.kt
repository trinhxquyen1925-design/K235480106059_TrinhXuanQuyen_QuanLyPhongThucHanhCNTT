package com.quyen.quanlyphongmay.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Locale

@IgnoreExtraProperties
data class MachineReport(
    val id: String = "",
    val computerId: String = "",
    val computerCode: String = "",
    val computerName: String = "",
    val roomId: String = "",
    val roomCode: String = "",
    val roomName: String = "",
    val row: Int = 1,
    val column: Int = 1,
    val title: String = "",
    val description: String = "",
    val errorType: String = MachineReportErrorType.Other.firestoreValue,
    val priority: String = MachineReportPriority.Normal.firestoreValue,
    val status: String = MachineReportStatus.Pending.firestoreValue,
    val reporterId: String = "",
    val reporterEmail: String = "",
    val reporterName: String = "",
    val reporterRole: String = "",
    val handledById: String = "",
    val handledByEmail: String = "",
    val handledByName: String = "",
    val adminNote: String = "",
    val resolvedNote: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val resolvedAt: Timestamp? = null
) {
    val safeId: String
        get() = id.trim()

    val safeComputerId: String
        get() = computerId.trim()

    val safeRoomId: String
        get() = roomId.trim()

    val displayComputerCode: String
        get() = computerCode.trim().ifBlank {
            safeComputerId.ifBlank {
                "Máy tính"
            }
        }

    val displayComputerName: String
        get() = computerName.trim().ifBlank {
            if (displayComputerCode.isNotBlank()) {
                "Máy $displayComputerCode"
            } else {
                "Máy tính"
            }
        }

    val displayRoomCode: String
        get() = roomCode.trim()

    val displayRoomName: String
        get() = roomName.trim().ifBlank {
            if (displayRoomCode.isNotBlank()) {
                "Phòng $displayRoomCode"
            } else {
                "Phòng máy"
            }
        }

    val normalizedRow: Int
        get() = row.coerceIn(1, 20)

    val normalizedColumn: Int
        get() = column.coerceIn(1, 20)

    val seatLabel: String
        get() = "Hàng $normalizedRow • Cột $normalizedColumn"

    val displayTitle: String
        get() = title.trim().ifBlank {
            "${errorTypeEnum.vietnameseName} - $displayComputerCode"
        }

    val displayDescription: String
        get() = description.trim().ifBlank {
            "Không có mô tả chi tiết."
        }

    val displayReporterName: String
        get() = reporterName.trim().ifBlank {
            reporterEmail.trim().ifBlank {
                "Người báo lỗi"
            }
        }

    val displayHandlerName: String
        get() = handledByName.trim().ifBlank {
            handledByEmail.trim().ifBlank {
                "Chưa phân công"
            }
        }

    val statusEnum: MachineReportStatus
        get() = MachineReportStatus.fromRaw(status)

    val priorityEnum: MachineReportPriority
        get() = MachineReportPriority.fromRaw(priority)

    val errorTypeEnum: MachineReportErrorType
        get() = MachineReportErrorType.fromRaw(errorType)

    val statusNameVi: String
        get() = statusEnum.vietnameseName

    val priorityNameVi: String
        get() = priorityEnum.vietnameseName

    val errorTypeNameVi: String
        get() = errorTypeEnum.vietnameseName

    val isPending: Boolean
        get() = statusEnum == MachineReportStatus.Pending

    val isProcessing: Boolean
        get() = statusEnum == MachineReportStatus.Processing

    val isResolved: Boolean
        get() = statusEnum == MachineReportStatus.Resolved

    val isCancelled: Boolean
        get() = statusEnum == MachineReportStatus.Cancelled

    val canEditByReporter: Boolean
        get() = isPending

    val canProcessByAdminOrTeacher: Boolean
        get() = isPending || isProcessing

    val searchableText: String
        get() = listOf(
            displayTitle,
            displayDescription,
            displayComputerCode,
            displayComputerName,
            displayRoomCode,
            displayRoomName,
            seatLabel,
            errorTypeNameVi,
            priorityNameVi,
            statusNameVi,
            reporterEmail,
            reporterName,
            handledByEmail,
            handledByName,
            adminNote,
            resolvedNote
        ).joinToString(" ").lowercase(Locale.getDefault())

    fun matchesKeyword(
        keyword: String
    ): Boolean {
        val cleanKeyword = keyword.trim().lowercase(Locale.getDefault())

        if (cleanKeyword.isBlank()) {
            return true
        }

        return searchableText.contains(cleanKeyword)
    }

    fun normalizedCopy(): MachineReport {
        return copy(
            id = safeId,
            computerId = safeComputerId,
            computerCode = computerCode.trim(),
            computerName = computerName.trim(),
            roomId = safeRoomId,
            roomCode = roomCode.trim(),
            roomName = roomName.trim(),
            row = normalizedRow,
            column = normalizedColumn,
            title = title.trim(),
            description = description.trim(),
            errorType = MachineReportErrorType.fromRaw(errorType).firestoreValue,
            priority = MachineReportPriority.fromRaw(priority).firestoreValue,
            status = MachineReportStatus.fromRaw(status).firestoreValue,
            reporterId = reporterId.trim(),
            reporterEmail = reporterEmail.trim(),
            reporterName = reporterName.trim(),
            reporterRole = reporterRole.trim(),
            handledById = handledById.trim(),
            handledByEmail = handledByEmail.trim(),
            handledByName = handledByName.trim(),
            adminNote = adminNote.trim(),
            resolvedNote = resolvedNote.trim(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            resolvedAt = resolvedAt
        )
    }

    enum class MachineReportStatus(
        val firestoreValue: String,
        val vietnameseName: String
    ) {
        Pending(
            firestoreValue = "pending",
            vietnameseName = "Chờ xử lý"
        ),
        Processing(
            firestoreValue = "processing",
            vietnameseName = "Đang xử lý"
        ),
        Resolved(
            firestoreValue = "resolved",
            vietnameseName = "Đã xử lý"
        ),
        Cancelled(
            firestoreValue = "cancelled",
            vietnameseName = "Đã hủy"
        );

        companion object {
            fun fromRaw(
                raw: String?
            ): MachineReportStatus {
                val value = raw
                    .orEmpty()
                    .trim()
                    .lowercase(Locale.getDefault())

                return when (value) {
                    "pending",
                    "wait",
                    "waiting",
                    "cho_xu_ly",
                    "cho xu ly",
                    "chờ xử lý",
                    "cho",
                    "chờ" -> Pending

                    "processing",
                    "process",
                    "handling",
                    "in_progress",
                    "dang_xu_ly",
                    "dang xu ly",
                    "đang xử lý" -> Processing

                    "resolved",
                    "done",
                    "fixed",
                    "completed",
                    "da_xu_ly",
                    "da xu ly",
                    "đã xử lý",
                    "hoan_tat",
                    "hoàn tất" -> Resolved

                    "cancelled",
                    "canceled",
                    "cancel",
                    "huy",
                    "hủy",
                    "da_huy",
                    "đã hủy" -> Cancelled

                    else -> Pending
                }
            }

            fun options(): List<MachineReportStatus> {
                return values().toList()
            }
        }
    }

    enum class MachineReportPriority(
        val firestoreValue: String,
        val vietnameseName: String
    ) {
        Low(
            firestoreValue = "low",
            vietnameseName = "Thấp"
        ),
        Normal(
            firestoreValue = "normal",
            vietnameseName = "Bình thường"
        ),
        High(
            firestoreValue = "high",
            vietnameseName = "Cao"
        ),
        Critical(
            firestoreValue = "critical",
            vietnameseName = "Khẩn cấp"
        );

        companion object {
            fun fromRaw(
                raw: String?
            ): MachineReportPriority {
                val value = raw
                    .orEmpty()
                    .trim()
                    .lowercase(Locale.getDefault())

                return when (value) {
                    "low",
                    "thap",
                    "thấp" -> Low

                    "high",
                    "cao" -> High

                    "critical",
                    "urgent",
                    "khan_cap",
                    "khẩn cấp",
                    "rat_cao",
                    "rất cao" -> Critical

                    else -> Normal
                }
            }

            fun options(): List<MachineReportPriority> {
                return values().toList()
            }
        }
    }

    enum class MachineReportErrorType(
        val firestoreValue: String,
        val vietnameseName: String
    ) {
        Hardware(
            firestoreValue = "hardware",
            vietnameseName = "Lỗi phần cứng"
        ),
        Software(
            firestoreValue = "software",
            vietnameseName = "Lỗi phần mềm"
        ),
        Network(
            firestoreValue = "network",
            vietnameseName = "Lỗi mạng"
        ),
        Peripheral(
            firestoreValue = "peripheral",
            vietnameseName = "Lỗi thiết bị ngoại vi"
        ),
        Performance(
            firestoreValue = "performance",
            vietnameseName = "Máy chậm/treo"
        ),
        Other(
            firestoreValue = "other",
            vietnameseName = "Lỗi khác"
        );

        companion object {
            fun fromRaw(
                raw: String?
            ): MachineReportErrorType {
                val value = raw
                    .orEmpty()
                    .trim()
                    .lowercase(Locale.getDefault())

                return when (value) {
                    "hardware",
                    "phan_cung",
                    "phần cứng",
                    "main",
                    "ram",
                    "cpu",
                    "disk",
                    "ssd",
                    "hdd" -> Hardware

                    "software",
                    "phan_mem",
                    "phần mềm",
                    "app",
                    "windows",
                    "os" -> Software

                    "network",
                    "internet",
                    "mang",
                    "mạng",
                    "wifi",
                    "lan" -> Network

                    "peripheral",
                    "ngoai_vi",
                    "ngoại vi",
                    "keyboard",
                    "mouse",
                    "monitor",
                    "man_hinh",
                    "màn hình" -> Peripheral

                    "performance",
                    "slow",
                    "lag",
                    "treo",
                    "cham",
                    "chậm" -> Performance

                    else -> Other
                }
            }

            fun options(): List<MachineReportErrorType> {
                return values().toList()
            }
        }
    }

    companion object {
        const val COLLECTION = "machineReports"

        const val FIELD_ID = "id"
        const val FIELD_COMPUTER_ID = "computerId"
        const val FIELD_COMPUTER_CODE = "computerCode"
        const val FIELD_COMPUTER_NAME = "computerName"
        const val FIELD_ROOM_ID = "roomId"
        const val FIELD_ROOM_CODE = "roomCode"
        const val FIELD_ROOM_NAME = "roomName"
        const val FIELD_ROW = "row"
        const val FIELD_COLUMN = "column"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_ERROR_TYPE = "errorType"
        const val FIELD_PRIORITY = "priority"
        const val FIELD_STATUS = "status"
        const val FIELD_REPORTER_ID = "reporterId"
        const val FIELD_REPORTER_EMAIL = "reporterEmail"
        const val FIELD_REPORTER_NAME = "reporterName"
        const val FIELD_REPORTER_ROLE = "reporterRole"
        const val FIELD_HANDLED_BY_ID = "handledById"
        const val FIELD_HANDLED_BY_EMAIL = "handledByEmail"
        const val FIELD_HANDLED_BY_NAME = "handledByName"
        const val FIELD_ADMIN_NOTE = "adminNote"
        const val FIELD_RESOLVED_NOTE = "resolvedNote"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_RESOLVED_AT = "resolvedAt"

        fun empty(): MachineReport {
            return MachineReport()
        }

        fun createDraft(
            computer: Computer,
            reporter: User,
            title: String,
            description: String,
            errorType: MachineReportErrorType = MachineReportErrorType.Other,
            priority: MachineReportPriority = MachineReportPriority.Normal
        ): MachineReport {
            val safeComputer = computer.normalizedCopy()
            val safeReporter = reporter.normalizedCopy()

            return MachineReport(
                computerId = safeComputer.safeId,
                computerCode = safeComputer.displayCode,
                computerName = safeComputer.displayName,
                roomId = safeComputer.safeRoomId,
                roomCode = safeComputer.displayRoomCode,
                roomName = safeComputer.displayRoomName,
                row = safeComputer.normalizedRow,
                column = safeComputer.normalizedColumn,
                title = title.trim(),
                description = description.trim(),
                errorType = errorType.firestoreValue,
                priority = priority.firestoreValue,
                status = MachineReportStatus.Pending.firestoreValue,
                reporterId = safeReporter.uid,
                reporterEmail = safeReporter.email,
                reporterName = safeReporter.displayName.ifBlank { safeReporter.email },
                reporterRole = safeReporter.role
            ).normalizedCopy()
        }

        fun statusOptions(): List<MachineReportStatus> {
            return MachineReportStatus.options()
        }

        fun priorityOptions(): List<MachineReportPriority> {
            return MachineReportPriority.options()
        }

        fun errorTypeOptions(): List<MachineReportErrorType> {
            return MachineReportErrorType.options()
        }
    }
}
