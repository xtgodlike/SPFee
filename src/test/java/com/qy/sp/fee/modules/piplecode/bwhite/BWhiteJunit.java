package com.qy.sp.fee.modules.piplecode.bwhite;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class BWhiteJunit {
	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
//	String baseURL = "http://www.chinaunigame.net:8100/spfee";
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("mobile", "18313024197");
			parameters.put("pipleId", "14714136609207184697301");
			parameters.put("productCode", "P00100");
			parameters.put("appId", "a102");
			parameters.put("pipleOrderId", "12345678");
			parameters.put("contentId", "300000008397");
			parameters.put("releaseChannelId", "00000000");
			parameters.put("cpId", "102725");
			
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


}
