package com.qy.sp.fee.modules.piplecode.kongmeng;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class ShiJiDongManController {

	@Resource
	private ShiJiDongManService shiJiDongManService;
	@RequestMapping(value = "/sjdm/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String RptStat,String ExtData,	String LinkID,String Mobile){
		String result = "error";
		try{
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("RptStat", RptStat);
			jsonObject.put("ExtData", ExtData);
			jsonObject.put("LinkID", LinkID);
			jsonObject.put("Mobile", Mobile);
			result = shiJiDongManService.processPaySuccess(jsonObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
