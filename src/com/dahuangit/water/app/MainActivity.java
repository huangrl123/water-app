package com.dahuangit.water.app;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.mapping.Mapping;
import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dahuangit.water.app.dao.UserDao;
import com.dahuangit.water.app.util.DialogUtils;
import com.dahuangit.water.app.util.HttpUtils;
import com.dahuangit.water.app.util.Response;
import com.dahuangit.water.app.util.XmlUtils;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Button loginBtn = null;

	private EditText userIdEditText = null;

	private EditText passwordEditText = null;

	private ImageView selectUserImageView = null;

	private UserDao userDao = null;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			msg.getData();
			switch (what) {
			case 1:// 成功
				String userId = (String) msg.getData().get("userId");
				String password = (String) msg.getData().get("password");
				userDao.addUser(userId, password);

				finish();
				Intent in = new Intent(getApplicationContext(), WebviewActivity.class);
				in.putExtra("userId", InitConfig.userId);
				startActivity(in);
				break;
			case 2:// 失败
				Response r = (Response) msg.getData().get("response");
				Builder b = DialogUtils.createAlertDialog(MainActivity.this, r.getMsg());
				b.show();
				break;
			}

			Resources resources = MainActivity.this.getResources();
			Drawable btnDrawable = resources.getDrawable(R.drawable.login_btn);
			loginBtn.setBackground(btnDrawable);
			loginBtn.setTextColor(Color.parseColor("#38CBF7"));
			loginBtn.setText("登录");
			loginBtn.setEnabled(true);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 加载配置文件
		Properties prop = new Properties();
		try {
			prop.load(getAssets().open("config.properties"));
			InitConfig.prop = prop;
		} catch (Exception e) {
			e.printStackTrace();
		}

		Mapping mapping = new Mapping();
		InputStream in = getResources().openRawResource(R.raw.castor_mapping);
		InputSource is = new InputSource(in);
		mapping.loadMapping(is);
		InitConfig.mapping = mapping;

		userIdEditText = (EditText) findViewById(R.id.accountEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		selectUserImageView = (ImageView) findViewById(R.id.selectUserImg);

		userDao = new UserDao(MainActivity.this);

		// 登录按钮处理
		loginBtn = (Button) findViewById(R.id.login_btn);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String userId = userIdEditText.getText().toString();
				if (null == userId || "".equals(userId)) {
					Toast.makeText(MainActivity.this, "账号不为空", Toast.LENGTH_SHORT).show();
					return;
				}

				final String password = passwordEditText.getText().toString();
				if (null == password || "".equals(password)) {
					Toast.makeText(MainActivity.this, "密码不为空", Toast.LENGTH_SHORT).show();
					return;
				}

				Resources resources = MainActivity.this.getResources();
				Drawable btnDrawable = resources.getDrawable(R.drawable.login_ing_btn);
				loginBtn.setBackground(btnDrawable);
				loginBtn.setText("正在登录...");
				loginBtn.setTextColor(Color.parseColor("#4092CC"));
				loginBtn.setEnabled(false);

				new Thread() {
					public void run() {
						Message msg = new Message();

						try {
							InitConfig.userId = userId;
							String host = InitConfig.prop.getProperty("server.login.url");
							Map<String, String> params = new HashMap<String, String>();
							params.put("userId", userId);
							params.put("password", password);

							String xml = HttpUtils.getHttpRequestContent(host, params);
							Response response = XmlUtils.xml2obj(InitConfig.mapping, xml, Response.class);

							msg.what = 1;
							Bundle data = new Bundle();
							data.putSerializable("response", response);
							data.putString("userId", userId);
							data.putString("password", password);
							msg.setData(data);
						} catch (Exception e) {
							msg.what = 2;
							Bundle data = new Bundle();
							Response response = new Response();
							response.setSuccess(false);
							response.setMsg(e.getMessage());
							data.putSerializable("response", response);
							msg.setData(data);
							e.printStackTrace();
						}

						handler.sendMessage(msg);
					}
				}.start();
			}
		});

		// 切换账号按钮处理
		selectUserImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final List<String> list = userDao.getAllUserId();
				if (list.isEmpty()) {
					return;
				}

				Builder historyUserWin = new AlertDialog.Builder(MainActivity.this);
				String[] historUserArr = list.toArray(new String[list.size()]);
				historyUserWin.setItems(historUserArr, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						String userId = list.get(which);
						String password = userDao.getPasswordByUserId(userId);
						userIdEditText.setText(userId);
						passwordEditText.setText(password);
						dialog.dismiss();
					}
				});

				historyUserWin.show();
			}
		});
	}

	/**
	 * 设置回退
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
			return true;
		}

		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
