package com.qy.sp.fee.modules.piplecode.zsadm;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;


public class DSJunit {
	String baseURL = "http://127.0.0.1:8888/SPFee";
	//String baseURL = "http://www.chinaunigame.net:8100/spfee";
//	@Test//获取验证码
//	public void testGetSMS(){
//			JSONObject parameters = new JSONObject();
//			parameters.put("apiKey", "1003");
//			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
//			parameters.put("mobile", "18073070117");
//			parameters.put("pipleId", "14780541337500310032872");
//			parameters.put("productCode", "P00200");
//			parameters.put("imsi", "460037511605982");
//			parameters.put("extData", "xt");
//			try {
//				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
//				System.out.println("result:\n"+result);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	@Test//验证验证码
	public void testVertifySMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14780541337500310032872");
		parameters.put("orderId","14781669202832532991040");
		parameters.put("verifyCode","2");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	/*@Test//调试回调接口
	public void testSync(){
		JSONObject parameters = new JSONObject();
		parameters.put("code", "0");
		parameters.put("msg", "红名单用户测试");
		parameters.put("orderId", "10415893");
		parameters.put("price","1.00");
		parameters.put("chargeCode","zl22098A00100");
		parameters.put("mobile", "18665848051");
		parameters.put("transmissionData","14779917933351678451230");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/piple/dswo/sync", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}*/
}
