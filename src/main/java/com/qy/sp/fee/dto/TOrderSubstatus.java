package com.qy.sp.fee.dto;

public class TOrderSubstatus extends TOrderSubstatusKey {
    private String subStatusDesc;

    public String getSubStatusDesc() {
        return subStatusDesc;
    }

    public void setSubStatusDesc(String subStatusDesc) {
        this.subStatusDesc = subStatusDesc == null ? null : subStatusDesc.trim();
    }
}