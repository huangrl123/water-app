package com.dahuangit.water.app;

import java.io.Serializable;

public class User implements Serializable {
	private String user_id = null;
	private String password = null;
	private String last_login_time = null;
	private String is_auto_login = "0";

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLast_login_time() {
		return last_login_time;
	}

	public void setLast_login_time(String last_login_time) {
		this.last_login_time = last_login_time;
	}

	public String getIs_auto_login() {
		return is_auto_login;
	}

	public void setIs_auto_login(String is_auto_login) {
		this.is_auto_login = is_auto_login;
	}

}
