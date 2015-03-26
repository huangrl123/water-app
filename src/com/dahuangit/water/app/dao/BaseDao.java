package com.dahuangit.water.app.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.protocol.HTTP;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dahuangit.water.app.R;

/**
 * sqlite辅助类
 * 
 * @author 黄仁良
 * 
 *         创建时间 2014年12月10日 下午4:01:59
 */
public class BaseDao extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "water.db";
	private static final int DATABASE_VERSION = 1;

	private Context context = null;

	public BaseDao(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		InputStream in = context.getResources().openRawResource(R.raw.water);

		StringBuffer responseContent = new StringBuffer();
		InputStreamReader inputStreamReader = null;
		BufferedReader read = null;

		try {
			inputStreamReader = new InputStreamReader(in, HTTP.UTF_8);
			read = new BufferedReader(inputStreamReader);

			String inputLine = null;
			while ((inputLine = read.readLine()) != null) {
				responseContent.append(inputLine);
			}

			String[] arr = responseContent.toString().split(";");
			for (String s : arr) {
				db.execSQL(s);
			}

			inputStreamReader.close();
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStreamReader.close();
				read.close();
			} catch (IOException e) {
			}
		}
	}

	public void insert(String table, ContentValues values) {
		this.getWritableDatabase().insert(table, null, values);
	}

	public Cursor query(String sql, String[] selectionArgs) {
		return this.getReadableDatabase().rawQuery(sql, selectionArgs);
	}

	public Cursor query(String sql) {
		return this.getReadableDatabase().rawQuery(sql, null);
	}

	public void delete(String table, String whereClause, String[] whereArgs) {
		this.getWritableDatabase().delete(table, whereClause, whereArgs);
	}

	public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		this.getWritableDatabase().update(table, values, whereClause, whereArgs);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
