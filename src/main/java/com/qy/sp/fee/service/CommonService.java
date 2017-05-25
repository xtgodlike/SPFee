package com.qy.sp.fee.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.CommonServiceUtil;
import com.qy.sp.fee.dao.impl.base.RedisDao;

@Service
public class CommonService extends BaseService{

	@Resource
	protected RedisDao redisDao;
	
	@PostConstruct
	public void init() {
		CommonServiceUtil.setCommonService(this);
	}

	public RedisDao getRedisDao() {
		return redisDao;
	}
}
