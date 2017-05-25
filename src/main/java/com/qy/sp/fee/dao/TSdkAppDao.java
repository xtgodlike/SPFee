package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.SdkAppDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkApp;

@Component
public class TSdkAppDao extends BaseDao{
	public static final String KEY_CACHE_TSDKAPP = "KEY_CACHE_TSDKAPP";
	@Resource
	private SdkAppDao sdkAppDao;

	public TSdkApp selectByPrimaryKey(String appId){
		TSdkApp tSdkApp = null;
    	String redisKey = appId;
    	String value = redisDao.get(KEY_CACHE_TSDKAPP, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tSdkApp = sdkAppDao.selectByPrimaryKey(appId);
    		if(tSdkApp != null){
    			redisDao.put(KEY_CACHE_TSDKAPP, redisKey, JsonUtils.bean2Json(tSdkApp));
    		}
    	}else{
    		tSdkApp = JsonUtils.json2Bean(value, TSdkApp.class);
    	}
    	return tSdkApp;
	}

	
}