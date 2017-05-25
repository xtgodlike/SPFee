package com.qy.sp.fee.modules.piplecode.ct;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class CTSWController {
	@Resource
	private CTSWService ctswService;
	@RequestMapping(value = "/ct/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String linkid,String type,String merchantid,String code,String mr_amount,String province,String status,String cpparam,String ext1){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("linkid", linkid);
			bodyObject.put("type", type);
			bodyObject.put("merchantid", merchantid);
			bodyObject.put("code", code);
			bodyObject.put("status", status);
			bodyObject.put("mr_amount",mr_amount);
			bodyObject.put("province",province);
			bodyObject.put("status", status);
			bodyObject.put("cpparam", cpparam);
			bodyObject.put("ext1", ext1);
			result = ctswService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
}
