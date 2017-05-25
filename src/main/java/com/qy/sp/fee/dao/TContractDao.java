package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.ContractDao;
import com.qy.sp.fee.dto.TContract;

@Component
public class TContractDao {
	
	@Resource
	private ContractDao contractDao;
    public int deleteByPrimaryKey(String contractId){
    	return contractDao.deleteByPrimaryKey(contractId);
    }

    public int insert(TContract record){
    	return contractDao.insert(record);
    }

    public int insertSelective(TContract record){
    	return contractDao.insertSelective(record);
    }

    public TContract selectByPrimaryKey(String contractId){
    	return contractDao.selectByPrimaryKey(contractId);
    }

    public int updateByPrimaryKeySelective(TContract record){
    	return contractDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TContract record){
    	return contractDao.updateByPrimaryKey(record);
    }
}