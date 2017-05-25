package com.qy.sp.fee.dto;

public class TPipleProvince extends TPipleProvinceKey {
    private Integer priority;
    private Integer opStatus;
    private String pipleProvinceCode;
   
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

	public Integer getOpStatus() {
		return opStatus;
	}

	public void setOpStatus(Integer opStatus) {
		this.opStatus = opStatus;
	}

	public String getPipleProvinceCode() {
		return pipleProvinceCode;
	}

	public void setPipleProvinceCode(String pipleProvinceCode) {
		this.pipleProvinceCode = pipleProvinceCode;
	}
    
}