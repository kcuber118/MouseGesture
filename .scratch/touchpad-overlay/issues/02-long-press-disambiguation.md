# 02 — Inject long-press + phân biệt tap/press/move

Status: ready-for-agent

## What to build

Mở rộng input của Touchpad: ngoài tap, nhấn-giữ trong Touchpad Inject long-press (`dispatchGesture` với stroke giữ) tại Cursor. Phải phân biệt chắc chắn 3 ý định từ cử chỉ 1 ngón bằng ngưỡng thời gian và khoảng cách: down-up nhanh + ít dịch = tap; giữ quá ngưỡng thời gian + ít dịch = long-press; dịch quá ngưỡng khoảng cách = move Cursor (không Inject). Khi cử chỉ đã được phân loại là move thì khi nhả tay không được Inject thêm tap/long-press.

## Acceptance criteria

- [x] Nhấn-giữ trong Touchpad (vượt ngưỡng thời gian, dịch chuyển tối thiểu) Inject long-press tại Cursor, kích hoạt long-press ở app bên dưới (vd mở menu context của icon launcher). — Domain logic + wiring done; cần test thủ công trên device
- [x] Tap nhanh vẫn Inject tap; dịch chuyển quá ngưỡng khoảng cách chỉ được coi là move Cursor và KHÔNG Inject tap/long-press khi nhả. — JVM unit test pass
- [x] Rung nhẹ (jitter) dưới ngưỡng khoảng cách không bị phân loại nhầm thành move. — JVM unit test pass
- [x] Các ngưỡng là hằng số tập trung一处; có JVM unit test phủ máy trạng thái phân biệt (tap / long-press / move) cho các trường hợp đại diện. — TouchGestureClassifier.Companion constants + 13 tests pass

## Blocked by

- #01
