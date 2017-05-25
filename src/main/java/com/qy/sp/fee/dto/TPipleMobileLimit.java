package com.qy.sp.fee.dto;

public class TPipleMobileLimit {
    private String pipleId;
    private int tradeDay;
    private int tradeMonth;
    private int tradeDayCount;
    private int tradeMonthCount;
    private int requestDayCount;
    private int requestMonthCount;
    private int limitType = 1;
    private int pipleTradeDay;
    private int pipleTradeMonth;
    
	public String getPipleId() {
		return pipleId;
	}
	public void setPipleId(String pipleId) {
		this.pipleId = pipleId;
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
	public int getTradeDayCount() {
		return tradeDayCount;
	}
	public void setTradeDayCount(int tradeDayCount) {
		this.tradeDayCount = tradeDayCount;
	}
	public int getTradeMonthCount() {
		return tradeMonthCount;
	}
	public void setTradeMonthCount(int tradeMonthCount) {
		this.tradeMonthCount = tradeMonthCount;
	}
	public int getRequestDayCount() {
		return requestDayCount;
	}
	public void setRequestDayCount(int requestDayCount) {
		this.requestDayCount = requestDayCount;
	}
	public int getRequestMonthCount() {
		return requestMonthCount;
	}
	public void setRequestMonthCount(int requestMonthCount) {
		this.requestMonthCount = requestMonthCount;
	}
	public int getLimitType() {
		return limitType;
	}
	public void setLimitType(int limitType) {
		this.limitType = limitType;
	}
	public int getPipleTradeDay() {
		return pipleTradeDay;
	}
	public void setPipleTradeDay(int pipleTradeDay) {
		this.pipleTradeDay = pipleTradeDay;
	}
	public int getPipleTradeMonth() {
		return pipleTradeMonth;
	}
	public void setPipleTradeMonth(int pipleTradeMonth) {
		this.pipleTradeMonth = pipleTradeMonth;
	}
	

}