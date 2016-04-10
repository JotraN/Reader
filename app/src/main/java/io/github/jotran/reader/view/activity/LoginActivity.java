package io.github.jotran.reader.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.github.jotran.reader.R;
import io.github.jotran.reader.model.JrawReaderHelper;

public class LoginActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;
    public static final String LOGIN_URL = "LOGIN_URL";
    public static final String RETURN_URL = "RETURN_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final Intent intent = getIntent();
        String authUrl = intent.getStringExtra(LoginActivity.LOGIN_URL);

        WebView webView = (WebView) findViewById(R.id.webview);
        // TODO For some reason ide is showing a warning when this is not here.
        if(webView == null) return;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals(Uri
                        .parse(JrawReaderHelper.REDIRECT_URL).getHost())) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(LoginActivity.RETURN_URL, url);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        clearCookies();
        webView.loadUrl(authUrl);
    }

    private void clearCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else cookieManager.removeAllCookie();
    }
}
