package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ChannelProvinceLimitDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TChannelProvinceLimit;

@Component
public class TChannelProvinceLimitDao extends BaseDao{
	public static final String KEY_CACHE_TCHANNELPROVINCELIMIT = "KEY_CACHE_TCHANNELPROVINCELIMIT";
	
	@Resource
	private ChannelProvinceLimitDao channelProvinceLimitDao;
	
    public TChannelProvinceLimit selectByPrimaryKey(TChannelProvinceLimit key){
    	TChannelProvinceLimit tChannelProvinceLimit = null;
    	String redisKey = key.getPipleId()+key.getChannelId()+key.getProvinceId();
    	String value = redisDao.get(KEY_CACHE_TCHANNELPROVINCELIMIT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tChannelProvinceLimit = channelProvinceLimitDao.selectByPrimaryKey(key);
    		redisDao.put(KEY_CACHE_TCHANNELPROVINCELIMIT, redisKey, JsonUtils.bean2Json(tChannelProvinceLimit));
    	}else{
    		tChannelProvinceLimit = JsonUtils.json2Bean(value, TChannelProvinceLimit.class);
    	}
    	return tChannelProvinceLimit;
    }

}