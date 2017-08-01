package com.qy.sp.fee.modules.piplecode.my;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class MYYXController {

	@Resource
	private MYYXService myyxService;
	@RequestMapping(value = "/myyx/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String message,String spnumber,String linkid,String exData,String phone,String orderTime,String msgstatus){
		String resultMsg = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("message", message);
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("linkid", linkid);
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("linkid",linkid);
			bodyObject.put("exData",exData);
			bodyObject.put("phone",phone);
			bodyObject.put("orderTime",orderTime);
			bodyObject.put("msgstatus",msgstatus);
			resultMsg = myyxService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
