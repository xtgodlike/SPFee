package com.qy.sp.fee.dto;

public class TPipleProduct extends TPipleProductKey {
    private String pipleProductCode;

    private String pipleProductAbbrCode;

    private Integer opStatus;

    public String getPipleProductCode() {
        return pipleProductCode;
    }

    public void setPipleProductCode(String pipleProductCode) {
        this.pipleProductCode = pipleProductCode == null ? null : pipleProductCode.trim();
    }

    public String getPipleProductAbbrCode() {
        return pipleProductAbbrCode;
    }

    public void setPipleProductAbbrCode(String pipleProductAbbrCode) {
        this.pipleProductAbbrCode = pipleProductAbbrCode == null ? null : pipleProductAbbrCode.trim();
    }

    public Integer getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(Integer opStatus) {
        this.opStatus = opStatus;
    }
}