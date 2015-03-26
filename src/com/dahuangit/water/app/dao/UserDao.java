package com.dahuangit.water.app.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class UserDao extends BaseDao {

	public UserDao(Context context) {
		super(context);
	}

	public void addUser(String userId, String password) {
		ContentValues values = new ContentValues();
		values.put("user_id", userId);
		values.put("password", password);
		String sql = "select * from t_user where user_id='" + userId + "'";
		Cursor c = this.query(sql);
		while (c.moveToNext()) {
			ContentValues v = new ContentValues();
			v.put("password", password);
			this.update("t_user", v, "user_id=?", new String[] { userId });
			return;
		}
		
		this.insert("t_user", values);
	}

	public List<String> getAllUserId() {
		String sql = "select user_id from t_user";
		Cursor c = this.query(sql);

		List<String> list = new ArrayList<String>();
		while (c.moveToNext()) {
			int index = c.getColumnIndex("user_id");
			String userId = c.getString(index);
			list.add(userId);
		}

		return list;
	}

	public String getPasswordByUserId(String userId) {
		String sql = "select password from t_user where user_id='" + userId + "'";
		Cursor c = this.query(sql);

		while (c.moveToNext()) {
			int index = c.getColumnIndex("password");
			String password = c.getString(index);
			return password;
		}

		return null;
	}

	public void deleteUser(String userId) {
		this.delete("t_user", "user_id=?", new String[] { userId });
	}
}
