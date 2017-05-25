package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.GlobalDao;
import com.qy.sp.fee.dto.TGlobal;

@Component
public class TGlobalDao {
	@Resource
	private GlobalDao globalDao;
    public int deleteByPrimaryKey(Integer globalId){
    	return globalDao.deleteByPrimaryKey(globalId);
    }

    public int insert(TGlobal record){
    	return globalDao.insert(record);
    }

    public int insertSelective(TGlobal record){
    	return globalDao.insertSelective(record);
    }

    public TGlobal selectByPrimaryKey(Integer globalId){
    	return globalDao.selectByPrimaryKey(globalId);
    }

    public int updateByPrimaryKeySelective(TGlobal record){
    	return globalDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TGlobal record){
    	return globalDao.updateByPrimaryKey(record);
    }
}