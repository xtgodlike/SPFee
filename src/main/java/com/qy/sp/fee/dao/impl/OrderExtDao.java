package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TOrderExtKey;

@Component @MyBatisRepository
public interface OrderExtDao {
    int deleteByPrimaryKey(TOrderExtKey key);

    int insert(TOrderExt record);

    int insertSelective(TOrderExt record);

    TOrderExt selectByPrimaryKey(TOrderExtKey key);

    int updateByPrimaryKeySelective(TOrderExt record);

    int updateByPrimaryKey(TOrderExt record);
    
    List<TOrderExt> selectByOrderId(String orderId);
}