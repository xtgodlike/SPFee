package com.qy.sp.fee.modules.apisdk.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TSdkConfigDao;
import com.qy.sp.fee.dto.TSdkConfig;
import com.qy.sp.fee.dto.TSdkConfigQueryKey;

import net.sf.json.JSONObject;

@Service
public class ApiSdkConfigService {

	@Resource
	private TSdkConfigDao tSdkConfigDao;
	public JSONObject queryConfigurations(JSONObject requestJSonObject){
		String phoneId = requestJSonObject.optString("imei");
		String appId = requestJSonObject.optString("appId");
		String channelId = requestJSonObject.optString("channelId");
		String provinceId = requestJSonObject.optString("provinceId");
		String configId = requestJSonObject.optString("configId");
		
		JSONObject resultObject = new JSONObject();
		TSdkConfig config = null;
		if(StringUtil.isNotEmptyString(phoneId) && config == null){
			TSdkConfigQueryKey key = new TSdkConfigQueryKey();
			key.setConfigId(configId);
			key.setPhoneId(phoneId);
			List<TSdkConfig> configs = tSdkConfigDao.selectConfigurationsByConfigQueryKey(key);
			if(configs.size() >0){
				config = configs.get(0); 
			}
		}
		if(config == null && (StringUtil.isNotEmptyString(appId) || StringUtil.isNotEmptyString(channelId) || StringUtil.isNotEmptyString(provinceId) )){
			TSdkConfigQueryKey key = new TSdkConfigQueryKey();
			key.setConfigId(configId);
			if(StringUtil.isNotEmptyString(appId)){
				key.setAppId(appId);
			}
			if(StringUtil.isNotEmptyString(channelId)){
				key.setChannelId(channelId);
			}
			if(StringUtil.isNotEmptyString(provinceId)){
				key.setProvinceId(provinceId);
			}
			List<TSdkConfig> configs = tSdkConfigDao.selectConfigurationsByConfigQueryKey(key);
			if(configs.size() >0){
				config = configs.get(0); 
			}
		}
		if(config == null){
			TSdkConfigQueryKey key = new TSdkConfigQueryKey();
			key.setConfigId(configId);
			List<TSdkConfig> configs = tSdkConfigDao.selectConfigurationsByConfigQueryKey(key);
			if(configs.size() >0){
				config = configs.get(0); 
			}
		}
		if(config != null){
			resultObject.put("configId", config.getConfigId());
			resultObject.put("configValue", config.getConfigValue());
		}
		return resultObject;
	}
}
