package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSdkOperation;
@Component @MyBatisRepository
public interface SdkOperationDao {
	 int insert(TSdkOperation operation);
	 TSdkOperation selectByPrimaryKey(String operationId);
}
