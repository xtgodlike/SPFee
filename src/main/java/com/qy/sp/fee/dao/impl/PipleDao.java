package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleApply;
import com.qy.sp.fee.dto.TPipleFilterKey;

@Component @MyBatisRepository
public interface PipleDao {
    int deleteByPrimaryKey(String pipleId);

    int insert(TPiple record);

    int insertSelective(TPiple record);

    TPiple selectByPrimaryKey(String pipleId);

    int updateByPrimaryKeySelective(TPiple record);

    int updateByPrimaryKey(TPiple record);
    
    List<TPipleApply> selectPipleByFilterKey(TPipleFilterKey key);
    
}