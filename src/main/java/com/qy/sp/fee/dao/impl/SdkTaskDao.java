package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TSdkTask;
import com.qy.sp.fee.dto.TSdkTaskQueryKey;
@Component @MyBatisRepository
public interface SdkTaskDao {
	
	int deleteByPrimaryKey(String taskId);

    int insert(TSdkTask task);

    TSdkTask selectByPrimaryKey(String taskId);
    
    int updateByPrimaryKey(TSdkTask task);
    
    public List<TSdkTask> selectTasksByTaskQueryKey(TSdkTaskQueryKey key);
    public void deleteTasksByTaskQueryKey(TSdkTaskQueryKey key);
}
