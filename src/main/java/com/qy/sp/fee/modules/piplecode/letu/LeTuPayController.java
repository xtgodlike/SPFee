package com.qy.sp.fee.modules.piplecode.letu;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.ClientProperty;
import com.qy.sp.fee.modules.piplecode.ds.DSWoService;


@Controller
@RequestMapping(value = "/piple")
public class LeTuPayController {
	@Resource
	private LeTuPayService leTuPayService;
	@RequestMapping(value = "/letu/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@RequestBody String requestBody){
		String result = "error";
		try{
			JSONObject requestObject = JSONObject.fromObject(requestBody);
			result = leTuPayService.processPaySuccess(requestObject);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
