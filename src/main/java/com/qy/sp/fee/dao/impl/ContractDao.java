package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TContract;

@Component @MyBatisRepository
public interface ContractDao {
    int deleteByPrimaryKey(String contractId);

    int insert(TContract record);

    int insertSelective(TContract record);

    TContract selectByPrimaryKey(String contractId);

    int updateByPrimaryKeySelective(TContract record);

    int updateByPrimaryKey(TContract record);
}