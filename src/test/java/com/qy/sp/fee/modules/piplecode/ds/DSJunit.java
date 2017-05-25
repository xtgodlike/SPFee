package com.qy.sp.fee.modules.piplecode.ds;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;


public class DSJunit {
	//String baseURL = "http://127.0.0.1:8888/SPFee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("mobile", "18682165792");
			parameters.put("pipleId", "14775389896200102268388");
			parameters.put("productCode", "P00800");
			parameters.put("imsi", "460012168618225");
			parameters.put("extData", "zcz");
			parameters.put("ipProvince", "江苏");
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
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14775389896200102268388");
		parameters.put("orderId","14823062242758435442141");
		parameters.put("verifyCode","3529");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	@Test//调试回调接口
	public void testSync(){
		JSONObject parameters = new JSONObject();
		parameters.put("code", "0");
		parameters.put("msg", "扣费成功");
		parameters.put("orderId", "10431483");
		parameters.put("price","4.00");
		parameters.put("chargeCode","zl22098A00400");
		parameters.put("mobile", "13044208484");
		parameters.put("transmissionData","14781604929965326430665");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/piple/dswo/sync", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
