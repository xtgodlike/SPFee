package com.qy.sp.fee.dto;

public class TOrderExtKey {
    private String orderId;

    private String extKey;
    
    
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getExtKey() {
        return extKey;
    }

    public void setExtKey(String extKey) {
        this.extKey = extKey == null ? null : extKey.trim();
    }
}