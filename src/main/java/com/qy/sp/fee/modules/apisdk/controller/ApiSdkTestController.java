package com.qy.sp.fee.modules.apisdk.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.modules.apisdk.service.ApiSdkTestService;

@Controller
@RequestMapping(value = "/api/sdk/test")
public class ApiSdkTestController {

	@Resource
	private ApiSdkTestService apiSdkTestService;
	@RequestMapping(value = "/query" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String query(String operation,String key,String value){
		String result = "{}";
		try {
			result =  apiSdkTestService.query(operation, key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
