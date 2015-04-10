package com.dahuangit.water.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebviewActivity extends Activity {
	private static final String TAG = WebviewActivity.class.getSimpleName();

	private WebView webview;

	private Handler handler = new Handler();

	private ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);

		// 设置Web视图
		webview.setWebViewClient(new HelloWebViewClient());

		webview.addJavascriptInterface(new Object() {

			@JavascriptInterface
			public void exitSys() {
				android.app.AlertDialog.Builder b = new AlertDialog.Builder(WebviewActivity.this);
				b.setTitle("提示");
				b.setMessage("确认退出?");

				b.setPositiveButton("是", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
						Intent in = new Intent(getApplicationContext(), MainActivity.class);
						in.putExtra("isFromWeb", "true");
						startActivity(in);
					}
				});

				b.setNegativeButton("否", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				b.show();
			}
		}, "app");

		progressDialog = ProgressDialog.show(WebviewActivity.this, null, "加载中...", true);

		// 加载需要显示的网页
		webview.loadUrl(InitConfig.prop.getProperty("server.index.url") + "?systemId=" + InitConfig.systemId);
	}

	/**
	 * 设置回退
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// 不做任何动作
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			String title = webview.getTitle();
			if ("功能列表".equals(title)) {
				android.app.AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setTitle("提示");
				b.setMessage("确认关闭软件?");

				b.setPositiveButton("是", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

				b.setNegativeButton("否", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				b.show();
			} else {
				webview.goBack();
			}

			return true;
		}

		return false;
	}

	public void exit() {

	}

	// Web视图
	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			progressDialog.dismiss();
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			progressDialog.dismiss();
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
