package com.mcyoutube.overlay;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WindowManager windowManager;
    private FrameLayout overlay;
    private WindowManager.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.BLACK);

        WebView webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.youtube.com");

        overlay.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView close = new TextView(this);
        close.setText("×");
        close.setTextColor(Color.WHITE);
        close.setTextSize(28);
        close.setGravity(Gravity.CENTER);
        close.setBackgroundColor(0x99000000);
        close.setPadding(15, 0, 15, 0);

        FrameLayout.LayoutParams closeParams =
            new FrameLayout.LayoutParams(70, 70, Gravity.TOP | Gravity.END);

        overlay.addView(close, closeParams);

        close.setOnClickListener(v -> {
            if (overlay != null) {
                windowManager.removeView(overlay);
                overlay = null;
            }
            finish();
        });

        params = new WindowManager.LayoutParams(
            700,
            450,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 150;

        // جابه‌جایی پنجره با لمس و کشیدن
        View.OnTouchListener dragListener = new View.OnTouchListener() {

            private int startX;
            private int startY;
            private float touchX;
            private float touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = startX +
                            (int)(event.getRawX() - touchX);

                        params.y = startY +
                            (int)(event.getRawY() - touchY);

                        windowManager.updateViewLayout(
                            overlay,
                            params
                        );

                        return true;
                }

                return false;
            }
        };

        overlay.setOnTouchListener(dragListener);

        windowManager.addView(overlay, params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (overlay != null && windowManager != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }

            overlay = null;
        }
    }
}
