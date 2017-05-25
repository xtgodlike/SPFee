package com.qy.sp.fee.dto;

import java.util.Date;

public class TSdkApp {
    private String appId;

    private String cpId;

    private String appName;

    private String appPacketname;

    private String appSigin;

    private Integer appStatus;

    private String apkId;

    private Date createTime;

    private Date modTime;

    private long appSize;
    
    private String cpName;
    

    public String getCpName() {
		return cpName;
	}

	public void setCpName(String cpName) {
		this.cpName = cpName;
	}

	public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    public String getCpId() {
        return cpId;
    }

    public void setCpId(String cpId) {
        this.cpId = cpId == null ? null : cpId.trim();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName == null ? null : appName.trim();
    }

    public String getAppPacketname() {
        return appPacketname;
    }

    public void setAppPacketname(String appPacketname) {
        this.appPacketname = appPacketname == null ? null : appPacketname.trim();
    }

    public String getAppSigin() {
        return appSigin;
    }

    public void setAppSigin(String appSigin) {
        this.appSigin = appSigin == null ? null : appSigin.trim();
    }

    public Integer getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(Integer appStatus) {
        this.appStatus = appStatus;
    }

    public String getApkId() {
        return apkId;
    }

    public void setApkId(String apkId) {
        this.apkId = apkId == null ? null : apkId.trim();
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

	public long getAppSize() {
		return appSize;
	}

	public void setAppSize(long appSize) {
		this.appSize = appSize;
	}

}