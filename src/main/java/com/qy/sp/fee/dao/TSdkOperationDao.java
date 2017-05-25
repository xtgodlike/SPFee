package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.SdkOperationDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkOperation;
@Component
public class TSdkOperationDao extends BaseDao{
	@Resource
	private SdkOperationDao sdkOperationDao;
	 public int insert(TSdkOperation operation){
		 return sdkOperationDao.insert(operation);
	 }
	 public TSdkOperation selectByPrimaryKey(String operationId){
		 return sdkOperationDao.selectByPrimaryKey(operationId);
	 }
}
