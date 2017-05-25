package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.PipleProvinceDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TPipleProvince;
import com.qy.sp.fee.dto.TPipleProvinceKey;

@Component
public class TPipleProvinceDao extends BaseDao {
	public static final String KEY_CACHE_TPIPLEPROVINCE = "KEY_CACHE_TPIPLEPROVINCE";
	@Resource
	private PipleProvinceDao pipleProvinceDao;
	
    public int deleteByPrimaryKey(TPipleProvinceKey key){
    	return pipleProvinceDao.deleteByPrimaryKey(key);
    }

    public int insert(TPipleProvince record){
    	return pipleProvinceDao.insert(record);
    }

    public int insertSelective(TPipleProvince record){
    	return pipleProvinceDao.insertSelective(record);
    }

    public TPipleProvince selectByPrimaryKey(TPipleProvinceKey key){
    	TPipleProvince tPipleProvince = null;
    	String redisKey = key.getPipleId()+key.getProvinceId();
    	String value = redisDao.get(KEY_CACHE_TPIPLEPROVINCE, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tPipleProvince = pipleProvinceDao.selectByPrimaryKey(key);
    		if(tPipleProvince != null){
    			redisDao.put(KEY_CACHE_TPIPLEPROVINCE, redisKey, JsonUtils.bean2Json(tPipleProvince));
    		}
    	}else{
    		tPipleProvince = JsonUtils.json2Bean(value, TPipleProvince.class);
    	}
    	return tPipleProvince;
    }

    public int updateByPrimaryKeySelective(TPipleProvince record){
    	return pipleProvinceDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TPipleProvince record){
    	return pipleProvinceDao.updateByPrimaryKey(record);
    }
}