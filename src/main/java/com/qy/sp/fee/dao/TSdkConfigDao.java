package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.SdkConfigDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkConfig;
import com.qy.sp.fee.dto.TSdkConfigQueryKey;
@Component
public class TSdkConfigDao extends BaseDao{
	public static final String KEY_CACHE_TSDKCONFIG = "KEY_CACHE_TSDKCONFIG";
	@Resource
	private SdkConfigDao sdkConfigDao;
	public int deleteByPrimaryKey(String configId){
		return sdkConfigDao.deleteByPrimaryKey(configId);
	}

	public int insert(TSdkConfig config){
		return sdkConfigDao.insert(config);
    }

	public TSdkConfig selectByPrimaryKey(String configId){
		TSdkConfig tSdkConfig = null;
    	String redisKey = configId;
    	String value = redisDao.get(KEY_CACHE_TSDKCONFIG, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tSdkConfig = sdkConfigDao.selectByPrimaryKey(configId);
    		if(tSdkConfig != null){
    			redisDao.put(KEY_CACHE_TSDKCONFIG, redisKey, JsonUtils.bean2Json(tSdkConfig));
    		}
    	}else{
    		tSdkConfig = JsonUtils.json2Bean(value, TSdkConfig.class);
    	}
    	return tSdkConfig;
    }
    
	public int updateByPrimaryKey(TSdkConfig config){
		return sdkConfigDao.updateByPrimaryKey(config);
    }
    
	public List<TSdkConfig> selectConfigurationsByConfigQueryKey(TSdkConfigQueryKey key){
		List<TSdkConfig> list = null;
    	String redisKey = "";
    	if(StringUtil.isNotEmptyString(key.getPhoneId())){
    		redisKey += key.getPhoneId();
    	}
    	if(StringUtil.isNotEmptyString(key.getChannelId())){
    		redisKey += key.getChannelId();
    	}
    	if(StringUtil.isNotEmptyString(key.getAppId())){
    		redisKey += key.getAppId();
    	}
    	if(StringUtil.isNotEmptyString(key.getProvinceId())){
    		redisKey += key.getProvinceId();
    	}
    	if(StringUtil.isNotEmptyString(key.getPipleId())){
    		redisKey += key.getPipleId();
    	}
    	String value = redisDao.get(KEY_CACHE_TSDKCONFIG+key.getConfigId(), redisKey);
    	if(StringUtil.isEmpty(value)){
    		list = sdkConfigDao.selectConfigurationsByConfigQueryKey(key);
    		if(list != null){
    			redisDao.put(KEY_CACHE_TSDKCONFIG+key.getConfigId(), redisKey, JsonUtils.list2Json(list));
    		}
    	}else{
    		list = JsonUtils.json2List(value,TSdkConfig.class);
    	}
    	return list;
	}
	
}
