package com.qy.sp.fee.modules.piplecode.zt;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class ZTVideoController {

	@Resource
	private ZTVideoService ztVideoService;
	@RequestMapping(value = "/ztv/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String channel,String flag,String billid,String status,String imsi,String amount,String cpparam,String mobile){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("channel", channel);
			bodyObject.put("flag", flag);
			bodyObject.put("billid", billid);
			bodyObject.put("status", status);
			bodyObject.put("imsi",imsi);
			bodyObject.put("amount",amount);
			bodyObject.put("cpparam",cpparam);
			bodyObject.put("mobile",mobile);
			result = ztVideoService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
