package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.LocationDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TLocationKey;

@Component
public class TLocationDao extends BaseDao{
	public static final String KEY_CACHE_TLOCATION = "KEY_CACHE_TLOCATION";
	@Resource
	private LocationDao locationDao;
	public int deleteByPrimaryKey(TLocationKey key){
    	return locationDao.deleteByPrimaryKey(key);
    }

    public int insert(TLocation record){
    	return locationDao.insert(record);
    }

    public int insertSelective(TLocation record){
    	return locationDao.insertSelective(record);
    }

    public TLocation selectByPrimaryKey(TLocationKey key){
    	return locationDao.selectByPrimaryKey(key);
    }
    
    public TLocation selectBySegment(String segment){
    	TLocation tLocation = null;
    	String value = redisDao.get(KEY_CACHE_TLOCATION, segment);
    	if(StringUtil.isEmpty(value)){
    		tLocation =  locationDao.selectBySegment(segment);
    		if(tLocation != null){
    			redisDao.put(KEY_CACHE_TLOCATION, segment, JsonUtils.bean2Json(tLocation));
    		}
    	}else{
    		tLocation = JsonUtils.json2Bean(value, TLocation.class);
    	}
    	return tLocation;
    }

    public int updateByPrimaryKeySelective(TLocation record){
    	return locationDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TLocation record){
    	return locationDao.updateByPrimaryKey(record);
    }
    
    public int deleteBatch(List<TLocation> locations){
    	return locationDao.deleteBatch(locations);
    }
    
    public int insertBatch(List<TLocation> locations){
    	return locationDao.insertBatch(locations);
    }
}