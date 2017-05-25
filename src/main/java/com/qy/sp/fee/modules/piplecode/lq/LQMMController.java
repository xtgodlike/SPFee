package com.qy.sp.fee.modules.piplecode.lq;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class LQMMController {

	@Resource
	private LQMMService lqmmService;
	@RequestMapping(value = "/lqmm/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String appid,String price,String state,String sn,String imsi,String tel,String cpparam){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("appid", appid);
			bodyObject.put("price", price);
			bodyObject.put("state", state);
			bodyObject.put("sn", sn);
			bodyObject.put("imsi",imsi);
			bodyObject.put("tel",tel);
			bodyObject.put("cpparam",cpparam);
			result = lqmmService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
