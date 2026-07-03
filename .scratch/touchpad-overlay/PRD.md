# PRD — Touchpad Overlay (MouseGesture)

## Mục tiêu

App trợ năng overlay một **Touchpad** lên màn hình. Người dùng vuốt/chạm trong Touchpad để điều khiển một **Cursor** chạy toàn màn hình; cử chỉ được **Inject** tại tọa độ Cursor qua `AccessibilityService.dispatchGesture`. Mục đích: hỗ trợ điện thoại có **Dead zone** (vùng cảm ứng liệt do hỏng phần cứng) và điều khiển một tay.

## Người dùng

- Người có màn hình liệt cảm ứng một phần (đặc biệt vùng trên gần status bar).
- Người muốn điều khiển toàn màn hình bằng một tay.

## Tham chiếu

- Glossary / ngôn ngữ dùng chung: [`CONTEXT.md`](../../CONTEXT.md)
- ADR-0001 — Một overlay `TYPE_ACCESSIBILITY_OVERLAY` xuyên thấu: [`docs/adr/0001-single-accessibility-overlay.md`](../../docs/adr/0001-single-accessibility-overlay.md)
- ADR-0002 — Bật/tắt overlay không phụ thuộc thanh trạng thái: [`docs/adr/0002-control-surface-without-status-bar.md`](../../docs/adr/0002-control-surface-without-status-bar.md)

## Phạm vi MVP

**Trong MVP:** Overlay 1 cửa sổ xuyên thấu · 3 cử chỉ (vuốt/tap/long-press) · Edit mode qua Grip (dời + resize) · tăng tốc + độ nhạy + kẹp biên · bật/tắt không dùng thanh trạng thái · lưu cấu hình · onboarding cấp quyền Trợ năng.

**Để sau:** kéo-thả (drag) · cuộn 2 ngón (swipe inject) · Quick Settings tile · chuột/bàn phím vật lý.

## Cách slicing

5 vertical slice (tracer bullet). Slice 01 là viên đạn xuyên thấu (xấu nhưng end-to-end), các slice sau tôi luyện từng phần.

| # | Issue | Blocked by |
|---|---|---|
| 01 | Tracer bullet: Touchpad → Cursor → Inject tap | — |
| 02 | Inject long-press + phân biệt tap/press/move | 01 |
| 03 | Edit mode + Grip (dời & resize) | 01 |
| 04 | Cảm giác di chuyển (tăng tốc + độ nhạy) + lưu cấu hình | 03 |
| 05 | Bật/tắt overlay + onboarding đầy đủ | 03 |

Đồ thị: `01 → {02, 03}`; `03 → {04, 05}`.
