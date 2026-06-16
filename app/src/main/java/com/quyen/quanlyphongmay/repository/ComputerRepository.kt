package com.quyen.quanlyphongmay.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quyen.quanlyphongmay.model.Computer
import com.quyen.quanlyphongmay.model.ComputerRoom
import com.quyen.quanlyphongmay.model.User
import java.util.Locale

class ComputerRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val computersCollection = firestore.collection(COLLECTION_COMPUTERS)

    data class ComputerFilter(
        val keyword: String = "",
        val roomId: String = "",
        val status: String = ""
    )

    data class ComputerResult(
        val computers: List<Computer> = emptyList(),
        val total: Int = 0,
        val availableCount: Int = 0,
        val inUseCount: Int = 0,
        val brokenCount: Int = 0,
        val maintenanceCount: Int = 0,
        val lockedCount: Int = 0
    )

    fun getComputers(
        filter: ComputerFilter = ComputerFilter(),
        callback: (Boolean, String?, ComputerResult) -> Unit
    ) {
        try {
            computersCollection
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val allComputers = snapshot.documents.mapNotNull { document ->
                            documentToComputerSafe(
                                documentId = document.id,
                                data = document.data.orEmpty()
                            )
                        }

                        val filteredComputers = allComputers
                            .filter { computer ->
                                val safeComputer = computer.normalizedCopy()
                                val roomMatched = filter.roomId.trim().isBlank() ||
                                        safeComputer.safeRoomId == filter.roomId.trim()
                                val statusMatched = filter.status.trim().isBlank() ||
                                        safeComputer.statusEnum.firestoreValue == normalizeStatusValue(filter.status)
                                val keywordMatched = filter.keyword.trim().isBlank() ||
                                        safeComputer.matchesKeyword(filter.keyword.trim())

                                roomMatched && statusMatched && keywordMatched
                            }
                            .sortedWith(
                                compareBy<Computer>(
                                    { it.displayRoomCode.lowercase(Locale.getDefault()) },
                                    { it.normalizedRow },
                                    { it.normalizedColumn },
                                    { it.displayCode.lowercase(Locale.getDefault()) }
                                )
                            )

                        callback(
                            true,
                            null,
                            buildComputerResult(filteredComputers)
                        )
                    } catch (exception: Exception) {
                        callback(
                            false,
                            normalizeFirestoreError(exception),
                            ComputerResult()
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        ComputerResult()
                    )
                }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                ComputerResult()
            )
        }
    }

    fun getComputersByRoom(
        roomId: String,
        callback: (Boolean, String?, List<Computer>) -> Unit
    ) {
        val cleanRoomId = roomId.trim()

        if (cleanRoomId.isBlank()) {
            callback(true, null, emptyList())
            return
        }

        try {
            computersCollection
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val computers = snapshot.documents
                            .mapNotNull { document ->
                                documentToComputerSafe(
                                    documentId = document.id,
                                    data = document.data.orEmpty()
                                )
                            }
                            .filter { computer ->
                                computer.normalizedCopy().safeRoomId == cleanRoomId
                            }
                            .sortedWith(
                                compareBy<Computer>(
                                    { it.normalizedRow },
                                    { it.normalizedColumn },
                                    { it.displayCode.lowercase(Locale.getDefault()) }
                                )
                            )

                        callback(true, null, computers)
                    } catch (exception: Exception) {
                        callback(
                            false,
                            normalizeFirestoreError(exception),
                            emptyList()
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        emptyList()
                    )
                }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                emptyList()
            )
        }
    }

    fun createComputer(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?, Computer?) -> Unit
    ) {
        try {
            val normalizedComputer = computer.normalizedCopy()
            val documentRef = if (normalizedComputer.safeId.isNotBlank()) {
                computersCollection.document(normalizedComputer.safeId)
            } else {
                computersCollection.document()
            }

            val computerToSave = normalizedComputer.copy(
                id = documentRef.id
            ).normalizedCopy()

            documentRef
                .set(
                    computerToMap(
                        computer = computerToSave,
                        adminUser = adminUser,
                        includeCreatedAt = true
                    ),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    callback(
                        true,
                        "Đã thêm máy tính.",
                        computerToSave
                    )
                }
                .addOnFailureListener { exception ->
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        null
                    )
                }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                null
            )
        }
    }

    fun updateComputer(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?, Computer?) -> Unit
    ) {
        try {
            val normalizedComputer = computer.normalizedCopy()
            val computerId = normalizedComputer.safeId

            if (computerId.isBlank()) {
                callback(false, "Không tìm thấy mã máy tính để cập nhật.", null)
                return
            }

            computersCollection
                .document(computerId)
                .set(
                    computerToMap(
                        computer = normalizedComputer,
                        adminUser = adminUser,
                        includeCreatedAt = false
                    ),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    callback(
                        true,
                        "Đã cập nhật máy tính.",
                        normalizedComputer
                    )
                }
                .addOnFailureListener { exception ->
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        null
                    )
                }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                null
            )
        }
    }

    fun deleteComputer(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            val computerId = computer.normalizedCopy().safeId

            if (computerId.isBlank()) {
                callback(false, "Không tìm thấy máy tính để xóa.")
                return
            }

            computersCollection
                .document(computerId)
                .delete()
                .addOnSuccessListener {
                    callback(true, "Đã xóa máy tính.")
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception))
                }
        } catch (exception: Exception) {
            callback(false, normalizeFirestoreError(exception))
        }
    }

    fun createBulkComputers(
        room: ComputerRoom,
        prefix: String,
        adminUser: User,
        startIndex: Int = 1,
        replaceExistingInRoom: Boolean = false,
        cpu: String = "",
        ram: String = "",
        storage: String = "",
        monitor: String = "",
        gpu: String = "",
        operatingSystem: String = "",
        callback: (Boolean, String?, Int) -> Unit
    ) {
        try {
            val safeRoom = room.normalizedCopy()
            val cleanPrefix = prefix.trim().ifBlank {
                safeRoom.displayCode
            }

            if (safeRoom.safeId.isBlank()) {
                callback(false, "Phòng máy không hợp lệ.", 0)
                return
            }

            if (cleanPrefix.isBlank()) {
                callback(false, "Vui lòng nhập tiền tố mã máy.", 0)
                return
            }

            if (safeRoom.normalizedRows <= 0 || safeRoom.normalizedColumns <= 0) {
                callback(false, "Sơ đồ phòng máy không hợp lệ.", 0)
                return
            }

            val normalizedStartIndex = if (startIndex <= 0) 1 else startIndex

            fun createNewComputers(existingComputers: List<Computer>) {
                try {
                    val existingPositionKeys = existingComputers.map { computer ->
                        computer.normalizedRow to computer.normalizedColumn
                    }.toSet()

                    val batch = firestore.batch()
                    var createdCount = 0
                    var runningIndex = normalizedStartIndex

                    for (row in 1..safeRoom.normalizedRows) {
                        for (column in 1..safeRoom.normalizedColumns) {
                            val positionKey = row to column

                            if (!replaceExistingInRoom && existingPositionKeys.contains(positionKey)) {
                                continue
                            }

                            val documentRef = computersCollection.document()
                            val computerCode = buildBulkComputerCode(
                                prefix = cleanPrefix,
                                index = runningIndex
                            )

                            val computer = Computer(
                                id = documentRef.id,
                                roomId = safeRoom.safeId,
                                roomCode = safeRoom.displayCode,
                                roomName = safeRoom.displayName,
                                code = computerCode,
                                name = "Máy $computerCode",
                                row = row,
                                column = column,
                                status = Computer.ComputerStatus.Available.firestoreValue,
                                cpu = cpu.trim(),
                                ram = ram.trim(),
                                storage = storage.trim(),
                                monitor = monitor.trim(),
                                ipAddress = "",
                                gpu = gpu.trim(),
                                operatingSystem = operatingSystem.trim(),
                                note = ""
                            ).normalizedCopy()

                            batch.set(
                                documentRef,
                                computerToMap(
                                    computer = computer,
                                    adminUser = adminUser,
                                    includeCreatedAt = true
                                ),
                                SetOptions.merge()
                            )

                            createdCount++
                            runningIndex++
                        }
                    }

                    if (createdCount <= 0) {
                        callback(true, "Không có vị trí trống cần tạo thêm.", 0)
                        return
                    }

                    batch
                        .commit()
                        .addOnSuccessListener {
                            callback(
                                true,
                                "Đã tạo $createdCount máy tính.",
                                createdCount
                            )
                        }
                        .addOnFailureListener { exception ->
                            callback(
                                false,
                                normalizeFirestoreError(exception),
                                0
                            )
                        }
                } catch (exception: Exception) {
                    callback(
                        false,
                        normalizeFirestoreError(exception),
                        0
                    )
                }
            }

            getComputersByRoom(safeRoom.safeId) { success, message, existingComputers ->
                if (!success) {
                    callback(false, message ?: "Không thể kiểm tra máy trong phòng.", 0)
                    return@getComputersByRoom
                }

                if (replaceExistingInRoom && existingComputers.isNotEmpty()) {
                    try {
                        val deleteBatch = firestore.batch()

                        existingComputers.forEach { computer ->
                            val computerId = computer.normalizedCopy().safeId
                            if (computerId.isNotBlank()) {
                                deleteBatch.delete(computersCollection.document(computerId))
                            }
                        }

                        deleteBatch
                            .commit()
                            .addOnSuccessListener {
                                createNewComputers(emptyList())
                            }
                            .addOnFailureListener { exception ->
                                callback(
                                    false,
                                    normalizeFirestoreError(exception),
                                    0
                                )
                            }
                    } catch (exception: Exception) {
                        callback(
                            false,
                            normalizeFirestoreError(exception),
                            0
                        )
                    }
                } else {
                    createNewComputers(existingComputers)
                }
            }
        } catch (exception: Exception) {
            callback(
                false,
                normalizeFirestoreError(exception),
                0
            )
        }
    }

    fun setComputerAvailable(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        updateComputerStatus(
            computer = computer,
            adminUser = adminUser,
            status = Computer.ComputerStatus.Available,
            successMessage = "Đã chuyển máy sang trạng thái rảnh.",
            callback = callback
        )
    }

    fun setComputerInUse(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        updateComputerStatus(
            computer = computer,
            adminUser = adminUser,
            status = Computer.ComputerStatus.InUse,
            successMessage = "Đã chuyển máy sang trạng thái đang dùng.",
            callback = callback
        )
    }

    fun setComputerBroken(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        updateComputerStatus(
            computer = computer,
            adminUser = adminUser,
            status = Computer.ComputerStatus.Broken,
            successMessage = "Đã chuyển máy sang trạng thái hỏng.",
            callback = callback
        )
    }

    fun setComputerMaintenance(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        updateComputerStatus(
            computer = computer,
            adminUser = adminUser,
            status = Computer.ComputerStatus.Maintenance,
            successMessage = "Đã chuyển máy sang trạng thái bảo trì.",
            callback = callback
        )
    }

    fun lockComputer(
        computer: Computer,
        adminUser: User,
        callback: (Boolean, String?) -> Unit
    ) {
        updateComputerStatus(
            computer = computer,
            adminUser = adminUser,
            status = Computer.ComputerStatus.Locked,
            successMessage = "Đã khóa máy tính.",
            callback = callback
        )
    }

    private fun updateComputerStatus(
        computer: Computer,
        adminUser: User,
        status: Computer.ComputerStatus,
        successMessage: String,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            val safeComputer = computer.normalizedCopy()
            val computerId = safeComputer.safeId

            if (computerId.isBlank()) {
                callback(false, "Không tìm thấy máy tính để cập nhật.")
                return
            }

            computersCollection
                .document(computerId)
                .set(
                    mapOf(
                        "status" to status.firestoreValue,
                        "updatedAt" to Timestamp.now(),
                        "updatedBy" to adminUser.uid,
                        "updatedByEmail" to adminUser.email,
                        "updatedByName" to adminUser.displayName
                    ),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    callback(true, successMessage)
                }
                .addOnFailureListener { exception ->
                    callback(false, normalizeFirestoreError(exception))
                }
        } catch (exception: Exception) {
            callback(false, normalizeFirestoreError(exception))
        }
    }

    private fun buildComputerResult(
        computers: List<Computer>
    ): ComputerResult {
        val safeComputers = computers.map { computer ->
            computer.normalizedCopy()
        }

        return ComputerResult(
            computers = safeComputers,
            total = safeComputers.size,
            availableCount = safeComputers.count { it.isAvailable },
            inUseCount = safeComputers.count { it.isInUse },
            brokenCount = safeComputers.count { it.isBroken },
            maintenanceCount = safeComputers.count { it.isMaintenance },
            lockedCount = safeComputers.count { it.isLocked }
        )
    }

    private fun documentToComputerSafe(
        documentId: String,
        data: Map<String, Any>
    ): Computer? {
        return try {
            val row = readInt(
                value = data["row"],
                defaultValue = Computer.DEFAULT_ROW
            )

            val column = readInt(
                value = data["column"],
                defaultValue = Computer.DEFAULT_COLUMN
            )

            val legacyIpAddress = readString(data["ipAddress"])
            val gpuValue = readFirstNonBlankString(
                data = data,
                keys = listOf(
                    "gpu",
                    "graphicsCard",
                    "displayAdapter",
                    "vga",
                    "cardManHinh"
                )
            ).ifBlank {
                legacyIpAddress
            }

            val operatingSystemValue = readFirstNonBlankString(
                data = data,
                keys = listOf(
                    "operatingSystem",
                    "os",
                    "heDieuHanh",
                    "systemOs",
                    "windowsVersion"
                )
            )

            Computer(
                id = readString(data["id"]).ifBlank { documentId },
                roomId = readString(data["roomId"]),
                roomCode = readString(data["roomCode"]),
                roomName = readString(data["roomName"]),
                code = readString(data["code"]).ifBlank { documentId },
                name = readString(data["name"]).ifBlank {
                    readString(data["code"]).ifBlank { "Máy tính" }
                },
                row = row,
                column = column,
                status = normalizeStatusValue(readString(data["status"])),
                cpu = readString(data["cpu"]),
                ram = readString(data["ram"]),
                storage = readString(data["storage"]),
                monitor = readString(data["monitor"]),
                ipAddress = legacyIpAddress,
                gpu = gpuValue,
                operatingSystem = operatingSystemValue,
                note = readString(data["note"]),
                currentUserId = readString(data["currentUserId"]),
                currentBookingId = readString(data["currentBookingId"]),
                createdBy = readFirstNonBlankString(
                    data = data,
                    keys = listOf(
                        "createdBy",
                        "createdByEmail",
                        "createdByName"
                    )
                ),
                updatedBy = readFirstNonBlankString(
                    data = data,
                    keys = listOf(
                        "updatedBy",
                        "updatedByEmail",
                        "updatedByName"
                    )
                ),
                createdAt = readTimestamp(data["createdAt"]),
                updatedAt = readTimestamp(data["updatedAt"])
            ).normalizedCopy()
        } catch (exception: Exception) {
            null
        }
    }

    private fun computerToMap(
        computer: Computer,
        adminUser: User,
        includeCreatedAt: Boolean
    ): MutableMap<String, Any?> {
        val safeComputer = computer.normalizedCopy()

        val map = mutableMapOf<String, Any?>(
            "id" to safeComputer.safeId,
            "roomId" to safeComputer.safeRoomId,
            "roomCode" to safeComputer.displayRoomCode,
            "roomName" to safeComputer.displayRoomName,
            "code" to safeComputer.displayCode,
            "name" to safeComputer.displayName,
            "row" to safeComputer.normalizedRow,
            "column" to safeComputer.normalizedColumn,
            "status" to safeComputer.statusEnum.firestoreValue,
            "cpu" to safeComputer.cpu,
            "ram" to safeComputer.ram,
            "storage" to safeComputer.storage,
            "monitor" to safeComputer.monitor,

            "ipAddress" to safeComputer.ipAddress,
            "gpu" to safeComputer.gpu,
            "graphicsCard" to safeComputer.gpu,
            "operatingSystem" to safeComputer.operatingSystem,
            "os" to safeComputer.operatingSystem,

            "note" to safeComputer.note,
            "currentUserId" to safeComputer.currentUserId,
            "currentBookingId" to safeComputer.currentBookingId,
            "updatedAt" to Timestamp.now(),
            "updatedBy" to adminUser.uid,
            "updatedByEmail" to adminUser.email,
            "updatedByName" to adminUser.displayName
        )

        if (includeCreatedAt) {
            map["createdAt"] = Timestamp.now()
            map["createdBy"] = adminUser.uid
            map["createdByEmail"] = adminUser.email
            map["createdByName"] = adminUser.displayName
        }

        return map
    }

    private fun buildBulkComputerCode(
        prefix: String,
        index: Int
    ): String {
        val cleanPrefix = prefix.trim().uppercase(Locale.getDefault())
        val cleanIndex = if (index <= 0) 1 else index
        return "$cleanPrefix-${cleanIndex.toString().padStart(2, '0')}"
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

    private fun readInt(
        value: Any?,
        defaultValue: Int
    ): Int {
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Double -> value.toInt()
            is Float -> value.toInt()
            is String -> value.trim().toIntOrNull() ?: defaultValue
            else -> defaultValue
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

    private fun normalizeStatusValue(
        raw: String
    ): String {
        val value = raw.trim().lowercase(Locale.getDefault())

        return when (value) {
            "available",
            "free",
            "ready",
            "rảnh",
            "san_sang",
            "sẵn sàng" -> Computer.ComputerStatus.Available.firestoreValue

            "in_use",
            "inuse",
            "using",
            "used",
            "busy",
            "đang dùng",
            "dang_dung" -> Computer.ComputerStatus.InUse.firestoreValue

            "broken",
            "error",
            "damaged",
            "hỏng",
            "hong" -> Computer.ComputerStatus.Broken.firestoreValue

            "maintenance",
            "repair",
            "bảo trì",
            "bao_tri" -> Computer.ComputerStatus.Maintenance.firestoreValue

            "locked",
            "lock",
            "khóa",
            "khoa" -> Computer.ComputerStatus.Locked.firestoreValue

            else -> Computer.ComputerStatus.Available.firestoreValue
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
        private const val COLLECTION_COMPUTERS = "computers"
    }
}