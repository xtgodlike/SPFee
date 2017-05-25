package com.qy.sp.fee.modules.piplecode.ds;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class DSWoController {
	@Resource
	private DSWoService dsWoService;
	@RequestMapping(value = "/dswo/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String result = "error";
		try{
			JSONObject requestObject = JSONObject.fromObject(requestBody);
			result = dsWoService.processPaySuccess(requestObject);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
