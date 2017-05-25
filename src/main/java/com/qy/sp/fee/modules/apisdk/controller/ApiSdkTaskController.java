package com.qy.sp.fee.modules.apisdk.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkPhoneService;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkTaskService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/api/sdk/task")
public class ApiSdkTaskController {

	@Resource
	private ApiSdkTaskService apiSdkTaskService;
	@Resource
	private ApiSdkPhoneService apiSdkPhoneService;
	@RequestMapping(value = "/load" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String loadTasks(@RequestHeader String imei,@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			body = apiSdkPhoneService.decode(imei,desKey, body);
			if(StringUtil.isEmpty(body))
				return result;
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = apiSdkTaskService.queryTasks(bodyObj);
			result =  apiSdkPhoneService.encode(imei, desKey, resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/download" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public void taskDownload(@RequestHeader String imei,@RequestHeader String desKey,@RequestBody String body,HttpServletResponse response){
		try {
			body = apiSdkPhoneService.decode(imei,desKey, body);
			if(StringUtil.isEmpty(body))
				return ;
			JSONObject bodyObj = JSONObject.fromObject(body);
			apiSdkTaskService.downloadTask(bodyObj, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(value = "/remove" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String taskRemove(@RequestHeader String imei,@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			body = apiSdkPhoneService.decode(imei,desKey, body);
			if(StringUtil.isEmpty(body))
				return result;
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = apiSdkTaskService.removeTask(bodyObj);
			result =  apiSdkPhoneService.encode(imei, desKey, resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
