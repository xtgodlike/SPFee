package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.JsonUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.impl.BlobContentDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TBlobContent;

@Component
public class TBlobContentDao extends BaseDao{
	public static final String KEY_CACHE_TBLOBCONTENT= "KEY_CACHE_TBLOBCONTENT";
	@Resource
	private BlobContentDao blobContentDao;
	public int deleteByPrimaryKey(String fileId){
    	return blobContentDao.deleteByPrimaryKey(fileId);
    }

    public int insert(TBlobContent record){
    	return blobContentDao.insert(record);
    }

    public int insertSelective(TBlobContent record){
    	return blobContentDao.insertSelective(record);
    }

    public TBlobContent selectByPrimaryKey(String fileId){
    	TBlobContent tBlobContent = null;
    	String redisKey = fileId;
    	String value = redisDao.get(KEY_CACHE_TBLOBCONTENT, redisKey);
    	if(StringUtil.isEmpty(value)){
    		tBlobContent =  blobContentDao.selectByPrimaryKey(fileId);
    		if(tBlobContent != null){
    			redisDao.put(KEY_CACHE_TBLOBCONTENT, redisKey, JsonUtils.bean2Json(tBlobContent));
    		}
    	}else{
    		tBlobContent = JsonUtils.json2Bean(value, TBlobContent.class);
    	}
    	return tBlobContent;
    }

    public int updateByPrimaryKeySelective(TBlobContent record){
    	return blobContentDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKeyWithBLOBs(TBlobContent record){
    	return blobContentDao.updateByPrimaryKeyWithBLOBs(record);
    }

    public int updateByPrimaryKey(TBlobContent record){
    	return blobContentDao.updateByPrimaryKey(record);
    }
    
    public TBlobContent selectByName(String name){
    	return blobContentDao.selectByName(name);
    }
}