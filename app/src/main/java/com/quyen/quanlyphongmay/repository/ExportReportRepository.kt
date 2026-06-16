package com.quyen.quanlyphongmay.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.quyen.quanlyphongmay.model.User
import java.io.File
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    data class ExportDocument(
        val collectionName: String = "",
        val documentId: String = "",
        val data: Map<String, Any?> = emptyMap()
    ) {
        fun normalizedCopy(): ExportDocument {
            return copy(
                collectionName = collectionName.trim(),
                documentId = documentId.trim(),
                data = data
            )
        }
    }

    data class FullExportData(
        val users: List<ExportDocument> = emptyList(),
        val rooms: List<ExportDocument> = emptyList(),
        val legacyRooms: List<ExportDocument> = emptyList(),
        val computers: List<ExportDocument> = emptyList(),
        val bookings: List<ExportDocument> = emptyList(),
        val notifications: List<ExportDocument> = emptyList(),
        val machineReports: List<ExportDocument> = emptyList(),
        val snapshot: ExportReportSnapshot = ExportReportSnapshot(),
        val generatedAt: Long = System.currentTimeMillis()
    ) {
        val generatedTimeText: String
            get() {
                return SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()
                ).format(Date(generatedAt))
            }

        fun allRooms(): List<ExportDocument> {
            return rooms + legacyRooms
        }

        fun normalizedCopy(): FullExportData {
            val safeGeneratedAt = if (generatedAt <= 0L) {
                System.currentTimeMillis()
            } else {
                generatedAt
            }

            return copy(
                users = users.map { it.normalizedCopy() },
                rooms = rooms.map { it.normalizedCopy() },
                legacyRooms = legacyRooms.map { it.normalizedCopy() },
                computers = computers.map { it.normalizedCopy() },
                bookings = bookings.map { it.normalizedCopy() },
                notifications = notifications.map { it.normalizedCopy() },
                machineReports = machineReports.map { it.normalizedCopy() },
                snapshot = snapshot.normalizedCopy(),
                generatedAt = safeGeneratedAt
            )
        }
    }

    data class ExportReportSnapshot(
        val accountTotal: Int = 0,
        val accountPending: Int = 0,
        val accountApproved: Int = 0,
        val accountRejected: Int = 0,
        val adminCount: Int = 0,
        val teacherCount: Int = 0,
        val studentCount: Int = 0,

        val roomTotal: Int = 0,
        val roomActive: Int = 0,
        val roomInactive: Int = 0,
        val roomMaintenance: Int = 0,

        val computerTotal: Int = 0,
        val computerAvailable: Int = 0,
        val computerInUse: Int = 0,
        val computerBroken: Int = 0,
        val computerMaintenance: Int = 0,
        val computerLocked: Int = 0,

        val bookingTotal: Int = 0,
        val bookingPending: Int = 0,
        val bookingApproved: Int = 0,
        val bookingRejected: Int = 0,
        val bookingCancelled: Int = 0,

        val notificationTotal: Int = 0,
        val notificationSystem: Int = 0,
        val notificationBooking: Int = 0,
        val notificationAccount: Int = 0,

        val machineReportTotal: Int = 0,
        val machineReportPending: Int = 0,
        val machineReportDone: Int = 0,

        val generatedAt: Long = System.currentTimeMillis()
    ) {
        val generatedTimeText: String
            get() {
                return SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(generatedAt))
            }

        val hasAnyData: Boolean
            get() {
                return accountTotal > 0 ||
                        roomTotal > 0 ||
                        computerTotal > 0 ||
                        bookingTotal > 0 ||
                        notificationTotal > 0 ||
                        machineReportTotal > 0
            }

        fun normalizedCopy(): ExportReportSnapshot {
            return copy(
                accountTotal = accountTotal.coerceAtLeast(0),
                accountPending = accountPending.coerceAtLeast(0),
                accountApproved = accountApproved.coerceAtLeast(0),
                accountRejected = accountRejected.coerceAtLeast(0),
                adminCount = adminCount.coerceAtLeast(0),
                teacherCount = teacherCount.coerceAtLeast(0),
                studentCount = studentCount.coerceAtLeast(0),

                roomTotal = roomTotal.coerceAtLeast(0),
                roomActive = roomActive.coerceAtLeast(0),
                roomInactive = roomInactive.coerceAtLeast(0),
                roomMaintenance = roomMaintenance.coerceAtLeast(0),

                computerTotal = computerTotal.coerceAtLeast(0),
                computerAvailable = computerAvailable.coerceAtLeast(0),
                computerInUse = computerInUse.coerceAtLeast(0),
                computerBroken = computerBroken.coerceAtLeast(0),
                computerMaintenance = computerMaintenance.coerceAtLeast(0),
                computerLocked = computerLocked.coerceAtLeast(0),

                bookingTotal = bookingTotal.coerceAtLeast(0),
                bookingPending = bookingPending.coerceAtLeast(0),
                bookingApproved = bookingApproved.coerceAtLeast(0),
                bookingRejected = bookingRejected.coerceAtLeast(0),
                bookingCancelled = bookingCancelled.coerceAtLeast(0),

                notificationTotal = notificationTotal.coerceAtLeast(0),
                notificationSystem = notificationSystem.coerceAtLeast(0),
                notificationBooking = notificationBooking.coerceAtLeast(0),
                notificationAccount = notificationAccount.coerceAtLeast(0),

                machineReportTotal = machineReportTotal.coerceAtLeast(0),
                machineReportPending = machineReportPending.coerceAtLeast(0),
                machineReportDone = machineReportDone.coerceAtLeast(0),

                generatedAt = generatedAt
            )
        }
    }

    data class ExportReportOptions(
        val reportType: String = "overview",
        val format: String = "csv",
        val includeAccounts: Boolean = true,
        val includeRooms: Boolean = true,
        val includeComputers: Boolean = true,
        val includeBookings: Boolean = true,
        val includeNotifications: Boolean = true,
        val includeMachineReports: Boolean = true
    ) {
        fun normalizedCopy(): ExportReportOptions {
            val cleanReportType = when (reportType.trim().lowercase(Locale.getDefault())) {
                "overview",
                "management",
                "submission",
                "full" -> reportType.trim().lowercase(Locale.getDefault())

                else -> "full"
            }

            val cleanFormat = when (format.trim().lowercase(Locale.getDefault())) {
                "text",
                "markdown",
                "checklist",
                "csv" -> format.trim().lowercase(Locale.getDefault())

                else -> "csv"
            }

            return copy(
                reportType = cleanReportType,
                format = cleanFormat,
                includeAccounts = includeAccounts,
                includeRooms = includeRooms,
                includeComputers = includeComputers,
                includeBookings = includeBookings,
                includeNotifications = includeNotifications,
                includeMachineReports = includeMachineReports
            )
        }
    }

    fun loadReportSnapshot(
        currentUser: User,
        callback: (Boolean, String?, ExportReportSnapshot) -> Unit
    ) {
        loadFullReportData(
            currentUser = currentUser
        ) { success, message, data ->
            callback(
                success,
                message,
                data.snapshot.normalizedCopy()
            )
        }
    }

    fun loadFullReportData(
        currentUser: User,
        callback: (Boolean, String?, FullExportData) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isAdmin) {
            callback(
                false,
                "Chỉ quản trị viên có quyền xuất báo cáo hệ thống.",
                FullExportData()
            )
            return
        }

        val collectionNames = listOf(
            COLLECTION_USERS,
            COLLECTION_ROOMS,
            COLLECTION_ROOMS_LEGACY,
            COLLECTION_COMPUTERS,
            COLLECTION_BOOKINGS,
            COLLECTION_NOTIFICATIONS,
            COLLECTION_MACHINE_REPORTS
        )

        loadCollectionsSequentially(
            collectionNames = collectionNames,
            index = 0,
            output = mutableMapOf()
        ) { success, message, result ->
            if (!success) {
                callback(
                    false,
                    message ?: "Không thể tải dữ liệu báo cáo.",
                    FullExportData()
                )
                return@loadCollectionsSequentially
            }

            val users = result[COLLECTION_USERS].orEmpty()
            val rooms = result[COLLECTION_ROOMS].orEmpty()
            val legacyRooms = result[COLLECTION_ROOMS_LEGACY].orEmpty()
            val computers = result[COLLECTION_COMPUTERS].orEmpty()
            val bookings = result[COLLECTION_BOOKINGS].orEmpty()
            val notifications = result[COLLECTION_NOTIFICATIONS].orEmpty()
            val machineReports = result[COLLECTION_MACHINE_REPORTS].orEmpty()

            val snapshot = buildSnapshot(
                users = users,
                rooms = rooms + legacyRooms,
                computers = computers,
                bookings = bookings,
                notifications = notifications,
                machineReports = machineReports
            )

            callback(
                true,
                "Đã tải dữ liệu báo cáo thật từ Firestore.",
                FullExportData(
                    users = users,
                    rooms = rooms,
                    legacyRooms = legacyRooms,
                    computers = computers,
                    bookings = bookings,
                    notifications = notifications,
                    machineReports = machineReports,
                    snapshot = snapshot,
                    generatedAt = System.currentTimeMillis()
                ).normalizedCopy()
            )
        }
    }

    fun exportFullCsvToDownloads(
        context: Context,
        currentUser: User,
        options: ExportReportOptions = ExportReportOptions(),
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isAdmin) {
            callback(
                false,
                "Chỉ quản trị viên có quyền xuất báo cáo CSV.",
                null
            )
            return
        }

        loadFullReportData(
            currentUser = safeUser
        ) { success, message, data ->
            if (!success) {
                callback(
                    false,
                    message ?: "Không thể tải dữ liệu để xuất CSV.",
                    null
                )
                return@loadFullReportData
            }

            val csvContent = buildFullCsvReport(
                currentUser = safeUser,
                data = data,
                options = options.normalizedCopy()
            )

            saveCsvToDownloads(
                context = context,
                fileName = buildCsvFileName(),
                csvContent = csvContent,
                callback = callback
            )
        }
    }

    fun exportSeparatedCsvFilesToDownloads(
        context: Context,
        currentUser: User,
        options: ExportReportOptions = ExportReportOptions(),
        callback: (Boolean, String?, List<String>) -> Unit
    ) {
        val safeUser = currentUser.normalizedCopy()

        if (!safeUser.isAdmin) {
            callback(
                false,
                "Chỉ quản trị viên có quyền xuất nhiều file CSV.",
                emptyList()
            )
            return
        }

        loadFullReportData(
            currentUser = safeUser
        ) { success, message, data ->
            if (!success) {
                callback(
                    false,
                    message ?: "Không thể tải dữ liệu để xuất nhiều file CSV.",
                    emptyList()
                )
                return@loadFullReportData
            }

            val safeData = data.normalizedCopy()
            val safeOptions = options.normalizedCopy()
            val timeText = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())

            val exportItems = mutableListOf<Pair<String, String>>()

            exportItems.add(
                "tong_hop_$timeText.csv" to buildString {
                    appendProfessionalSummary(snapshot = safeData.snapshot)
                }
            )

            if (safeOptions.includeAccounts) {
                exportItems.add(
                    "tai_khoan_$timeText.csv" to buildString {
                        appendUserTable(safeData.users)
                    }
                )
            }

            if (safeOptions.includeRooms) {
                exportItems.add(
                    "phong_may_$timeText.csv" to buildString {
                        appendRoomTable(safeData.allRooms())
                    }
                )
            }

            if (safeOptions.includeComputers) {
                exportItems.add(
                    "may_tinh_$timeText.csv" to buildString {
                        appendComputerTable(safeData.computers)
                    }
                )
            }

            if (safeOptions.includeBookings) {
                exportItems.add(
                    "booking_$timeText.csv" to buildString {
                        appendBookingTable(safeData.bookings)
                    }
                )
            }

            if (safeOptions.includeNotifications) {
                exportItems.add(
                    "thong_bao_$timeText.csv" to buildString {
                        appendNotificationTable(safeData.notifications)
                    }
                )
            }

            if (safeOptions.includeMachineReports) {
                exportItems.add(
                    "bao_loi_may_$timeText.csv" to buildString {
                        appendMachineReportTable(safeData.machineReports)
                    }
                )
            }

            if (exportItems.isEmpty()) {
                callback(
                    false,
                    "Không có nhóm dữ liệu nào được chọn để xuất.",
                    emptyList()
                )
                return@loadFullReportData
            }

            val savedPaths = mutableListOf<String>()

            fun saveNext(index: Int) {
                if (index >= exportItems.size) {
                    callback(
                        true,
                        "Đã xuất ${savedPaths.size} file CSV vào Download/$EXPORT_FOLDER_NAME.",
                        savedPaths.toList()
                    )
                    return
                }

                val item = exportItems[index]

                saveCsvToDownloads(
                    context = context,
                    fileName = item.first,
                    csvContent = item.second
                ) { saveSuccess, saveMessage, savedPath ->
                    if (!saveSuccess) {
                        callback(
                            false,
                            saveMessage ?: "Không thể lưu file ${item.first}.",
                            savedPaths.toList()
                        )
                        return@saveCsvToDownloads
                    }

                    savedPaths.add(savedPath.orEmpty().ifBlank { item.first })
                    saveNext(index + 1)
                }
            }

            saveNext(0)
        }
    }

    fun buildReportText(
        currentUser: User,
        snapshot: ExportReportSnapshot,
        options: ExportReportOptions
    ): String {
        val safeUser = currentUser.normalizedCopy()
        val safeSnapshot = snapshot.normalizedCopy()
        val safeOptions = options.normalizedCopy()

        return when (safeOptions.format) {
            FORMAT_MARKDOWN -> {
                buildMarkdownReport(
                    currentUser = safeUser,
                    snapshot = safeSnapshot,
                    options = safeOptions
                )
            }

            FORMAT_CHECKLIST -> {
                buildChecklistReport(
                    currentUser = safeUser,
                    snapshot = safeSnapshot,
                    options = safeOptions
                )
            }

            FORMAT_CSV -> {
                buildSummaryCsvReport(
                    currentUser = safeUser,
                    snapshot = safeSnapshot,
                    options = safeOptions
                )
            }

            else -> {
                buildPlainReport(
                    currentUser = safeUser,
                    snapshot = safeSnapshot,
                    options = safeOptions
                )
            }
        }
    }

    fun buildFullCsvReport(
        currentUser: User,
        data: FullExportData,
        options: ExportReportOptions = ExportReportOptions()
    ): String {
        val safeUser = currentUser.normalizedCopy()
        val safeData = data.normalizedCopy()
        val safeOptions = options.normalizedCopy()

        return buildString {
            appendCsvRow(listOf("ThongTin", "TenBaoCao", "Bao cao du lieu he thong Quan Ly Phong May"))
            appendCsvRow(listOf("ThongTin", "ThoiGianTao", safeData.generatedTimeText))
            appendCsvRow(listOf("ThongTin", "NguoiTao", safeUser.displayName.ifBlank { safeUser.email }))
            appendCsvRow(listOf("ThongTin", "EmailNguoiTao", safeUser.email))
            appendCsvRow(listOf("ThongTin", "VaiTroNguoiTao", safeUser.roleNameVi))
            appendCsvRow(listOf("ThongTin", "NoiLuu", "Download/QuanLyPhongMay"))
            appendCsvRow(emptyList())

            appendProfessionalSummary(snapshot = safeData.snapshot)

            if (safeOptions.includeAccounts) {
                appendUserTable(safeData.users)
            }

            if (safeOptions.includeRooms) {
                appendRoomTable(safeData.allRooms())
            }

            if (safeOptions.includeComputers) {
                appendComputerTable(safeData.computers)
            }

            if (safeOptions.includeBookings) {
                appendBookingTable(safeData.bookings)
            }

            if (safeOptions.includeNotifications) {
                appendNotificationTable(safeData.notifications)
            }

            if (safeOptions.includeMachineReports) {
                appendMachineReportTable(safeData.machineReports)
            }
        }
    }

    private fun StringBuilder.appendProfessionalSummary(
        snapshot: ExportReportSnapshot
    ) {
        val safeSnapshot = snapshot.normalizedCopy()

        appendCsvRow(listOf("NhomDuLieu", "Tong", "ChiTiet1", "ChiTiet2", "ChiTiet3", "ChiTiet4", "ChiTiet5"))
        appendCsvRow(
            listOf(
                "Tai khoan",
                safeSnapshot.accountTotal.toString(),
                "Admin: ${safeSnapshot.adminCount}",
                "Giao vien: ${safeSnapshot.teacherCount}",
                "Sinh vien: ${safeSnapshot.studentCount}",
                "Cho duyet: ${safeSnapshot.accountPending}",
                "Da duyet: ${safeSnapshot.accountApproved}"
            )
        )
        appendCsvRow(
            listOf(
                "Phong may",
                safeSnapshot.roomTotal.toString(),
                "Hoat dong: ${safeSnapshot.roomActive}",
                "Ngung: ${safeSnapshot.roomInactive}",
                "Bao tri: ${safeSnapshot.roomMaintenance}",
                "",
                ""
            )
        )
        appendCsvRow(
            listOf(
                "May tinh",
                safeSnapshot.computerTotal.toString(),
                "Ranh: ${safeSnapshot.computerAvailable}",
                "Dang dung: ${safeSnapshot.computerInUse}",
                "Hong: ${safeSnapshot.computerBroken}",
                "Bao tri: ${safeSnapshot.computerMaintenance}",
                "Da khoa: ${safeSnapshot.computerLocked}"
            )
        )
        appendCsvRow(
            listOf(
                "Booking",
                safeSnapshot.bookingTotal.toString(),
                "Cho duyet: ${safeSnapshot.bookingPending}",
                "Da duyet: ${safeSnapshot.bookingApproved}",
                "Tu choi: ${safeSnapshot.bookingRejected}",
                "Da huy: ${safeSnapshot.bookingCancelled}",
                ""
            )
        )
        appendCsvRow(
            listOf(
                "Thong bao",
                safeSnapshot.notificationTotal.toString(),
                "He thong: ${safeSnapshot.notificationSystem}",
                "Booking: ${safeSnapshot.notificationBooking}",
                "Tai khoan: ${safeSnapshot.notificationAccount}",
                "",
                ""
            )
        )
        appendCsvRow(
            listOf(
                "Bao loi may",
                safeSnapshot.machineReportTotal.toString(),
                "Dang xu ly: ${safeSnapshot.machineReportPending}",
                "Hoan tat: ${safeSnapshot.machineReportDone}",
                "",
                "",
                ""
            )
        )
    }

    private fun StringBuilder.appendUserTable(
        users: List<ExportDocument>
    ) {
        appendCsvRow(
            listOf(
                "STT",
                "HoTen",
                "Email",
                "VaiTro",
                "TrangThai",
                "MSSV_MSGV_MaQuanTri",
                "Lop_DonVi_Khoa",
                "SoDienThoai",
                "NgayTao",
                "NgayCapNhat"
            )
        )

        appendDataRowsOrEmpty(
            groupName = "Tai khoan",
            documents = users
        ) { index, document ->
            val data = document.data
            appendCsvRow(
                listOf(
                    index.toString(),
                    firstString(data, "displayName", "fullName", "name", "hoten", "hoTen", "teacherName", "studentName"),
                    firstString(data, "email"),
                    normalizeDisplayRole(firstString(data, "role")),
                    normalizeDisplayStatus(firstString(data, "status")),
                    firstString(data, "studentCode", "teacherCode", "adminCode", "mssv", "msgv", "code", "userCode"),
                    firstString(data, "className", "department", "faculty", "unit", "lop", "donVi", "khoa"),
                    firstString(data, "phone", "phoneNumber", "soDienThoai"),
                    firstString(data, "createdAt", "createAt", "registeredAt"),
                    firstString(data, "updatedAt", "approvedAt")
                )
            )
        }
    }

    private fun StringBuilder.appendRoomTable(
        rooms: List<ExportDocument>
    ) {
        appendCsvRow(
            listOf(
                "STT",
                "MaPhong",
                "TenPhong",
                "TrangThai",
                "SoHang",
                "SoCot",
                "SucChua",
                "ViTri",
                "GhiChu",
                "NgayTao"
            )
        )

        appendDataRowsOrEmpty(
            groupName = "Phong may",
            documents = rooms
        ) { index, document ->
            val data = document.data
            appendCsvRow(
                listOf(
                    index.toString(),
                    firstString(data, "code", "roomCode", "maPhong"),
                    firstString(data, "name", "roomName", "tenPhong"),
                    normalizeDisplayStatus(firstString(data, "status")),
                    firstString(data, "rows", "rowCount", "totalRows"),
                    firstString(data, "columns", "columnCount", "totalColumns"),
                    firstString(data, "capacity", "computerCount", "totalComputers"),
                    firstString(data, "location", "address", "viTri"),
                    firstString(data, "note", "description", "ghiChu"),
                    firstString(data, "createdAt")
                )
            )
        }
    }

    private fun StringBuilder.appendComputerTable(
        computers: List<ExportDocument>
    ) {
        appendCsvRow(
            listOf(
                "STT",
                "MaMay",
                "TenMay",
                "Phong",
                "Hang",
                "Cot",
                "TrangThai",
                "CPU",
                "RAM",
                "O_Cung",
                "GPU",
                "HeDieuHanh",
                "ManHinh",
                "NguoiDangDung",
                "BookingHienTai",
                "GhiChu"
            )
        )

        appendDataRowsOrEmpty(
            groupName = "May tinh",
            documents = computers
        ) { index, document ->
            val data = document.data
            appendCsvRow(
                listOf(
                    index.toString(),
                    firstString(data, "code", "computerCode", "maMay"),
                    firstString(data, "name", "computerName", "tenMay"),
                    firstString(data, "roomCode", "roomName", "roomId", "phong"),
                    firstString(data, "row", "hang"),
                    firstString(data, "column", "col", "cot"),
                    normalizeDisplayComputerStatus(firstString(data, "status")),
                    firstString(data, "cpu", "CPU"),
                    firstString(data, "ram", "RAM"),
                    firstString(data, "storage", "hardDrive", "disk", "oCung"),
                    firstString(data, "gpu", "GPU", "graphicsCard"),
                    firstString(data, "operatingSystem", "os", "heDieuHanh"),
                    firstString(data, "monitor", "screen", "manHinh"),
                    firstString(data, "currentUserId", "currentUserEmail", "currentUserName"),
                    firstString(data, "currentBookingId", "bookingId"),
                    firstString(data, "note", "description", "ghiChu")
                )
            )
        }
    }

    private fun StringBuilder.appendBookingTable(
        bookings: List<ExportDocument>
    ) {
        appendCsvRow(
            listOf(
                "STT",
                "TieuDe",
                "Lop",
                "MonHoc",
                "BaiHoc",
                "Phong",
                "Ngay",
                "BatDau",
                "KetThuc",
                "SoSinhVien",
                "TrangThai",
                "GiaoVien",
                "EmailGiaoVien",
                "NguoiDuyet",
                "LyDoTuChoi_Huy",
                "ChoNgoiDaGan",
                "GhiChu"
            )
        )

        appendDataRowsOrEmpty(
            groupName = "Booking",
            documents = bookings
        ) { index, document ->
            val data = document.data
            appendCsvRow(
                listOf(
                    index.toString(),
                    firstString(data, "title", "bookingTitle", "name"),
                    firstString(data, "className", "classCode", "lop"),
                    firstString(data, "subjectName", "subject", "monHoc"),
                    firstString(data, "lessonTitle", "lesson", "baiHoc"),
                    firstString(data, "roomCode", "roomName", "roomId"),
                    firstString(data, "dateKey", "date", "bookingDate"),
                    firstString(data, "startTime", "startDateTime", "startAt"),
                    firstString(data, "endTime", "endDateTime", "endAt"),
                    firstString(data, "studentCount", "requiredComputers", "computerCount"),
                    normalizeDisplayStatus(firstString(data, "status")),
                    firstString(data, "teacherName", "createdByName", "teacherId"),
                    firstString(data, "teacherEmail", "createdByEmail"),
                    firstString(data, "approvedByName", "approvedByEmail", "approvedById"),
                    firstString(data, "rejectReason", "cancelReason", "reason"),
                    firstString(data, "assignedSeats", "seatAssignments", "seatAssignment"),
                    firstString(data, "note", "description", "ghiChu")
                )
            )
        }
    }

    private fun StringBuilder.appendNotificationTable(
        notifications: List<ExportDocument>
    ) {
        appendCsvRow(
            listOf(
                "STT",
                "TieuDe",
                "NoiDung",
                "Loai",
                "NguoiGui",
                "EmailNguoiGui",
                "VaiTroNguoiGui",
                "GuiDenVaiTro",
                "GuiDenEmail",
                "DaDoc",
                "ThoiGianTao"
            )
        )

        appendDataRowsOrEmpty(
            groupName = "Thong bao",
            documents = notifications
        ) { index, document ->
            val data = document.data
            appendCsvRow(
                listOf(
                    index.toString(),
                    firstString(data, "title", "subject"),
                    firstString(data, "message", "content", "body"),
                    normalizeNotificationType(firstString(data, "type")),
                    firstString(data, "createdByName", "senderName"),
                    firstString(data, "createdByEmail", "senderEmail"),
                    normalizeDisplayRole(firstString(data, "createdByRole", "senderRole")),
                    firstString(data, "targetRoles", "roles"),
                    firstString(data, "targetEmails", "targetEmail"),
                    firstString(data, "readByEmails", "readByUserIds"),
                    firstString(data, "createdAt")
                )
            )
        }
    }

    private fun StringBuilder.appendMachineReportTable(
        reports: List<ExportDocument>
    ) {
        appendCsvRow(
            listOf(
                "STT",
                "MayTinh",
                "Phong",
                "NguoiBao",
                "EmailNguoiBao",
                "LoaiLoi",
                "MoTa",
                "TrangThai",
                "NguoiXuLy",
                "NgayTao",
                "NgayCapNhat"
            )
        )

        appendDataRowsOrEmpty(
            groupName = "Bao loi may",
            documents = reports
        ) { index, document ->
            val data = document.data
            appendCsvRow(
                listOf(
                    index.toString(),
                    firstString(data, "computerCode", "computerName", "computerId", "maMay"),
                    firstString(data, "roomCode", "roomName", "roomId"),
                    firstString(data, "reporterName", "createdByName", "userName"),
                    firstString(data, "reporterEmail", "createdByEmail", "userEmail"),
                    firstString(data, "errorType", "type", "category"),
                    firstString(data, "description", "message", "note"),
                    normalizeDisplayStatus(firstString(data, "status")),
                    firstString(data, "handledByName", "handledByEmail", "updatedBy"),
                    firstString(data, "createdAt"),
                    firstString(data, "updatedAt", "resolvedAt")
                )
            )
        }
    }

    private fun StringBuilder.appendCsvSectionTitle(
        title: String
    ) {
        appendCsvRow(listOf("=== $title ==="))
    }

    private fun StringBuilder.appendDataRowsOrEmpty(
        groupName: String,
        documents: List<ExportDocument>,
        appendRow: StringBuilder.(Int, ExportDocument) -> Unit
    ) {
        if (documents.isEmpty()) {
            appendCsvRow(listOf("Khong co du lieu $groupName"))
            return
        }

        documents.map { it.normalizedCopy() }.forEachIndexed { index, document ->
            appendRow(index + 1, document)
        }
    }

    private fun firstString(
        data: Map<String, Any?>,
        vararg keys: String
    ): String {
        keys.forEach { key ->
            val exact = data[key]
            val exactValue = readString(exact)
            if (exactValue.isNotBlank()) {
                return exactValue
            }

            val foundKey = data.keys.firstOrNull { existingKey ->
                existingKey.equals(key, ignoreCase = true)
            }

            if (foundKey != null) {
                val foundValue = readString(data[foundKey])
                if (foundValue.isNotBlank()) {
                    return foundValue
                }
            }
        }

        return ""
    }

    private fun compactRawData(
        data: Map<String, Any?>
    ): String {
        return flattenMap(data)
            .joinToString(" | ") { field ->
                "${field.first}=${field.second}"
            }
    }

    private fun normalizeDisplayRole(
        raw: String
    ): String {
        return when (normalizeRole(raw)) {
            ROLE_ADMIN -> "Quan tri vien"
            ROLE_TEACHER -> "Giao vien"
            ROLE_STUDENT -> "Sinh vien"
            else -> raw
        }
    }

    private fun normalizeDisplayComputerStatus(
        raw: String
    ): String {
        return when (normalizeComputerStatus(raw)) {
            COMPUTER_AVAILABLE -> "Ranh"
            COMPUTER_IN_USE -> "Dang dung"
            COMPUTER_BROKEN -> "Hong"
            COMPUTER_MAINTENANCE -> "Bao tri"
            COMPUTER_LOCKED -> "Da khoa"
            else -> raw
        }
    }

    private fun normalizeDisplayStatus(
        raw: String
    ): String {
        return when {
            isPendingStatus(raw) -> "Cho duyet"
            isApprovedStatus(raw) -> "Da duyet"
            isRejectedStatus(raw) -> "Tu choi"
            isCancelledStatus(raw) -> "Da huy"
            isMaintenanceStatus(raw) -> "Bao tri"
            isInactiveRoom(raw) -> "Ngung hoat dong"
            isActiveRoom(raw) -> "Hoat dong"
            else -> raw
        }
    }

    private fun StringBuilder.appendSummaryRows(
        snapshot: ExportReportSnapshot
    ) {
        val safeSnapshot = snapshot.normalizedCopy()

        val rows = listOf(
            "TongTaiKhoan" to safeSnapshot.accountTotal,
            "TaiKhoanChoDuyet" to safeSnapshot.accountPending,
            "TaiKhoanDaDuyet" to safeSnapshot.accountApproved,
            "TaiKhoanBiTuChoi" to safeSnapshot.accountRejected,
            "Admin" to safeSnapshot.adminCount,
            "GiaoVien" to safeSnapshot.teacherCount,
            "SinhVien" to safeSnapshot.studentCount,
            "TongPhongMay" to safeSnapshot.roomTotal,
            "PhongHoatDong" to safeSnapshot.roomActive,
            "PhongNgungHoatDong" to safeSnapshot.roomInactive,
            "PhongBaoTri" to safeSnapshot.roomMaintenance,
            "TongMayTinh" to safeSnapshot.computerTotal,
            "MayRanh" to safeSnapshot.computerAvailable,
            "MayDangDung" to safeSnapshot.computerInUse,
            "MayHong" to safeSnapshot.computerBroken,
            "MayBaoTri" to safeSnapshot.computerMaintenance,
            "MayDaKhoa" to safeSnapshot.computerLocked,
            "TongBooking" to safeSnapshot.bookingTotal,
            "BookingChoDuyet" to safeSnapshot.bookingPending,
            "BookingDaDuyet" to safeSnapshot.bookingApproved,
            "BookingBiTuChoi" to safeSnapshot.bookingRejected,
            "BookingDaHuy" to safeSnapshot.bookingCancelled,
            "TongThongBao" to safeSnapshot.notificationTotal,
            "ThongBaoHeThong" to safeSnapshot.notificationSystem,
            "ThongBaoBooking" to safeSnapshot.notificationBooking,
            "ThongBaoTaiKhoan" to safeSnapshot.notificationAccount,
            "TongBaoLoiMay" to safeSnapshot.machineReportTotal,
            "BaoLoiMayDangXuLy" to safeSnapshot.machineReportPending,
            "BaoLoiMayHoanTat" to safeSnapshot.machineReportDone
        )

        rows.forEach { row ->
            appendCsvRow(
                listOf(
                    "SUMMARY",
                    "TongHop",
                    "",
                    "",
                    "",
                    row.first,
                    row.second.toString()
                )
            )
        }
    }

    private fun StringBuilder.appendDocumentRows(
        groupName: String,
        documents: List<ExportDocument>
    ) {
        if (documents.isEmpty()) {
            appendCsvRow(
                listOf(
                    "DATA_EMPTY",
                    groupName,
                    "",
                    "",
                    "",
                    "TrangThai",
                    "Khong co du lieu"
                )
            )
            return
        }

        documents.forEachIndexed { index, document ->
            val safeDocument = document.normalizedCopy()
            val flattenedFields = flattenMap(
                data = safeDocument.data
            )

            appendCsvRow(
                listOf(
                    "DATA_DOC",
                    groupName,
                    safeDocument.collectionName,
                    safeDocument.documentId,
                    (index + 1).toString(),
                    "__documentId",
                    safeDocument.documentId
                )
            )

            if (flattenedFields.isEmpty()) {
                appendCsvRow(
                    listOf(
                        "DATA_FIELD",
                        groupName,
                        safeDocument.collectionName,
                        safeDocument.documentId,
                        (index + 1).toString(),
                        "__empty",
                        ""
                    )
                )
            } else {
                flattenedFields.forEach { field ->
                    appendCsvRow(
                        listOf(
                            "DATA_FIELD",
                            groupName,
                            safeDocument.collectionName,
                            safeDocument.documentId,
                            (index + 1).toString(),
                            field.first,
                            field.second
                        )
                    )
                }
            }
        }
    }

    private fun buildSnapshot(
        users: List<ExportDocument>,
        rooms: List<ExportDocument>,
        computers: List<ExportDocument>,
        bookings: List<ExportDocument>,
        notifications: List<ExportDocument>,
        machineReports: List<ExportDocument>
    ): ExportReportSnapshot {
        return ExportReportSnapshot(
            accountTotal = users.size,
            accountPending = users.count { isPendingStatus(readString(it.data["status"])) },
            accountApproved = users.count { isApprovedStatus(readString(it.data["status"])) },
            accountRejected = users.count { isRejectedStatus(readString(it.data["status"])) },
            adminCount = users.count { normalizeRole(readString(it.data["role"])) == ROLE_ADMIN },
            teacherCount = users.count { normalizeRole(readString(it.data["role"])) == ROLE_TEACHER },
            studentCount = users.count { normalizeRole(readString(it.data["role"])) == ROLE_STUDENT },

            roomTotal = rooms.size,
            roomActive = rooms.count { isActiveRoom(readString(it.data["status"])) },
            roomInactive = rooms.count { isInactiveRoom(readString(it.data["status"])) },
            roomMaintenance = rooms.count { isMaintenanceStatus(readString(it.data["status"])) },

            computerTotal = computers.size,
            computerAvailable = computers.count { normalizeComputerStatus(readString(it.data["status"])) == COMPUTER_AVAILABLE },
            computerInUse = computers.count { normalizeComputerStatus(readString(it.data["status"])) == COMPUTER_IN_USE },
            computerBroken = computers.count { normalizeComputerStatus(readString(it.data["status"])) == COMPUTER_BROKEN },
            computerMaintenance = computers.count { normalizeComputerStatus(readString(it.data["status"])) == COMPUTER_MAINTENANCE },
            computerLocked = computers.count { normalizeComputerStatus(readString(it.data["status"])) == COMPUTER_LOCKED },

            bookingTotal = bookings.size,
            bookingPending = bookings.count { isPendingStatus(readString(it.data["status"])) },
            bookingApproved = bookings.count { isApprovedStatus(readString(it.data["status"])) },
            bookingRejected = bookings.count { isRejectedStatus(readString(it.data["status"])) },
            bookingCancelled = bookings.count { isCancelledStatus(readString(it.data["status"])) },

            notificationTotal = notifications.size,
            notificationSystem = notifications.count { normalizeNotificationType(readString(it.data["type"])) == NOTIFICATION_SYSTEM },
            notificationBooking = notifications.count { normalizeNotificationType(readString(it.data["type"])) == NOTIFICATION_BOOKING },
            notificationAccount = notifications.count { normalizeNotificationType(readString(it.data["type"])) == NOTIFICATION_ACCOUNT },

            machineReportTotal = machineReports.size,
            machineReportPending = machineReports.count { !isDoneStatus(readString(it.data["status"])) },
            machineReportDone = machineReports.count { isDoneStatus(readString(it.data["status"])) },

            generatedAt = System.currentTimeMillis()
        ).normalizedCopy()
    }

    private fun loadCollectionsSequentially(
        collectionNames: List<String>,
        index: Int,
        output: MutableMap<String, List<ExportDocument>>,
        callback: (Boolean, String?, Map<String, List<ExportDocument>>) -> Unit
    ) {
        if (index >= collectionNames.size) {
            callback(true, null, output)
            return
        }

        val collectionName = collectionNames[index]

        firestore.collection(collectionName)
            .get()
            .addOnSuccessListener { snapshot ->
                output[collectionName] = snapshot.documents.map { document ->
                    ExportDocument(
                        collectionName = collectionName,
                        documentId = document.id,
                        data = document.data.orEmpty()
                    ).normalizedCopy()
                }

                loadCollectionsSequentially(
                    collectionNames = collectionNames,
                    index = index + 1,
                    output = output,
                    callback = callback
                )
            }
            .addOnFailureListener { exception ->
                callback(
                    false,
                    "Không thể đọc collection $collectionName. ${normalizeFirestoreError(exception)}",
                    output
                )
            }
    }

    private fun saveCsvToDownloads(
        context: Context,
        fileName: String,
        csvContent: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val safeFileName = fileName.trim().ifBlank {
            buildCsvFileName()
        }

        val contentWithBom = "\uFEFF$csvContent"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, safeFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/$EXPORT_FOLDER_NAME"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )

                if (uri == null) {
                    callback(false, "Không tạo được file CSV trong Download.", null)
                    return
                }

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(contentWithBom.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                } ?: run {
                    callback(false, "Không mở được luồng ghi file CSV.", null)
                    return
                }

                val finishValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }

                resolver.update(
                    uri,
                    finishValues,
                    null,
                    null
                )

                callback(
                    true,
                    "Đã xuất CSV vào Download/$EXPORT_FOLDER_NAME/$safeFileName",
                    uri.toString()
                )
            } else {
                saveCsvToLegacyDownloads(
                    safeFileName = safeFileName,
                    contentWithBom = contentWithBom,
                    callback = callback
                )
            }
        } catch (exception: Exception) {
            callback(
                false,
                "Không thể xuất CSV. ${exception.message.orEmpty().ifBlank { "Vui lòng thử lại." }}",
                null
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun saveCsvToLegacyDownloads(
        safeFileName: String,
        contentWithBom: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        try {
            val downloadsDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val exportDirectory = File(
                downloadsDirectory,
                EXPORT_FOLDER_NAME
            )

            if (!exportDirectory.exists()) {
                exportDirectory.mkdirs()
            }

            val exportFile = File(
                exportDirectory,
                safeFileName
            )

            exportFile.writeText(
                text = contentWithBom,
                charset = Charsets.UTF_8
            )

            callback(
                true,
                "Đã xuất CSV vào ${exportFile.absolutePath}",
                exportFile.absolutePath
            )
        } catch (exception: Exception) {
            callback(
                false,
                "Không thể lưu file vào Download. ${exception.message.orEmpty().ifBlank { "Vui lòng thử lại." }}",
                null
            )
        }
    }

    private fun buildCsvFileName(): String {
        val timeText = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        return "bao_cao_quan_ly_phong_may_$timeText.csv"
    }

    private fun flattenMap(
        data: Map<String, Any?>,
        prefix: String = ""
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        data.toSortedMap().forEach { entry ->
            val fieldName = if (prefix.isBlank()) {
                entry.key
            } else {
                "$prefix.${entry.key}"
            }

            result.addAll(
                flattenValue(
                    fieldName = fieldName,
                    value = entry.value
                )
            )
        }

        return result
    }

    private fun flattenValue(
        fieldName: String,
        value: Any?
    ): List<Pair<String, String>> {
        return when (value) {
            null -> {
                listOf(fieldName to "")
            }

            is Map<*, *> -> {
                val mapValue = value.entries.associate { entry ->
                    readString(entry.key) to entry.value
                }

                if (mapValue.isEmpty()) {
                    listOf(fieldName to "")
                } else {
                    flattenMap(
                        data = mapValue,
                        prefix = fieldName
                    )
                }
            }

            is List<*> -> {
                if (value.isEmpty()) {
                    listOf(fieldName to "[]")
                } else {
                    value.flatMapIndexed { index, item ->
                        flattenValue(
                            fieldName = "$fieldName[$index]",
                            value = item
                        )
                    }
                }
            }

            is Array<*> -> {
                flattenValue(
                    fieldName = fieldName,
                    value = value.toList()
                )
            }

            is Timestamp -> {
                listOf(fieldName to formatTimestamp(value))
            }

            is Date -> {
                listOf(fieldName to formatDate(value))
            }

            else -> {
                listOf(fieldName to readString(value))
            }
        }
    }

    private fun StringBuilder.appendCsvRow(
        values: List<String>
    ) {
        append(
            values.joinToString(",") { value ->
                escapeCsv(value)
            }
        )
        append('\n')
    }

    private fun buildPlainReport(
        currentUser: User,
        snapshot: ExportReportSnapshot,
        options: ExportReportOptions
    ): String {
        return buildString {
            appendLine("BÁO CÁO HỆ THỐNG QUẢN LÝ PHÒNG MÁY")
            appendLine("Loại báo cáo: ${reportTypeName(options.reportType)}")
            appendLine("Định dạng: Văn bản thường")
            appendLine("Thời gian tạo: ${snapshot.generatedTimeText}")
            appendLine("Người tạo: ${currentUser.displayName.ifBlank { currentUser.email.ifBlank { "Quản trị viên" } }}")
            appendLine("Vai trò: ${currentUser.roleNameVi}")
            appendLine()
            appendLine("1. TỔNG QUAN")
            appendLine("Báo cáo được tạo từ dữ liệu Firestore thật của ứng dụng Quản Lý Phòng Máy.")
            appendLine()

            appendPlainSection(
                enabled = options.includeAccounts,
                title = "2. TÀI KHOẢN",
                lines = listOf(
                    "Tổng tài khoản: ${snapshot.accountTotal}",
                    "Đã duyệt: ${snapshot.accountApproved}",
                    "Chờ duyệt: ${snapshot.accountPending}",
                    "Bị từ chối: ${snapshot.accountRejected}",
                    "Quản trị viên: ${snapshot.adminCount}",
                    "Giáo viên: ${snapshot.teacherCount}",
                    "Sinh viên: ${snapshot.studentCount}"
                )
            )

            appendPlainSection(
                enabled = options.includeRooms,
                title = "3. PHÒNG MÁY",
                lines = listOf(
                    "Tổng phòng máy: ${snapshot.roomTotal}",
                    "Đang hoạt động: ${snapshot.roomActive}",
                    "Ngưng hoạt động: ${snapshot.roomInactive}",
                    "Bảo trì: ${snapshot.roomMaintenance}"
                )
            )

            appendPlainSection(
                enabled = options.includeComputers,
                title = "4. MÁY TÍNH",
                lines = listOf(
                    "Tổng máy tính: ${snapshot.computerTotal}",
                    "Rảnh: ${snapshot.computerAvailable}",
                    "Đang dùng: ${snapshot.computerInUse}",
                    "Hỏng: ${snapshot.computerBroken}",
                    "Bảo trì: ${snapshot.computerMaintenance}",
                    "Đã khóa: ${snapshot.computerLocked}"
                )
            )

            appendPlainSection(
                enabled = options.includeBookings,
                title = "5. BOOKING",
                lines = listOf(
                    "Tổng booking: ${snapshot.bookingTotal}",
                    "Chờ duyệt: ${snapshot.bookingPending}",
                    "Đã duyệt: ${snapshot.bookingApproved}",
                    "Từ chối: ${snapshot.bookingRejected}",
                    "Đã hủy: ${snapshot.bookingCancelled}"
                )
            )

            appendPlainSection(
                enabled = options.includeNotifications,
                title = "6. THÔNG BÁO",
                lines = listOf(
                    "Tổng thông báo: ${snapshot.notificationTotal}",
                    "Thông báo hệ thống: ${snapshot.notificationSystem}",
                    "Thông báo booking: ${snapshot.notificationBooking}",
                    "Thông báo tài khoản: ${snapshot.notificationAccount}"
                )
            )

            appendPlainSection(
                enabled = options.includeMachineReports,
                title = "7. BÁO LỖI MÁY",
                lines = listOf(
                    "Tổng báo lỗi: ${snapshot.machineReportTotal}",
                    "Đang xử lý: ${snapshot.machineReportPending}",
                    "Hoàn tất: ${snapshot.machineReportDone}"
                )
            )
        }
    }

    private fun StringBuilder.appendPlainSection(
        enabled: Boolean,
        title: String,
        lines: List<String>
    ) {
        if (!enabled) {
            return
        }

        appendLine(title)
        lines.forEach { line ->
            appendLine("- $line")
        }
        appendLine()
    }

    private fun buildMarkdownReport(
        currentUser: User,
        snapshot: ExportReportSnapshot,
        options: ExportReportOptions
    ): String {
        return buildString {
            appendLine("# BÁO CÁO HỆ THỐNG QUẢN LÝ PHÒNG MÁY")
            appendLine()
            appendLine("- **Loại báo cáo:** ${reportTypeName(options.reportType)}")
            appendLine("- **Định dạng:** Markdown")
            appendLine("- **Thời gian tạo:** ${snapshot.generatedTimeText}")
            appendLine("- **Người tạo:** ${currentUser.displayName.ifBlank { currentUser.email.ifBlank { "Quản trị viên" } }}")
            appendLine("- **Vai trò:** ${currentUser.roleNameVi}")
            appendLine()

            if (options.includeAccounts) {
                appendMarkdownTable(
                    title = "Tài khoản",
                    rows = listOf(
                        "Tổng tài khoản" to snapshot.accountTotal,
                        "Đã duyệt" to snapshot.accountApproved,
                        "Chờ duyệt" to snapshot.accountPending,
                        "Bị từ chối" to snapshot.accountRejected,
                        "Quản trị viên" to snapshot.adminCount,
                        "Giáo viên" to snapshot.teacherCount,
                        "Sinh viên" to snapshot.studentCount
                    )
                )
            }

            if (options.includeRooms) {
                appendMarkdownTable(
                    title = "Phòng máy",
                    rows = listOf(
                        "Tổng phòng máy" to snapshot.roomTotal,
                        "Đang hoạt động" to snapshot.roomActive,
                        "Ngưng hoạt động" to snapshot.roomInactive,
                        "Bảo trì" to snapshot.roomMaintenance
                    )
                )
            }

            if (options.includeComputers) {
                appendMarkdownTable(
                    title = "Máy tính",
                    rows = listOf(
                        "Tổng máy tính" to snapshot.computerTotal,
                        "Rảnh" to snapshot.computerAvailable,
                        "Đang dùng" to snapshot.computerInUse,
                        "Hỏng" to snapshot.computerBroken,
                        "Bảo trì" to snapshot.computerMaintenance,
                        "Đã khóa" to snapshot.computerLocked
                    )
                )
            }

            if (options.includeBookings) {
                appendMarkdownTable(
                    title = "Booking",
                    rows = listOf(
                        "Tổng booking" to snapshot.bookingTotal,
                        "Chờ duyệt" to snapshot.bookingPending,
                        "Đã duyệt" to snapshot.bookingApproved,
                        "Từ chối" to snapshot.bookingRejected,
                        "Đã hủy" to snapshot.bookingCancelled
                    )
                )
            }

            if (options.includeNotifications) {
                appendMarkdownTable(
                    title = "Thông báo",
                    rows = listOf(
                        "Tổng thông báo" to snapshot.notificationTotal,
                        "Hệ thống" to snapshot.notificationSystem,
                        "Booking" to snapshot.notificationBooking,
                        "Tài khoản" to snapshot.notificationAccount
                    )
                )
            }

            if (options.includeMachineReports) {
                appendMarkdownTable(
                    title = "Báo lỗi máy",
                    rows = listOf(
                        "Tổng báo lỗi" to snapshot.machineReportTotal,
                        "Đang xử lý" to snapshot.machineReportPending,
                        "Hoàn tất" to snapshot.machineReportDone
                    )
                )
            }
        }
    }

    private fun StringBuilder.appendMarkdownTable(
        title: String,
        rows: List<Pair<String, Int>>
    ) {
        appendLine("## $title")
        appendLine("| Chỉ số | Số lượng |")
        appendLine("|---|---:|")
        rows.forEach { row ->
            appendLine("| ${row.first} | ${row.second} |")
        }
        appendLine()
    }

    private fun buildChecklistReport(
        currentUser: User,
        snapshot: ExportReportSnapshot,
        options: ExportReportOptions
    ): String {
        return buildString {
            appendLine("CHECKLIST NGHIỆM THU HỆ THỐNG QUẢN LÝ PHÒNG MÁY")
            appendLine("Thời gian tạo: ${snapshot.generatedTimeText}")
            appendLine("Người tạo: ${currentUser.displayName.ifBlank { currentUser.email.ifBlank { "Quản trị viên" } }}")
            appendLine()

            if (options.includeAccounts) {
                appendLine("[x] Tài khoản: ${snapshot.accountTotal} bản ghi.")
                appendLine("[x] Phân quyền admin/giáo viên/sinh viên.")
            }

            if (options.includeRooms) {
                appendLine("[x] Phòng máy: ${snapshot.roomTotal} bản ghi.")
            }

            if (options.includeComputers) {
                appendLine("[x] Máy tính: ${snapshot.computerTotal} bản ghi.")
            }

            if (options.includeBookings) {
                appendLine("[x] Booking: ${snapshot.bookingTotal} bản ghi.")
            }

            if (options.includeNotifications) {
                appendLine("[x] Thông báo: ${snapshot.notificationTotal} bản ghi.")
            }

            if (options.includeMachineReports) {
                appendLine("[x] Báo lỗi máy: ${snapshot.machineReportTotal} bản ghi.")
            }

            appendLine("[x] CSV đầy đủ có thể xuất vào Download/$EXPORT_FOLDER_NAME.")
        }
    }

    private fun buildSummaryCsvReport(
        currentUser: User,
        snapshot: ExportReportSnapshot,
        options: ExportReportOptions
    ): String {
        return buildString {
            appendCsvRow(listOf("Nhom", "ChiSo", "GiaTri"))
            appendCsvRow(listOf("ThongTin", "NguoiTao", currentUser.displayName.ifBlank { currentUser.email }))
            appendCsvRow(listOf("ThongTin", "ThoiGian", snapshot.generatedTimeText))
            appendCsvRow(listOf("ThongTin", "LoaiBaoCao", reportTypeName(options.reportType)))

            if (options.includeAccounts) {
                appendCsvRow(listOf("TaiKhoan", "Tong", snapshot.accountTotal.toString()))
                appendCsvRow(listOf("TaiKhoan", "DaDuyet", snapshot.accountApproved.toString()))
                appendCsvRow(listOf("TaiKhoan", "ChoDuyet", snapshot.accountPending.toString()))
                appendCsvRow(listOf("TaiKhoan", "BiTuChoi", snapshot.accountRejected.toString()))
            }

            if (options.includeRooms) {
                appendCsvRow(listOf("PhongMay", "Tong", snapshot.roomTotal.toString()))
                appendCsvRow(listOf("PhongMay", "HoatDong", snapshot.roomActive.toString()))
                appendCsvRow(listOf("PhongMay", "BaoTri", snapshot.roomMaintenance.toString()))
            }

            if (options.includeComputers) {
                appendCsvRow(listOf("MayTinh", "Tong", snapshot.computerTotal.toString()))
                appendCsvRow(listOf("MayTinh", "Ranh", snapshot.computerAvailable.toString()))
                appendCsvRow(listOf("MayTinh", "DangDung", snapshot.computerInUse.toString()))
                appendCsvRow(listOf("MayTinh", "Hong", snapshot.computerBroken.toString()))
            }

            if (options.includeBookings) {
                appendCsvRow(listOf("Booking", "Tong", snapshot.bookingTotal.toString()))
                appendCsvRow(listOf("Booking", "ChoDuyet", snapshot.bookingPending.toString()))
                appendCsvRow(listOf("Booking", "DaDuyet", snapshot.bookingApproved.toString()))
            }

            if (options.includeNotifications) {
                appendCsvRow(listOf("ThongBao", "Tong", snapshot.notificationTotal.toString()))
            }

            if (options.includeMachineReports) {
                appendCsvRow(listOf("BaoLoiMay", "Tong", snapshot.machineReportTotal.toString()))
            }
        }
    }

    private fun readString(
        value: Any?
    ): String {
        return when (value) {
            null -> ""
            is String -> value.trim()
            is Number -> value.toString()
            is Boolean -> value.toString()
            is Timestamp -> formatTimestamp(value)
            is Date -> formatDate(value)
            else -> value.toString().trim()
        }
    }

    private fun formatTimestamp(
        value: Timestamp
    ): String {
        return formatDate(value.toDate())
    }

    private fun formatDate(
        value: Date
    ): String {
        return SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(value)
    }

    private fun normalizeRole(
        raw: String
    ): String {
        val value = normalizeRaw(raw)

        return when (value) {
            "admin",
            "administrator",
            "quan_tri",
            "quan tri",
            "quan tri",
            "qtv" -> ROLE_ADMIN

            "teacher",
            "giao_vien",
            "giao vien",
            "giang_vien",
            "giang vien",
            "giaovien",
            "giangvien" -> ROLE_TEACHER

            "student",
            "sinh_vien",
            "sinh vien",
            "sinhvien" -> ROLE_STUDENT

            else -> value
        }
    }

    private fun normalizeComputerStatus(
        raw: String
    ): String {
        val value = normalizeRaw(raw)

        return when (value) {
            "available",
            "free",
            "ready",
            "idle",
            "empty",
            "ranh",
            "san sang" -> COMPUTER_AVAILABLE

            "in_use",
            "inuse",
            "using",
            "used",
            "busy",
            "occupied",
            "dang dung",
            "dang su dung" -> COMPUTER_IN_USE

            "broken",
            "error",
            "failed",
            "damaged",
            "hong",
            "loi" -> COMPUTER_BROKEN

            "maintenance",
            "maintain",
            "repair",
            "fixing",
            "bao tri",
            "sua chua" -> COMPUTER_MAINTENANCE

            "locked",
            "lock",
            "disabled",
            "blocked",
            "khoa",
            "da khoa" -> COMPUTER_LOCKED

            else -> COMPUTER_AVAILABLE
        }
    }

    private fun normalizeNotificationType(
        raw: String
    ): String {
        val value = normalizeRaw(raw)

        return when (value) {
            "booking",
            "book",
            "lich",
            "schedule",
            "seat",
            "seating",
            "cho_ngoi",
            "cho ngoi" -> NOTIFICATION_BOOKING

            "account",
            "user",
            "tai_khoan",
            "tai khoan" -> NOTIFICATION_ACCOUNT

            else -> NOTIFICATION_SYSTEM
        }
    }

    private fun isPendingStatus(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "pending",
            "wait",
            "waiting",
            "cho_duyet",
            "cho duyet",
            "dang cho"
        )
    }

    private fun isApprovedStatus(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "approved",
            "approve",
            "active",
            "accepted",
            "duyet",
            "da duyet",
            "hoat dong"
        )
    }

    private fun isRejectedStatus(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "rejected",
            "reject",
            "denied",
            "declined",
            "tu choi",
            "bi tu choi"
        )
    }

    private fun isCancelledStatus(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "cancelled",
            "canceled",
            "cancel",
            "huy",
            "da huy"
        )
    }

    private fun isDoneStatus(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "done",
            "fixed",
            "completed",
            "complete",
            "resolved",
            "da xu ly",
            "hoan tat",
            "xong"
        )
    }

    private fun isActiveRoom(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value.isBlank() ||
                value in setOf(
            "active",
            "available",
            "ready",
            "open",
            "hoat dong",
            "san sang"
        )
    }

    private fun isInactiveRoom(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "inactive",
            "closed",
            "disabled",
            "ngung",
            "dong",
            "khong hoat dong"
        )
    }

    private fun isMaintenanceStatus(
        raw: String
    ): Boolean {
        val value = normalizeRaw(raw)

        return value in setOf(
            "maintenance",
            "maintain",
            "repair",
            "bao tri",
            "sua chua"
        )
    }

    private fun reportTypeName(
        type: String
    ): String {
        return when (type.trim().lowercase(Locale.getDefault())) {
            REPORT_TYPE_MANAGEMENT -> "Báo cáo quản trị"
            REPORT_TYPE_SUBMISSION -> "Báo cáo nộp bài"
            REPORT_TYPE_FULL -> "Báo cáo đầy đủ"
            else -> "Báo cáo tổng quan"
        }
    }

    private fun normalizeRaw(
        raw: String
    ): String {
        val lowercase = raw.trim().lowercase(Locale.getDefault())
        val normalized = Normalizer.normalize(
            lowercase,
            Normalizer.Form.NFD
        )

        return normalized.replace("\\p{M}+".toRegex(), "")
    }

    private fun escapeCsv(
        value: String
    ): String {
        val cleanValue = value.replace("\"", "\"\"")

        return if (
            cleanValue.contains(",") ||
            cleanValue.contains("\"") ||
            cleanValue.contains("\n") ||
            cleanValue.contains("\r")
        ) {
            "\"$cleanValue\""
        } else {
            cleanValue
        }
    }

    private fun normalizeFirestoreError(
        exception: Exception
    ): String {
        val raw = exception.message.orEmpty().lowercase(Locale.getDefault())

        return when {
            raw.contains("permission") ||
                    raw.contains("missing or insufficient permissions") -> {
                "Bạn không có quyền đọc dữ liệu báo cáo."
            }

            raw.contains("network") ||
                    raw.contains("unavailable") ||
                    raw.contains("deadline") -> {
                "Không thể kết nối Firestore. Vui lòng thử lại."
            }

            raw.contains("failed_precondition") ||
                    raw.contains("index") -> {
                "Firestore cần tạo index hoặc truy vấn chưa sẵn sàng."
            }

            else -> {
                "Không thể tải dữ liệu báo cáo. Vui lòng thử lại."
            }
        }
    }

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_ROOMS = "computerRooms"
        const val COLLECTION_ROOMS_LEGACY = "rooms"
        const val COLLECTION_COMPUTERS = "computers"
        const val COLLECTION_BOOKINGS = "bookings"
        const val COLLECTION_NOTIFICATIONS = "notifications"
        const val COLLECTION_MACHINE_REPORTS = "machineReports"

        const val EXPORT_FOLDER_NAME = "QuanLyPhongMay"

        const val ROLE_ADMIN = "admin"
        const val ROLE_TEACHER = "teacher"
        const val ROLE_STUDENT = "student"

        const val COMPUTER_AVAILABLE = "available"
        const val COMPUTER_IN_USE = "in_use"
        const val COMPUTER_BROKEN = "broken"
        const val COMPUTER_MAINTENANCE = "maintenance"
        const val COMPUTER_LOCKED = "locked"

        const val NOTIFICATION_SYSTEM = "system"
        const val NOTIFICATION_BOOKING = "booking"
        const val NOTIFICATION_ACCOUNT = "account"

        const val REPORT_TYPE_OVERVIEW = "overview"
        const val REPORT_TYPE_MANAGEMENT = "management"
        const val REPORT_TYPE_SUBMISSION = "submission"
        const val REPORT_TYPE_FULL = "full"

        const val FORMAT_TEXT = "text"
        const val FORMAT_MARKDOWN = "markdown"
        const val FORMAT_CHECKLIST = "checklist"
        const val FORMAT_CSV = "csv"
    }
}
