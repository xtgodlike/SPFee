package com.qy.sp.fee.modules.piplecode.mp;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/piple")
public class MPController {
	@Resource
	private MPService mpService;
	@RequestMapping(value = "/mp/sync" ,produces = {"application/json;charset=UTF-8"})
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
			result = mpService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
//	@RequestMapping(value = "/mp/debook",produces ={"application/json;charset=UTF-8"})
//	@ResponseBody
//	public String debookRequest(String linkid,String mobile,String port,String msg,String status,String param ,String subtime,String unsubtime){
//		String result = "error";
//		try{
//			JSONObject bodyObject = new JSONObject();
//			bodyObject.put("linkid", linkid);
//			bodyObject.put("mobile", mobile);
//			bodyObject.put("port", port);
//			bodyObject.put("msg", msg);
//			bodyObject.put("status",status);
//			bodyObject.put("param",param);
//			bodyObject.put("subtime", subtime);
//			bodyObject.put("unsubtime", unsubtime);
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return result;
//	}
}
