package com.quyen.quanlyphongmay.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quyen.quanlyphongmay.core.AppAuthContract
import com.quyen.quanlyphongmay.model.ComputerRoom
import com.quyen.quanlyphongmay.model.User
import java.util.Locale

class ComputerRoomRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val roomsCollection = firestore.collection(
        ComputerRoom.COLLECTION
    )

    data class RoomFilter(
        val keyword: String = "",
        val status: String = ""
    )

    data class RoomResult(
        val rooms: List<ComputerRoom> = emptyList(),
        val total: Int = 0,
        val activeCount: Int = 0,
        val maintenanceCount: Int = 0,
        val lockedCount: Int = 0,
        val totalCapacity: Int = 0
    )

    fun getRooms(
        filter: RoomFilter = RoomFilter(),
        callback: (Boolean, String?, RoomResult) -> Unit
    ) {
        roomsCollection
            .orderBy(
                ComputerRoom.FIELD_CREATED_AT,
                Query.Direction.DESCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->
                val rawRooms = snapshot.documents.mapNotNull { document ->
                    document.toSafeComputerRoom()
                }

                val normalizedRooms = rawRooms.map { room ->
                    room.normalizedCopy()
                }

                val filteredRooms = applyFilter(
                    rooms = normalizedRooms,
                    filter = filter
                )

                callback(
                    true,
                    null,
                    buildRoomResult(filteredRooms)
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message),
                    RoomResult()
                )
            }
    }

    fun getRoomById(
        roomId: String,
        callback: (Boolean, String?, ComputerRoom?) -> Unit
    ) {
        val cleanRoomId = roomId.trim()

        if (cleanRoomId.isBlank()) {
            callback(false, "Không tìm thấy mã phòng máy.", null)
            return
        }

        roomsCollection
            .document(cleanRoomId)
            .get()
            .addOnSuccessListener { document ->
                val room = document.toSafeComputerRoom()?.normalizedCopy()

                if (room == null) {
                    callback(false, "Không tìm thấy phòng máy.", null)
                } else {
                    callback(true, null, room)
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

    fun createRoom(
        room: ComputerRoom,
        adminUser: User,
        callback: (Boolean, String?, ComputerRoom?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.", null)
            return
        }

        val validationMessage = validateRoom(room)

        if (validationMessage != null) {
            callback(false, validationMessage, null)
            return
        }

        val normalizedRoom = room.normalizedCopy()
        val documentRef = roomsCollection.document()
        val adminId = adminUser.safeId.ifBlank { adminUser.email }

        val data = linkedMapOf<String, Any>(
            ComputerRoom.FIELD_ID to documentRef.id,
            ComputerRoom.FIELD_CODE to normalizedRoom.displayCode.ifBlank {
                buildRoomCodeFromName(normalizedRoom.displayName)
            },
            ComputerRoom.FIELD_NAME to normalizedRoom.displayName,
            ComputerRoom.FIELD_LOCATION to normalizedRoom.displayLocation,
            ComputerRoom.FIELD_ROWS to normalizedRoom.normalizedRows,
            ComputerRoom.FIELD_COLUMNS to normalizedRoom.normalizedColumns,
            ComputerRoom.FIELD_STATUS to normalizedRoom.statusEnum.firestoreValue,
            ComputerRoom.FIELD_NOTE to normalizedRoom.note,
            ComputerRoom.FIELD_CREATED_BY to adminId,
            ComputerRoom.FIELD_UPDATED_BY to adminId,
            ComputerRoom.FIELD_CREATED_AT to FieldValue.serverTimestamp(),
            ComputerRoom.FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )

        documentRef
            .set(data)
            .addOnSuccessListener {
                documentRef
                    .get()
                    .addOnSuccessListener { createdDocument ->
                        callback(
                            true,
                            "Đã thêm phòng máy.",
                            createdDocument.toSafeComputerRoom()?.normalizedCopy()
                        )
                    }
                    .addOnFailureListener { exception ->
                        callback(
                            false,
                            AppAuthContract.normalizeAuthMessage(exception.message),
                            null
                        )
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

    fun updateRoom(
        room: ComputerRoom,
        adminUser: User,
        callback: (Boolean, String?, ComputerRoom?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.", null)
            return
        }

        val normalizedRoom = room.normalizedCopy()
        val roomId = normalizedRoom.safeId

        if (roomId.isBlank()) {
            callback(false, "Không tìm thấy mã phòng máy cần cập nhật.", null)
            return
        }

        val validationMessage = validateRoom(normalizedRoom)

        if (validationMessage != null) {
            callback(false, validationMessage, null)
            return
        }

        val adminId = adminUser.safeId.ifBlank { adminUser.email }

        val patch = linkedMapOf<String, Any>(
            ComputerRoom.FIELD_CODE to normalizedRoom.displayCode,
            ComputerRoom.FIELD_NAME to normalizedRoom.displayName,
            ComputerRoom.FIELD_LOCATION to normalizedRoom.displayLocation,
            ComputerRoom.FIELD_ROWS to normalizedRoom.normalizedRows,
            ComputerRoom.FIELD_COLUMNS to normalizedRoom.normalizedColumns,
            ComputerRoom.FIELD_STATUS to normalizedRoom.statusEnum.firestoreValue,
            ComputerRoom.FIELD_NOTE to normalizedRoom.note,
            ComputerRoom.FIELD_UPDATED_BY to adminId,
            ComputerRoom.FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )

        roomsCollection
            .document(roomId)
            .update(patch)
            .addOnSuccessListener {
                roomsCollection
                    .document(roomId)
                    .get()
                    .addOnSuccessListener { updatedDocument ->
                        callback(
                            true,
                            "Đã cập nhật phòng máy.",
                            updatedDocument.toSafeComputerRoom()?.normalizedCopy()
                        )
                    }
                    .addOnFailureListener { exception ->
                        callback(
                            false,
                            AppAuthContract.normalizeAuthMessage(exception.message),
                            null
                        )
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

    fun changeRoomStatus(
        room: ComputerRoom,
        status: ComputerRoom.RoomStatus,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val normalizedRoom = room.normalizedCopy()
        val roomId = normalizedRoom.safeId

        if (roomId.isBlank()) {
            callback(false, "Không tìm thấy mã phòng máy cần cập nhật.")
            return
        }

        val adminId = adminUser.safeId.ifBlank { adminUser.email }

        val patch = linkedMapOf<String, Any>(
            ComputerRoom.FIELD_STATUS to status.firestoreValue,
            ComputerRoom.FIELD_UPDATED_BY to adminId,
            ComputerRoom.FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )

        roomsCollection
            .document(roomId)
            .update(patch)
            .addOnSuccessListener {
                callback(true, statusMessage(status))
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun setRoomActive(
        room: ComputerRoom,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        changeRoomStatus(
            room = room,
            status = ComputerRoom.RoomStatus.Active,
            adminUser = adminUser,
            callback = callback
        )
    }

    fun setRoomMaintenance(
        room: ComputerRoom,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        changeRoomStatus(
            room = room,
            status = ComputerRoom.RoomStatus.Maintenance,
            adminUser = adminUser,
            callback = callback
        )
    }

    fun lockRoom(
        room: ComputerRoom,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        changeRoomStatus(
            room = room,
            status = ComputerRoom.RoomStatus.Locked,
            adminUser = adminUser,
            callback = callback
        )
    }

    fun deleteRoom(
        room: ComputerRoom,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        val normalizedRoom = room.normalizedCopy()
        val roomId = normalizedRoom.safeId

        if (roomId.isBlank()) {
            callback(false, "Không tìm thấy mã phòng máy cần xóa.")
            return
        }

        roomsCollection
            .document(roomId)
            .delete()
            .addOnSuccessListener {
                callback(true, "Đã xóa phòng máy.")
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    AppAuthContract.normalizeAuthMessage(exception.message)
                )
            }
    }

    fun normalizeExistingRooms(
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!adminUser.isAdmin) {
            callback(false, "Bạn không có quyền thực hiện thao tác này.")
            return
        }

        roomsCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                var changedCount = 0
                val adminId = adminUser.safeId.ifBlank { adminUser.email }

                snapshot.documents.forEach { document ->
                    val room = document.toSafeComputerRoom() ?: return@forEach
                    val normalizedRoom = room.normalizedCopy()

                    val patch = linkedMapOf<String, Any>(
                        ComputerRoom.FIELD_ID to normalizedRoom.safeId.ifBlank { document.id },
                        ComputerRoom.FIELD_CODE to normalizedRoom.displayCode.ifBlank {
                            buildRoomCodeFromName(normalizedRoom.displayName)
                        },
                        ComputerRoom.FIELD_NAME to normalizedRoom.displayName,
                        ComputerRoom.FIELD_LOCATION to normalizedRoom.displayLocation,
                        ComputerRoom.FIELD_ROWS to normalizedRoom.normalizedRows,
                        ComputerRoom.FIELD_COLUMNS to normalizedRoom.normalizedColumns,
                        ComputerRoom.FIELD_STATUS to normalizedRoom.statusEnum.firestoreValue,
                        ComputerRoom.FIELD_NOTE to normalizedRoom.note,
                        ComputerRoom.FIELD_UPDATED_BY to adminId,
                        ComputerRoom.FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                    )

                    batch.update(
                        roomsCollection.document(document.id),
                        patch
                    )

                    changedCount++
                }

                if (changedCount == 0) {
                    callback(true, "Không có phòng máy cần chuẩn hóa.")
                    return@addOnSuccessListener
                }

                batch.commit()
                    .addOnSuccessListener {
                        callback(true, "Đã chuẩn hóa $changedCount phòng máy.")
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
        rooms: List<ComputerRoom>,
        filter: RoomFilter
    ): List<ComputerRoom> {
        val keyword = filter.keyword.trim()
        val status = filter.status.trim()

        return rooms.filter { room ->
            val matchesKeyword = room.matchesKeyword(keyword)

            val matchesStatus = if (status.isBlank()) {
                true
            } else {
                room.statusEnum == ComputerRoom.RoomStatus.fromRaw(status)
            }

            matchesKeyword && matchesStatus
        }
    }

    private fun buildRoomResult(
        rooms: List<ComputerRoom>
    ): RoomResult {
        return RoomResult(
            rooms = rooms,
            total = rooms.size,
            activeCount = rooms.count { it.isActive },
            maintenanceCount = rooms.count { it.isMaintenance },
            lockedCount = rooms.count { it.isLocked },
            totalCapacity = rooms.sumOf { it.capacity }
        )
    }

    private fun validateRoom(
        room: ComputerRoom
    ): String? {
        val normalizedRoom = room.normalizedCopy()

        return when {
            normalizedRoom.displayName.isBlank() -> {
                "Vui lòng nhập tên phòng máy."
            }

            normalizedRoom.displayCode.isBlank() -> {
                "Vui lòng nhập mã phòng máy."
            }

            normalizedRoom.normalizedRows !in ComputerRoom.MIN_ROWS..ComputerRoom.MAX_ROWS -> {
                "Số hàng phải từ ${ComputerRoom.MIN_ROWS} đến ${ComputerRoom.MAX_ROWS}."
            }

            normalizedRoom.normalizedColumns !in ComputerRoom.MIN_COLUMNS..ComputerRoom.MAX_COLUMNS -> {
                "Số cột phải từ ${ComputerRoom.MIN_COLUMNS} đến ${ComputerRoom.MAX_COLUMNS}."
            }

            normalizedRoom.capacity <= 0 -> {
                "Sức chứa phòng máy không hợp lệ."
            }

            else -> null
        }
    }

    private fun statusMessage(
        status: ComputerRoom.RoomStatus
    ): String {
        return when (status) {
            ComputerRoom.RoomStatus.Active -> "Đã mở hoạt động phòng máy."
            ComputerRoom.RoomStatus.Maintenance -> "Đã chuyển phòng máy sang bảo trì."
            ComputerRoom.RoomStatus.Locked -> "Đã khóa phòng máy."
        }
    }

    private fun buildRoomCodeFromName(
        name: String
    ): String {
        val cleanName = name
            .trim()
            .uppercase(Locale.getDefault())
            .replace(Regex("[^A-Z0-9]+"), "-")
            .trim('-')

        return cleanName.ifBlank {
            "ROOM"
        }
    }

    private fun DocumentSnapshot.toSafeComputerRoom(): ComputerRoom? {
        if (!exists()) {
            return null
        }

        val firestoreRoom = toObject(ComputerRoom::class.java) ?: return null

        val fixedId = firestoreRoom.id.ifBlank {
            getString(ComputerRoom.FIELD_ID).orEmpty().ifBlank {
                id
            }
        }

        val fixedCode = firestoreRoom.code.ifBlank {
            getString(ComputerRoom.FIELD_CODE).orEmpty()
        }

        val fixedName = firestoreRoom.name.ifBlank {
            getString(ComputerRoom.FIELD_NAME).orEmpty()
        }

        val fixedLocation = firestoreRoom.location.ifBlank {
            getString(ComputerRoom.FIELD_LOCATION).orEmpty()
        }

        val fixedRows = readInt(
            field = ComputerRoom.FIELD_ROWS,
            fallback = firestoreRoom.rows
        )

        val fixedColumns = readInt(
            field = ComputerRoom.FIELD_COLUMNS,
            fallback = firestoreRoom.columns
        )

        val fixedStatus = ComputerRoom.RoomStatus.fromRaw(
            firestoreRoom.status.ifBlank {
                getString(ComputerRoom.FIELD_STATUS).orEmpty()
            }
        ).firestoreValue

        val fixedNote = firestoreRoom.note.ifBlank {
            getString(ComputerRoom.FIELD_NOTE).orEmpty()
        }

        val fixedCreatedBy = firestoreRoom.createdBy.ifBlank {
            getString(ComputerRoom.FIELD_CREATED_BY).orEmpty()
        }

        val fixedUpdatedBy = firestoreRoom.updatedBy.ifBlank {
            getString(ComputerRoom.FIELD_UPDATED_BY).orEmpty()
        }

        return firestoreRoom.copy(
            id = fixedId,
            code = fixedCode,
            name = fixedName,
            location = fixedLocation,
            rows = fixedRows,
            columns = fixedColumns,
            status = fixedStatus,
            note = fixedNote,
            createdBy = fixedCreatedBy,
            updatedBy = fixedUpdatedBy
        )
    }

    private fun DocumentSnapshot.readInt(
        field: String,
        fallback: Int
    ): Int {
        val value = get(field)

        return when (value) {
            is Long -> value.toInt()
            is Int -> value
            is Double -> value.toInt()
            is Float -> value.toInt()
            is String -> value.toIntOrNull() ?: fallback
            else -> fallback
        }
    }
}