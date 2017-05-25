package com.qy.sp.fee.dao.impl;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TPipleMobileLimit;

@Component @MyBatisRepository
public interface PipleMobileLimitDao {

	TPipleMobileLimit selectByPrimaryKey(TPipleMobileLimit key);

}