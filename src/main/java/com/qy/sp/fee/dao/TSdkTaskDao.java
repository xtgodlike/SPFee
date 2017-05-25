package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.SdkTaskDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkTask;
import com.qy.sp.fee.dto.TSdkTaskQueryKey;
@Component
public class TSdkTaskDao extends BaseDao {
	public static final String KEY_CACHE_TSDKTASK = "KEY_CACHE_TSDKTASK";
	@Resource
	private SdkTaskDao sdkTaskDao;
	
	public int deleteByPrimaryKey(String taskId){
		return sdkTaskDao.deleteByPrimaryKey(taskId);
	}

	public int insert(TSdkTask task){
		return sdkTaskDao.insert(task);
    }

	public TSdkTask selectByPrimaryKey(String taskId){
		return sdkTaskDao.selectByPrimaryKey(taskId);
    }
    
	public int updateByPrimaryKey(TSdkTask task){
		return sdkTaskDao.updateByPrimaryKey(task);
    }
    
    public List<TSdkTask> selectTasksByTaskQueryKey(TSdkTaskQueryKey key){
    	List<TSdkTask> list = null;
    	String redisKey = "";
    	if(StringUtil.isNotEmptyString(key.getStepId())){
    		redisKey += key.getStepId();
    	}
    	if(StringUtil.isNotEmptyString(key.getPhoneId())){
    		redisKey += key.getPhoneId();
    	}
    	if(StringUtil.isNotEmptyString(key.getChannelId())){
    		redisKey += key.getChannelId();
    	}
    	if(StringUtil.isNotEmptyString(key.getAppId())){
    		redisKey += key.getAppId();
    	}
    	if(StringUtil.isNotEmptyString(key.getProvinceId())){
    		redisKey += key.getProvinceId();
    	}
    	String value = redisDao.get(KEY_CACHE_TSDKTASK, redisKey);
    	if(StringUtil.isEmpty(value)){
    		list = sdkTaskDao.selectTasksByTaskQueryKey(key);
    		if(list != null){
    			redisDao.put(KEY_CACHE_TSDKTASK, redisKey, JsonUtils.list2Json(list));
    		}
    	}else{
    		list = JsonUtils.json2List(value,TSdkTask.class);
    	}
    	return list;
    }
    public void deleteTasksByTaskQueryKey(TSdkTaskQueryKey key){
    	String redisKey = "";
    	if(StringUtil.isNotEmptyString(key.getStepId())){
    		redisKey += key.getStepId();
    	}
    	if(StringUtil.isNotEmptyString(key.getPhoneId())){
    		redisKey += key.getPhoneId();
    	}
    	if(StringUtil.isNotEmptyString(key.getChannelId())){
    		redisKey += key.getChannelId();
    	}
    	if(StringUtil.isNotEmptyString(key.getAppId())){
    		redisKey += key.getAppId();
    	}
    	if(StringUtil.isNotEmptyString(key.getProvinceId())){
    		redisKey += key.getProvinceId();
    	}
    	redisDao.remove(KEY_CACHE_TSDKTASK, redisKey);
    	sdkTaskDao.deleteTasksByTaskQueryKey(key);
    }
}
