package com.qy.sp.fee.modules.piplecode.qianya;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class QYVACTController {

	@Resource
	private QYVACTService qyvactService;
	@RequestMapping(value = "/qyvact/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mo_msg, String linkid,String commandid,String remark,String mobile,
								 String code, String transactionid,String statemsg){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			
			requestObj.put("mo_msg", mo_msg);
			requestObj.put("linkid", linkid);
			requestObj.put("commandid", commandid);
			requestObj.put("remark", remark);
			requestObj.put("mobile", mobile);
			requestObj.put("code", code);
			requestObj.put("transactionid", transactionid);
			requestObj.put("statemsg", statemsg);
			result = qyvactService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
