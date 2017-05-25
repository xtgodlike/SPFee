package com.qy.sp.fee.modules.piplecode.huozuan;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class HZJunit {
	//String baseURL = "http://127.0.0.1:8888/SPFee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//调试回调接口
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("mobile", "13851528062");
		parameters.put("pipleId", "14815351573456688034681");
		parameters.put("productCode", "P01000");
		parameters.put("imsi", "460018548284145");
		parameters.put("ipProvince", "江苏");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
