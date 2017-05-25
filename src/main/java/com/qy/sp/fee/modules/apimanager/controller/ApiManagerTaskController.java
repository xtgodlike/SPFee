package com.qy.sp.fee.modules.apimanager.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.modules.apimanager.service.ApiManagerTaskService;

@Controller
@RequestMapping(value = "/api/manager/task")
public class ApiManagerTaskController {
	
	@Resource
	private ApiManagerTaskService apiManagerTaskService;
	
	@RequestMapping(value = "/upload" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updateTask(){
		String result = apiManagerTaskService.saveTask();
		return result;
	}
}
