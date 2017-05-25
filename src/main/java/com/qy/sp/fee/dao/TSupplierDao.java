package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.SupplierDao;
import com.qy.sp.fee.dto.TSupplier;

@Component
public class TSupplierDao {
	@Resource
	private SupplierDao supplierDao;
    public int deleteByPrimaryKey(String supplierId){
    	return supplierDao.deleteByPrimaryKey(supplierId);
    }

    public int insert(TSupplier record){
    	return supplierDao.insert(record);
    }

    public int insertSelective(TSupplier record){
    	return supplierDao.insertSelective(record);
    }

    public TSupplier selectByPrimaryKey(String supplierId){
    	return supplierDao.selectByPrimaryKey(supplierId);
    }

    public int updateByPrimaryKeySelective(TSupplier record){
    	return supplierDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TSupplier record){
    	return supplierDao.updateByPrimaryKey(record);
    }
}