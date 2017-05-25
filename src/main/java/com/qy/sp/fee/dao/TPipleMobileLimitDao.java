package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.PipleMobileLimitDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TPipleMobileLimit;

@Component
public class TPipleMobileLimitDao extends BaseDao{
	public static final String KEY_CACHE_TPIPLEMOBILELIMIT = "KEY_CACHE_TPIPLEMOBILELIMIT";
	public static final String KEY_CACHE_PREFIX_TPIPLEMOBILELIMIT = "PipleMobileLimit";
	
	@Resource
	private PipleMobileLimitDao pipleMobileLimitDao;
    public TPipleMobileLimit selectByPrimaryKey(TPipleMobileLimit key){
    	TPipleMobileLimit tPipleMobileLimit = null;
    	String redisKey = KEY_CACHE_PREFIX_TPIPLEMOBILELIMIT+key.getPipleId();
    	String value = redisDao.get(KEY_CACHE_TPIPLEMOBILELIMIT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tPipleMobileLimit = pipleMobileLimitDao.selectByPrimaryKey(key);
    		if(tPipleMobileLimit != null){
    			redisDao.put(KEY_CACHE_TPIPLEMOBILELIMIT, redisKey, JsonUtils.bean2Json(tPipleMobileLimit));
    		}
    	}else{
    		tPipleMobileLimit = JsonUtils.json2Bean(value, TPipleMobileLimit.class);
    	}
    	return tPipleMobileLimit;
    }

}