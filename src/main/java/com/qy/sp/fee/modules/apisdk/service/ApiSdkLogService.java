package com.qy.sp.fee.modules.apisdk.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.dao.TSdkLogDao;
import com.qy.sp.fee.dto.TSdkLog;

import net.sf.json.JSONObject;

@Service
public class ApiSdkLogService {
	@Resource
	private TSdkLogDao  tSdkLogDao;
	public void logSdkError(JSONObject jsonObject){
		String imei = jsonObject.optString("imei");
		String level = jsonObject.optString("level");
		String appId = jsonObject.optString("appId");
		String channelId = jsonObject.optString("channelId");
		String exception = jsonObject.optString("exception");
		TSdkLog log = new TSdkLog();
		log.setChannelId(channelId);
		log.setLevel(level);
		log.setImei(imei);
		log.setAppId(appId);
		log.setLogContent(exception);
		log.setLogId(KeyHelper.createID());
		log.setLogTime(new Date());
		tSdkLogDao.insert(log);
	}

}
