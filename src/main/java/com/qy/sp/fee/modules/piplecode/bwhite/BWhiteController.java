package com.qy.sp.fee.modules.piplecode.bwhite;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class BWhiteController {

	@Resource
	private BWhiteService bWhiteService;
	
	@RequestMapping(value = "/bwhite/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(){
		String result = "0";
		return result;
	}
}
