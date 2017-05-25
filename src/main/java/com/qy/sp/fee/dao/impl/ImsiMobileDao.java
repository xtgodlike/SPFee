package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TImsiMobile;

@Component @MyBatisRepository
public interface ImsiMobileDao {
    int deleteByPrimaryKey(String imsi);

    int insert(TImsiMobile record);

    int insertSelective(TImsiMobile record);

    TImsiMobile selectByPrimaryKey(String imsi);

    int updateByPrimaryKeySelective(TImsiMobile record);

    int updateByPrimaryKey(TImsiMobile record);
}