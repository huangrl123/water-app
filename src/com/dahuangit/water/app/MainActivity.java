package com.dahuangit.water.app;

import java.util.Properties;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);

		Properties prop = new Properties();

		try {
			prop.load(getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 加载需要显示的网页
		webview.loadUrl(prop.getProperty("server.url"));
		// 设置Web视图
		webview.setWebViewClient(new HelloWebViewClient());
	}

	/**
	 * 设置回退
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// 不做任何动作
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			return false;
		}

		return false;
	}

	// Web视图
	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
