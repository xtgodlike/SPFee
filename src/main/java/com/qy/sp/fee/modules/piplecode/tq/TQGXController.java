package com.qy.sp.fee.modules.piplecode.tq;

import com.qy.sp.fee.modules.piplecode.dxtv.DXTVideoService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class TQGXController {

	@Resource
	private TQGXService tqgxService;


	@RequestMapping(value = "/jsgx/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String params,String order_string,String orderResult){
		String resultMsg = "error";
		try{
			JSONObject paramsObj = JSONObject.fromObject(params);
			JSONObject orderStringObj = JSONObject.fromObject(order_string);
			JSONObject result = JSONObject.fromObject(orderResult);
			resultMsg = tqgxService.processPay(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
