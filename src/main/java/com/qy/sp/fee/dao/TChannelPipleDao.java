package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ChannelPipleDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;

@Component
public class TChannelPipleDao extends BaseDao{
	public static final String KEY_CACHE_TCHANNELPIPLE= "KEY_CACHE_TCHANNELPIPLE";
	
	@Resource
	private ChannelPipleDao channelPipleDao;
    public int deleteByPrimaryKey(TChannelPipleKey key){
    	return channelPipleDao.deleteByPrimaryKey(key);
    }

    public int insert(TChannelPiple record){
    	return channelPipleDao.insert(record);
    }

    public int insertSelective(TChannelPiple record){
    	return channelPipleDao.insertSelective(record);
    }

    public TChannelPiple selectByPrimaryKey(TChannelPipleKey key){
    	TChannelPiple channelPiple = null;
    	String redisKey = key.getChannelId()+key.getPipleId();
    	String value = redisDao.get(KEY_CACHE_TCHANNELPIPLE, redisKey);
    	if(StringUtil.isEmpty(value)){
    		channelPiple =  channelPipleDao.selectByPrimaryKey(key);
    		if(channelPiple != null){
    			redisDao.put(KEY_CACHE_TCHANNELPIPLE, redisKey, JsonUtils.bean2Json(channelPiple));
    		}
    	}else{
    		channelPiple = JsonUtils.json2Bean(value, TChannelPiple.class);
    	}
    	return channelPiple;
    }

    public int updateByPrimaryKeySelective(TChannelPiple record){
    	return channelPipleDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TChannelPiple record){
    	return channelPipleDao.updateByPrimaryKey(record);
    }
    
    public List<TChannelPiple> getListByPipleId(String pipleId){
    	return channelPipleDao.getListByPipleId(pipleId);
    }
}