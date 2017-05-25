package com.qy.sp.fee.entity;

import java.io.Serializable;

public class BaseResult implements Serializable {

	private static final long serialVersionUID = -1394389830254774838L;

	private String resultCode;
	private String resultMsg;

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
	
	

}
