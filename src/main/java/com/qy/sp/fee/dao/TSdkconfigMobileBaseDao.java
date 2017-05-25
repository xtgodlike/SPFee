package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.SdkconfigMobileBaseDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkconfigMobileBase;

@Component
public class TSdkconfigMobileBaseDao extends BaseDao {
	public static final String KEY_CACHE_TSDKCONFIGMOBILEBASE = "KEY_CACHE_TSDKCONFIGMOBILEBASE";
	@Resource
	private SdkconfigMobileBaseDao sdkconfigMobileBaseDao;
	
    public int deleteByPrimaryKey(TSdkconfigMobileBase record){
    	int result = sdkconfigMobileBaseDao.deleteByPrimaryKey(record);
    	String redisKey  = record.getAppId()+record.getContentId()+record.getCpId()+record.getReleaseChannelId();
    	redisDao.remove(KEY_CACHE_TSDKCONFIGMOBILEBASE, redisKey);
    	return result;
    }

    public int insert(TSdkconfigMobileBase record){
    	return sdkconfigMobileBaseDao.insert(record);
    }

    public int insertSelective(TSdkconfigMobileBase record){
    	return sdkconfigMobileBaseDao.insertSelective(record);
    }

    public TSdkconfigMobileBase selectByPrimaryKey(TSdkconfigMobileBase record){
    	String redisKey  = record.getAppId()+record.getContentId()+record.getCpId()+record.getReleaseChannelId();
    	String value = redisDao.get(redisKey);
    	TSdkconfigMobileBase tSdkconfigMobileBase = null;
    	if(StringUtil.isEmpty(value)){
    		tSdkconfigMobileBase = sdkconfigMobileBaseDao.selectByPrimaryKey(record);
    		if(tSdkconfigMobileBase != null){
    			redisDao.put(KEY_CACHE_TSDKCONFIGMOBILEBASE, redisKey, JsonUtils.bean2Json(tSdkconfigMobileBase));
    		}
    	}else{
    		tSdkconfigMobileBase = JsonUtils.json2Bean(value, TSdkconfigMobileBase.class);
    	}
    	return tSdkconfigMobileBase;
    }
    
    public List<TSdkconfigMobileBase> selectSelective(TSdkconfigMobileBase record){
    	return sdkconfigMobileBaseDao.selectSelective(record);
    }

    public int updateByPrimaryKeySelective(TSdkconfigMobileBase record){
    	int result = sdkconfigMobileBaseDao.updateByPrimaryKeySelective(record);
    	String redisKey  = record.getAppId()+record.getContentId()+record.getCpId()+record.getReleaseChannelId();
    	redisDao.remove(KEY_CACHE_TSDKCONFIGMOBILEBASE, redisKey);
    	return result ;
    }

    public int updateByPrimaryKey(TSdkconfigMobileBase record){
    	int result = sdkconfigMobileBaseDao.updateByPrimaryKey(record);
    	String redisKey  = record.getAppId()+record.getContentId()+record.getCpId()+record.getReleaseChannelId();
    	redisDao.remove(KEY_CACHE_TSDKCONFIGMOBILEBASE, redisKey);
    	return result;
    }
}