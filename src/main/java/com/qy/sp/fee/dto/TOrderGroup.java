package com.qy.sp.fee.dto;

import java.util.Date;

public class TOrderGroup {
    private String orderGroupId;

    private Date createTime;

    private Integer priceRequest;

    private Integer priceComplete;

    private Date completeTime;

    private Integer orderStatus;

    public String getOrderGroupId() {
        return orderGroupId;
    }

    public void setOrderGroupId(String orderGroupId) {
        this.orderGroupId = orderGroupId == null ? null : orderGroupId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getPriceRequest() {
        return priceRequest;
    }

    public void setPriceRequest(Integer priceRequest) {
        this.priceRequest = priceRequest;
    }

    public Integer getPriceComplete() {
        return priceComplete;
    }

    public void setPriceComplete(Integer priceComplete) {
        this.priceComplete = priceComplete;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }
}