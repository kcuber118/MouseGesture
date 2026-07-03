# 04 — Cảm giác di chuyển (tăng tốc + độ nhạy) + lưu cấu hình

Status: ready-for-agent

## What to build

Thay movment tuyến tính ở #01 bằng đường cong tăng tốc: vuốt chậm → Cursor đi ít (chuẩn xác), vuốt nhanh/lớn → boost (xa). Thêm điều khiển độ nhạy cho người dùng. Lưu Rect của Touchpad, vị trí Cursor và độ nhạy bằng DataStore Preferences để khôi phục qua các lần khởi động lại service. Rect đã lưu được khôi phục khi Overlay attach; độ nhạy ảnh hưởng đường cong tăng tốc ngay tức thì (live).

## Acceptance criteria

- [ ] Di chuyển Cursor dùng đường cong tăng tốc: vuốt chậm cho dịch Cursor tỉ lệ nhỏ hơn vuốt nhanh cùng khoảng cách.
- [ ] Có điều khiển độ nhạy (vd slider trong MainActivity) thay đổi cảm giác di chuyển và được áp dụng live.
- [ ] Rect Touchpad, vị trí Cursor, độ nhạy được lưu qua restart service/app và khôi phục khi Overlay attach.
- [ ] Đổi độ nhạy không làm Cursor nhảy discontinuous.

## Blocked by

- #03
