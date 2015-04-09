package com.dahuangit.water.app;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.mapping.Mapping;
import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.dahuangit.water.app.dao.UserDao;
import com.dahuangit.water.app.util.DateUtils;
import com.dahuangit.water.app.util.HttpUtils;
import com.dahuangit.water.app.util.Response;
import com.dahuangit.water.app.util.XmlUtils;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Button loginBtn = null;

	private EditText userIdEditText = null;

	private EditText passwordEditText = null;

	private CheckBox rememberCheckBox = null;

	private CheckBox autoLoginCheckBox = null;

	private UserDao userDao = null;

	// PopupWindow对象
	private PopupWindow selectPopupWindow = null;
	// 自定义Adapter
	private OptionsAdapter optionsAdapter = null;
	// 下拉框选项数据源
	private ArrayList<String> datas = new ArrayList<String>();;
	// 下拉框依附组件
	private LinearLayout parent;
	// 下拉框依附组件宽度，也将作为下拉框的宽度
	private int pwidth;
	// 下拉箭头图片组件
	private ImageView image;
	// 恢复数据源按钮
	private Button button;
	// 展示所有下拉选项的ListView
	private ListView listView = null;
	// 用来处理选中或者删除下拉项消息
	private Handler selectHistoryLoginInfoHander;
	// 是否初始化完成标志
	private boolean flag = false;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			Bundle data = msg.getData();
			switch (what) {
			case 1:// 成功
				Response response = (Response) msg.getData().get("response");
				if (!response.getSuccess()) {
					Toast.makeText(MainActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
					break;
				}

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
				Toast.makeText(MainActivity.this, "网络或者服务器异常", Toast.LENGTH_SHORT).show();
				break;
			case 3:// 选中下拉项，下拉框消失
				int selIndex = data.getInt("selIndex");
				String userId = datas.get(selIndex);
				userIdEditText.setText(userId);

				User u = userDao.getUserByUserId(userId);
				userIdEditText.setText(userId);
				passwordEditText.setText(u.getPassword());
				String isAutoLogin = u.getIs_auto_login();
				if ("0".equals(isAutoLogin)) {
					autoLoginCheckBox.setChecked(false);
				} else {
					autoLoginCheckBox.setChecked(true);
				}

				dismiss();
				break;
			case 4:// 移除下拉项数据
				int delIndex = data.getInt("delIndex");
				String deluserId = datas.get(delIndex);
				userDao.deleteUser(deluserId);
				datas.remove(delIndex);
				// 刷新下拉列表
				optionsAdapter.notifyDataSetChanged();
				break;
			}
			Drawable d = getResources().getDrawable(R.drawable.login_btn);
			loginBtn.setBackground(d);
			loginBtn.setText("登录");
			loginBtn.setEnabled(true);
		}
	};

	// 选择账号
	private OnTouchListener account_OnTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				int curX = (int) event.getX();
				if (curX > v.getWidth() - 150) {

					final List<String> list = userDao.getAllUserId();
					if (list.isEmpty()) {
						return false;
					}

					String[] historUserArr = list.toArray(new String[list.size()]);
					datas.clear();
					datas.addAll(Arrays.asList(historUserArr));

					if (flag) {
						// 显示PopupWindow窗口
						popupWindwShowing();
					}

					return false;
				}
				break;
			}
			return false;
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
		userIdEditText.setSingleLine();

		passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		passwordEditText.setSingleLine();
		passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

		rememberCheckBox = (CheckBox) findViewById(R.id.rememberCheckBox);
		autoLoginCheckBox = (CheckBox) findViewById(R.id.autoLoginCheckBox);

		userDao = new UserDao(MainActivity.this);

		// 登录按钮处理
		loginBtn = (Button) findViewById(R.id.login_btn);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String userId = userIdEditText.getText().toString().trim();
				if (null == userId || "".equals(userId)) {
					Toast.makeText(MainActivity.this, "账号不为空", Toast.LENGTH_SHORT).show();
					return;
				}

				final String password = passwordEditText.getText().toString().trim();
				if (null == password || "".equals(password)) {
					Toast.makeText(MainActivity.this, "密码不为空", Toast.LENGTH_SHORT).show();
					return;
				}

				Drawable d = getResources().getDrawable(R.drawable.login_ing);
				loginBtn.setBackground(d);
				loginBtn.setText("登录中...");
				loginBtn.setEnabled(false);

				new Thread() {
					public void run() {
						Message msg = new Message();

						try {
							InitConfig.userId = userId;
							String host = InitConfig.prop.getProperty("server.login.url");
							Map<String, String> params = new HashMap<String, String>();
							params.put("optNum", userId);
							params.put("password", password);

							String xml = HttpUtils.getHttpRequestContent(host, params);
							Response response = XmlUtils.xml2obj(InitConfig.mapping, xml, Response.class);

							InitConfig.systemId = response.getSystemId();

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

		// 记住密码框事件
		rememberCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				// 点击取消，会删除数据库中的该记录
				String userId = userIdEditText.getText().toString().trim();

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
	 * 没有在onCreate方法中调用initWedget()，而是在onWindowFocusChanged方法中调用，
	 * 是因为initWedget()中需要获取PopupWindow浮动下拉框依附的组件宽度，在onCreate方法中是无法获取到该宽度的
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		while (!flag) {
			initWedget();
			flag = true;
		}

	}

	/**
	 * 初始化界面控件
	 */
	private void initWedget() {
		// 初始化界面组件
		parent = (LinearLayout) findViewById(R.id.parent);

		// 获取下拉框依附的组件宽度
		int width = parent.getWidth() - 60;
		pwidth = width;

		userIdEditText.setOnTouchListener(account_OnTouch);

		// 初始化PopupWindow
		initPopuWindow();
	}

	/**
	 * 初始化PopupWindow
	 */
	private void initPopuWindow() {

		// PopupWindow浮动下拉框布局
		View loginwindow = (View) this.getLayoutInflater().inflate(R.layout.options, null);
		listView = (ListView) loginwindow.findViewById(R.id.list);

		// 设置自定义Adapter
		optionsAdapter = new OptionsAdapter(this, handler, datas);
		listView.setAdapter(optionsAdapter);
		selectPopupWindow = new PopupWindow(loginwindow, pwidth, LayoutParams.WRAP_CONTENT, true);

		selectPopupWindow.setOutsideTouchable(true);

		// 这一句是为了实现弹出PopupWindow后，当点击屏幕其他部分及Back键时PopupWindow会消失，
		// 没有这一句则效果不能出来，但并不会影响背景
		// 本人能力极其有限，不明白其原因，还望高手、知情者指点一下
		selectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
	}

	/**
	 * 显示PopupWindow窗口
	 * 
	 * @param popupwindow
	 */
	public void popupWindwShowing() {
		// 将selectPopupWindow作为parent的下拉框显示，并指定selectPopupWindow在Y方向上向上偏移3pix，
		// 这是为了防止下拉框与文本框之间产生缝隙，影响界面美化
		// （是否会产生缝隙，及产生缝隙的大小，可能会根据机型、Android系统版本不同而异吧，不太清楚）
		selectPopupWindow.showAsDropDown(userIdEditText, 0, 1);
	}

	/**
	 * PopupWindow消失
	 */
	public void dismiss() {
		selectPopupWindow.dismiss();
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
