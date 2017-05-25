package com.qy.sp.fee.modules.piplecode.ystx;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class LTKDController {

	@Resource
	private LTKDService ltkdService;
	@RequestMapping(value = "/ltkd/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String upport, String order,String mobile,String linkId,String state){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			
			requestObj.put("upport", upport);
			requestObj.put("order", order);
			requestObj.put("mobile", mobile);
			requestObj.put("linkId", linkId);
			requestObj.put("state", state);
			result = ltkdService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
