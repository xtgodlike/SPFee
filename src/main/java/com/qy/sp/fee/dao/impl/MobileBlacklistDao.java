package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TMobileBlacklist;

@Component @MyBatisRepository
public interface MobileBlacklistDao {
    int deleteByPrimaryKey(String mobile);

    int insert(TMobileBlacklist record);

    int insertSelective(TMobileBlacklist record);

    TMobileBlacklist selectByPrimaryKey(String mobile);

    int updateByPrimaryKeySelective(TMobileBlacklist record);

    int updateByPrimaryKey(TMobileBlacklist record);
}