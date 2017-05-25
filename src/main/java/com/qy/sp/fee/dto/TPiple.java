package com.qy.sp.fee.dto;

import java.util.Date;

public class TPiple {
    private String pipleId;

    private String pipleName;

    private String supplierId;

    private Integer opStatus;

    private String contractId;

    private String pipleUrlA;

    private String pipleUrlB;

    private String notifyUrlA;

    private String notifyUrlB;

    private String channelUrlA;

    private String channelUrlB;

    private String pipleDoc;

    private String channelDoc;

    private String pipleAuthA;

    private String pipleAuthB;

    private String pipleAuthC;

    private String pipleAuthD;
    
    private Integer hostId;
    private String pluginFile;
    private String pluginVersion;
    private String testPluginId;  // 测试插件ID
    private int codeType;
    private int pipleType;
    private int caclType;//结算方式
    private String pipleNumber;
    private Date createTime;
    private Date modifyTime;

    public void setHostId(Integer hostId){
    	this.hostId = hostId;
    }
    
    public Integer getHostId(){
    	return this.hostId;
    }
    
    public String getPipleId() {
        return pipleId;
    }

    public void setPipleId(String pipleId) {
        this.pipleId = pipleId == null ? null : pipleId.trim();
    }

    public String getPipleName() {
        return pipleName;
    }

    public void setPipleName(String pipleName) {
        this.pipleName = pipleName == null ? null : pipleName.trim();
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId == null ? null : supplierId.trim();
    }

    public Integer getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(Integer opStatus) {
        this.opStatus = opStatus;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId == null ? null : contractId.trim();
    }

    public String getPipleUrlA() {
        return pipleUrlA;
    }

    public void setPipleUrlA(String pipleUrlA) {
        this.pipleUrlA = pipleUrlA == null ? null : pipleUrlA.trim();
    }

    public String getPipleUrlB() {
        return pipleUrlB;
    }

    public void setPipleUrlB(String pipleUrlB) {
        this.pipleUrlB = pipleUrlB == null ? null : pipleUrlB.trim();
    }

    public String getNotifyUrlA() {
        return notifyUrlA;
    }

    public void setNotifyUrlA(String notifyUrlA) {
        this.notifyUrlA = notifyUrlA == null ? null : notifyUrlA.trim();
    }

    public String getNotifyUrlB() {
        return notifyUrlB;
    }

    public void setNotifyUrlB(String notifyUrlB) {
        this.notifyUrlB = notifyUrlB == null ? null : notifyUrlB.trim();
    }

    public String getChannelUrlA() {
        return channelUrlA;
    }

    public void setChannelUrlA(String channelUrlA) {
        this.channelUrlA = channelUrlA == null ? null : channelUrlA.trim();
    }

    public String getChannelUrlB() {
        return channelUrlB;
    }

    public void setChannelUrlB(String channelUrlB) {
        this.channelUrlB = channelUrlB == null ? null : channelUrlB.trim();
    }

    public String getPipleDoc() {
        return pipleDoc;
    }

    public void setPipleDoc(String pipleDoc) {
        this.pipleDoc = pipleDoc == null ? null : pipleDoc.trim();
    }

    public String getChannelDoc() {
        return channelDoc;
    }

    public void setChannelDoc(String channelDoc) {
        this.channelDoc = channelDoc == null ? null : channelDoc.trim();
    }

    public String getPipleAuthA() {
        return pipleAuthA;
    }

    public void setPipleAuthA(String pipleAuthA) {
        this.pipleAuthA = pipleAuthA == null ? null : pipleAuthA.trim();
    }

    public String getPipleAuthB() {
        return pipleAuthB;
    }

    public void setPipleAuthB(String pipleAuthB) {
        this.pipleAuthB = pipleAuthB == null ? null : pipleAuthB.trim();
    }

    public String getPipleAuthC() {
        return pipleAuthC;
    }

    public void setPipleAuthC(String pipleAuthC) {
        this.pipleAuthC = pipleAuthC == null ? null : pipleAuthC.trim();
    }

    public String getPipleAuthD() {
        return pipleAuthD;
    }

    public void setPipleAuthD(String pipleAuthD) {
        this.pipleAuthD = pipleAuthD == null ? null : pipleAuthD.trim();
    }

	public String getPluginFile() {
		return pluginFile;
	}

	public void setPluginFile(String pluginFile) {
		this.pluginFile = pluginFile;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}

	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	public int getCodeType() {
		return codeType;
	}

	public void setCodeType(int codeType) {
		this.codeType = codeType;
	}

	public int getPipleType() {
		return pipleType;
	}

	public void setPipleType(int pipleType) {
		this.pipleType = pipleType;
	}


	public int getCaclType() {
		return caclType;
	}

	public void setCaclType(int caclType) {
		this.caclType = caclType;
	}

	public String getPipleNumber() {
		return pipleNumber;
	}

	public void setPipleNumber(String pipleNumber) {
		this.pipleNumber = pipleNumber;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getTestPluginId() {
		return testPluginId;
	}

	public void setTestPluginId(String testPluginId) {
		this.testPluginId = testPluginId;
	}
    
}