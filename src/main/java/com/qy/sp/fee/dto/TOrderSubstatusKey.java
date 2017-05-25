package com.qy.sp.fee.dto;

public class TOrderSubstatusKey {
    private String pipleId;

    private Integer subStatus;

    public String getPipleId() {
        return pipleId;
    }

    public void setPipleId(String pipleId) {
        this.pipleId = pipleId == null ? null : pipleId.trim();
    }

    public Integer getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(Integer subStatus) {
        this.subStatus = subStatus;
    }
}