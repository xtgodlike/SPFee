package com.qy.sp.fee.modules.piplecode.zw;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class ZWRdoController {

	@Resource
	private ZWRdoService zwRdoService;
	@RequestMapping(value = "/zwrdo/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String ppid,String imsi,String msisdn,String price,String time ,String orderid,String custom){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("ppid", ppid);
			bodyObject.put("imsi", imsi);
			bodyObject.put("msisdn", msisdn);
			bodyObject.put("price", price);
			bodyObject.put("time",time);
			bodyObject.put("orderid",orderid);
			bodyObject.put("custom",custom);
			result = zwRdoService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
