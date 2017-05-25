package com.qy.sp.fee.dto;

import java.util.Date;

public class TImsiMobile {
    private String imsi;

    private String mobile;

    private Date importtime;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi == null ? null : imsi.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public Date getImporttime() {
        return importtime;
    }

    public void setImporttime(Date importtime) {
        this.importtime = importtime;
    }
}