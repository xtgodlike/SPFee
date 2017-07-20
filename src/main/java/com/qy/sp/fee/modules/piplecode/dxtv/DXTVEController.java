package com.qy.sp.fee.modules.piplecode.dxtv;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/piple")
public class DXTVEController {

	@Resource
	private DXTVideoService dxTVideoService;
	@RequestMapping(value = "/dxtve/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String imsi,String qid,String subscribe_time,String cp_param,String result,String tran_id){
		String resultMsg = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("imsi", imsi);
			bodyObject.put("qid", qid);
			bodyObject.put("subscribe_time", subscribe_time);
			bodyObject.put("cp_param", cp_param);
			bodyObject.put("result",result);
			bodyObject.put("tran_id",tran_id);
			resultMsg = dxTVideoService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
