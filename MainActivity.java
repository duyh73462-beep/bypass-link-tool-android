package com.example.bypasslinktool;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(7, 11, 16);
    private final int PANEL = Color.rgb(14, 22, 29);
    private final int GREEN = Color.rgb(0, 255, 156);
    private final int WHITE = Color.rgb(220, 235, 230);
    private final int MUTED = Color.rgb(125, 153, 151);
    private EditText urlInput;
    private TextView output;
    private Button analyzeButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(14));
        root.setBackgroundColor(BG);

        TextView eyebrow = new TextView(this);
        eyebrow.setText("● SYSTEM ONLINE  /  HTTP TRACE");
        eyebrow.setTextColor(GREEN);
        eyebrow.setTextSize(12);
        eyebrow.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        root.addView(eyebrow);

        TextView title = new TextView(this);
        title.setText("BYPASS LINK TOOL");
        title.setTextColor(WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(10);
        root.addView(title, titleLp);

        TextView subtitle = new TextView(this);
        subtitle.setText("Phân tích redirect HTTP/HTTPS • Không vượt CAPTCHA/login");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(12);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(5);
        root.addView(subtitle, subLp);

        urlInput = new EditText(this);
        urlInput.setSingleLine(false);
        urlInput.setMinLines(2);
        urlInput.setTextSize(14);
        urlInput.setTextColor(WHITE);
        urlInput.setHintTextColor(MUTED);
        urlInput.setHint("https://example.com/short-link");
        urlInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        urlInput.setTypeface(Typeface.MONOSPACE);
        urlInput.setBackground(roundRect(PANEL, GREEN, 1, 10));
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(-1, -2);
        inputLp.topMargin = dp(22);
        root.addView(urlInput, inputLp);

        analyzeButton = new Button(this);
        analyzeButton.setText("▶  ANALYZE LINK");
        analyzeButton.setTextColor(BG);
        analyzeButton.setTextSize(14);
        analyzeButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        analyzeButton.setAllCaps(false);
        analyzeButton.setBackground(roundRect(GREEN, GREEN, 1, 10));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, dp(50));
        btnLp.topMargin = dp(12);
        root.addView(analyzeButton, btnLp);

        TextView domains = new TextView(this);
        domains.setText("DETECT: ONTOP  •  LINK4M  •  LAYMA.NET");
        domains.setTextColor(GREEN);
        domains.setTextSize(11);
        domains.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams domainsLp = new LinearLayout.LayoutParams(-1, -2);
        domainsLp.topMargin = dp(16);
        root.addView(domains, domainsLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        output = new TextView(this);
        output.setText(">> Sẵn sàng.\n>> Dán URL rồi nhấn ANALYZE LINK.\n\nLưu ý: Máy chủ có thể chặn yêu cầu tự động hoặc cần JavaScript/cookies.");
        output.setTextColor(WHITE);
        output.setTextSize(13);
        output.setTypeface(Typeface.MONOSPACE);
        output.setPadding(dp(14), dp(14), dp(14), dp(14));
        output.setBackground(roundRect(PANEL, Color.rgb(25, 48, 47), 1, 10));
        scroll.addView(output, new ScrollView.LayoutParams(-1, -2));
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        scrollLp.topMargin = dp(14);
        root.addView(scroll, scrollLp);

        analyzeButton.setOnClickListener(v -> analyze());
        setContentView(root);
    }

    private void analyze() {
        String raw = urlInput.getText().toString().trim();
        if (raw.isEmpty()) {
            output.setText("!! Hãy nhập URL trước.");
            return;
        }
        if (!raw.matches("(?i)^https?://.*")) raw = "https://" + raw;
        final String startUrl = raw;
        try {
            URI uri = URI.create(startUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                output.setText("!! URL không hợp lệ. Hãy dùng http:// hoặc https://");
                return;
            }
        } catch (Exception e) {
            output.setText("!! URL không hợp lệ.");
            return;
        }
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
        analyzeButton.setEnabled(false);
        output.setText(">> Đang phân tích redirect...\n>> Vui lòng chờ.");
        new Thread(() -> {
            String result = trace(startUrl);
            runOnUiThread(() -> {
                output.setText(result);
                analyzeButton.setEnabled(true);
            });
        }).start();
    }

    private String trace(String start) {
        StringBuilder sb = new StringBuilder();
        List<String> visited = new ArrayList<>();
        String current = start;
        sb.append(">> HTTP TRACE START\n\n");
        try {
            for (int hop = 0; hop <= 10; hop++) {
                if (visited.contains(current)) {
                    sb.append("!! Phát hiện vòng lặp redirect.\n");
                    break;
                }
                visited.add(current);
                URL url = new URL(current);
                String host = url.getHost().toLowerCase(Locale.ROOT);
                if (!("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))) {
                    sb.append("!! Chỉ hỗ trợ HTTP/HTTPS.\n");
                    break;
                }

                sb.append("[").append(hop).append("] ").append(current).append("\n");
                sb.append("    Host: ").append(host).append("\n");
                sb.append("    Nhận diện: ").append(detect(host)).append("\n");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "BypassLinkTool/1.0 (HTTP redirect analyzer)");
                conn.setRequestProperty("Range", "bytes=0-0");
                int code = conn.getResponseCode();
                String location = conn.getHeaderField("Location");
                sb.append("    HTTP: ").append(code).append("\n");

                if (code >= 300 && code < 400 && location != null && !location.trim().isEmpty()) {
                    String next = new URL(url, location).toString();
                    sb.append("    Redirect → ").append(next).append("\n\n");
                    conn.disconnect();
                    current = next;
                    continue;
                }
                conn.disconnect();
                sb.append("\n>> URL CUỐI CÙNG (theo HTTP):\n").append(current).append("\n");
                if (code == 401 || code == 403 || code == 429) {
                    sb.append("\n!! Máy chủ yêu cầu quyền truy cập hoặc giới hạn yêu cầu. App không vượt qua cơ chế này.\n");
                } else if (code >= 200 && code < 300) {
                    sb.append("\n>> Không thấy redirect HTTP tiếp theo. Trang có thể dùng JavaScript hoặc cookies.\n");
                }
                return sb.toString();
            }
            if (visited.size() > 10) sb.append("!! Dừng sau tối đa 10 lần chuyển hướng.\n");
        } catch (Exception e) {
            sb.append("\n!! Không thể hoàn tất: ").append(e.getClass().getSimpleName())
              .append(" — ").append(e.getMessage() == null ? "lỗi mạng hoặc máy chủ từ chối" : e.getMessage()).append("\n");
            sb.append("Gợi ý: kiểm tra URL/kết nối; một số link cần JavaScript, cookies hoặc tương tác người dùng.\n");
        }
        return sb.toString();
    }

    private String detect(String host) {
        if (isDomain(host, "layma.net")) return "LAYMA.NET";
        if (isDomain(host, "link4m.com") || host.contains("link4m")) return "LINK4M (theo hostname)";
        if (host.contains("ontop")) return "ONTOP (theo hostname)";
        return "Chưa nhận diện / tên miền khác";
    }

    private boolean isDomain(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }

    private GradientDrawable roundRect(int color, int strokeColor, int strokeWidth, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(strokeWidth), strokeColor);
        return d;
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
}
