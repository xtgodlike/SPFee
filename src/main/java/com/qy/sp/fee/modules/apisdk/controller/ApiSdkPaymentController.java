package com.qy.sp.fee.modules.apisdk.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.tools.TaskExecutor;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkOperationService;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkPaymentService;
import com.qy.sp.fee.modules.apisdk.service.ApiSdkPhoneService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/api/sdk/payment")
public class ApiSdkPaymentController {
	private Logger logger = LoggerFactory.getLogger(ApiSdkPaymentController.class);
	@Resource
	private ApiSdkPaymentService apiSdkPaymentService;
	@Resource
	private ApiSdkPhoneService apiSdkPhoneService;
	@Resource
	private ApiSdkOperationService apiSdkOperationService;
	@RequestMapping(value = "/apply" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String applyPayment(@RequestHeader String imei,@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try {
			body = apiSdkPhoneService.decode(imei,desKey, body);
			if(StringUtil.isEmpty(body))
				return result;
			JSONObject bodyObj = JSONObject.fromObject(body);
			logger.debug("applyPayment request:"+bodyObj.toString());
			JSONObject resultObj = apiSdkPaymentService.queryPayment(bodyObj);
			logger.debug("applyPayment response:"+resultObj.toString());
			result =  apiSdkPhoneService.encode(imei, desKey, resultObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/download" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public void downPayment(@RequestHeader String imei,@RequestHeader String desKey,@RequestBody String body,HttpServletResponse response){
		try {
			body = apiSdkPhoneService.decode(imei,desKey, body);
			if(StringUtil.isEmpty(body))
				return ;
			JSONObject bodyObj = JSONObject.fromObject(body);
			apiSdkPaymentService.downloadPayment(bodyObj,response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/statistics" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public void statistics(@RequestHeader final String imei,@RequestHeader final String desKey,@RequestBody final String body){
		TaskExecutor.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					String reqBody = apiSdkPhoneService.decode(imei,desKey, body);
					if(StringUtil.isEmpty(reqBody))
						return ;
					JSONObject bodyObj = JSONObject.fromObject(reqBody);
					apiSdkPaymentService.updateOrder(bodyObj);
					apiSdkOperationService.staticsUserOperation(bodyObj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
