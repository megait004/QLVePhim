# Hệ Thống Quản Lý Rạp Chiếu Phim

Ứng dụng web full-stack để quản lý vé  phim, được xây dựng bằng Spring Boot và React.

## Cấu Trúc Dự Án

```
cinema-project/
├── backend/           # Ứng dụng Spring Boot
│   ├── src/          # Mã nguồn
│   ├── test-data/    # Dữ liệu test và ví dụ API
│   └── pom.xml       # Quản lý dependencies Maven
└── frontend/         # Ứng dụng React (Sắp ra mắt)
```

## Backend

Backend được xây dựng với:
- Spring Boot 3.x
- Spring Security với JWT
- Spring Data JPA
- PostgreSQL(Supabase)
- Maven

### Tính Năng
- Xác thực và phân quyền người dùng (JWT)
- Quản lý phim
- Quản lý lịch chiếu
- Hệ thống đặt vé
- Quản lý thông tin người dùng

### Tài Liệu API
Tài liệu API chi tiết có thể được tìm thấy trong `backend/test-data/README.md`

## Bắt Đầu

### Yêu Cầu Hệ Thống
- Java 17 trở lên
- Maven
- PostgreSQL
- Node.js và npm (cho frontend - sắp ra mắt)

### Chạy Backend
1. Di chuyển vào thư mục backend:
```bash
cd backend
```

2. Build dự án:
```bash
mvn clean install
```

3. Chạy ứng dụng:
```bash
mvn spring-boot:run
```

Server backend sẽ chạy tại `http://localhost:8080`

## Frontend (Sắp Ra Mắt)
Frontend sẽ được xây dựng bằng React và bao gồm:
- Giao diện hiện đại với Material-UI
- Quản lý state bằng Redux
- Điều hướng với React Router
- Thiết kế responsive
- Giao diện đặt vé thân thiện với người dùng

## Các API Chính

### 1. Quản Lý Người Dùng
- Đăng ký tài khoản
- Đăng nhập
- Cập nhật thông tin cá nhân
- Xem thông tin cá nhân

### 2. Quản Lý Phim
- Xem danh sách phim
- Xem phim đang chiếu
- Tìm kiếm phim theo tiêu đề, thể loại
- Thêm/Sửa/Xóa phim (Admin)

### 3. Quản Lý Suất Chiếu
- Xem lịch chiếu theo phim
- Xem lịch chiếu theo ngày
- Thêm/Xóa suất chiếu (Admin)

### 4. Đặt Vé
- Đặt một vé
- Đặt nhiều vé
- Xem lịch sử đặt vé
- Hủy vé

## Hướng Dẫn Đóng Góp
1. Fork repository
2. Tạo nhánh tính năng mới (`git checkout -b feature/TinhNangMoi`)
3. Commit thay đổi (`git commit -m 'Thêm tính năng mới'`)
4. Push lên nhánh (`git push origin feature/TinhNangMoi`)
5. Tạo Pull Request
