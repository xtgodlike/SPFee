package com.qy.sp.fee.modules.piplecode.xty;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class XTYJunit {
	String baseURL = "http://127.0.0.1:8888/SPFee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//获取验证码
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("mobile", "13851528062");
		parameters.put("pipleId", "14797841710198054897225");
		parameters.put("productCode", "P01000");
		parameters.put("imsi", "460078097939709");
		parameters.put("imei", "860734020424530");
		parameters.put("iccid", "898600a1011550157022");
		parameters.put("ip", "120.0.0.1");
		parameters.put("mac", "00:0c:e7:12:5d:53");
		parameters.put("model", "KliTON+I818LH");
		parameters.put("subject", "%E8%B4%AD%E4%B9%B0%E7%A4%BC%E5%8C%85");
		parameters.put("appName", "%E6%8D%95%E9%B1%BC%E8%BE%BE%E4%BA%BA");
		try {
			String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test//调试回调接口
	public void testSync(){
		try {
			String result = HttpClientUtils.doGet(baseURL+"/piple/xtymm/sync?type=MR&linkid=394508826044&spnumber=10658424&momsg=D0&mobile=13800138000&pid=3051&status=DELIVRD&province=北京&city=北京",  HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
