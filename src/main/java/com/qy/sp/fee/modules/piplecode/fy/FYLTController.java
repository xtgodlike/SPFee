package com.qy.sp.fee.modules.piplecode.fy;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class FYLTController {

	@Resource
	private FYLTService fyltService;
	@RequestMapping(value = "/fylt/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String msg, String linkid,String spnumber,String mobile,String status,String ctime){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			
			requestObj.put("msg", msg);
			requestObj.put("linkid", linkid);
			requestObj.put("spnumber", spnumber);
			requestObj.put("mobile", mobile);
			requestObj.put("status", status);
			requestObj.put("ctime", ctime);

			result = fyltService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
