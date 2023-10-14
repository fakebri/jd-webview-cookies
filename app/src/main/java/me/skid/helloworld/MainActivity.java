package me.skid.helloworld;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Timer;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MyWebView";
    public static final String LOGIN_URL = "https://plogin.m.jd.com/login/login?appid=300&returnurl=https%3A%2F%2Fwq.jd.com%2Fpassport%2FLoginRedirect%3Fstate%3D3354011784%26returnurl%3Dhttps%253A%252F%252Fhome.m.jd.com%252FmyJd%252Fhome.action&source=wq_passport";
//    public static final String LOGIN_URL = "https://m.jd.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        LinearLayout wv_layout = findViewById(R.id.wv_layout);
        WebView webView = findViewById(R.id.wv_webview);
        Button resetButton = findViewById(R.id.resetUrl);
        Button tokenButton = findViewById(R.id.getToken);

        // 用硬编码动态设置 Webview 的 Layout 高度

        // 获取屏幕高度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDp = displayMetrics.heightPixels / displayMetrics.density;
        // 创建布局参数
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) ((screenDp - 75) * displayMetrics.density)
        );

        // 应用布局参数到LinearLayout
        wv_layout.setLayoutParams(layoutParams);


        resetButton.setOnClickListener(v -> {
            clearCache(webView); // 清理缓存
            webView.loadUrl(LOGIN_URL);
            Toast.makeText(MainActivity.this, "重置成功！", Toast.LENGTH_SHORT).show();
        });

        tokenButton.setOnClickListener(view -> {
            getToken(webView.getUrl());
        });


        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        clearCache(webView); // 清理缓存
        webView.loadUrl(LOGIN_URL);
        webView.setWebViewClient(new WebViewClient() {

            long lastTime = System.currentTimeMillis();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("https://plogin.m.jd.com/login/login")) {
                    long currentTimestamp = System.currentTimeMillis();
                    long elapsedTime = currentTimestamp - lastTime;

                    if (elapsedTime >= 2000) {
                        // 执行js自动切换密码登录和勾选用户协议
                        webView.evaluateJavascript("setTimeout(function(){var elem=document.querySelectorAll('span.J_ping:nth-child(1)')[0];if(elem.innerText==\"账号密码登录\"){elem.click()}document.querySelectorAll(\".policy_tip-checkbox\")[0].click()},1500);", null);
                        lastTime = currentTimestamp;
                    }
                } else {
                    getToken(url);
                }
                super.onPageFinished(view,url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message());
                return true;
            }
        });
        // 弹个窗，告诉用户怎么操作
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("操作指南")
                .setMessage("在浏览器窗口页面中进行登录（**建议使用密码登录**），登录完毕后提示并自动复制到粘贴板。如果需要重新登录，请点击重置按钮；如果登录了没有获取，请点击获取按钮。")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("知道了", (dialogInterface, i) -> {
                })
                .create();
        alertDialog.show();
    }

    void getToken(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager.hasCookies() && cookieManager.getCookie(url).contains("pt_key=")) {
            // https://home.m.jd.com/myJd/home.action
            String ck = cookieManager.getCookie(url);
            int beginIndex = ck.indexOf("pt_key");
            int endIndex = ck.indexOf("pt_token");
            String result = ck.substring(beginIndex, endIndex);
            Log.d(TAG, "Cookies: " + result);
            Toast.makeText(MainActivity.this, "获取成功, 已经复制到剪贴板!", Toast.LENGTH_SHORT).show();
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("text", result));
        }
    }

    void clearCache(WebView webView) {
        CookieManager.getInstance().removeAllCookies(null);
        webView.clearCache(true); // 清除缓存
        webView.clearFormData(); // 清除表单数据
        webView.clearHistory(); // 清除浏览历史

        WebViewDatabase.getInstance(this).clearUsernamePassword(); // 清除用户名密码
        WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword(); // 清除HTTP认证用户名密码
        WebViewDatabase.getInstance(this).clearFormData(); // 清除表单数据
        CookieManager.getInstance().setAcceptCookie(true);
    }

}