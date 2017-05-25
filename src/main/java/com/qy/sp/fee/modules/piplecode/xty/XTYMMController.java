package com.qy.sp.fee.modules.piplecode.xty;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping(value = "/piple")
public class XTYMMController {

	@Resource
	private XTYMMService xtymmService;
	@RequestMapping(value = "/xtymm/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mobile,String spnumber,String linkid,String momsg,String status){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("mobile", mobile);
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("linkid", linkid);
			bodyObject.put("momsg", momsg);
			bodyObject.put("status",status);
			result = xtymmService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
