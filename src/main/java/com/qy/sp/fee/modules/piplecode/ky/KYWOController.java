package com.qy.sp.fee.modules.piplecode.ky;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.net.URLDecoder;

@Controller
@RequestMapping(value = "/piple")
public class KYWOController {

	@Resource
	private KYWOService kywoService;
	@RequestMapping(value = "/kywo/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String resultMsg = "error";
		try{
			String params = URLDecoder.decode(requestBody,"utf-8");
			JSONObject requestObject = JSONObject.fromObject(params);
			resultMsg = kywoService.processPaySuccess(requestObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}

}
