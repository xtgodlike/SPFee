package com.qy.sp.fee.dto;

import java.io.Serializable;

public class PiplePriorityIccidParam implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5817317274025943059L;

	private String channelId;
	private String productCode;
	private int provinceId;
	private int hostId;
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
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
}
