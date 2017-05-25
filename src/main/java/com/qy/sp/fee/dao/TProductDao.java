package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ProductDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TProduct;

@Component
public class TProductDao extends BaseDao{
	public static final String KEY_CACHE_TPRODUCT = "KEY_CACHE_TPRODUCT";
	@Resource
	private ProductDao productDao;
	
    public int deleteByPrimaryKey(String productId){
    	return productDao.deleteByPrimaryKey(productId);
    }

    public int insert(TProduct record){
    	return productDao.insert(record);
    }

    public int insertSelective(TProduct record){
    	return productDao.insertSelective(record);
    }

    public TProduct selectByPrimaryKey(String productId){
    	TProduct tProduct = null;
    	String redisKey = productId;
    	String value = redisDao.get(KEY_CACHE_TPRODUCT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tProduct = productDao.selectByPrimaryKey(productId);
    		if(tProduct != null){
    			redisDao.put(KEY_CACHE_TPRODUCT, redisKey, JsonUtils.bean2Json(tProduct));
    		}
    	}else{
    		tProduct = JsonUtils.json2Bean(value, TProduct.class);
    	}
    	return tProduct;
    }

    public int updateByPrimaryKeySelective(TProduct record){
    	return productDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TProduct record){
    	return productDao.updateByPrimaryKey(record);
    }
    
    public TProduct selectByCode(String productCode){
    	TProduct tProduct = null;
    	String redisKey = productCode;
    	String value = redisDao.get(KEY_CACHE_TPRODUCT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tProduct = productDao.selectByCode(productCode);
    		if(tProduct != null){
    			redisDao.put(KEY_CACHE_TPRODUCT, redisKey, JsonUtils.bean2Json(tProduct));
    		}
    	}else{
    		tProduct = JsonUtils.json2Bean(value, TProduct.class);
    	}
    	return tProduct;
    }
    
    public TProduct selectByPrice(int price){
    	return productDao.selectByPrice(price);
    }
}