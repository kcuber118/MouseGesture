# MouseGesture

App trợ năng overlay một Touchpad lên màn hình; người dùng vuốt/chạm trong Touchpad để điều khiển một Cursor chạy toàn màn hình (đặc biệt bù đắp cho màn hình có vùng cảm ứng liệt do hỏng phần cứng, và hỗ trợ điều khiển một tay). Cử chỉ được Inject tại tọa độ Cursor thông qua `AccessibilityService.dispatchGesture`.

## Language

**Touchpad (Vùng chạm)**:
Hình tứ giác trên Overlay, có thể điều chỉnh kích thước và vị trí; là nơi duy nhất trên Overlay nhận thao tác vuốt/chạm của người dùng để điều khiển Cursor.
_Avoid_: vùng cảm ứng, pad, trackpad

**Cursor (Con trỏ)**:
Hình mũi tên/chấm được vẽ trên Overlay, di chuyển tương đối theo thao tác vuốt trên Touchpad. Tọa độ tuyệt đối của Cursor chính là nơi cử chỉ được Inject xuống màn hình.
_Avoid_: chuột, pointer, nhãn

**Dead zone (Vùng liệt)**:
Vùng vật lý trên màn hình không còn đăng ký cảm ứng do hỏng phần cứng. App đặt Touchpad ở vùng còn hoạt động và Inject các cử chỉ vào (kể cả) Dead zone để người dùng vẫn điều khiển được.
_Avoid_: vùng chết, dead pixel, vùng hỏng

**Inject (Tiêm cử chỉ)**:
Tổng hợp một cử chỉ (tap/long-press/swipe…) tại tọa độ tuyệt đối của Cursor bằng `AccessibilityService.dispatchGesture`, thay vì chạm vật lý.
_Avoid_: mô phỏng chạm, simulate touch, giả lập

**Overlay**:
Cửa sổ `TYPE_ACCESSIBILITY_OVERLAY` full-screen duy nhất, vẽ cả Touchpad lẫn Cursor. Xuyên thấu cảm ứng ở mọi nơi trừ Touchpad (view gốc không-clickable truyền chạm xuống app bên dưới; view con Touchpad clickable bắt chạm).
_Avoid_: floating window, popup

## Trạng thái & điều khiển

**Active mode**:
Trạng thái Touchpad đang nhận input (vuốt/tap/long-press) để điều khiển Cursor.
_Avoid_: chế độ dùng, running mode

**Edit mode**:
Trạng thái đang chỉnh Touchpad (dời/đổi kích thước); không nhận input làm Cursor.
_Avoid_: chế độ chỉnh, config mode

**Grip (Quai cầm)**:
Element nhỏ luôn dính theo Touchpad kể cả trong Active mode. Tap Grip = chuyển Active↔Edit; trong Edit, kéo Grip để dời Touchpad (các chấm góc để đổi kích thước).
_Avoid_: nút, handle, tay nắm
