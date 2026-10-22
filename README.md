# Bypass Link Tool — Android APK (dark hacker UI)

Ứng dụng Android native tối giản để:
- Nhập URL và phân tích chuyển hướng HTTP/HTTPS.
- Hiển thị từng bước redirect và URL cuối cùng nếu truy cập được.
- Nhận diện tên miền Ontop, Link4M, layma.net theo hostname.
- Không vượt CAPTCHA, đăng nhập, paywall hoặc cơ chế bảo vệ truy cập.

## Build APK trên Android bằng GitHub Actions

Cách này không cần cài Android Studio trên điện thoại.

1. Tạo repository GitHub mới, ví dụ `bypass-link-tool-android`.
2. Giải nén ZIP này trên điện thoại.
3. Tải toàn bộ nội dung thư mục `BypassLinkTool_Android` lên repository, giữ nguyên cấu trúc.
4. Mở tab **Actions** của repository và bật GitHub Actions nếu được yêu cầu.
5. Chạy workflow **Build Android APK** (Actions → Build Android APK → Run workflow).
6. Khi chạy xong, mở run mới nhất → **Artifacts** → tải `bypass-link-tool-debug-apk`.
7. Giải nén artifact và mở file `app-debug.apk`. Android có thể hỏi cho phép cài ứng dụng từ nguồn này; chỉ tiếp tục nếu bạn tin tưởng mã nguồn mình vừa tải.

## Lưu ý
- Đây là mã nguồn mẫu. Hãy thử với URL bạn có quyền truy cập.
- Một số máy chủ chặn truy vấn tự động hoặc yêu cầu JavaScript/cookies. Khi đó app sẽ báo lỗi hoặc dừng ở URL trung gian.
- Chỉ phân tích redirect HTTP tiêu chuẩn; không tự động giải CAPTCHA, đăng nhập hoặc né kiểm soát truy cập.
- Yêu cầu Android 7.0 trở lên (minSdk 24).
