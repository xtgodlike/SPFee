package com.qy.sp.fee.modules.piplecode.kongmeng;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

@Controller
@RequestMapping(value = "/piple")
public class YiYeFootballController {

	@Resource
	private YiYeFootballService yiYeFootballService;
	@RequestMapping(value = "/yiyedm/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String result = "error";
		try{
			XMLSerializer xmlSerializer = new XMLSerializer();
			xmlSerializer.setExpandableProperties(new String[]{"response"});
			JSONObject jsonObject = (JSONObject) xmlSerializer.read(requestBody);
			result = yiYeFootballService.processPaySuccess(jsonObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
