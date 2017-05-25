package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TChannelProduct;
import com.qy.sp.fee.dto.TChannelProductKey;

@Component @MyBatisRepository
public interface ChannelProductDao {
    int deleteByPrimaryKey(TChannelProductKey key);

    int insert(TChannelProduct record);

    int insertSelective(TChannelProduct record);

    TChannelProduct selectByPrimaryKey(TChannelProductKey key);

    int updateByPrimaryKeySelective(TChannelProduct record);

    int updateByPrimaryKey(TChannelProduct record);
}