package com.qy.sp.fee.modules.piplecode.alipay;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.ClientProperty;

@Controller
public class AliPayController {

	@Resource
	private AliPayService aliPayService;

	@RequestMapping(value = "/channel/alipay/order" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String createOrder(String apiKey,String mobile,String appId,String body,String subject,String price,String extData){
		String result = "{}";
		try{
			String platformUrl = ClientProperty.getProperty("config","PLATFORM_SERVER_URL");
			String url =  platformUrl+"/piple/alipay/notify";
			result = aliPayService.createOrder(apiKey,mobile,appId,subject, body, price,extData,url).toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/piple/alipay/notify" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String notify( HttpServletRequest request,HttpServletResponse response){
		String result = "fail";
		try{
		return aliPayService.notify(request);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/channel/alipay/query" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String query(String orderId){
		String result = "fail";
		try{
			return aliPayService.queryOrder(orderId).toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
