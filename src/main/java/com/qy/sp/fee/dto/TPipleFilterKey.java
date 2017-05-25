package com.qy.sp.fee.dto;

public class TPipleFilterKey {
    private String pipleId;

    private String provinceId;
    private String productId;
    private String hostId;
    private String channelId;
    private String pluginType;

    public String getPipleId() {
        return pipleId;
    }

    public void setPipleId(String pipleId) {
        this.pipleId = pipleId == null ? null : pipleId.trim();
    }

	public String getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(String provinceId) {
		this.provinceId = provinceId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getPluginType() {
		return pluginType;
	}

	public void setPluginType(String pluginType) {
		this.pluginType = pluginType;
	}

}