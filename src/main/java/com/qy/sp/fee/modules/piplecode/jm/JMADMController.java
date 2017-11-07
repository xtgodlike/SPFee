package com.qy.sp.fee.modules.piplecode.jm;

import com.qy.sp.fee.modules.piplecode.fswh.FSWHWoReadService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class JMADMController {

	@Resource
	private JMADMService jmadmService;
	@RequestMapping(value = "/jmadm/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String requestData){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("requestData", requestData);
			result = jmadmService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
