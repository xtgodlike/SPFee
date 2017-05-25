package com.qy.sp.fee.modules.piplecode.qianya;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class QianyaWoAppController {

	@Resource
	private QianyaWoAppService qianyaWoAppService;
	@RequestMapping(value = "/qianyawoapp/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String Timespan,String Mobile,String ProductCode, String Thefee, String TheOutTradeNo, String OrderStatus, String ApiKey, String ExpansionData){
		String result = "0";
		return result;
	}
	@RequestMapping(value = "/qianyawoapp/getappid" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getAppId(@RequestHeader final String imei,@RequestHeader final String desKey,@RequestBody final String body ){
		String result= "{}";
		
		
		try {
			String reqBody = decode(imei,desKey, body);
			if(StringUtil.isEmpty(reqBody)){
				return result ;
			}
			JSONObject bodyObj = JSONObject.fromObject(reqBody);
			
			return qianyaWoAppService.getAppId(bodyObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public String decode(String imei,String desKey,String requestBody) throws Exception{
		DesUtil util = new DesUtil();
		util.setKey(desKey);
		String decodeStr = util.Decode(requestBody);
		return decodeStr;
	}
	@RequestMapping(value = "/qianyawoapp/test" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequestTest(String Timespan,String Mobile,String ProductCode, String Thefee, String TheOutTradeNo, String OrderStatus, String ApiKey, String ExpansionData){
		try {
			JSONObject object = new JSONObject();
			object.put("pipleOrderId", TheOutTradeNo);
			object.put("status", OrderStatus);
			object.put("mobile", Mobile);
			qianyaWoAppService.processPaySuccess(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0";
	}
	
	
}
