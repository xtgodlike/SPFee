package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.HostDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.THost;

@Component
public class THostDao extends BaseDao{
	public static final String KEY_CACHE_THOST = "KEY_CACHE_THOST";
	@Resource
	private HostDao hostDao;
	
    public int deleteByPrimaryKey(Integer hostId){
    	return hostDao.deleteByPrimaryKey(hostId);
    }

    public int insert(THost record){
    	return hostDao.insert(record);
    }

    public int insertSelective(THost record){
    	return hostDao.insertSelective(record);
    }

    public THost selectByPrimaryKey(Integer hostId){
    	return hostDao.selectByPrimaryKey(hostId);
    }
    
    public THost selectByName(String hostName){
    	THost tHost = null;
    	String redisKey = hostName;
    	String value = redisDao.get(KEY_CACHE_THOST, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tHost = hostDao.selectByName(hostName);
    		if(tHost != null){
    			redisDao.put(KEY_CACHE_THOST, redisKey, JsonUtils.bean2Json(tHost));
    		}
    	}else{
    		tHost = JsonUtils.json2Bean(value, THost.class);
    	}
    	return tHost;
    }

    public int updateByPrimaryKeySelective(THost record){
    	return hostDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(THost record){
    	return hostDao.updateByPrimaryKey(record);
    }
}