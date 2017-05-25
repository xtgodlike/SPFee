package com.qy.sp.fee.modules.piplecode.panmei;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class PMWoReadController {

	@Resource
	private PMWoReadService pmWoReadService;
	

	@RequestMapping(value = "/pmworead/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String linkid,String spId,String spCode,String mobile,String msg ,String longNum,String payMoney,
			String payTime,String status,String statusDesc,String spParams,String province ,String operator,String sign){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("linkid", linkid);
			bodyObject.put("spId", spId);
			bodyObject.put("spCode", spCode);
			bodyObject.put("mobile", mobile);
			bodyObject.put("msg",msg);
			bodyObject.put("longNum",longNum);
			bodyObject.put("payMoney",payMoney);
			bodyObject.put("payTime", payTime);
			bodyObject.put("status", status);
			bodyObject.put("statusDesc", statusDesc);
			bodyObject.put("spParams", spParams);
			bodyObject.put("province", province);
			bodyObject.put("operator", operator);
			bodyObject.put("sign", sign);
			result = pmWoReadService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
