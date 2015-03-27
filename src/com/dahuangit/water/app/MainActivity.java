package com.dahuangit.water.app;

import java.io.InputStream;
import java.util.Date;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dahuangit.water.app.dao.UserDao;
import com.dahuangit.water.app.util.DateUtils;
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

	private CheckBox rememberCheckBox = null;

	private CheckBox autoLoginCheckBox = null;

	private UserDao userDao = null;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			msg.getData();
			switch (what) {
			case 1:// 成功
					// 选择记住密码的才保存
				if (rememberCheckBox.isChecked()) {
					User u = new User();
					String userId = (String) msg.getData().get("userId");
					String password = (String) msg.getData().get("password");
					u.setUser_id(userId);
					u.setPassword(password);
					boolean isAutoLogin = autoLoginCheckBox.isChecked();
					if (isAutoLogin) {
						u.setIs_auto_login("1");
					} else {
						u.setIs_auto_login("0");
					}

					u.setLast_login_time(DateUtils.format(new Date()));
					userDao.addUser(u);
				}

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
		rememberCheckBox = (CheckBox) findViewById(R.id.rememberCheckBox);
		autoLoginCheckBox = (CheckBox) findViewById(R.id.autoLoginCheckBox);

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
						User u = userDao.getUserByUserId(userId);
						userIdEditText.setText(userId);
						passwordEditText.setText(u.getPassword());

						String isAutoLogin = u.getIs_auto_login();
						if ("0".equals(isAutoLogin)) {
							autoLoginCheckBox.setChecked(false);
						} else {
							autoLoginCheckBox.setChecked(true);
						}
						dialog.dismiss();
					}
				});

				historyUserWin.show();
			}
		});

		// 记住密码框事件
		rememberCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				// 点击取消，会删除数据库中的该记录
				String userId = userIdEditText.getText().toString();

				if (!checked) {
					autoLoginCheckBox.setChecked(false);
					if (!"".equals(userId) && null != userId) {
						userDao.deleteUser(userId);
					}
				}
			}
		});

		// 自动登录框事件
		autoLoginCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				if (checked) {
					rememberCheckBox.setChecked(true);
				}
			}
		});

		// 启动界面的时候
		Intent intent = getIntent();
		if (null == intent) {
			return;
		}

		List<String> userIdList = this.userDao.getAllUserId();
		if (!userIdList.isEmpty()) {
			this.rememberCheckBox.setChecked(true);
			String userId = userIdList.get(0);
			User u = userDao.getUserByUserId(userId);
			userIdEditText.setText(userId);
			passwordEditText.setText(u.getPassword());

			String isAutoLogin = u.getIs_auto_login();
			if ("0".equals(isAutoLogin)) {
				autoLoginCheckBox.setChecked(false);
			} else {
				autoLoginCheckBox.setChecked(true);

				String isFromWeb = intent.getStringExtra("isFromWeb");
				if (null == isFromWeb) {
					this.loginBtn.performClick();
				}
			}
		}

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
