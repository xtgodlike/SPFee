package com.qy.sp.fee.modules.piplecode.qianya;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class RHWoController {

	@Resource
	private RHWoService rhWoService;
	@RequestMapping(value = "/rhwo/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String orderId, String cpId,String goodsCode,String channelCode,String cpOrderId,
								 String timestamp, String scope,String orderType,String status,String sign){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			
			requestObj.put("orderId", orderId);
			requestObj.put("cpId", cpId);
			requestObj.put("goodsCode", goodsCode);
			requestObj.put("channelCode", channelCode);
			requestObj.put("cpOrderId", cpOrderId);
			requestObj.put("timestamp", timestamp);
			requestObj.put("scope", scope);
			requestObj.put("orderType", orderType);
			requestObj.put("status", status);
			requestObj.put("sign", sign);
			result = rhWoService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
