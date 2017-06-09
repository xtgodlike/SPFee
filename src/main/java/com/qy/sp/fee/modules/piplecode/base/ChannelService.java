package com.qy.sp.fee.modules.piplecode.base;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.service.BaseService;

import net.sf.json.JSONObject;

public abstract class ChannelService extends BaseService {

	protected Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	public ChannelService() {
	}

	public JSONObject processGetSMS(JSONObject requestBody)  throws Exception{
		return null;
	}
	public String getPaySuccessHttpDataType(){
		return GlobalConst.GetDataHttpType.HTTP_GET;
	}
	public String processPaySuccess(JSONObject requestBody)  throws Exception{
		return null;
	}

	public JSONObject processVertifySMS(JSONObject requestBody){
		return null;
	}

	public String getPipleId() {
		return null;
	}

	@PostConstruct
	public void init() {
		if(StringUtil.isNotEmptyString(getPipleId())){
			ChannelManager.getInstance().putChannelService(getPipleId(), this);
		}
		if(StringUtil.isNotEmptyString(getPipleKey())){
			ChannelManager.getInstance().putSmsService(getPipleKey(), this);
			ChannelManager.getInstance().putChannelService(getPipleKey(), this);
		}
	}
	
	public String getPipleKey(){
		return null;
	}
	public String processGetMessage(String mobile,String requestBody)  throws Exception{
		return "";
	}
	public String processSubmitMessage(String mobile,String requestBody)  throws Exception{
		return "";
	}
}
