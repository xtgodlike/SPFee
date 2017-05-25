package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;

@Component @MyBatisRepository
public interface PipleProductDao {
    int deleteByPrimaryKey(TPipleProductKey key);

    int insert(TPipleProduct record);

    int insertSelective(TPipleProduct record);

    TPipleProduct selectByPrimaryKey(TPipleProductKey key);

    int updateByPrimaryKeySelective(TPipleProduct record);

    int updateByPrimaryKey(TPipleProduct record);
    
    TPipleProduct selectByPipleProductCode(TPipleProduct pipleProduct);
}