package com.qy.sp.fee.dto;

public class TChannelProvinceLimit {
    private String pipleId;
    private String channelId;
    private int provinceId;
    private int tradeDay;
    private int tradeMonth;
	public String getPipleId() {
		return pipleId;
	}
	public void setPipleId(String pipleId) {
		this.pipleId = pipleId;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}
	public int getTradeDay() {
		return tradeDay;
	}
	public void setTradeDay(int tradeDay) {
		this.tradeDay = tradeDay;
	}
	public int getTradeMonth() {
		return tradeMonth;
	}
	public void setTradeMonth(int tradeMonth) {
		this.tradeMonth = tradeMonth;
	}

}