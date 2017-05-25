package com.qy.sp.fee.modules.usercenter.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.ClientProperty;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TChannelDao;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkConfigService;

import net.sf.json.JSONObject;

@Service
public class ResourceDistributionService {
	@Resource
	private ApiSdkConfigService apiSdkConfigService;
	@Resource
	private TChannelDao tChannelDao;
	public JSONObject queryResourceSdkInfo(JSONObject requestObject){
		String imei = requestObject.optString("imei");
		String channelApi = requestObject.optString("channelApi");
		String appId = requestObject.optString("appId");
		String sdkVersion = requestObject.optString("sdkVersion");
		String osVersion = requestObject.optString("osVersion");
		String cpuModel = requestObject.optString("cpuModel");
		String phoneModel = requestObject.optString("phoneModel");
	
		JSONObject resultObj = new JSONObject();
		if(NumberUtil.getInteger(sdkVersion) <= 1){
			if("armeabi-v7a".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl", ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X32"));
			}else if("armeabi".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl", ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X32"));
			}else if("mips".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl", ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X32"));
			}else if("x86".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl",ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X32"));
			}else if("arm64-v8a".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl", ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X64"));
			}else if("mips64".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl",ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X64"));
			}else if("x86_64".equals(cpuModel)){
				resultObj.put("fileServerConfigUrl", ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X64"));
			}else{
				resultObj.put("fileServerConfigUrl", ClientProperty.getProperty("config","FILE_SERVER_CONFIG_URL_X32"));
			}
		}
		resultObj.put("platformServerUrl",ClientProperty.getProperty("config","PLATFORM_SERVER_URL"));
		resultObj.put("platformTradeUrl",ClientProperty.getProperty("config","PLATFORM_SERVER_URL"));
		TChannel channel = tChannelDao.selectByApiKey(channelApi);
		String isLoggerFile = "false";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("appId", appId);
		jsonObject.put("imei", imei);
		if(channel != null){
			jsonObject.put("channelId", channel.getChannelId());
		}
		jsonObject.put("configId", "isLoggerFile");
		JSONObject config = apiSdkConfigService.queryConfigurations(jsonObject);
		if(config != null && StringUtil.isNotEmptyString(config.optString("configValue"))){
			isLoggerFile = config.optString("configValue");
		}
		resultObj.put("isLoggerFile", isLoggerFile);
		resultObj.put("fileUploadUrl", ClientProperty.getProperty("config","FILE_UPLOAD_URL"));
		return resultObj;
		
	}
}
