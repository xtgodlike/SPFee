package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TChannelProvinceLimit;

@Component @MyBatisRepository
public interface ChannelProvinceLimitDao {

    TChannelProvinceLimit selectByPrimaryKey(TChannelProvinceLimit key);

}