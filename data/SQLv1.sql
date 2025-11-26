USE master;
GO

-- 1. Xóa Database cũ nếu tồn tại để tạo mới (Lưu ý: Sẽ mất hết dữ liệu cũ)
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'RestaurantDB')
BEGIN
    ALTER DATABASE RestaurantDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE RestaurantDB;
END
GO

-- 2. Tạo Database mới
CREATE DATABASE RestaurantDB;
GO

USE RestaurantDB;
GO

-- =============================================
-- 3. TẠO BẢNG (TABLES)
-- =============================================

-- Bảng 1: Người dùng (Quản lý & Nhân viên)
-- Cập nhật: Thêm cột is_active cho chức năng Xóa mềm (Soft Delete)
CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('MANAGER', 'STAFF')),
    avatar_path NVARCHAR(500) NULL,
    phone VARCHAR(15) NULL,
    created_at DATETIME DEFAULT GETDATE(),
    is_active BIT DEFAULT 1 -- 1: Đang làm, 0: Đã nghỉ việc
);

-- Bảng 2: Danh mục Món ăn
CREATE TABLE Foods (
    food_id INT PRIMARY KEY IDENTITY(1,1),
    food_name NVARCHAR(100) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    category NVARCHAR(50) DEFAULT N'Món chính',
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, SOLD_OUT, STOPPED
    image_path NVARCHAR(500) NULL
);

-- Bảng 3: Sơ đồ Bàn ăn
-- Cập nhật: Thêm cột floor (Tầng)
CREATE TABLE Tables (
    table_id INT PRIMARY KEY IDENTITY(1,1),
    table_name NVARCHAR(50) NOT NULL,
    capacity INT DEFAULT 4,
    status VARCHAR(20) DEFAULT 'EMPTY', -- EMPTY, OCCUPIED, BOOKED
    floor INT DEFAULT 1 -- Tầng 1, 2, 3...
);

-- Bảng 4: Đặt bàn (Booking)
CREATE TABLE Bookings (
    booking_id INT PRIMARY KEY IDENTITY(1,1),
    customer_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    table_id INT NOT NULL,
    booking_time DATETIME NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    FOREIGN KEY (table_id) REFERENCES Tables(table_id)
);

-- Bảng 5: Ca làm việc (Shifts) - Tính năng mới
CREATE TABLE Shifts (
    shift_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    shift_date DATE NOT NULL,
    shift_name NVARCHAR(50), -- Ví dụ: "Ca Sáng", "Ca Chiều"
    note NVARCHAR(200),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng 6: Hóa đơn / Đơn hàng (Orders)
CREATE TABLE Orders (
    order_id INT PRIMARY KEY IDENTITY(1,1),
    table_id INT NULL,
    user_id INT NOT NULL, -- Nhân viên tạo đơn
    order_date DATETIME DEFAULT GETDATE(),
    total_amount DECIMAL(18, 2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING (Đang ăn), PAID (Đã tt)
    FOREIGN KEY (table_id) REFERENCES Tables(table_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng 7: Chi tiết hóa đơn (OrderDetails)
CREATE TABLE OrderDetails (
    detail_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price_at_order DECIMAL(18, 2) NOT NULL, -- Lưu giá gốc lúc bán
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (food_id) REFERENCES Foods(food_id)
);

-- Bảng 8: Kho Dụng cụ (Tools)
CREATE TABLE Tools (
    tool_id INT PRIMARY KEY IDENTITY(1,1),
    tool_name NVARCHAR(100) NOT NULL,
    quantity INT DEFAULT 0,
    min_threshold INT DEFAULT 10, -- Mức cảnh báo
    status NVARCHAR(50) DEFAULT N'Tốt'
);
GO

-- =============================================
-- 4. THÊM DỮ LIỆU MẪU (SEED DATA)
-- =============================================

-- Users (Mặc định is_active = 1)
INSERT INTO Users (username, password, full_name, role, is_active) VALUES
('admin', '123', N'Nguyễn Quản Lý', 'MANAGER', 1),
('staff1', '123', N'Trần Phục Vụ', 'STAFF', 1),
('staff2', '123', N'Lê Thu Ngân', 'STAFF', 1);

-- Foods
INSERT INTO Foods (food_name, price, category, status) VALUES
(N'Bò Bít Tết', 150000, N'Món Chính', 'AVAILABLE'),
(N'Mỳ Ý Sốt Kem', 120000, N'Món Chính', 'AVAILABLE'),
(N'Salad Cá Ngừ', 85000, N'Khai Vị', 'AVAILABLE'),
(N'Khoai Tây Chiên', 45000, N'Khai Vị', 'AVAILABLE'),
(N'Coca Cola', 20000, N'Đồ Uống', 'AVAILABLE'),
(N'Rượu Vang Đỏ', 550000, N'Đồ Uống', 'AVAILABLE');

-- Tables (Chia 2 tầng)
INSERT INTO Tables (table_name, capacity, status, floor) VALUES
(N'Bàn 1.01', 4, 'EMPTY', 1),
(N'Bàn 1.02', 4, 'EMPTY', 1),
(N'Bàn 1.03', 6, 'EMPTY', 1),
(N'Bàn 2.01', 4, 'EMPTY', 2),
(N'Bàn 2.02', 8, 'EMPTY', 2),
(N'Bàn VIP', 10, 'EMPTY', 2);

-- Tools
INSERT INTO Tools (tool_name, quantity, min_threshold) VALUES
(N'Dĩa Inox', 45, 50), -- Cảnh báo đỏ
(N'Ly Thủy Tinh', 100, 20),
(N'Khăn Trải Bàn', 8, 10); -- Cảnh báo đỏ

-- Tạo một vài đơn hàng mẫu đã thanh toán để test Báo cáo
-- Đơn 1: Ngày hôm nay
INSERT INTO Orders (user_id, order_date, total_amount, status) VALUES (1, GETDATE(), 320000, 'PAID');
-- Đơn 2: Ngày hôm qua
INSERT INTO Orders (user_id, order_date, total_amount, status) VALUES (1, DATEADD(day, -1, GETDATE()), 550000, 'PAID');
-- Đơn 3: Tuần trước
INSERT INTO Orders (user_id, order_date, total_amount, status) VALUES (1, DATEADD(day, -7, GETDATE()), 1200000, 'PAID');

GO

PRINT N'Cài đặt Database thành công!';