package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TPipleProvince;
import com.qy.sp.fee.dto.TPipleProvinceKey;

@Component @MyBatisRepository
public interface PipleProvinceDao {
    int deleteByPrimaryKey(TPipleProvinceKey key);

    int insert(TPipleProvince record);

    int insertSelective(TPipleProvince record);

    TPipleProvince selectByPrimaryKey(TPipleProvinceKey key);

    int updateByPrimaryKeySelective(TPipleProvince record);

    int updateByPrimaryKey(TPipleProvince record);
}