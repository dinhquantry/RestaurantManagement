USE master;
GO

-- 1. Xóa Database cũ nếu tồn tại để tạo mới (Cẩn thận khi dùng trên production)
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
CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Lưu ý: Thực tế nên mã hóa
    full_name NVARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('MANAGER', 'STAFF')),
    avatar_path NVARCHAR(500) NULL, -- Cho phép để trống (NULL)
    phone VARCHAR(15) NULL,
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng 2: Danh mục Món ăn
CREATE TABLE Foods (
    food_id INT PRIMARY KEY IDENTITY(1,1),
    food_name NVARCHAR(100) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    category NVARCHAR(50) DEFAULT N'Món chính',
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, SOLD_OUT, STOPPED
    image_path NVARCHAR(500) NULL -- Cho phép để trống (NULL)
);

-- Bảng 3: Sơ đồ Bàn ăn
CREATE TABLE Tables (
    table_id INT PRIMARY KEY IDENTITY(1,1),
    table_name NVARCHAR(50) NOT NULL,
    capacity INT DEFAULT 4,
    status VARCHAR(20) DEFAULT 'EMPTY' -- EMPTY, OCCUPIED, BOOKED
);

-- Bảng 4: Đặt bàn (Booking)
CREATE TABLE Bookings (
    booking_id INT PRIMARY KEY IDENTITY(1,1),
    customer_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    table_id INT NOT NULL,
    booking_time DATETIME NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'CONFIRMED', -- CONFIRMED, CANCELLED, COMPLETED
    FOREIGN KEY (table_id) REFERENCES Tables(table_id)
);

-- Bảng 5: Hóa đơn / Đơn hàng (Order)
CREATE TABLE Orders (
    order_id INT PRIMARY KEY IDENTITY(1,1),
    table_id INT NULL, -- Có thể NULL nếu mua mang về
    user_id INT NOT NULL, -- Nhân viên tạo đơn
    order_date DATETIME DEFAULT GETDATE(),
    total_amount DECIMAL(18, 2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING (Đang ăn), PAID (Đã tt), CANCELLED
    FOREIGN KEY (table_id) REFERENCES Tables(table_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng 6: Chi tiết hóa đơn (Lưu món ăn trong từng đơn)
CREATE TABLE OrderDetails (
    detail_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price_at_order DECIMAL(18, 2) NOT NULL, -- Lưu giá tại thời điểm bán (đề phòng giá gốc đổi)
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (food_id) REFERENCES Foods(food_id)
);

-- Bảng 7: Kho Dụng cụ
CREATE TABLE Tools (
    tool_id INT PRIMARY KEY IDENTITY(1,1),
    tool_name NVARCHAR(100) NOT NULL,
    quantity INT DEFAULT 0,
    min_threshold INT DEFAULT 10, -- Mức cảnh báo tối thiểu
    status NVARCHAR(50) DEFAULT N'Tốt'
);
GO

-- =============================================
-- 4. THÊM DỮ LIỆU MẪU (SEED DATA)
-- =============================================

-- Users
INSERT INTO Users (username, password, full_name, role, avatar_path) VALUES 
('admin', '123', N'Nguyễn Quản Lý', 'MANAGER', 'data/images/admin_avatar.png'),
('staff1', '123', N'Trần Phục Vụ', 'STAFF', NULL), -- Avatar NULL
('staff2', '123', N'Lê Thu Ngân', 'STAFF', NULL);

-- Foods
INSERT INTO Foods (food_name, price, category, image_path) VALUES 
(N'Bò Bít Tết', 150000, N'Món Chính', 'data/images/beef_steak.jpg'),
(N'Mỳ Ý Sốt Kem', 120000, N'Món Chính', NULL), -- Ảnh NULL
(N'Salad Cá Ngừ', 85000, N'Khai Vị', NULL),
(N'Coca Cola', 20000, N'Đồ Uống', 'data/images/coca.png'),
(N'Rượu Vang Đỏ', 550000, N'Đồ Uống', NULL);

-- Tables
INSERT INTO Tables (table_name, status) VALUES 
(N'Bàn 01', 'EMPTY'),
(N'Bàn 02', 'OCCUPIED'), -- Đang có khách
(N'Bàn 03', 'EMPTY'),
(N'Bàn VIP 1', 'BOOKED'), -- Đã đặt
(N'Bàn VIP 2', 'EMPTY');

-- Tools
INSERT INTO Tools (tool_name, quantity, min_threshold) VALUES 
(N'Dĩa Inox', 45, 50), -- Sẽ bị cảnh báo vì < 50
(N'Ly Thủy Tinh', 100, 20),
(N'Khăn Trải Bàn', 8, 10); -- Sẽ bị cảnh báo

-- Bookings (Dữ liệu mẫu cho Bàn VIP 1)
INSERT INTO Bookings (customer_name, phone, table_id, booking_time) VALUES 
(N'Anh Nam', '0988123456', 4, DATEADD(hour, 2, GETDATE())); -- Đặt sau 2 tiếng nữa

-- Orders (Dữ liệu mẫu cho Bàn 02 đang ăn)
INSERT INTO Orders (table_id, user_id, status, total_amount) VALUES 
(2, 2, 'PENDING', 0); -- Do staff1 tạo

-- Order Details (Khách bàn 2 đang ăn gì?)
INSERT INTO OrderDetails (order_id, food_id, quantity, price_at_order) VALUES
(1, 1, 2, 150000), -- 2 Bò bít tết
(1, 4, 3, 20000);  -- 3 Coca

-- Cập nhật lại tổng tiền cho Order mẫu
UPDATE Orders SET total_amount = (2*150000 + 3*20000) WHERE order_id = 1;

GO

PRINT N'Cài đặt Database thành công!';