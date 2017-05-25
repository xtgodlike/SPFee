package com.qy.sp.fee.modules.piplecode.woshop;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class WOShopJUnit {
	//String baseURL = "http://127.0.0.1:8888/SPFee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("mobile", "13205688279");
			parameters.put("pipleId", "14821138830617686955782");
			parameters.put("productCode", "P00800");
			parameters.put("imsi", "460018548284145");
			parameters.put("ipProvince", "安徽");
			parameters.put("extData", "zcz");
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	@Test//验证验证码
	public void testVertifySMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("pipleId", "14821138830617686955782");
		parameters.put("orderId","14822185496457296661435");
		parameters.put("verifyCode","3529");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
