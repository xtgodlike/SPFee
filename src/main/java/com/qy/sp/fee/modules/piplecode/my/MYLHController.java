package com.qy.sp.fee.modules.piplecode.my;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class MYLHController {

	@Resource
	private MYLHService mylhyService;
	@RequestMapping(value = "/mylh/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String exData,String orderTime,String phone,String spnumber,String linkid,String message,String msgstatus){
		String resultMsg = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("exData", exData);
			bodyObject.put("orderTime", orderTime);
			bodyObject.put("phone", phone);
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("linkid",linkid);
			bodyObject.put("message",message);
			bodyObject.put("msgstatus",msgstatus);
			resultMsg = mylhyService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
