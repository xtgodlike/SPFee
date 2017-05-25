package com.qy.sp.fee.dto;

import java.io.Serializable;

public class PiplePriority implements Serializable {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4031996991898056218L;
	public String getPipleId() {
		return pipleId;
	}
	public void setPipleId(String pipleId) {
		this.pipleId = pipleId;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}

	private String pipleId;
	private Integer priority;
	private Integer price;

}
