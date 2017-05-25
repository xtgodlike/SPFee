package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ChannelDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TChannel;


@Component
public class TChannelDao extends BaseDao{
	public static final String KEY_CACHE_TCHANNEL= "KEY_CACHE_TCHANNEL";
	public static final String KEY_CACHE_PREFIX_API= "API_";
	public static final String KEY_CACHE_PREFIX_ID= "ID_";
	@Resource
	private ChannelDao channelDao;
    public int deleteByPrimaryKey(String channelId){
    	return channelDao.deleteByPrimaryKey(channelId);
    }

    public int insert(TChannel record){
    	return channelDao.insert(record);
    }

    public int insertSelective(TChannel record){
    	return channelDao.insertSelective(record);
    }

    public TChannel selectByPrimaryKey(String channelId){
    	TChannel tChannel = null;
    	String redisKey = KEY_CACHE_PREFIX_ID+channelId;
    	String value = redisDao.get(KEY_CACHE_TCHANNEL, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tChannel =  channelDao.selectByPrimaryKey(channelId);
    		if(tChannel != null){
    			redisDao.put(KEY_CACHE_TCHANNEL, redisKey, JsonUtils.bean2Json(tChannel));
    		}
    	}else{
    		tChannel = JsonUtils.json2Bean(value, TChannel.class);
    	}
    	return tChannel;
    }

    public int updateByPrimaryKeySelective(TChannel record){
    	return channelDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TChannel record){
    	return channelDao.updateByPrimaryKey(record);
    }
    
    public TChannel selectByApiKey(String apiKey){
    	TChannel tChannel = null;
    	String redisKey = KEY_CACHE_PREFIX_API+apiKey;
    	String value = redisDao.get(KEY_CACHE_TCHANNEL, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tChannel =  channelDao.selectByApiKey(apiKey);
    		if(tChannel != null){
    			redisDao.put(KEY_CACHE_TCHANNEL, redisKey, JsonUtils.bean2Json(tChannel));
    		}
    	}else{
    		tChannel = JsonUtils.json2Bean(value, TChannel.class);
    	}
    	return tChannel;
    }
}