package com.qy.sp.fee.modules.piplecode.weixin;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.ClientProperty;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

@Controller
public class WeiXinPayController {

	@Resource
	private WeiXinPayService weiXinPayService;

	@RequestMapping(value = "/channel/weixin/order" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String createOrder(String apiKey,String mobile,String appId,String body,String detail,String price,String extData,String clientIp){
		String result = "{}";
		try{
			String platformUrl = ClientProperty.getProperty("config","PLATFORM_SERVER_URL");
			String url =  platformUrl+"/piple/weixin/notify";
			result = weiXinPayService.createOrder(apiKey,mobile,appId,detail, body, price,extData,url,clientIp).toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/piple/weixin/notify" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String notify(@RequestBody String requestBody){
		String result = "fail";
		try{
			XMLSerializer xmlSerializer = new XMLSerializer();
			JSONObject jsonObject = (JSONObject) xmlSerializer.read(requestBody);
			return weiXinPayService.notify(jsonObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/channel/weixin/query" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String query(String orderId){
		String result = "fail";
		try{
			return weiXinPayService.queryOrder(orderId).toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
