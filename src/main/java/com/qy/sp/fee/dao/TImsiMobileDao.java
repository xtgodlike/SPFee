package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ImsiMobileDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TImsiMobile;

@Component
public class TImsiMobileDao extends BaseDao{
	public static final String KEY_CACHE_TIMSIMOBILE = "KEY_CACHE_TIMSIMOBILE";
	@Resource
	private ImsiMobileDao imsiMobileDao;
	
    public int deleteByPrimaryKey(String imsi){
    	return imsiMobileDao.deleteByPrimaryKey(imsi);
    }

    public int insert(TImsiMobile record){
    	return imsiMobileDao.insert(record);
    }

    public int insertSelective(TImsiMobile record){
    	return imsiMobileDao.insertSelective(record);
    }

    public TImsiMobile selectByPrimaryKey(String imsi){
    	TImsiMobile tImsiMobile = null;
    	String redisKey = imsi;
    	String value = redisDao.get(KEY_CACHE_TIMSIMOBILE, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tImsiMobile = imsiMobileDao.selectByPrimaryKey(imsi);
    		if(tImsiMobile != null){
    			redisDao.put(KEY_CACHE_TIMSIMOBILE, redisKey, JsonUtils.bean2Json(tImsiMobile));
    		}
    	}else{
    		tImsiMobile = JsonUtils.json2Bean(value, TImsiMobile.class);
    	}
    	return tImsiMobile;
    }

    public int updateByPrimaryKeySelective(TImsiMobile record){
    	return imsiMobileDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TImsiMobile record){
    	return imsiMobileDao.updateByPrimaryKey(record);
    }
}