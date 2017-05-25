package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSdkLog;
@Component @MyBatisRepository
public interface SdkLogDao {
    int deleteByPrimaryKey(String logId);

    int insert(TSdkLog record);

    int insertSelective(TSdkLog record);

    TSdkLog selectByPrimaryKey(String logId);

    int updateByPrimaryKeySelective(TSdkLog record);

    int updateByPrimaryKey(TSdkLog record);
}