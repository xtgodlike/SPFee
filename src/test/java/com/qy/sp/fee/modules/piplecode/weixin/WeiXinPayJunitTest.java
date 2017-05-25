package com.qy.sp.fee.modules.piplecode.weixin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class WeiXinPayJunitTest {
	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	@Test
	public void testGetOrder() throws Exception{
		 Map<String,String> param = new HashMap<String,String>();
		param.put("apiKey","1003");
        param.put("apiPwd","B97FED4E9994E33353DFAA8A31428E11BD7AE59");
        param.put("appId","a102");
        param.put("body",StringUtil.urlEncodeWithUtf8("body"));
        param.put("detail",StringUtil.urlEncodeWithUtf8("detail"));
        param.put("price","10");
        param.put("clientIp","192.168.1.100");
        String result = HttpClientUtils.doPost(baseURL+"/channel/weixin/order", param, HttpClientUtils.UTF8);
        System.out.println(result);
	}
	@Test
	public void testNotify() throws Exception{
		String requestBody = "<xml>"
				+ "<appid><![CDATA[wx2421b1c4370ec43b]]></appid>"
				+ "<attach><![CDATA[支付测试]]></attach>"
				+ "<bank_type><![CDATA[CFT]]></bank_type>"
				+ "<fee_type><![CDATA[CNY]]></fee_type>"
				+ "<is_subscribe><![CDATA[Y]]></is_subscribe>"
				+ "<mch_id><![CDATA[10000100]]></mch_id>"
				+ "<nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>"
				+ "<openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>"
				+ "<out_trade_no><![CDATA[14709835603986031837666]]></out_trade_no>"
				+ "<result_code><![CDATA[SUCCESS]]></result_code>"
				+ "<return_code><![CDATA[SUCCESS]]></return_code>"
				+ "<sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>"
				+ "<sub_mch_id><![CDATA[10000100]]></sub_mch_id>"
				+ "<time_end><![CDATA[20140903131540]]></time_end>"
				+ "<total_fee>1</total_fee>"
				+ "<trade_type><![CDATA[JSAPI]]></trade_type>"
				+ "<transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id>"
				+ "</xml>";
		String requestUrl = baseURL+"/piple/weixin/notify";
		String result = HttpClientUtils.doPost(requestUrl,requestBody,HttpClientUtils.UTF8);
		System.out.println(result);
	}
}
