package com.qy.sp.fee.modules.piplecode.hz;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class HZVideoController {

	@Resource
	private HZVideoService hzVideoService;
	@RequestMapping(value = "/hzvideo/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String orderid,String cooperid,String result,String excode,String exmsg){
		String resultMsg = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("orderid", orderid);
			bodyObject.put("cooperid", cooperid);
			bodyObject.put("result", result);
			bodyObject.put("excode", excode);
			bodyObject.put("exmsg",exmsg);
			resultMsg = hzVideoService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
