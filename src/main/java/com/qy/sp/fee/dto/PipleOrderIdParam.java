package com.qy.sp.fee.dto;

public class PipleOrderIdParam {
	private String pipleID;
	private String pipleOrderID;
	
	public String getPipleID(){
		return this.pipleID;
	}
	
	public void setPipleID(String pipleID){
		this.pipleID = pipleID;
	}
	
	public String getPipleOrderID(){
		return this.pipleOrderID;
	}
	
	public void setPipleOrderID(String pipleOrderID){
		this.pipleOrderID = pipleOrderID;
	}
}
