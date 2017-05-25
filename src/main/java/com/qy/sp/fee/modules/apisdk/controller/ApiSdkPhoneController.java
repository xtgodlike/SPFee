package com.qy.sp.fee.modules.apisdk.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.tools.TaskExecutor;
import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkConfigService;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkLogService;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkOperationService;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkPhoneService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/api/sdk/phone")
public class ApiSdkPhoneController {

	@Resource
	private ApiSdkPhoneService apiSdkPhoneService;
	@Resource
	private ApiSdkConfigService apiSdkConfigService;
	@Resource
	private ApiSdkOperationService apiSdkOperationService;
	@Resource
	private ApiSdkLogService apiSdkLogService;
	@RequestMapping(value = "/configuration" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getPhoneConfigurations(@RequestHeader String imei,@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			body = apiSdkPhoneService.decode(imei,desKey, body);
			if(StringUtil.isEmpty(body))
				return result;
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = apiSdkConfigService.queryConfigurations(bodyObj);
			result =  apiSdkPhoneService.encode(imei, desKey, resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/statistics" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public void staticsUserOPeration(@RequestHeader final String imei,@RequestHeader final String desKey,@RequestBody final String body){
		TaskExecutor.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					String reqBody = apiSdkPhoneService.decode(imei,desKey, body);
					if(StringUtil.isEmpty(reqBody)){
						return ;
					}
					JSONObject bodyObj = JSONObject.fromObject(reqBody);
					apiSdkOperationService.staticsUserOperationCustom(bodyObj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	@RequestMapping(value = "/exception" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public void log(@RequestHeader final String imei,@RequestHeader final String desKey,@RequestBody final String body){
		TaskExecutor.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					String reqBody = apiSdkPhoneService.decode(imei,desKey, body);
					if(StringUtil.isEmpty(reqBody)){
						return ;
					}
					JSONObject bodyObj = JSONObject.fromObject(reqBody);
					apiSdkLogService.logSdkError(bodyObj);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}
	@RequestMapping(value = "/simcardcommond" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getSimcardCommond(@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			if(StringUtil.isEmpty(body))
				return result;
			DesUtil util = new DesUtil();
			util.setKey(desKey);
			body = util.Decode(body);
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = apiSdkPhoneService.querySimcardCommond(bodyObj);
			result =  util.Encode(resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	//http://pushMoUrl?receiver=admin&pswd=12345&moTime=1208212205&mobile=13800210021&msg=hello&destcode=10657109012345
	@RequestMapping(value = "/syncsimcard" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String syncSimcard(String moTime,String mobile,String msg){
		String result = "{}";
		try {
			final JSONObject requestObject = new JSONObject();
			requestObject.put("mobile", mobile);
			requestObject.put("msg", msg);
			TaskExecutor.getInstance().execute(new Runnable() {
				
				@Override
				public void run() {
					apiSdkPhoneService.reciiveSmsMessage(requestObject);
				}
			});
			return "0";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/simcarddispatcher" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String syncSimcarddispatcher(String moTime,String mobile,String msg){
		String result = "{}";
		try {
			final JSONObject requestObject = new JSONObject();
			requestObject.put("mobile", mobile);
			requestObject.put("msg", msg);
			TaskExecutor.getInstance().execute(new Runnable() {
				
				@Override
				public void run() {
					apiSdkPhoneService.dispatcherInSmsMessage(requestObject);
				}
			});
			return "0";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/simcard" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getSimcard(@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			if(StringUtil.isEmpty(body))
				return result;
			DesUtil util = new DesUtil();
			util.setKey(desKey);
			body = util.Decode(body);
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = apiSdkPhoneService.querySimcardInfo(bodyObj);
			result =  util.Encode(resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/queryimsi" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String querySimcard(String imsi){
		String result = "";
		try {
			result = apiSdkPhoneService.queryMobileByImsi(imsi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/uploadsimcard" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public void uploadSimcard(@RequestHeader String desKey,@RequestBody String body){
		try {
			DesUtil util = new DesUtil();
			util.setKey(desKey);
			body = util.Decode(body);
			JSONObject bodyObj = JSONObject.fromObject(body);
			apiSdkPhoneService.uploadSimcardInfo(bodyObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(value = "/mobileInfo" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String uploadMobileInfo(@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			if(StringUtil.isEmpty(body))
					return result;
			DesUtil util = new DesUtil();
			util.setKey(desKey);
			body = util.Decode(body);
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = apiSdkPhoneService.uploadMobilePhone(bodyObj);
			result =  util.Encode(resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
