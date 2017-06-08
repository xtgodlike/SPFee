package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.ClientProperty;
import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.PipleDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleApply;
import com.qy.sp.fee.dto.TPipleFilterKey;

@Component
public class TPipleDao extends BaseDao {
	public static final String KEY_CACHE_TPIPLE = "KEY_CACHE_TPIPLE";
	public static final String PREFIX_EXPIRE = "EXPIRE_PIPLE_";
	@Resource
	private PipleDao pipleDao;
    public int deleteByPrimaryKey(String pipleId){
    	return pipleDao.deleteByPrimaryKey(pipleId);
    }

    public int insert(TPiple record){
    	return pipleDao.insert(record);
    }

    public int insertSelective(TPiple record){
    	return pipleDao.insertSelective(record);
    }

    public TPiple selectByPrimaryKey(String pipleId){
    	TPiple tPiple = null;
    	String redisKey = pipleId;
    	String value = redisDao.get(KEY_CACHE_TPIPLE, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tPiple = pipleDao.selectByPrimaryKey(pipleId);
    		if(tPiple != null){
    			redisDao.put(KEY_CACHE_TPIPLE, redisKey, JsonUtils.bean2Json(tPiple));
    		}
    	}else{
    		tPiple = JsonUtils.json2Bean(value, TPiple.class);
    	}
    	return tPiple;
    }

	public TPiple selectByPipleKey(String pipleKey){
		TPiple tPiple = null;
		String redisKey = pipleKey;
		String value = redisDao.get(KEY_CACHE_TPIPLE, redisKey);
		if(StringUtil.isEmpty(value)){
			tPiple = pipleDao.selectByPipleKey(pipleKey);
			if(tPiple != null){
				redisDao.put(KEY_CACHE_TPIPLE, redisKey, JsonUtils.bean2Json(tPiple));
			}
		}else{
			tPiple = JsonUtils.json2Bean(value, TPiple.class);
		}
		return tPiple;
	}

    public int updateByPrimaryKeySelective(TPiple record){
    	return pipleDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TPiple record){
    	return pipleDao.updateByPrimaryKey(record);
    }
    
    public List<TPipleApply> selectPipleByFilterKey(TPipleFilterKey key){
    	return pipleDao.selectPipleByFilterKey(key);
    }
    public void deleteFrequentlyRequest(String mobile,String pipleId){
    	if(StringUtil.isNotEmptyString(mobile) && StringUtil.isNotEmptyString(pipleId)){
    		String redisKey = PREFIX_EXPIRE+pipleId+mobile;
    		redisDao.clear(redisKey);
    	}
    }
    public boolean filterFrequentlyRequest(String mobile,String pipleId){
    	String redisKey = PREFIX_EXPIRE+pipleId+mobile;
    	boolean hasKey = redisDao.contains(redisKey);
    	if(hasKey){
    		return true;
    	}else{
    		redisDao.put(redisKey, redisKey);
    		redisDao.expire(redisKey, NumberUtil.getLong(ClientProperty.getProperty("config","ORDER_FREQUENTLY_TIME")));
    	}
    	return false;
    }
    
}