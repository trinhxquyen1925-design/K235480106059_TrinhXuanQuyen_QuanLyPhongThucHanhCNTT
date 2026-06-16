package com.quyen.quanlyphongmay.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Locale

@IgnoreExtraProperties
data class Computer(
    val id: String = "",
    val roomId: String = "",
    val roomCode: String = "",
    val roomName: String = "",
    val code: String = "",
    val name: String = "",
    val row: Int = DEFAULT_ROW,
    val column: Int = DEFAULT_COLUMN,
    val status: String = ComputerStatus.Available.firestoreValue,
    val cpu: String = "",
    val ram: String = "",
    val storage: String = "",
    val monitor: String = "",
    val ipAddress: String = "",
    val gpu: String = "",
    val operatingSystem: String = "",
    val note: String = "",
    val currentUserId: String = "",
    val currentBookingId: String = "",
    val createdBy: String = "",
    val updatedBy: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    val safeId: String
        get() = id.trim()

    val safeRoomId: String
        get() = roomId.trim()

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

    val displayCode: String
        get() = code.trim().ifBlank { safeId }

    val displayName: String
        get() = name.trim().ifBlank {
            if (displayCode.isNotBlank()) {
                "Máy $displayCode"
            } else {
                "Máy tính"
            }
        }

    val displayCpu: String
        get() = cpu.trim().ifBlank { "Chưa nhập CPU" }

    val displayRam: String
        get() = ram.trim().ifBlank { "Chưa nhập RAM" }

    val displayStorage: String
        get() = storage.trim().ifBlank { "Chưa nhập ổ cứng" }

    val displayMonitor: String
        get() = monitor.trim().ifBlank { "Chưa nhập màn hình" }

    val displayGpu: String
        get() = gpu.trim().ifBlank {
            ipAddress.trim().ifBlank {
                "Chưa nhập GPU"
            }
        }

    val displayOperatingSystem: String
        get() = operatingSystem.trim().ifBlank { "Chưa nhập hệ điều hành" }

    val normalizedRow: Int
        get() = row.coerceIn(MIN_ROW, MAX_ROW)

    val normalizedColumn: Int
        get() = column.coerceIn(MIN_COLUMN, MAX_COLUMN)

    val seatLabel: String
        get() = "Hàng $normalizedRow - Cột $normalizedColumn"

    val compactSeatLabel: String
        get() = "H$normalizedRow-C$normalizedColumn"

    val statusEnum: ComputerStatus
        get() = ComputerStatus.fromRaw(status)

    val statusNameVi: String
        get() = statusEnum.vietnameseName

    val isAvailable: Boolean
        get() = statusEnum == ComputerStatus.Available

    val isInUse: Boolean
        get() = statusEnum == ComputerStatus.InUse

    val isBroken: Boolean
        get() = statusEnum == ComputerStatus.Broken

    val isMaintenance: Boolean
        get() = statusEnum == ComputerStatus.Maintenance

    val isLocked: Boolean
        get() = statusEnum == ComputerStatus.Locked

    val canBeAssigned: Boolean
        get() = isAvailable && safeRoomId.isNotBlank()

    val isOccupiedByBooking: Boolean
        get() = currentBookingId.trim().isNotBlank()

    val isOccupiedByUser: Boolean
        get() = currentUserId.trim().isNotBlank()

    val hardwareSummary: String
        get() = listOf(
            cpu.trim(),
            ram.trim(),
            storage.trim(),
            gpu.trim().ifBlank { ipAddress.trim() },
            operatingSystem.trim()
        )
            .filter { value -> value.isNotBlank() }
            .joinToString(" • ")
            .ifBlank { "Chưa nhập cấu hình" }

    val searchableText: String
        get() = listOf(
            displayCode,
            displayName,
            displayRoomCode,
            displayRoomName,
            seatLabel,
            compactSeatLabel,
            statusNameVi,
            cpu,
            ram,
            storage,
            monitor,
            ipAddress,
            gpu,
            operatingSystem,
            note
        ).joinToString(" ").lowercase(Locale.getDefault())

    fun normalizedCopy(): Computer {
        val normalizedStatus = ComputerStatus.fromRaw(status).firestoreValue
        val cleanGpu = gpu.trim().ifBlank {
            ipAddress.trim()
        }

        return copy(
            id = safeId,
            roomId = safeRoomId,
            roomCode = roomCode.trim(),
            roomName = roomName.trim(),
            code = code.trim(),
            name = name.trim(),
            row = normalizedRow,
            column = normalizedColumn,
            status = normalizedStatus,
            cpu = cpu.trim(),
            ram = ram.trim(),
            storage = storage.trim(),
            monitor = monitor.trim(),
            ipAddress = ipAddress.trim(),
            gpu = cleanGpu,
            operatingSystem = operatingSystem.trim(),
            note = note.trim(),
            currentUserId = currentUserId.trim(),
            currentBookingId = currentBookingId.trim(),
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

    fun isInRoom(targetRoomId: String): Boolean {
        val cleanRoomId = targetRoomId.trim()

        if (cleanRoomId.isBlank()) {
            return true
        }

        return safeRoomId == cleanRoomId
    }

    fun isAtPosition(targetRow: Int, targetColumn: Int): Boolean {
        return normalizedRow == targetRow && normalizedColumn == targetColumn
    }

    fun canChangeToStatus(targetStatus: ComputerStatus): Boolean {
        return when (targetStatus) {
            ComputerStatus.Available -> !isLocked
            ComputerStatus.InUse -> isAvailable
            ComputerStatus.Broken -> !isLocked
            ComputerStatus.Maintenance -> !isLocked
            ComputerStatus.Locked -> true
        }
    }

    enum class ComputerStatus(
        val firestoreValue: String,
        val vietnameseName: String
    ) {
        Available(
            firestoreValue = "available",
            vietnameseName = "Rảnh"
        ),
        InUse(
            firestoreValue = "in_use",
            vietnameseName = "Đang dùng"
        ),
        Broken(
            firestoreValue = "broken",
            vietnameseName = "Hỏng"
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
            fun fromRaw(raw: String?): ComputerStatus {
                val value = raw
                    .orEmpty()
                    .trim()
                    .lowercase(Locale.getDefault())

                return when (value) {
                    "available",
                    "free",
                    "ready",
                    "idle",
                    "empty",
                    "ranh",
                    "rảnh",
                    "san sang",
                    "sẵn sàng" -> Available

                    "in_use",
                    "inuse",
                    "using",
                    "used",
                    "busy",
                    "occupied",
                    "dang dung",
                    "đang dùng",
                    "dang su dung",
                    "đang sử dụng" -> InUse

                    "broken",
                    "error",
                    "failed",
                    "damaged",
                    "hong",
                    "hỏng",
                    "loi",
                    "lỗi" -> Broken

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
                    "disabled",
                    "blocked",
                    "khoa",
                    "khóa",
                    "da khoa",
                    "đã khóa" -> Locked

                    else -> Available
                }
            }

            fun options(): List<ComputerStatus> {
                return values().toList()
            }
        }
    }

    companion object {
        const val COLLECTION = "computers"

        const val FIELD_ID = "id"
        const val FIELD_ROOM_ID = "roomId"
        const val FIELD_ROOM_CODE = "roomCode"
        const val FIELD_ROOM_NAME = "roomName"
        const val FIELD_CODE = "code"
        const val FIELD_NAME = "name"
        const val FIELD_ROW = "row"
        const val FIELD_COLUMN = "column"
        const val FIELD_STATUS = "status"
        const val FIELD_CPU = "cpu"
        const val FIELD_RAM = "ram"
        const val FIELD_STORAGE = "storage"
        const val FIELD_MONITOR = "monitor"

        const val FIELD_IP_ADDRESS = "ipAddress"
        const val FIELD_GPU = "gpu"
        const val FIELD_OPERATING_SYSTEM = "operatingSystem"
        const val FIELD_OS = "os"

        const val FIELD_NOTE = "note"
        const val FIELD_CURRENT_USER_ID = "currentUserId"
        const val FIELD_CURRENT_BOOKING_ID = "currentBookingId"
        const val FIELD_CREATED_BY = "createdBy"
        const val FIELD_UPDATED_BY = "updatedBy"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"

        const val MIN_ROW = 1
        const val MAX_ROW = 20
        const val MIN_COLUMN = 1
        const val MAX_COLUMN = 20

        const val DEFAULT_ROW = 1
        const val DEFAULT_COLUMN = 1

        fun empty(): Computer {
            return Computer()
        }

        fun createDraft(
            room: ComputerRoom,
            code: String,
            name: String,
            row: Int,
            column: Int,
            status: ComputerStatus = ComputerStatus.Available,
            cpu: String = "",
            ram: String = "",
            storage: String = "",
            monitor: String = "",
            ipAddress: String = "",
            gpu: String = "",
            operatingSystem: String = "",
            note: String = "",
            createdBy: String = ""
        ): Computer {
            val normalizedRoom = room.normalizedCopy()
            val cleanGpu = gpu.trim().ifBlank {
                ipAddress.trim()
            }

            return Computer(
                id = "",
                roomId = normalizedRoom.safeId,
                roomCode = normalizedRoom.displayCode,
                roomName = normalizedRoom.displayName,
                code = code.trim(),
                name = name.trim(),
                row = row.coerceIn(MIN_ROW, MAX_ROW),
                column = column.coerceIn(MIN_COLUMN, MAX_COLUMN),
                status = status.firestoreValue,
                cpu = cpu.trim(),
                ram = ram.trim(),
                storage = storage.trim(),
                monitor = monitor.trim(),
                ipAddress = ipAddress.trim(),
                gpu = cleanGpu,
                operatingSystem = operatingSystem.trim(),
                note = note.trim(),
                createdBy = createdBy.trim(),
                updatedBy = createdBy.trim()
            )
        }

        fun createBulkDrafts(
            room: ComputerRoom,
            prefix: String,
            startIndex: Int = 1,
            cpu: String = "",
            ram: String = "",
            storage: String = "",
            monitor: String = "",
            gpu: String = "",
            operatingSystem: String = "",
            createdBy: String = ""
        ): List<Computer> {
            val normalizedRoom = room.normalizedCopy()
            val safePrefix = prefix.trim().ifBlank { normalizedRoom.displayCode.ifBlank { "PC" } }
            val drafts = mutableListOf<Computer>()
            var index = startIndex.coerceAtLeast(1)

            for (row in 1..normalizedRoom.normalizedRows) {
                for (column in 1..normalizedRoom.normalizedColumns) {
                    val code = "$safePrefix-${index.toString().padStart(2, '0')}"

                    drafts.add(
                        createDraft(
                            room = normalizedRoom,
                            code = code,
                            name = "Máy $code",
                            row = row,
                            column = column,
                            status = ComputerStatus.Available,
                            cpu = cpu,
                            ram = ram,
                            storage = storage,
                            monitor = monitor,
                            gpu = gpu,
                            operatingSystem = operatingSystem,
                            createdBy = createdBy
                        )
                    )

                    index++
                }
            }

            return drafts
        }

        fun statusOptions(): List<ComputerStatus> {
            return ComputerStatus.options()
        }
    }
}