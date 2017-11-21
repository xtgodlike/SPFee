package com.qy.sp.fee.common.utils;

import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;


public class GXTestHttpRequest {

 public static void main(String[] args) throws UnsupportedEncodingException {


//	 JSONObject parameters = new JSONObject();
	 HashMap<String,String> parameters = new HashMap<String, String>();
	 String spId = "11105199";
	 String chargeId = "2001";
	 String timestamp = DateTimeUtils.getCurrentYMDHMSNo();
	 String sercet = "1a9311163d2a432bd4a8";
	 // SHA1(chargeId+timestamp+sercet)
	 String token = SecurityUtils.getSha1(chargeId+timestamp+sercet);
	 parameters.put("spId", spId);
	 parameters.put("chargeId", chargeId);
	 parameters.put("orderType", 1+"");
	 parameters.put("timestamp", timestamp);
	 parameters.put("accessToken", token);
	 parameters.put("imsi", "460016878515303");
	 parameters.put("ip", "123.56.158.156");

	 	try {// 139.196.27.18
//			String result = HttpClientUtils.doPost("http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe", parameters.toString(), HttpClientUtils.UTF8);
			String result = HttpClientUtils.doPost("http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe",parameters,"utf-8");
//			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	
}
