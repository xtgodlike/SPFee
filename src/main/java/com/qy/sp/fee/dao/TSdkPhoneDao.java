package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.SdkPhoneDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkPhone;
@Component
public class TSdkPhoneDao extends BaseDao{
	public static final String KEY_CACHE_TSDKPHONE = "KEY_CACHE_TSDKPHONE";
	@Resource
	private SdkPhoneDao sdkPhoneDao;
	
	public int insertSelective(TSdkPhone phone){
		return sdkPhoneDao.insertSelective(phone);
	}

	public TSdkPhone selectByPrimaryKey(String phoneId){
		TSdkPhone tSdkPhone = null;
    	String redisKey = phoneId;
    	String value = redisDao.get(KEY_CACHE_TSDKPHONE, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tSdkPhone = sdkPhoneDao.selectByPrimaryKey(phoneId);
    		if(tSdkPhone != null){
    			redisDao.put(KEY_CACHE_TSDKPHONE, redisKey, JsonUtils.bean2Json(tSdkPhone));
    		}
    	}else{
    		tSdkPhone = JsonUtils.json2Bean(value, TSdkPhone.class);
    	}
    	return tSdkPhone;
	}

	public int updateByPrimaryKeySelective(TSdkPhone phone){
		String redisKey = phone.getPhoneId();
		redisDao.put(KEY_CACHE_TSDKPHONE, redisKey,JsonUtils.bean2Json(phone));
		return sdkPhoneDao.updateByPrimaryKeySelective(phone);
    }
}
