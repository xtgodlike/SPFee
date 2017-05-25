package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TLocationKey;

@Component @MyBatisRepository
public interface LocationDao {
    int deleteByPrimaryKey(TLocationKey key);

    int insert(TLocation record);

    int insertSelective(TLocation record);

    TLocation selectByPrimaryKey(TLocationKey key);
    
    TLocation selectBySegment(String segment);

    int updateByPrimaryKeySelective(TLocation record);

    int updateByPrimaryKey(TLocation record);
    
    int deleteBatch(List<TLocation> locations);
    
    int insertBatch(List<TLocation> locations);
}