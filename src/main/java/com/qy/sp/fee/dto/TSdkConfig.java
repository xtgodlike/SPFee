package com.qy.sp.fee.dto;

import java.util.Date;

public class TSdkConfig {

	private String configId;
	private String configValue;
	private String configDescription;
	private Date createTime;
	private Date modifyTime;
	public String getConfigId() {
		return configId;
	}
	public void setConfigId(String configId) {
		this.configId = configId;
	}
	public String getConfigValue() {
		return configValue;
	}
	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}
	public String getConfigDescription() {
		return configDescription;
	}
	public void setConfigDescription(String configDescription) {
		this.configDescription = configDescription;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	
	
}
