package com.qy.sp.fee.common.utils;

import com.qy.sp.fee.service.CommonService;

public class CommonServiceUtil {

	private static CommonService service;
	public static void setCommonService(CommonService service){
		CommonServiceUtil.service = service;
	}
	public static CommonService getCommonService(){
		return service;
	}
	
}
