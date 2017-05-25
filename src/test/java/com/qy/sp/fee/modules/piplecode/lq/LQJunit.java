package com.qy.sp.fee.modules.piplecode.lq;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class LQJunit {
	//String baseURL = "http://www.chinaunigame.net/spfee";
	String baseURL = "http://127.0.0.1:8888/SPFee";
	@Test//调试回调接口
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("mobile", "13851528062");
		parameters.put("pipleId", "14797949784636409309867");
		parameters.put("productCode", "P00400");
		parameters.put("imsi", "460001523583055");
		parameters.put("imei","868721028101329");
		parameters.put("iccid", "89860085101151142361");
		parameters.put("ip","120.0.0.1");
		parameters.put("fromType", "1");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
