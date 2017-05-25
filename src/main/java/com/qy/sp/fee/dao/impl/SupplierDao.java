package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSupplier;

@Component @MyBatisRepository
public interface SupplierDao {
    int deleteByPrimaryKey(String supplierId);

    int insert(TSupplier record);

    int insertSelective(TSupplier record);

    TSupplier selectByPrimaryKey(String supplierId);

    int updateByPrimaryKeySelective(TSupplier record);

    int updateByPrimaryKey(TSupplier record);
}