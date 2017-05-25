package com.qy.sp.fee.modules.piplecode.others;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class LengShiIvrController {

	@Resource
	private LengShiIvrService lengShiIvrService;
	@RequestMapping(value = "/lengshiivr/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mobile,String longnumber,String linkid,String fees,String stime,String etime){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("fees", fees);
			bodyObject.put("mobile", mobile);
			bodyObject.put("linkid", linkid);
			bodyObject.put("stime", stime);
			bodyObject.put("etime", etime);
			bodyObject.put("longnumber", longnumber);
			result = lengShiIvrService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
