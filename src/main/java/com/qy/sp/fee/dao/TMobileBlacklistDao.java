package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.MobileBlacklistDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TMobileBlacklist;

@Component
public class TMobileBlacklistDao extends BaseDao{
	public static final String KEY_CACHE_TMOBILEBLACKLIST = "KEY_CACHE_TMOBILEBLACKLIST";
	
	@Resource
	private MobileBlacklistDao mobileBlacklistDao;
	
	
    public int deleteByPrimaryKey(String mobile){
    	return mobileBlacklistDao.deleteByPrimaryKey(mobile);
    }

    public int insert(TMobileBlacklist record){
    	return mobileBlacklistDao.insert(record);
    }

    public int insertSelective(TMobileBlacklist record){
    	return mobileBlacklistDao.insertSelective(record);
    }

    public TMobileBlacklist selectByPrimaryKey(String mobile){
    	TMobileBlacklist tMobileBlacklist = null;
    	String redisKey = mobile;
    	String value = redisDao.get(KEY_CACHE_TMOBILEBLACKLIST, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tMobileBlacklist = mobileBlacklistDao.selectByPrimaryKey(mobile);
    		redisDao.put(KEY_CACHE_TMOBILEBLACKLIST, redisKey, JsonUtils.bean2Json(tMobileBlacklist));
    	}else{
    		tMobileBlacklist = JsonUtils.json2Bean(value, TMobileBlacklist.class);
    	}
    	return tMobileBlacklist;
    }

    public int updateByPrimaryKeySelective(TMobileBlacklist record){
    	return mobileBlacklistDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TMobileBlacklist record){
    	return mobileBlacklistDao.updateByPrimaryKey(record);
    }
}