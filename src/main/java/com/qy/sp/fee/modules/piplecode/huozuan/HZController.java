package com.qy.sp.fee.modules.piplecode.huozuan;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/piple")
public class HZController {
	@Resource
	private HZService hzService;
	@RequestMapping(value = "/huozuan/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String spport,String mobile,String msg,String linkid,String cpparam,String mrtime,String fee ){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("spport",spport);
			bodyObject.put("linkid", linkid);
			bodyObject.put("mobile", mobile);
			bodyObject.put("msg", msg);
			bodyObject.put("cpparam",cpparam);
			bodyObject.put("mrtime", mrtime);
			bodyObject.put("fee", fee);
			result = hzService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
