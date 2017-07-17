package com.qy.sp.fee.modules.piplecode.sthd;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class STHDLTController {

	@Resource
	private STHDLTService sthdltService;
	@RequestMapping(value = "/sthdlt/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String telno, String mo,String dest,String stat,String errorcode,
								 String linkid, String motime){
		String result = "error";
		try{
			JSONObject requestObj = new JSONObject();
			
			requestObj.put("telno", telno);
			requestObj.put("mo", mo);
			requestObj.put("dest", dest);
			requestObj.put("stat", stat);
			requestObj.put("errorcode", errorcode);
			requestObj.put("linkid", linkid);
			requestObj.put("motime", motime);
			result = sthdltService.processPaySuccess(requestObj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
