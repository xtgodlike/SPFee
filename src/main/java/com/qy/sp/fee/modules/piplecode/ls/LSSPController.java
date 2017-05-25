package com.qy.sp.fee.modules.piplecode.ls;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class LSSPController {

	@Resource
	private LSSPService lsspService;
	@RequestMapping(value = "/lssp/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String linkid,String mobile,String port,String msg,String status,String param ,String ftime ){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("linkid", linkid);
			bodyObject.put("mobile", mobile);
			bodyObject.put("port", port);
			bodyObject.put("msg", msg);
			bodyObject.put("status",status);
			bodyObject.put("param",param);
			bodyObject.put("ftime",ftime);
			result = lsspService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
