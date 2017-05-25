package com.qy.sp.fee.modules.piplecode.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

public class ChannelManager {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String,ChannelService> channelServiceMap = new ConcurrentHashMap<String, ChannelService>();
	private Map<String,ChannelService> smsServiceMap = new ConcurrentHashMap<String, ChannelService>();
	public static final String PREFIX_MSG = "\\$";
	
	private static ChannelManager instance = new ChannelManager();
	private ChannelManager() {
	}
	public static ChannelManager getInstance(){
		return instance;
	}
	public void putChannelService(String pipleId,ChannelService service){
		channelServiceMap.put(pipleId,service);
	}
	public ChannelService getChannelService(String pipleId){
		return channelServiceMap.get(pipleId);
	}
	public void removeChannelService(String pipleId){
		channelServiceMap.remove(pipleId);
	}
	public JSONObject fireGetSMS(JSONObject requestObject) throws Exception{
		String pipleId = requestObject.optString("pipleId");
		if(channelServiceMap.containsKey(pipleId)){
			ChannelService service = channelServiceMap.get(pipleId);
			return service.processGetSMS(requestObject);
		}
		return getErrorResult();
	}
	public JSONObject fireVertifySMS(JSONObject requestObject) throws Exception{
		String pipleId = requestObject.optString("pipleId");
		if(channelServiceMap.containsKey(pipleId)){
			ChannelService service = channelServiceMap.get(pipleId);
			return service.processVertifySMS(requestObject);
		}
		return getErrorResult();
	}
	private JSONObject getErrorResult(){
		return new JSONObject();
	}
	
	public void putSmsService(String pipleKey,ChannelService service){
		smsServiceMap.put(pipleKey,service);
	}
	public void removeSmsService(String pipleKey){
		smsServiceMap.remove(pipleKey);
	}
	//001$1$PW1006$1003$5467
	public String fireGetMessage(JSONObject requestBody) throws Exception{
		String result = "";
		String mobile = requestBody.optString("mobile");
		String msg = requestBody.optString("msg");
		if(StringUtil.isEmpty(msg)){
			return null;
		}
		String args[] = msg.split(PREFIX_MSG);
		String pipleKey = args[0];
		ChannelService service = smsServiceMap.get(pipleKey);
		if(service != null){
			String body = msg.substring(pipleKey.length()+1);
			logger.info("fireGetMessage"+mobile+","+body);
			result =  service.processGetMessage(mobile,body);
		}
		return result;
	}
	public String fireSubmitMessage(JSONObject requestBody) throws Exception{
		String result = "";
		String mobile = requestBody.optString("mobile");
		String msg = requestBody.optString("msg");
		if(StringUtil.isEmpty(msg)){
			return null;
		}
		String args[] = msg.split(PREFIX_MSG);
		String pipleKey = args[0];
		ChannelService service = smsServiceMap.get(pipleKey);
		if(service != null){
			String body = msg.substring(pipleKey.length()+1);
			result =  service.processSubmitMessage(mobile,body);
		}
		return result;
	}
}
