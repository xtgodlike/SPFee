package com.qy.sp.fee.dto;

public class TPipleApply extends TPiple {
	private String productId;
	private int priority;
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
	
}
