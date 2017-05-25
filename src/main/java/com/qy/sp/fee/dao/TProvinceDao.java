package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.ProvinceDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TProvince;

@Component
public class TProvinceDao extends BaseDao{
	public static final String KEY_CACHE_TPROVINCE = "KEY_CACHE_TPROVINCE";
	@Resource
	private ProvinceDao provinceDao;
	public int deleteByPrimaryKey(Integer provinceId){
    	return provinceDao.deleteByPrimaryKey(provinceId);
    }

	public int insert(TProvince record){
		return provinceDao.insert(record);
    }

	public int insertSelective(TProvince record){
		return provinceDao.insertSelective(record);
    }

	public TProvince selectByPrimaryKey(Integer provinceId){
		return provinceDao.selectByPrimaryKey(provinceId);
    }
    
	public TProvince selectByProvinceName(String provinceName){
		TProvince tProvince = null;
    	String redisKey = provinceName;
    	String value = redisDao.get(KEY_CACHE_TPROVINCE, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tProvince = provinceDao.selectByProvinceName(provinceName);
    		if(tProvince != null){
    			redisDao.put(KEY_CACHE_TPROVINCE, redisKey, JsonUtils.bean2Json(tProvince));
    		}
    	}else{
    		tProvince = JsonUtils.json2Bean(value, TProvince.class);
    	}
    	return tProvince;
    }

	public int updateByPrimaryKeySelective(TProvince record){
		return provinceDao.updateByPrimaryKeySelective(record);
    }

	public int updateByPrimaryKey(TProvince record){
		return provinceDao.updateByPrimaryKey(record);
    }
}