package com.qy.sp.fee.modules.piplecode.fswh;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class FSWHWoReadController {

	@Resource
	private FSWHWoReadService fWoReadService;
	@RequestMapping(value = "/fswhworead/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String spnumber,String mobile,String linkid,String msg,String status,String fee){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("mobile", mobile);
			bodyObject.put("linkid", linkid);
			bodyObject.put("msg", msg);
			bodyObject.put("status",status);
			bodyObject.put("fee",fee);
			result = fWoReadService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
