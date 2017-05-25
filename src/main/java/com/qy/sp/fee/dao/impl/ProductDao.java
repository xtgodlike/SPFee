package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TProduct;

@Component @MyBatisRepository
public interface ProductDao {
    int deleteByPrimaryKey(String productId);

    int insert(TProduct record);

    int insertSelective(TProduct record);

    TProduct selectByPrimaryKey(String productId);

    int updateByPrimaryKeySelective(TProduct record);

    int updateByPrimaryKey(TProduct record);
    
    TProduct selectByCode(String productCode);
    
    TProduct selectByPrice(int price);
}