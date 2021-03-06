package com.qy.sp.fee.modules.piplecode.ds;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class DSMMTController {

	@Resource
	private DSMMService dsmmService;
	@RequestMapping(value = "/dsmmt/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String resultMsg = "error";
		try{
//			JSONObject bodyObject = new JSONObject();
//			bodyObject.put("code", code);
//			bodyObject.put("msg", msg);
//			bodyObject.put("orderId", orderId);
//			bodyObject.put("mobile", mobile);
//			bodyObject.put("price",price);
//			bodyObject.put("chargeCode",chargeCode);
//			bodyObject.put("transmissionData",transmissionData);
			JSONObject requestObject = JSONObject.fromObject(requestBody);
			resultMsg = dsmmService.processPaySuccess(requestObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
