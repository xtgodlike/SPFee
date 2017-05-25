package com.qy.sp.fee.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;


public class SMSTestHttpRequest {

 public static void main(String[] args) throws UnsupportedEncodingException {

	 	JSONObject parameters = new JSONObject();
	 	String mobile = "18313024197";
	 	String msg = "001$1$PM1026$1003$P00200$460021251261945#qwe";
	 	msg = StringUtil.urlEncodeWithUtf8(msg);
	 	parameters.put("mobile", mobile);
	 	// 001$1$PM1026$1003$P00100$460021251261945|qwe
	 	parameters.put("msg", msg);
	 	String url = "http://localhost:8888/SPFee/api/sdk/phone/syncsimcard"+"?"+"mobile="+mobile+"&msg="+msg;
	 	try {
//			String result = HttpClientUtils.doPost("http://www.chinaunigame.net/spfee/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
//	 		String result = HttpClientUtils.doPost("http://www.chinaunigame.net/spfee/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/SPFee/api/sdk/phone/syncsimcard", parameters.toString(), HttpClientUtils.UTF8);
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://123.56.158.156/spfee/api/sdk/phone/syncsimcard", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/SPFee/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/SPFee/api/sdk/phone/syncsimcard", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
//			JSONObject jsonObj = JSONObject.fromObject(result);
//			String  sms2 = jsonObj.getString("sms2");
//			System.out.println("sms2:\n"+sms2);
////			String sms2="MDAwMDAwMDA4NlEzMzMvJTY1MzE3OTY0ZjAwYzQwMTdFQjh1NzInenojVVJh WVdYVVUrYk15cyshaXRnPT01aDxeMDZ+dTRMMzUzM243MjApUm0wMTFlMDA3 MzR3TTAwMHwwSDAwMDAwTSVIczZmfF1WSXVxcjA6NTI1VFJTdWZbVnB1Pg==";
////			String decodeSms2 = base64Decode(sms2);
//			String decodeSms2 = new String(Base64.decode(sms2));
//			System.out.println("decodeSms2:\n"+decodeSms2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	
}
