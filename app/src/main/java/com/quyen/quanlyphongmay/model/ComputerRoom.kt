package com.quyen.quanlyphongmay.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Locale

@IgnoreExtraProperties
data class ComputerRoom(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val location: String = "",
    val rows: Int = DEFAULT_ROWS,
    val columns: Int = DEFAULT_COLUMNS,
    val status: String = RoomStatus.Active.firestoreValue,
    val note: String = "",
    val createdBy: String = "",
    val updatedBy: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    val safeId: String
        get() = id.trim()

    val displayCode: String
        get() = code.trim().ifBlank { safeId }

    val displayName: String
        get() = name.trim().ifBlank {
            if (displayCode.isNotBlank()) {
                "Phòng $displayCode"
            } else {
                "Phòng máy"
            }
        }

    val displayLocation: String
        get() = location.trim()

    val normalizedRows: Int
        get() = rows.coerceIn(MIN_ROWS, MAX_ROWS)

    val normalizedColumns: Int
        get() = columns.coerceIn(MIN_COLUMNS, MAX_COLUMNS)

    val capacity: Int
        get() = normalizedRows * normalizedColumns

    val statusEnum: RoomStatus
        get() = RoomStatus.fromRaw(status)

    val statusNameVi: String
        get() = statusEnum.vietnameseName

    val isActive: Boolean
        get() = statusEnum == RoomStatus.Active

    val isMaintenance: Boolean
        get() = statusEnum == RoomStatus.Maintenance

    val isLocked: Boolean
        get() = statusEnum == RoomStatus.Locked

    val canUseForBooking: Boolean
        get() = isActive && capacity > 0

    val searchableText: String
        get() = listOf(
            displayCode,
            displayName,
            displayLocation,
            statusNameVi,
            note
        ).joinToString(" ").lowercase(Locale.getDefault())

    fun normalizedCopy(): ComputerRoom {
        val normalizedStatus = RoomStatus.fromRaw(status).firestoreValue

        return copy(
            id = safeId,
            code = code.trim(),
            name = name.trim(),
            location = location.trim(),
            rows = normalizedRows,
            columns = normalizedColumns,
            status = normalizedStatus,
            note = note.trim(),
            createdBy = createdBy.trim(),
            updatedBy = updatedBy.trim()
        )
    }

    fun matchesKeyword(keyword: String): Boolean {
        val cleanKeyword = keyword.trim().lowercase(Locale.getDefault())

        if (cleanKeyword.isBlank()) {
            return true
        }

        return searchableText.contains(cleanKeyword)
    }

    fun hasValidSeatPosition(row: Int, column: Int): Boolean {
        return row in 1..normalizedRows && column in 1..normalizedColumns
    }

    enum class RoomStatus(
        val firestoreValue: String,
        val vietnameseName: String
    ) {
        Active(
            firestoreValue = "active",
            vietnameseName = "Đang hoạt động"
        ),
        Maintenance(
            firestoreValue = "maintenance",
            vietnameseName = "Bảo trì"
        ),
        Locked(
            firestoreValue = "locked",
            vietnameseName = "Đã khóa"
        );

        companion object {
            fun fromRaw(raw: String?): RoomStatus {
                val value = raw
                    .orEmpty()
                    .trim()
                    .lowercase(Locale.getDefault())

                return when (value) {
                    "active",
                    "available",
                    "open",
                    "enabled",
                    "hoatdong",
                    "hoạt động",
                    "dang hoat dong",
                    "đang hoạt động",
                    "đang hoạt động" -> Active

                    "maintenance",
                    "maintain",
                    "repair",
                    "fixing",
                    "bao tri",
                    "bảo trì",
                    "sua chua",
                    "sửa chữa" -> Maintenance

                    "locked",
                    "lock",
                    "closed",
                    "disabled",
                    "khoa",
                    "khóa",
                    "da khoa",
                    "đã khóa" -> Locked

                    else -> Active
                }
            }
        }
    }

    companion object {
        const val COLLECTION = "computerRooms"

        const val FIELD_ID = "id"
        const val FIELD_CODE = "code"
        const val FIELD_NAME = "name"
        const val FIELD_LOCATION = "location"
        const val FIELD_ROWS = "rows"
        const val FIELD_COLUMNS = "columns"
        const val FIELD_STATUS = "status"
        const val FIELD_NOTE = "note"
        const val FIELD_CREATED_BY = "createdBy"
        const val FIELD_UPDATED_BY = "updatedBy"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"

        const val MIN_ROWS = 1
        const val MAX_ROWS = 20
        const val MIN_COLUMNS = 1
        const val MAX_COLUMNS = 20

        const val DEFAULT_ROWS = 5
        const val DEFAULT_COLUMNS = 8

        fun empty(): ComputerRoom {
            return ComputerRoom()
        }

        fun createDraft(
            code: String,
            name: String,
            location: String,
            rows: Int,
            columns: Int,
            status: RoomStatus = RoomStatus.Active,
            note: String = "",
            createdBy: String = ""
        ): ComputerRoom {
            return ComputerRoom(
                id = "",
                code = code.trim(),
                name = name.trim(),
                location = location.trim(),
                rows = rows.coerceIn(MIN_ROWS, MAX_ROWS),
                columns = columns.coerceIn(MIN_COLUMNS, MAX_COLUMNS),
                status = status.firestoreValue,
                note = note.trim(),
                createdBy = createdBy.trim(),
                updatedBy = createdBy.trim()
            )
        }

        fun statusOptions(): List<RoomStatus> {
            return RoomStatus.entries
        }
    }
}