package com.qy.sp.fee.modules.piplecode.kongmeng;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class PaoPaoLongController {

	@Resource
	private PaoPaoLongService paoPaoLongService;
	@RequestMapping(value = "/paopao/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String status, String linkid,String spcode,String mobile,String mo){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			requestObj.put("status", status);
			requestObj.put("mo", mo);
			requestObj.put("mobile", mobile);
			requestObj.put("spcode", spcode);
			requestObj.put("linkid", linkid);
			result = paoPaoLongService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
