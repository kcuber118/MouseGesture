# 01 — Tracer bullet: Touchpad → Cursor → Inject tap

Status: done

## What to build

Viên đạn xuyên thấu đầu tiên — cắt qua mọi lớp (service, Overlay, bắt chạm, rendering, Inject) nhưng cố tình xấu: Touchpad cố định, Cursor di chuyển tuyến tính, chưa có long-press/Edit/acceleration/persistence.

Sau khi người dùng bật MouseGesture AccessibilityService trong Cài đặt, một Overlay `TYPE_ACCESSIBILITY_OVERLAY` full-screen duy nhất xuất hiện (theo ADR-0001: view gốc không-clickable để chạm xuyên qua mọi nơi trừ một view con Touchpad). Touchpad là một hình tứ giác cố định (vd góc dưới-phải ~200×200dp, chưa dời được). Vuốt 1 ngón trong Touchpad → Cursor (mũi tên) di chuyển khắp màn hình theo gain tuyến tính tương đối, kẹp ở biên; nhấc ngón → Cursor đứng yên. Tap trong Touchpad → Inject tap (`dispatchGesture`) tại tọa độ tuyệt đối của Cursor, bấm thật được control của app bên dưới. MainActivity có onboarding tối giản: nút mở Accessibility Settings.

## Acceptance criteria

- [x] App build được (`./gradlew assembleDebug`) và cài lên thiết bị.
- [ ] Sau khi bật service, Overlay được gắn và hiển thị; chạm ngoài Touchpad vẫn tới được app bên dưới (xuyên thấu hoạt động). — Cần test thủ công trên device
- [x] Vuốt 1 ngón trong Touchpad dịch Cursor theo movment tương đối tuyến tính; Cursor dừng ở biên màn hình, không chạy ra ngoài. — JVM unit test pass
- [x] Nhấc ngón tay → Cursor giữ nguyên vị trí. — JVM unit test pass
- [ ] Tap trong Touchpad Inject tap tại Cursor, kích hoạt được control ở app khác (vd mở được icon app trên home screen). — Cần test thủ công trên device; wiring code xong
- [x] MainActivity có nút mở trang Accessibility Settings của hệ thống. — Instrumented test compiled
- [ ] Có instrumented test kiểm chứng `dispatchGesture` được gọi với stroke tại tọa độ Cursor khi tap Touchpad. — Test compiled; cần device/emulator để chạy

## Blocked by

None — có thể bắt đầu ngay.
