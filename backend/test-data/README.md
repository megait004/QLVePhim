# Hướng dẫn Kiểm thử API

## Cấu trúc Thư mục

```
test-data/
├── admin/           # Dữ liệu kiểm thử cho thao tác admin
│   ├── register-admin.json
│   ├── create-movie.json
│   ├── update-movie.json
│   ├── create-screening.json
│   └── create-multiple-screenings.json
├── user/            # Dữ liệu kiểm thử cho thao tác người dùng
│   ├── register.json
│   ├── login.json
│   ├── update-user.json
│   ├── book-ticket.json
│   ├── book-multiple-tickets.json
│   └── update-ticket-status.json
└── public/          # Dữ liệu kiểm thử cho thao tác công khai
    └── search-movies.json
```

## Cài đặt

1. Khởi động ứng dụng
2. Đặt token JWT của bạn như một biến môi trường:

```cmd
set TOKEN=token của admin hoặc user
```

## Các Kịch bản Kiểm thử

### 1. Thao tác Công khai (Không yêu cầu xác thực)

#### 1.1 Thao tác với Phim

```cmd
# Lấy tất cả phim
curl -X GET http://localhost:8080/api/movies/public/all

# Lấy phim đang chiếu
curl -X GET http://localhost:8080/api/movies/public/now-showing

# Lấy phim theo thể loại
curl -X GET http://localhost:8080/api/movies/public/genre/Action

# Lấy chi tiết phim
curl -X GET http://localhost:8080/api/movies/public/1

# Tìm kiếm phim
curl -X POST http://localhost:8080/api/movies/public/search -H "Content-Type: application/json" -d @test-data/public/search-movies.json
```

### 2. Thao tác Người dùng (Yêu cầu xác thực)

#### 2.1 Xác thực

```cmd
# Đăng ký người dùng mới
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d @test-data/user/register.json

# Đăng nhập
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d @test-data/user/login.json

# Cập nhật thông tin cá nhân
curl -X PUT http://localhost:8080/api/users/profile -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/update-user.json

# Lấy thông tin cá nhân
curl -X GET http://localhost:8080/api/users/profile -H "Authorization: Bearer %TOKEN%"
```

#### 2.2 Thao tác với Vé

```cmd
# Đặt một vé
curl -X POST http://localhost:8080/api/tickets/book -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/book-ticket.json

# Đặt nhiều vé
curl -X POST http://localhost:8080/api/tickets/book/batch -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/book-multiple-tickets.json

# Lấy danh sách vé của người dùng
curl -X GET http://localhost:8080/api/tickets/user/1 -H "Authorization: Bearer %TOKEN%"

# Lấy chi tiết vé
curl -X GET http://localhost:8080/api/tickets/1 -H "Authorization: Bearer %TOKEN%"

# Cập nhật trạng thái vé
curl -X PUT http://localhost:8080/api/tickets/1/status -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/update-ticket-status.json
```

### 3. Thao tác Admin (Yêu cầu xác thực Admin)

#### 3.1 Xác thực Admin

```cmd
# Đăng ký tài khoản admin
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d @test-data/admin/register-admin.json
```

#### 3.2 Quản lý Phim

```cmd
# Tạo phim mới
curl -X POST http://localhost:8080/api/movies -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/create-movie.json

# Cập nhật phim
curl -X PUT http://localhost:8080/api/movies/1 -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/update-movie.json

# Xóa phim
curl -X DELETE http://localhost:8080/api/movies/1 -H "Authorization: Bearer %TOKEN%"
```

#### 3.3 Quản lý Suất chiếu

```cmd
# Tạo suất chiếu mới
curl -X POST http://localhost:8080/api/screenings -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/create-screening.json

# Tạo nhiều suất chiếu
curl -X POST http://localhost:8080/api/screenings/batch -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/create-multiple-screenings.json

# Xóa suất chiếu
curl -X DELETE http://localhost:8080/api/screenings/1 -H "Authorization: Bearer %TOKEN%"
```

### 4. Thao tác với Suất chiếu

```cmd
# Lấy thông tin suất chiếu theo ID
curl -X GET http://localhost:8080/api/screenings/1 -H "Authorization: Bearer %TOKEN%"

# Lấy danh sách suất chiếu theo phim
curl -X GET http://localhost:8080/api/screenings/movie/1 -H "Authorization: Bearer %TOKEN%"

# Lấy danh sách suất chiếu theo khoảng thời gian
curl -X GET "http://localhost:8080/api/screenings/date-range?start=2025-05-10T00:00:00&end=2025-05-17T23:59:59" -H "Authorization: Bearer %TOKEN%"
```

## Mô tả File Dữ liệu Kiểm thử

### Dữ liệu Kiểm thử Admin

- `register-admin.json`: Dữ liệu đăng ký tài khoản admin
- `create-movie.json`: Dữ liệu tạo phim mới
- `update-movie.json`: Dữ liệu cập nhật phim
- `create-screening.json`: Dữ liệu tạo một suất chiếu
- `create-multiple-screenings.json`: Dữ liệu tạo nhiều suất chiếu

### Dữ liệu Kiểm thử Người dùng

- `register.json`: Dữ liệu đăng ký người dùng thông thường
- `login.json`: Thông tin đăng nhập
- `update-user.json`: Dữ liệu cập nhật thông tin người dùng
- `book-ticket.json`: Dữ liệu đặt một vé
- `book-multiple-tickets.json`: Dữ liệu đặt nhiều vé
- `update-ticket-status.json`: Dữ liệu cập nhật trạng thái vé

### Dữ liệu Kiểm thử Công khai

- `search-movies.json`: Tiêu chí tìm kiếm phim

## Lời khuyên khi Kiểm thử

1. Luôn kiểm tra mã trạng thái và nội dung phản hồi
2. Đối với các endpoint được bảo vệ, đảm bảo token còn hiệu lực và chưa hết hạn
3. Kiểm thử cả trường hợp thành công và thất bại
4. Theo dõi ID của tài nguyên đã tạo để thực hiện cập nhật/xóa
5. Dọn dẹp dữ liệu kiểm thử sau khi hoàn thành (xóa các tài nguyên đã tạo)
6. Kiểm thử thao tác hàng loạt với cả dữ liệu hợp lệ và không hợp lệ
7. Xác minh việc xóa theo cascade (ví dụ: xóa phim sẽ xóa các suất chiếu và vé liên quan)

## Mã Trạng thái HTTP Thông dụng

- 200 OK: Yêu cầu thành công
- 201 Created: Tạo tài nguyên thành công
- 400 Bad Request: Dữ liệu đầu vào không hợp lệ
- 401 Unauthorized: Thiếu hoặc token không hợp lệ
- 403 Forbidden: Không đủ quyền truy cập
- 404 Not Found: Không tìm thấy tài nguyên
- 409 Conflict: Tài nguyên đã tồn tại
- 500 Internal Server Error: Lỗi phía máy chủ
