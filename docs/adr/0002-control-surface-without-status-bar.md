# 0002 — Điều khiển bật/tắt overlay không phụ thuộc thanh trạng thái

**Bối cảnh:** App phục vụ đúng những người dùng có vùng cảm ứng liệt — vùng đó thường nằm ở phần trên màn hình (gần status bar). Các cơ chế Android quen thuộc để bật/tắt một overlay — hành động trong thông báo thường trực, hoặc Quick Settings tile — đều yêu cầu **kéo thanh trạng thái từ trên cùng xuống**, tức là chạm đúng vùng hay bị liệt nhất. Người dùng cốt lõi therefore không với tới được các cơ chế đó.

**Quyết định:** Bật/tắt overlay hoàn toàn không phụ thuộc thanh trạng thái. Ẩn = nhấn giữ Grip (hoặc nút × nhỏ trên Grip) trong Active mode. Hiện lại = mở app qua biểu tượng launcher → nút "Bật overlay" trong `MainActivity`. Thông báo thường trực (nếu có) chỉ là fallback tùy chọn, không phải cơ chế chính.

**Lý do:** Grip luôn được vẽ khi overlay đang Active nên luôn trong tầm với của vùng màn hình còn hoạt động; mở app qua launcher icon không cần vùng trên cùng. Điều này đảm bảo người dùng vùng liệt trên vẫn tự bật/tắt được.

**Phương án đã xét:** (a) Thông báo thường trực có nút toggle, (b) Quick Settings tile — đều loại vì phụ thuộc thanh trạng thái, không với được khi vùng trên liệt.

**Hệ quả:** Khi overlay đang ẩn, không có entry từ system UI để hiện lại — phải mở app qua launcher. Đánh đổi chấp nhận được vì việc ẩn hiếm khi cần gấp. MVP cũng có thể bỏ luôn tính năng ẩn (overlay luôn hiện khi dịch vụ đang bật) để tinh gọn hơn nếu muốn.
