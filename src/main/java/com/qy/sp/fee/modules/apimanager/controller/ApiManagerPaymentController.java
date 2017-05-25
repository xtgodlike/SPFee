package com.qy.sp.fee.modules.apimanager.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.modules.apimanager.service.ApiManagerPaymentService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/api/manager/payment")
public class ApiManagerPaymentController {
	
	@Resource
	private ApiManagerPaymentService apiManagerPaymentService;
	
	@RequestMapping(value = "/upload" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public JSONObject uploadPayment(@RequestBody String body){
		JSONObject jsonObject = JSONObject.fromObject(body);
		JSONObject result = apiManagerPaymentService.savePayment(jsonObject);
		return result;
	}
}
