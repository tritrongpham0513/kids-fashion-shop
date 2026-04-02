-- Script to seed products and categories for Kids Fashion Shop
SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;

-- Delete existing products and categories to start fresh
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Categories
INSERT INTO categories (id, name, description) VALUES
(1, 'Áo Trẻ Em', 'Áo thun, áo sơ mi, áo khoác, và hoodie năng động'),
(2, 'Quần Trẻ Em', 'Quần jean, quần vải, quần short thoải mái'),
(3, 'Váy & Đầm', 'Váy công chúa, đầm dự tiệc và váy dạo phố'),
(4, 'Giày Dép', 'Sandal, giày thể thao và giày thời trang'),
(5, 'Phụ Kiện', 'Mũ, túi xách, băng đô và tất trẻ em');

-- 2. Insert Products
INSERT INTO products (name, description, price, image_url, stock_quantity, is_new_arrival, is_best_seller, created_at, category_id) VALUES
-- Áo Trẻ Em (Category 1)
('Áo Thun Cotton Basic Trắng', 'Chất liệu 100% cotton co giãn, thấm hút mồ hôi tốt.', 149000, 'https://images.unsplash.com/photo-1519234110450-1aa96369c478?w=800&q=80', 50, 1, 1, NOW(), 1),
('Áo Sơ Mi Sọc Caro Xanh', 'Thiết kế thanh lịch cho bé trai, phù hợp đi học, đi tiệc.', 229000, 'https://images.unsplash.com/photo-1519457431-7571f0181146?w=800&q=80', 30, 1, 0, NOW(), 1),
('Áo Hoodie Khủng Long Có Tai', 'Nỉ bông ấm áp với thiết kế khủng long ngộ nghĩnh.', 319000, 'https://images.unsplash.com/photo-1622273510268-aa18486413f3?w=800&q=80', 25, 1, 1, NOW(), 1),
('Áo Khoác Bomber Năng Động', 'Phong cách thể thao mạnh mẽ, phối màu cá tính.', 389000, 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80', 15, 0, 1, NOW(), 1),
('Áo Len Họa Tiết Tuần Lộc', 'Dành cho mùa đông ấm áp, len mềm không xơ.', 279000, 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800&q=80', 20, 0, 0, NOW(), 1),
('Áo Thun In Hình Hoạt Hình', 'In 3D sắc nét, không bong tróc khi giặt máy.', 169000, 'https://images.unsplash.com/photo-1519234110450-1aa96369c478?w=800&q=80', 45, 1, 0, NOW(), 1),
('Áo Polo Bé Trai Sang Trọng', 'Cổ bẻ thanh lịch, chất liệu cá sấu cao cấp.', 259000, 'https://images.unsplash.com/photo-1603910309855-3c82e6bbdf55?w=800&q=80', 22, 0, 1, NOW(), 1),
('Áo Khoác Jean Bụi Phủm', 'Jean cao cấp bền màu, phong cách cá tính.', 359000, 'https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=800&q=80', 12, 1, 0, NOW(), 1),
('Áo Sát Nách Mùa HÈ', 'Thoáng mát cho những ngày oi bức.', 129000, 'https://images.unsplash.com/photo-1503919005314-2a1ccc787d8c?w=800&q=80', 60, 0, 1, NOW(), 1),

-- Quần Trẻ Em (Category 2)
('Quần Jean Dài Co Giãn', 'Bền đẹp, phù hợp cho mọi hoạt động vui chơi.', 289000, 'https://images.unsplash.com/photo-1519457431-7571f0181146?w=800&q=80', 35, 1, 1, NOW(), 2),
('Quần Short Khaki Màu Be', 'Dễ phối đồ, phong cách vintage.', 189000, 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80', 40, 1, 0, NOW(), 2),
('Quần Jogger Nỉ Thể Thao', 'Thoải mái cho bé tập thể dục hoặc mặc nhà.', 199000, 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80', 50, 0, 1, NOW(), 2),
('Quần Vải Linen Mùa Hè', 'Mát mẻ, thấm hút tốt, an toàn cho da.', 219000, 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=800&q=80', 28, 1, 0, NOW(), 2),
('Quần Yếm Bò Bé Trai', 'Dễ thương, phong cách Hàn Quốc.', 329000, 'https://images.unsplash.com/photo-1541093306590-bad0a7a62f4e?w=800&q=80', 18, 0, 1, NOW(), 2),
('Quần Short Thun Đi Biển', 'Họa tiết nhiệt đới rực rỡ.', 159000, 'https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=800&q=80', 55, 1, 0, NOW(), 2),
('Quần Legging Bé Gái', 'Mềm mại, nhiều màu sắc lựa chọn.', 139000, 'https://images.unsplash.com/photo-1622273510268-aa18486413f3?w=800&q=80', 70, 0, 1, NOW(), 2),

-- Váy & Đầm (Category 3)
('Váy Công Chúa Tulle Hồng', 'Lộng lẫy với nhiều lớp voan mềm mại.', 459000, 'https://images.unsplash.com/photo-1596870230757-0b5d27f5f0b2?w=800&q=80', 15, 1, 1, NOW(), 3),
('Đầm Hoa Nhí Vintage', 'Chất liệu thô đũi mát mẻ, họa tiết dễ thương.', 299000, 'https://images.unsplash.com/photo-1621451537084-482c73073a0f?w=800&q=80', 25, 1, 0, NOW(), 3),
('Váy Yếm Dạ Thu Đông', 'Giữ ấm tốt, phối cùng áo len cực xinh.', 349000, 'https://images.unsplash.com/photo-1518831959646-742c3a14ebf7?w=800&q=80', 20, 0, 1, NOW(), 3),
('Đầm Xòe Chấm Bi Trắng Đen', 'Cổ điển, phù hợp dạo phố, chụp ảnh.', 279000, 'https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=800&q=80', 30, 0, 0, NOW(), 3),
('Váy Maxi Đi Biển Cho Bé', 'Dáng dài thướt tha, họa tiết hoa hướng dương.', 329000, 'https://images.unsplash.com/photo-1515377905703-c4788e51af15?w=800&q=80', 22, 1, 1, NOW(), 3),
('Chân Váy Xếp Ly Xám', 'Phong cách nữ sinh tiểu học.', 219000, 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80', 35, 1, 0, NOW(), 3),
('Đầm Ren Dự Tiệc Cao Cấp', 'Tinh xảo đến từng đường kim mũi chỉ.', 589000, 'https://images.unsplash.com/photo-1617331721458-bd3bd3f9c7f8?w=800&q=80', 10, 1, 1, NOW(), 3),

-- Giày Dép (Category 4)
('Giày Sneaker Trắng Năng Động', 'Đế êm, chống trơn trượt, bảo vệ chân bé.', 399000, 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80', 18, 1, 1, NOW(), 4),
('Sandal Quai Hậu Da Mềm', 'Thoái mái đi học cả ngày.', 259000, 'https://images.unsplash.com/photo-1503919005314-2a1ccc787d8c?w=800&q=80', 25, 0, 1, NOW(), 4),
('Giày Búp Bê Đính Nơ Óng Ánh', 'Xinh xắn như nàng công chúa.', 289000, 'https://images.unsplash.com/photo-1519457431-7571f0181146?w=800&q=80', 20, 1, 0, NOW(), 4),
('Giày Slip-on Họa Tiết Ngôi Sao', 'Dễ xỏ, phong cách hiện đại.', 229000, 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80', 30, 0, 0, NOW(), 4),
('Ủng Đi Mưa Màu Vàng Rực Rỡ', 'Chống thấm nước tuyệt đối.', 199000, 'https://images.unsplash.com/photo-1622273510268-aa18486413f3?w=800&q=80', 15, 1, 1, NOW(), 4),

-- Phụ Kiện (Category 5)
('Mũ Cói Đi Biển Thắt Nơ', 'Bảo vệ bé khỏi nắng hè.', 129000, 'https://images.unsplash.com/photo-1521369909029-2afed882baee?w=800&q=80', 40, 1, 0, NOW(), 5),
('Túi Xách Mini Silicon', 'Dễ thương, không bám bẩn.', 89000, 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80', 50, 1, 1, NOW(), 5),
('Set 10 Kẹp Tóc Nhiều Màu', 'Phụ kiện không thể thiếu cho bé gái.', 59000, 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=800&q=80', 100, 0, 1, NOW(), 5),
('Kính Râm Chống Tia UV', 'Gọng dẻo bền bỉ, an toàn cho mắt.', 149000, 'https://images.unsplash.com/photo-1503919203300-8485124b61ef?w=800&q=80', 35, 1, 1, NOW(), 5),
('Balô Nhỏ Tai Gấu', 'Người bạn đồng hành đến trường mẫu giáo.', 249000, 'https://images.unsplash.com/photo-1549439602-43ebca2327af?w=800&q=80', 20, 0, 0, NOW(), 5);
