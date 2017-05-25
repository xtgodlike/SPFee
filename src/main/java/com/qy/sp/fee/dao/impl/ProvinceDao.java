package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TProvince;

@Component @MyBatisRepository
public interface ProvinceDao {
    int deleteByPrimaryKey(Integer provinceId);

    int insert(TProvince record);

    int insertSelective(TProvince record);

    TProvince selectByPrimaryKey(Integer provinceId);
    
    TProvince selectByProvinceName(String provinceName);

    int updateByPrimaryKeySelective(TProvince record);

    int updateByPrimaryKey(TProvince record);
}