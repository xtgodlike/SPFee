package com.qy.sp.fee.modules.piplecode.wc;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class WCXWoController {

	@Resource
	private WCXWoService wcxWoService;
	@RequestMapping(value = "/wcwoshop/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String imsi,String mobile,String productId,String price,String timestamp,
								 String exData,String orderId,String province,String resultCode){
		String resultMsg = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("imsi", imsi);
			bodyObject.put("mobile", mobile);
			bodyObject.put("productId", productId);
			bodyObject.put("price", price);
			bodyObject.put("timestamp",timestamp);
			bodyObject.put("exData",exData);
			bodyObject.put("orderId",orderId);
			bodyObject.put("province",province);
			bodyObject.put("resultCode",resultCode);

			resultMsg = wcxWoService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
