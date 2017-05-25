package com.qy.sp.fee.modules.piplecode.base;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/channel")
public class ChannelController {
	private Logger logger = LoggerFactory.getLogger(ChannelController.class);
	@Resource
	private ChannelOtherService channelOtherService;
	
	@RequestMapping(value = "/getSms" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequestGetSms(@RequestBody String requestBody,HttpServletRequest request){
		String result = "{}";
		JSONObject requestObject = new JSONObject();
		boolean isTrue =false;;
		try{
			requestObject = JSONObject.fromObject(requestBody);
			isTrue = true;
		}
		catch(Exception e){
			isTrue = false;
		}
		if(!isTrue){
			try{
				Map requestParams = request.getParameterMap();
				for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
					String name = (String) iter.next();
					String[] values = (String[]) requestParams.get(name);
					String valueStr = "";
					for (int i = 0; i < values.length; i++) {
						valueStr = (i == values.length - 1) ? valueStr + values[i]
								: valueStr + values[i] + ",";
					}
					requestObject.put(name, valueStr);
				}
				isTrue = true;
			}catch(Exception e){
				isTrue = false;
			}
		}
		if(isTrue){
			try {
				result = ChannelManager.getInstance().fireGetSMS(requestObject).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info(requestBody+","+result.toString());
		return result;
	}
	@RequestMapping(value = "/vertifySms" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequestVertifySms(@RequestBody String requestBody,HttpServletRequest request){
		String result = "{}";
		JSONObject requestObject = new JSONObject();
		boolean isTrue =false;;
		try{
			requestObject = JSONObject.fromObject(requestBody);
			isTrue = true;
		}
		catch(Exception e){
			isTrue = false;
		}
		if(!isTrue){
			try{
				Map requestParams = request.getParameterMap();
				for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
					String name = (String) iter.next();
					String[] values = (String[]) requestParams.get(name);
					String valueStr = "";
					for (int i = 0; i < values.length; i++) {
						valueStr = (i == values.length - 1) ? valueStr + values[i]
								: valueStr + values[i] + ",";
					}
					requestObject.put(name, valueStr);
				}
				isTrue = true;
			}catch(Exception e){
				isTrue = false;
			}
		}
		if(isTrue){
			try {
				result = ChannelManager.getInstance().fireVertifySMS(requestObject).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info(requestBody+","+result.toString());
		return result;
	}
	
	@RequestMapping(value = "/syncall" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String syncall(){
		String result = "{}";
		try{
			return channelOtherService.synOrders().toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
