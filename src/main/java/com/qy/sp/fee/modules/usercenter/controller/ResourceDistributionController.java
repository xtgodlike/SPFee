package com.qy.sp.fee.modules.usercenter.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.modules.usercenter.service.ResourceDistributionService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/api/resource")
public class ResourceDistributionController {

	@Resource
	private ResourceDistributionService resourceDistributionService;
	@RequestMapping(value = "/sdk/sdkinfo" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getSdkInfo(@RequestHeader String desKey,@RequestBody String body){
		String result = "{}";
		try{
			if(StringUtil.isEmpty(body))
				return result;
			DesUtil util= new DesUtil();
			util.setKey(desKey);
			body = util.Decode(body);
			System.out.println("request body:"+body);
//			body = URLDecoder.decode(body,"UTF-8");
			JSONObject bodyObj = JSONObject.fromObject(body);
			JSONObject resultObj = resourceDistributionService.queryResourceSdkInfo(bodyObj);
			result = util.Encode(resultObj.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
