package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ChannelProductDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TChannelProduct;
import com.qy.sp.fee.dto.TChannelProductKey;

@Component
public class TChannelProductDao extends BaseDao{
	public static final String KEY_CACHE_TCHANNELPRODUCT = "KEY_CACHE_TCHANNELPRODUCT";
	@Resource
	private ChannelProductDao channelProductDao;
	
	
    public int deleteByPrimaryKey(TChannelProductKey key){
    	return channelProductDao.deleteByPrimaryKey(key);
    }

    public int insert(TChannelProduct record){
    	return channelProductDao.insert(record);
    }

    public int insertSelective(TChannelProduct record){
    	return channelProductDao.insertSelective(record);
    }

    public TChannelProduct selectByPrimaryKey(TChannelProductKey key){
    	TChannelProduct tChannelProduct = null;
    	String redisKey = key.getChannelId()+key.getProductId();
    	String value = redisDao.get(KEY_CACHE_TCHANNELPRODUCT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tChannelProduct = channelProductDao.selectByPrimaryKey(key);
    		if(tChannelProduct != null){
    			redisDao.put(KEY_CACHE_TCHANNELPRODUCT, redisKey, JsonUtils.bean2Json(tChannelProduct));
    		}
    	}else{
    		tChannelProduct = JsonUtils.json2Bean(value, TChannelProduct.class);
    	}
    	return tChannelProduct;
    }

    public int updateByPrimaryKeySelective(TChannelProduct record){
    	return channelProductDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TChannelProduct record){
    	return channelProductDao.updateByPrimaryKey(record);
    }
}