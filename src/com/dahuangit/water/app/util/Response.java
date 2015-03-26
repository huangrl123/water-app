package com.dahuangit.water.app.util;

import java.io.Serializable;

public class Response implements Serializable {
	private Boolean success = true;

	private String msg = null;

	private String systemId = null;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
}
