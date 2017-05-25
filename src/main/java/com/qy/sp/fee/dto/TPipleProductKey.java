package com.qy.sp.fee.dto;

public class TPipleProductKey {
    private String pipleId;

    private String productId;

    public String getPipleId() {
        return pipleId;
    }

    public void setPipleId(String pipleId) {
        this.pipleId = pipleId == null ? null : pipleId.trim();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId == null ? null : productId.trim();
    }
}