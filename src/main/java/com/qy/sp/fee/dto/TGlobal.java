package com.qy.sp.fee.dto;

public class TGlobal {
    private Integer globalId;

    private String globalName;

    private String globalValue;

    private String commetDesc;

    public Integer getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Integer globalId) {
        this.globalId = globalId;
    }

    public String getGlobalName() {
        return globalName;
    }

    public void setGlobalName(String globalName) {
        this.globalName = globalName == null ? null : globalName.trim();
    }

    public String getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(String globalValue) {
        this.globalValue = globalValue == null ? null : globalValue.trim();
    }

    public String getCommetDesc() {
        return commetDesc;
    }

    public void setCommetDesc(String commetDesc) {
        this.commetDesc = commetDesc == null ? null : commetDesc.trim();
    }
}