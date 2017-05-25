package com.qy.sp.fee.modules.piplecode.mrb;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/piple")
public class MRBController {
	@Resource
	private MRBService mrbService;
	@RequestMapping(value = "/mrb/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mobile,String msg,String spnumber,String linkid,String status ){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("linkid", linkid);
			bodyObject.put("mobile", mobile);
			bodyObject.put("msg", msg);
			bodyObject.put("status",status);
			bodyObject.put("spnumber", spnumber);
			result = mrbService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
