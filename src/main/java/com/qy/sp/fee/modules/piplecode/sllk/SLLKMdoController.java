package com.qy.sp.fee.modules.piplecode.sllk;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class SLLKMdoController {

	@Resource
	private SLLKMdoService sllkMdoService;
	@RequestMapping(value = "/sllkmdo/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mobile,String linkid,String msg,String spcode){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("mobile", mobile);
			bodyObject.put("linkid", linkid);
			bodyObject.put("msg", msg);
			bodyObject.put("spcode", spcode);
			result = sllkMdoService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
