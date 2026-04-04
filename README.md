# 🌟 Kids Fashion Shop - Hệ thống TMĐT Thời trang Trẻ em Cao cấp

Chào mừng bạn đến với **Kids Fashion Shop**, một nền tảng thương mại điện tử được thiết kế với phong cách **Minimalist High-Fashion (Luxury)**. Dự án được tối ưu hóa toàn diện về cả thẩm mỹ giao diện và luồng nghiệp vụ thanh toán tự động, mang đến trải nghiệm mua sắm đẳng cấp và hiện đại nhất.

---

## ✨ Tính năng Nổi bật

### 🛍️ Đối với Khách hàng (Storefront)
*   **Giao diện Luxury:** Thiết kế tối giản nhưng sang trọng, hỗ trợ đầy đủ chế độ **Dark Mode** với độ tương phản hoàn hảo.
*   **Thanh toán VietQR Tự động (SePay):** Quy trình quét mã QR cực nhanh, hệ thống tự động xác nhận đơn hàng sau vài giây ngay khi khách chuyển khoản thành công.
*   **Điểm Sản phẩm HOT (Hot Score):** Hệ thống tự động xếp hạng sản phẩm nổi bật dựa trên số lượt mua và đánh giá thực tế.
*   **Tìm kiếm & Lọc thông minh:** Tìm sản phẩm theo tên, danh mục và sắp xếp theo độ HOT.
*   **Giỏ hàng linh hoạt:** Hỗ trợ tính năng "Mua ngay" hoặc thanh toán nhiều sản phẩm cùng lúc.

### 🛡️ Đối với Quản trị viên (Admin Dashboard)
*   **Quản lý Sản phẩm:** Giao diện thêm/sửa/xóa sản phẩm trực quan, hỗ trợ quản lý kho hàng và thuộc tính (Size, Màu sắc).
*   **Quản lý Đơn hàng:** Theo dõi trạng thái đơn hàng theo thời gian thực (Chờ thanh toán, Chờ xác nhận, Đã thanh toán...).
*   **Thống kê & Hiệu quả:** Tự động tính toán điểm sản phẩm HOT để tối ưu hóa việc trưng bày.

---

## 🛠️ Công nghệ Sử dụng
*   **Backend:** Java 17, Spring Boot 3, Spring Data JPA.
*   **Frontend:** Thymeleaf, Vanilla CSS (Premium Design), JavaScript.
*   **Cơ sở dữ liệu:** MySQL v8.0.
*   **Tích hợp:** SePay Webhook (Thanh toán tự động qua VietQR).
*   **Bảo mật:** Spring Security, BCrypt, Environment Variables.

---

## 🚀 Hướng dẫn Triển khai (Deploy trên Render)

Dự án đã được cấu hình sẵn để triển khai mượt mà trên Render. Bạn cần cài đặt các **Environment Variables** sau:

### 💾 Kết nối Cơ sở dữ liệu
*   `DB_URL`: Link kết nối MySQL (ví dụ: `jdbc:mysql://host:port/db_name`).
*   `DB_USERNAME`: Tên đăng nhập database.
*   `DB_PASSWORD`: Mật khẩu database.

### 💳 Cấu hình Thanh toán SePay
*   `SEPAY_WEBHOOK_TOKEN`: Mã bí mật bạn đặt trên SePay để bảo mật Webhook. (Phải khớp 100% với mã trên SePay).
*   `SEPAY_BANK_ID`: Tên định danh ngân hàng (ví dụ: `MBBank`).
*   `SEPAY_ACC_NUMBER`: Số tài khoản nhận tiền thật.
*   `SEPAY_ACC_NAME`: Tên chủ tài khoản ngân hàng của bạn (VIẾT HOA KHÔNG DẤU).

---

## 💻 Cài đặt Local (Dành cho Lập trình viên)

1.  **Clone dự án:**
    ```bash
    git clone https://github.com/your-username/kids-fashion-shop.git
    ```
2.  **Cấu hình Database:**
    Tạo một Database MySQL và cập nhật thông tin trong tệp `application.properties` (hoặc dùng biến môi trường địa phương).
3.  **Chạy dự án:**
    Sử dụng Maven để build và chạy:
    ```bash
    mvn clean spring-boot:run
    ```
4.  **Truy cập:**
    *   Trang chủ: `http://localhost:8080/`
    *   Trang Admin: `http://localhost:8080/admin` (Mặc định cần tài khoản Role ADMIN).

---

## 📝 Lưu ý Vận hành Thanh toán (SePay)
Để luồng thanh toán tự động hoạt động, bạn cần cấu hình Webhook trên trang quản trị SePay như sau:
*   **URL:** `https://tên-miền-của-bạn.onrender.com/api/sepay/webhook`
*   **Sự kiện:** `Có tiền vào`.
*   **Xác thực thanh toán:** Chọn **ĐÚNG**.
*   **Kiểu chứng thực:** `API Key (X-SePay-Token)` và điền Token trùng với `SEPAY_WEBHOOK_TOKEN` bạn đã cài trên Render.

---


