package com.dahuangit.water.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webview = (WebView) findViewById(R.id.webview);
		// 设置WebView属性，能够执行Javascript脚本
		webview.getSettings().setJavaScriptEnabled(true);
		// 加载需要显示的网页
		webview.loadUrl("http://192.168.1.102:8080/water/spring/mobile/index");
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
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
