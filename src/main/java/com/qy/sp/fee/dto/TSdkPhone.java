package com.qy.sp.fee.dto;

import java.util.Date;

/**
 * Created by Administrator on 2016/2/25.
 */
public class TSdkPhone {
	private String phoneId;
    private String simImsi1;
    private String simIccid1;
    private String simNumber1;
    private int simProvince1;
    private String simImsi2;
    private String simIccid2;
    private String simNumber2;
    private int simProvince2;
    private String simImsi3;
    private String simIccid3;
    private String simNumber3;
    private int simProvince3;
    private String osVersion;
    private String imei;
    private String cpuModel;
    private String model;
    private String manufacture;
    private int screenWidth;
    private int screenHeight;
    private int isWifi;
    private String macAddress;
    private int isRoot;
    private String country;
    private String province;
    private String city;
    private String reqKey;
    private String processName;
    private String processName2;
    private int serviceId;
    private Date createTime;
    private Date modifyTime;

    
    public String getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}

	public String getSimImsi1() {
        return simImsi1;
    }

    public void setSimImsi1(String simImsi1) {
        this.simImsi1 = simImsi1;
    }

    public String getSimIccid1() {
        return simIccid1;
    }

    public void setSimIccid1(String simIccid1) {
        this.simIccid1 = simIccid1;
    }

    public String getSimNumber1() {
        return simNumber1;
    }

    public void setSimNumber1(String simNumber1) {
        this.simNumber1 = simNumber1;
    }


    public String getSimImsi2() {
        return simImsi2;
    }

    public void setSimImsi2(String simImsi2) {
        this.simImsi2 = simImsi2;
    }

    public String getSimIccid2() {
        return simIccid2;
    }

    public void setSimIccid2(String simIccid2) {
        this.simIccid2 = simIccid2;
    }

    public String getSimNumber2() {
        return simNumber2;
    }

    public void setSimNumber2(String simNumber2) {
        this.simNumber2 = simNumber2;
    }
    public String getSimImsi3() {
        return simImsi3;
    }

    public void setSimImsi3(String simImsi3) {
        this.simImsi3 = simImsi3;
    }

    public String getSimIccid3() {
        return simIccid3;
    }

    public void setSimIccid3(String simIccid3) {
        this.simIccid3 = simIccid3;
    }

    public String getSimNumber3() {
        return simNumber3;
    }

    public void setSimNumber3(String simNumber3) {
        this.simNumber3 = simNumber3;
    }


	public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacture() {
        return manufacture;
    }

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    
    public int getSimProvince1() {
		return simProvince1;
	}

	public void setSimProvince1(int simProvince1) {
		this.simProvince1 = simProvince1;
	}

	public int getSimProvince2() {
		return simProvince2;
	}

	public void setSimProvince2(int simProvince2) {
		this.simProvince2 = simProvince2;
	}

	public int getSimProvince3() {
		return simProvince3;
	}

	public void setSimProvince3(int simProvince3) {
		this.simProvince3 = simProvince3;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getIsWifi() {
		return isWifi;
	}

	public void setIsWifi(int isWifi) {
		this.isWifi = isWifi;
	}

	public int getIsRoot() {
		return isRoot;
	}

	public void setIsRoot(int isRoot) {
		this.isRoot = isRoot;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public String getReqKey() {
		return reqKey;
	}

	public void setReqKey(String reqKey) {
		this.reqKey = reqKey;
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

	public String getCpuModel() {
		return cpuModel;
	}

	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessName2() {
		return processName2;
	}

	public void setProcessName2(String processName2) {
		this.processName2 = processName2;
	}

	@Override
    public String toString() {
        return "imei:"+imei+","+"simImsi1:"+simImsi1+","+"simNumber1:"+simNumber1+","+"simImsi2:"+simImsi2+","+"simNumber2:"+simNumber2+","+"simImsi3:"+simImsi3+","+"simNumber3:"+simNumber3+"osVersion:"+osVersion+","+"isRoot:"+isRoot;
    }
    
}
