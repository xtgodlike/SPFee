package com.qy.sp.fee.dto;

import java.io.Serializable;

public class PiplePriorityParam implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7910273038627933480L;
	private String channelId;
	private String segment;
	private String productCode;
	
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getSegment() {
		return segment;
	}
	public void setSegment(String segment) {
		this.segment = segment;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	
}
