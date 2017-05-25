package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSdkApp;

@Component @MyBatisRepository
public interface SdkAppDao {

    TSdkApp selectByPrimaryKey(String appId);
}