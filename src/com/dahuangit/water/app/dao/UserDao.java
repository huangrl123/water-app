package com.dahuangit.water.app.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dahuangit.water.app.User;
import com.dahuangit.water.app.util.DateUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class UserDao extends BaseDao {

	public UserDao(Context context) {
		super(context);
	}

	public void addUser(User user) {
		ContentValues values = new ContentValues();
		String userId = user.getUser_id();
		String password = user.getPassword();
		String is_auto_login = user.getIs_auto_login();

		values.put("user_id", userId);
		values.put("password", password);
		values.put("is_auto_login", is_auto_login);
		values.put("last_login_time", user.getLast_login_time());
		String sql = "select * from t_user where user_id='" + userId + "'";
		Cursor c = this.query(sql);
		while (c.moveToNext()) {
			ContentValues v = new ContentValues();
			v.put("password", password);
			v.put("last_login_time", DateUtils.format(new Date()));
			v.put("is_auto_login", is_auto_login);

			this.update("t_user", v, "user_id=?", new String[] { userId });
			return;
		}

		this.insert("t_user", values);
	}

	public List<String> getAllUserId() {
		String sql = "select user_id from t_user order by last_login_time desc";
		Cursor c = this.query(sql);

		List<String> list = new ArrayList<String>();
		while (c.moveToNext()) {
			int index = c.getColumnIndex("user_id");
			String userId = c.getString(index);
			list.add(userId);
		}

		return list;
	}

	public User getUserByUserId(String userId) {
		String sql = "select * from t_user where user_id='" + userId + "'";
		Cursor c = this.query(sql);

		while (c.moveToNext()) {
			User u = new User();
			int index = c.getColumnIndex("password");
			String password = c.getString(index);

			index = c.getColumnIndex("last_login_time");
			String last_login_time = c.getString(index);

			index = c.getColumnIndex("is_auto_login");
			String is_auto_login = c.getString(index);

			u.setUser_id(userId);
			u.setPassword(password);
			u.setLast_login_time(last_login_time);
			u.setIs_auto_login(is_auto_login);

			return u;
		}

		return null;
	}

	public void deleteUser(String userId) {
		this.delete("t_user", "user_id=?", new String[] { userId });
	}

	public void updateUser(User user) {
		ContentValues values = new ContentValues();
		values.put("password", user.getPassword());
		values.put("last_login_time", user.getLast_login_time());
		values.put("is_auto_login", user.getIs_auto_login());
		this.update("t_user", values, "user_id=?", new String[] { user.getUser_id() });
	}
}
