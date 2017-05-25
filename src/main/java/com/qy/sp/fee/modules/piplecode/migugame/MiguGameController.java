package com.qy.sp.fee.modules.piplecode.migugame;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

@Controller
@RequestMapping(value = "/piple")
public class MiguGameController {

	@Resource
	private MiguGameService miguGameService;
	

	@RequestMapping(value = "/migugame/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String result = "error";
		try{
			XMLSerializer xmlSerializer = new XMLSerializer();
			JSONObject jsonObject = (JSONObject) xmlSerializer.read(requestBody);
			result = miguGameService.processPaySuccess(jsonObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
