package com.qy.sp.fee.modules.piplecode.ygxe;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping(value = "/piple")
public class YGXESController {
	@Resource
	private YGXEService ygxeService;
	@RequestMapping(value = "/ygxe/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mobile,String momsg,String spnumber,String linkid,String exdata){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("momsg", momsg);
			bodyObject.put("mobile", mobile);
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("linkid", linkid);
			bodyObject.put("exdata",exdata);
			result = ygxeService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

}
