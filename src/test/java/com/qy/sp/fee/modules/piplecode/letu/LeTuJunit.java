package com.qy.sp.fee.modules.piplecode.letu;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class LeTuJunit {
	String baseURL = "http://127.0.0.1:8888/SPFee";
	@Test//调试回调接口
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("mobile", "18665848051");
		parameters.put("pipleId", "14775599640686630083153");
		parameters.put("productCode", "P00200");
		parameters.put("imsi", "460018548284145");
		parameters.put("status", "0");
		parameters.put("statusMsg", "支付成功");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
