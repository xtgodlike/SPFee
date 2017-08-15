package com.qy.sp.fee.modules.piplecode.lh;

import com.qy.sp.fee.modules.piplecode.hz.HZVideoService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class LHHYController {

	@Resource
	private LHHYService lhhyService;
	@RequestMapping(value = "/lhhy/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String code,String msg,String orderId,String mobile,String price,String chargeCode,String transmissionData){
		String resultMsg = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("code", code);
			bodyObject.put("msg", msg);
			bodyObject.put("orderId", orderId);
			bodyObject.put("mobile", mobile);
			bodyObject.put("price",price);
			bodyObject.put("chargeCode",chargeCode);
			bodyObject.put("transmissionData",transmissionData);
			resultMsg = lhhyService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
