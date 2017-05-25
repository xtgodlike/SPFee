package com.qy.sp.fee.dto;

public class TOrderExt extends TOrderExtKey {
    private String extValue;
    public String getExtValue() {
        return extValue;
    }

    public void setExtValue(String extValue) {
        this.extValue = extValue == null ? null : extValue.trim();
    }
}