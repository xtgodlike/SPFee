package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.OrderExtDao;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TOrderExtKey;

@Component
public class TOrderExtDao {
	@Resource
	private OrderExtDao orderExtDao;
    public int deleteByPrimaryKey(TOrderExtKey key){
    	return orderExtDao.deleteByPrimaryKey(key);
    }

    public int insert(TOrderExt record){
    	return orderExtDao.insert(record);
    }

    public int insertSelective(TOrderExt record){
    	return orderExtDao.insertSelective(record);
    }

    public TOrderExt selectByPrimaryKey(TOrderExtKey key){
    	return orderExtDao.selectByPrimaryKey(key);
    }

    public int updateByPrimaryKeySelective(TOrderExt record){
    	return orderExtDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TOrderExt record){
    	return orderExtDao.updateByPrimaryKey(record);
    }
    
    public List<TOrderExt> selectByOrderId(String orderId){
    	return orderExtDao.selectByOrderId(orderId);
    }
}