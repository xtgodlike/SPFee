package com.qy.sp.fee.modules.piplecode.migudongman;

import javax.annotation.Resource;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class MiguDongmanController {

	@Resource
	private MiguDongmanService miguDongmanService;
	

	@RequestMapping(value = "/migudongman/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String result = "error";
		try{
			XMLSerializer xmlSerializer = new XMLSerializer();
			JSONObject jsonObject = (JSONObject) xmlSerializer.read(requestBody);
			result = miguDongmanService.processPaySuccess(jsonObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
