package com.qy.sp.fee.modules.piplecode.maiguang;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class MaiGuangJunit {
	String baseURL = "http://127.0.0.1:8888/SPFee";
	@Test//调试回调接口
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("mobile", "18665848051");
		parameters.put("pipleId", "14775599243830695760815");
		parameters.put("productCode", "P01000");
		parameters.put("imsi", "460018548284145");
		parameters.put("status", "1");
		parameters.put("statusMsg", "支付成功");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
