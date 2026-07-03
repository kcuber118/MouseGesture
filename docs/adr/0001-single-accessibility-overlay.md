# 0001 — Dùng một overlay TYPE_ACCESSIBILITY_OVERLAY xuyên thấu

**Bối cảnh:** Cần vẽ vừa vùng Touchpad (nhận chạm, thay đổi kích thước/vị trí được) vừa Cursor (chạy toàn màn hình), đè lên các app khác mà vẫn để chạm xuyên qua xuống app bên dưới ở mọi nơi ngoài Touchpad.

**Quyết định:** Một cửa sổ duy nhất `TYPE_ACCESSIBILITY_OVERLAY` full-screen. View gốc không-clickable (chạm đi xuyên xuống app dưới); view con Touchpad clickable (bắt chạm trong vùng Touchpad). Cursor là một element vẽ tĩnh trên cùng Overlay đó, tại tọa độ tuyệt đối khớp điểm sẽ Inject.

**Lý do:** Một nguồn sự thật, không phải đồng bộ hai cửa sổ/lifecycle WindowManager. Quan trọng: `TYPE_ACCESSIBILITY_OVERLAY` là loại cửa sổ tin cậy của dịch vụ trợ năng — là loại duy nhất trên Android 12+ (API 31+, khớp targetSdk 36) vẫn được phép đè và truyền chạm xuống app bên dưới; `SYSTEM_ALERT_WINDOW` thường sẽ bị chặn "untrusted touch". Chính dịch vụ trợ năng này cũng cấp `dispatchGesture` để Inject.

**Phương án đã xét — Hai overlay tách biệt** (cửa sổ Touchpad nhỏ nhận chạm + cửa sổ Cursor `FLAG_NOT_TOUCHABLE` full-screen riêng): loại vì phải đồng bộ hai cửa sổ, quản lý hai lifecycle, thêm phức tạp không đổi được giá trị cốt lõi.

**Hệ quả:** Phải triển khai AccessibilityService (người dùng bật thủ công trong Cài đặt). Di chuyển Cursor là cập nhật trạng thái vẽ trên cùng Overlay với Touchpad.
