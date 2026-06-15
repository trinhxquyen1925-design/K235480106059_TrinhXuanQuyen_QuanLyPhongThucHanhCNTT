# Xây dựng phần mềm Quản lý Phòng Thực hành Tin học Khoa CNTT

## Thông tin đề tài

- Sinh viên thực hiện: **Trịnh Xuân Quyền**
- MSSV: **K235480106059**
- Trường: **Đại học Kỹ thuật Công nghiệp - Đại học Thái Nguyên**
- Môn học: **Phân tích và Thiết kế Hệ thống Thông tin**

---

## Giới thiệu

Ứng dụng Android hỗ trợ quản lý phòng thực hành tin học dành cho khoa Công nghệ Thông tin.

Hệ thống cho phép quản lý tài khoản, đặt lịch sử dụng phòng máy, phân chỗ ngồi, gửi thông báo, quản lý thiết bị và xử lý báo lỗi máy tính trên nền tảng Firebase.

---

## Chức năng chính

### Sinh viên

- Đăng ký tài khoản
- Đăng nhập hệ thống
- Xem lịch học thực hành
- Xem vị trí chỗ ngồi
- Xem thông báo
- Báo lỗi máy tính

### Giáo viên

- Đăng ký tài khoản
- Đăng nhập hệ thống
- Tạo yêu cầu sử dụng phòng máy
- Xem lịch giảng dạy
- Gửi thông báo cho sinh viên
- Báo lỗi máy tính

### Quản trị viên

- Duyệt tài khoản người dùng
- Quản lý tài khoản
- Duyệt booking phòng máy
- Quản lý phòng máy
- Quản lý máy tính
- Xử lý báo lỗi máy
- Gửi thông báo hệ thống
- Xuất dữ liệu CSV

---

## Công nghệ sử dụng

- Kotlin
- Android Studio
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Material Design 3
- MVVM Architecture
- Repository Pattern

---

# Hướng dẫn cài đặt

## 1. Yêu cầu

- Android Studio Hedgehog hoặc mới hơn
- JDK 17
- Android SDK tương thích
- Thiết bị Android hoặc Android Emulator

---

## 2. Tải mã nguồn

Clone repository:

```bash
git clone <LINK_GITHUB>
```

Hoặc tải file ZIP và giải nén.

---

## 3. Mở dự án

- Mở Android Studio
- Chọn Open
- Chọn thư mục dự án
- Chờ Gradle Sync hoàn tất

---

## 4. Kiểm tra cấu hình Firebase

Dự án đã bao gồm file cấu hình Firebase.

Kiểm tra file:

```text
app/google-services.json
```

Nếu file đã tồn tại thì không cần cấu hình thêm.

---

## 5. Chạy ứng dụng

- Kết nối điện thoại Android hoặc mở Emulator
- Nhấn Run ▶
- Chờ ứng dụng build và cài đặt

---

## Tài khoản sử dụng

Người dùng tự đăng ký tài khoản trực tiếp trên ứng dụng.

Sau khi đăng ký:

- Tài khoản ở trạng thái chờ duyệt
- Quản trị viên xác nhận tài khoản
- Sau khi được duyệt có thể sử dụng đầy đủ chức năng

Lưu ý:

Tài khoản quản trị viên được sử dụng nội bộ và không công khai trong repository.

---

## Cấu trúc dự án

```text
app
├── manifests
├── model
├── repository
├── navigation
├── screens
├── core
└── utils
```

---

## Mã nguồn

Repository này được sử dụng để lưu trữ toàn bộ mã nguồn phục vụ đồ án môn học Phân tích và Thiết kế Hệ thống Thông tin.

---

## Tác giả

**Trịnh Xuân Quyền**

**MSSV: K235480106059**

Đại học Kỹ thuật Công nghiệp - Đại học Thái Nguyên

Năm thực hiện: 2026
