package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.PipleProductDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;

@Component
public class TPipleProductDao extends BaseDao {
	public static final String KEY_CACHE_TPIPLEPRODUCT = "KEY_CACHE_TPIPLEPRODUCT";
	@Resource
	private PipleProductDao pipleProductDao;
	
    public int deleteByPrimaryKey(TPipleProductKey key){
    	return pipleProductDao.deleteByPrimaryKey(key);
    }

    public int insert(TPipleProduct record){
    	return pipleProductDao.insert(record);
    }

    public int insertSelective(TPipleProduct record){
    	return pipleProductDao.insertSelective(record);
    }

    public TPipleProduct selectByPrimaryKey(TPipleProductKey key){
    	TPipleProduct tPipleProduct = null;
    	String redisKey = key.getPipleId()+key.getProductId();
    	String value = redisDao.get(KEY_CACHE_TPIPLEPRODUCT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tPipleProduct = pipleProductDao.selectByPrimaryKey(key);
    		if(tPipleProduct != null){
    			redisDao.put(KEY_CACHE_TPIPLEPRODUCT, redisKey, JsonUtils.bean2Json(tPipleProduct));
    		}
    	}else{
    		tPipleProduct = JsonUtils.json2Bean(value, TPipleProduct.class);
    	}
    	return tPipleProduct;
    }

    public int updateByPrimaryKeySelective(TPipleProduct record){
    	return pipleProductDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TPipleProduct record){
    	return pipleProductDao.updateByPrimaryKey(record);
    }
    
    public TPipleProduct selectByPipleProductCode(TPipleProduct pipleProduct){
    	return pipleProductDao.selectByPipleProductCode(pipleProduct);
    }
}