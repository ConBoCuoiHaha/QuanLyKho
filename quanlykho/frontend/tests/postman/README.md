# Bộ test Postman

## Chuẩn bị
1. Khởi động backend (`quanlykho/backend`) để lắng nghe ở `http://localhost:5050/api` (hoặc sửa `base_url` trong environment).
2. Trong Postman chọn **Import** và nạp hai file:
   - `ims-api.postman_collection.json`
   - `ims-api-env.postman_environment.json`
3. Chọn environment **IMS Local**, chỉnh lại các biến nếu cần:
   - `base_url`: đường dẫn API thực tế.
   - Thông tin người đăng nhập (`admin_email`, `admin_password`) – mặc định trùng tài khoản seed (`admin@example.com` / `12345678`).
   - Các biến đầu vào khác (`register_*`, `supplier_*`, `customer_*`, …) nếu muốn dữ liệu khác.

## Cách chạy
1. Mở Collection Runner, chọn collection **IMS API Full Suite** và environment **IMS Local**.
2. Nhấn **Run**. Bộ test sẽ tự:
   - Đăng ký một user mới, đăng nhập bằng admin mặc định và lưu `token`.
   - Lấy danh sách người dùng để tự động gán `managed_user_id` cho các bước GET/PUT/DELETE.
   - Tạo đơn đặt hàng / đơn bán hàng mẫu, sau đó lưu `purchase_order_id`, `sales_order_id`, chi tiết đơn để dùng cho các bước cập nhật trạng thái & nhận hàng.
   - Gọi toàn bộ endpoint đọc dữ liệu (báo cáo, giao dịch, kho, …).

Các request yêu cầu dữ liệu thủ công (OTP thực tế, upload file, quyền đặc biệt) đã được đánh dấu `disabled` nên Runner sẽ bỏ qua:

| Nhóm | Request bị bỏ qua | Lý do |
| --- | --- | --- |
| Auth | Đặt lại mật khẩu bằng OTP | OTP chỉ hiển thị trên console server, không thể lấy tự động |
| Backup | Danh sách / Xuất / Tải / Khôi phục | API dùng `@PreAuthorize('ADMIN')` thay vì quyền thao tác, token hiện tại không có authority này |
| Seed | `/seed/demo` | Cùng hạn chế bảo mật như backup |
| Sản phẩm | Thêm / Cập nhật (yêu cầu upload ảnh) | Runner không thể đính kèm file tự động |
| Xuất nhập file | Nhập Excel sản phẩm, khôi phục backup | Cần file local thực tế |
| Xóa dữ liệu gốc | Xóa sản phẩm, xóa khách hàng, xóa nhà cung cấp | Bị ràng buộc khóa ngoại trong database demo |

Nếu muốn chạy các request bị vô hiệu hóa, hãy:
1. Chuẩn bị dữ liệu cần thiết (OTP, file upload, quyền tài khoản).
2. Tick lại request tương ứng trong Postman.
3. Bổ sung giá trị biến môi trường (`products_excel_path`, `backup_file_path`, …).

## Một số lưu ý
- `managed_user_id` được set tự động sau bước “Danh sách người dùng”. Không cần chỉnh tay.
- `barcode_sku` mặc định là `SP-0001` (trùng dữ liệu seed). Nếu bạn tạo SKU khác, cập nhật biến này để các bước Barcode/PDF trả về 200.
- `purchase_status_update` (`CHO_DUYET`) và `sales_status_update` (`XAC_NHAN`) là các giá trị enum hợp lệ. Có thể thay đổi nếu muốn kiểm thử luồng khác.
- Nếu cần thay đổi số lượng nhận hàng, sửa `po_receive_quantity`, `lot_number`, `lot_expiry_date` – mọi trường đã được map vào body JSON.

Sau khi hoàn tất, bạn có thể export lại collection/environment nếu chạy ở môi trường khác.
