package com.qy.sp.fee.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;

public class TOrder implements Cloneable {
    private String orderId;

    private String pipleId;

    private String mobile;

    private String channelId;

    private String productId;

    private Integer orderStatus;

    private Integer subStatus;

    private String pipleOrderId;

    private Date createTime;

    private Date modTime;

    private Date completeTime;

    private Integer decStatus = GlobalConst.DEC_STATUS.UNDEDUCTED;

    private Integer rnd;

    private String imei;
    
    private String imsi;

    private String iccid;

    private String resultCode;
    
    private Integer volt;
    
    private int fromType;
    
    private int syncResultCode;
    
    private BigDecimal amount;
    
    private Integer provinceId;
    private String flowId;
    private String groupId;
    
    private String extData;
    private String appId;
    private List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();

    public String getExtData() {
		return extData;
	}

	public void setExtData(String extData) {
		this.extData = extData;
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getPipleId() {
        return pipleId;
    }

    public void setPipleId(String pipleId) {
        this.pipleId = pipleId == null ? null : pipleId.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId == null ? null : channelId.trim();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId == null ? null : productId.trim();
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(Integer subStatus) {
        this.subStatus = subStatus;
    }

    public String getPipleOrderId() {
        return pipleOrderId;
    }

    public void setPipleOrderId(String pipleOrderId) {
        this.pipleOrderId = pipleOrderId == null ? null : pipleOrderId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModTime() {
        return modTime;
    }

    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public Integer getDecStatus() {
        return decStatus;
    }

    public void setDecStatus(Integer decStatus) {
        this.decStatus = decStatus;
    }

    public Integer getRnd() {
        return rnd;
    }

    public void setRnd(Integer rnd) {
        this.rnd = rnd;
    }

    public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi == null ? null : imsi.trim();
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid == null ? null : iccid.trim();
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode == null ? null : resultCode.trim();
    }

	public Integer getVolt() {
		return volt;
	}

	public void setVolt(Integer volt) {
		this.volt = volt;
	}
    

	public boolean deduct(int volt){
		this.volt = volt;
		if(this.orderStatus != GlobalConst.OrderStatus.SUCCESS){
			return false;
		}
		
		this.rnd = KeyHelper.randomVal();
		boolean bDeducted = false;
		if(this.rnd <= this.volt){
			this.decStatus = GlobalConst.DEC_STATUS.DEDUCTED;
			bDeducted = true;
		}
		
		return bDeducted;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	public int getFromType() {
		return fromType;
	}

	public void setFromType(int fromType) {
		this.fromType = fromType;
	}

	public int getSyncResultCode() {
		return syncResultCode;
	}

	public void setSyncResultCode(int syncResultCode) {
		this.syncResultCode = syncResultCode;
	}

	public void setTOrder(TOrder order){
		this.channelId 		= order.getChannelId();
		this.completeTime 	= order.getCompleteTime();
		this.createTime 	= order.getCreateTime();
		this.decStatus 		= order.getDecStatus();
		this.iccid 			= order.getIccid();
		this.imsi 			= order.getImsi();
		this.mobile 		= order.getMobile();
		this.modTime 		= order.getModTime();
		this.orderId 		= order.getOrderId();
		this.orderStatus 	= order.getOrderStatus();
		this.pipleId 		= order.getPipleId();
		this.pipleOrderId 	= order.getPipleOrderId();
		this.productId 		= order.getProductId();
		this.resultCode 	= order.getResultCode();
		this.rnd 			= order.getRnd();
		this.subStatus 		= order.getSubStatus();
		this.volt 			= order.getVolt();
		this.groupId	= order.getGroupId();
		this.amount			= order.getAmount();
		this.extData		= order.getExtData();
		this.appId		= 	order.getAppId();
	}
	
	public void addToderExts(List<TOrderExt> tOrderExts){
		this.tOrderExts.addAll(tOrderExts);
	}
	public void addToderExts(TOrderExt tOrderExt){
		this.tOrderExts.add(tOrderExt);
	}
	public List<TOrderExt> gettOrderExts() {
		return tOrderExts;
	}
	
}