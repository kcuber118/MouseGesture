# 03 — Edit mode + Grip (dời & resize Touchpad)

Status: done

## What to build

Giới thiệu Active mode và Edit mode cho Touchpad, chuyển bằng Grip (theo CONTEXT.md). Grip luôn được vẽ dính theo Touchpad (hiện ở cả hai mode). Tap Grip = chuyển Active↔Edit. Trong Active, thân Touchpad bắt input như #01/#02, Grip không nhận input. Trong Edit, Touchpad không bắt input; kéo Grip dời hình tứ giác (kẹp trong biên màn hình), kéo chấm góc resize (có min/max size hợp lý). Trạng thái mode và rect hiện tại phản ánh ngay trên Overlay.

## Acceptance criteria

- [ ] Grip luôn hiện, dính theo Touchpad ở cả Active và Edit. — Wiring done; cần test thủ công trên device
- [ ] Tap Grip chuyển Active↔Edit; mode hiện tại phân biệt được trực quan. — Domain test pass + wiring done; cần test thủ công
- [ ] Trong Edit: kéo Grip dời Touchpad; kéo chấm góc resize; Touchpad nằm trong biên màn hình và tuân min/max size. — Domain test pass (moveBy + resizeBy) + wiring done; cần test thủ công
- [ ] Trong Active: thân Touchpad chỉ làm input (#01/#02) — vuốt/tap không vô tình dời/resize. — Domain test pass (mode-gated behavior) + wiring done; cần test thủ công
- [ ] Tương tác trong Edit (dời/resize) không bao giờ Inject cử chỉ xuống app bên dưới. — Wiring ensures this (callbacks guarded by mode check); cần test thủ công

## Blocked by

- #01
