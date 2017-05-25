package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSdkConfig;
import com.qy.sp.fee.dto.TSdkConfigQueryKey;
@Component @MyBatisRepository
public interface SdkConfigDao {

	int deleteByPrimaryKey(String configId);

    int insert(TSdkConfig config);

    TSdkConfig selectByPrimaryKey(String configId);
    
    int updateByPrimaryKey(TSdkConfig config);
    
	public List<TSdkConfig> selectConfigurationsByConfigQueryKey(TSdkConfigQueryKey key);
	
}
