package com.qy.sp.fee.dto;

import java.util.Date;

public class TContract {
    private String contractId;

    private String partyA;

    private String partyB;

    private String partyC;

    private Date validateStart;

    private Date validateEnd;

    private Date signDate;

    private Date createTime;

    private Integer contractType;

    private Integer opStatus;

    private String fileId;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId == null ? null : contractId.trim();
    }

    public String getPartyA() {
        return partyA;
    }

    public void setPartyA(String partyA) {
        this.partyA = partyA == null ? null : partyA.trim();
    }

    public String getPartyB() {
        return partyB;
    }

    public void setPartyB(String partyB) {
        this.partyB = partyB == null ? null : partyB.trim();
    }

    public String getPartyC() {
        return partyC;
    }

    public void setPartyC(String partyC) {
        this.partyC = partyC == null ? null : partyC.trim();
    }

    public Date getValidateStart() {
        return validateStart;
    }

    public void setValidateStart(Date validateStart) {
        this.validateStart = validateStart;
    }

    public Date getValidateEnd() {
        return validateEnd;
    }

    public void setValidateEnd(Date validateEnd) {
        this.validateEnd = validateEnd;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getContractType() {
        return contractType;
    }

    public void setContractType(Integer contractType) {
        this.contractType = contractType;
    }

    public Integer getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(Integer opStatus) {
        this.opStatus = opStatus;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }
}