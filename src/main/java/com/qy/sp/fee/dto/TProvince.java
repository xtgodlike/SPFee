package com.qy.sp.fee.dto;

public class TProvince {
    private Integer provinceId;

    private String provinceName;

    private String provinceAbsName;

    private String isoCode;

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName == null ? null : provinceName.trim();
    }

    public String getProvinceAbsName() {
        return provinceAbsName;
    }

    public void setProvinceAbsName(String provinceAbsName) {
        this.provinceAbsName = provinceAbsName == null ? null : provinceAbsName.trim();
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode == null ? null : isoCode.trim();
    }
}