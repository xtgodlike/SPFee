package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSdkPhone;
@Component @MyBatisRepository
public interface SdkPhoneDao {
	int insertSelective(TSdkPhone phone);

	TSdkPhone selectByPrimaryKey(String phoneId);

    int updateByPrimaryKeySelective(TSdkPhone phone);
}
