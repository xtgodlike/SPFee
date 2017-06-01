package com.qy.sp.fee.modules.piplecode.qianya;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class QYVACController {

	@Resource
	private QYVACService qyvacService;
	@RequestMapping(value = "/qyvac/callback" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String orderId, String pipleId,String apiKey,String productCode,String status,
								 String mobile, String imsi,String amount,String extData){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			
			requestObj.put("orderId", orderId);
			requestObj.put("pipleId", pipleId);
			requestObj.put("apiKey", apiKey);
			requestObj.put("productCode", productCode);
			requestObj.put("status", status);
			requestObj.put("mobile", mobile);
			requestObj.put("imsi", imsi);
			requestObj.put("amount", amount);
			requestObj.put("extData", extData);
			result = qyvacService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
