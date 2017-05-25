package com.qy.sp.fee.entity;

import java.io.Serializable;

public class BaseChannelRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -977876579476365724L;
	private String apiKey;
	private String productCode;
	private String imsi;
	private String mobile;
	private String pipleId;
	private String imei;
	private String ipProvince;
	private int provinceId;
	private int hostId;
	
	
	public String getImsi() {
		return imsi;
	}
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPipleId() {
		return pipleId;
	}
	public void setPipleId(String pipleId) {
		this.pipleId = pipleId;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}
	public int getHostId() {
		return hostId;
	}
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}
	public String getIpProvince() {
		return ipProvince;
	}
	public void setIpProvince(String ipProvince) {
		this.ipProvince = ipProvince;
	}
	
}
