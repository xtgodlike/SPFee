package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.THost;

@Component @MyBatisRepository
public interface HostDao {
    int deleteByPrimaryKey(Integer hostId);

    int insert(THost record);

    int insertSelective(THost record);

    THost selectByPrimaryKey(Integer hostId);
    
    THost selectByName(String hostName);

    int updateByPrimaryKeySelective(THost record);

    int updateByPrimaryKey(THost record);
}