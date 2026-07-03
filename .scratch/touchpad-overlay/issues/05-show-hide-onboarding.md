# 05 — Bật/tắt overlay + onboarding đầy đủ (ADR-0002)

Status: ready-for-agent

## What to build

Hiện thực bề mặt điều khiển show/hide theo ADR-0002 — tránh thanh trạng thái (người Dead zone thường không với được vùng trên). Nhấn-giữ Grip ẩn Overlay (service vẫn bật nhưng không render/nhận gì cho đến khi hiện lại). Để hiện lại, người dùng mở app (launcher icon) và tap "Bật overlay" trong MainActivity → Overlay gắn lại ở trạng thái đã lưu (kèm #04). Mở rộng MainActivity thành onboarding đầy đủ: hiện service đang bật hay chưa + nút mở Accessibility Settings. Thông báo thường trực (nếu có) chỉ là fallback, KHÔNG là cơ chế chính.

Lưu ý tương tác: giữ-Grip (ẩn) khác với tap-Grip (chuyển mode ở #03) — phân biệt bằng ngưỡng thời gian trên cùng mục tiêu Grip.

## Acceptance criteria

- [ ] Nhấn-giữ Grip ẩn Overlay; Overlay không còn render/nhận chạm cho đến khi được hiện lại.
- [ ] Sau khi ẩn, mở app và tap "Bật overlay" gắn lại Overlay ở trạng thái lần cuối (với #04).
- [ ] Show/hide hoạt động mà không cần kéo thanh thông báo / thanh trạng thái.
- [ ] MainActivity hiện trực tiếp trạng thái service (bật/tắt) và có nút mở Accessibility Settings.
- [ ] (Tùy chọn) Thông báo thường trực chỉ là fallback, không phải đường show/hide chính.

## Blocked by

- #03
