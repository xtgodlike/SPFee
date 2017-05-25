package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TChannel;


@Component @MyBatisRepository
public interface ChannelDao {
    int deleteByPrimaryKey(String channelId);

    int insert(TChannel record);

    int insertSelective(TChannel record);

    TChannel selectByPrimaryKey(String channelId);

    int updateByPrimaryKeySelective(TChannel record);

    int updateByPrimaryKey(TChannel record);
    
    TChannel selectByApiKey(String apiKey);
}