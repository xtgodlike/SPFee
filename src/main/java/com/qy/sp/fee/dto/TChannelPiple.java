package com.qy.sp.fee.dto;

public class TChannelPiple extends TChannelPipleKey {
    private String notifyUrl;
    private int volt;
    private Integer tradeDay;
    private Integer tradeMonth;

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl == null ? null : notifyUrl.trim();
    }

	public int getVolt() {
		return volt;
	}

	public void setVolt(int volt) {
		this.volt = volt;
	}

	public Integer getTradeDay() {
		return tradeDay;
	}

	public void setTradeDay(Integer tradeDay) {
		this.tradeDay = tradeDay;
	}

	public Integer getTradeMonth() {
		return tradeMonth;
	}

	public void setTradeMonth(Integer tradeMonth) {
		this.tradeMonth = tradeMonth;
	}
    
}