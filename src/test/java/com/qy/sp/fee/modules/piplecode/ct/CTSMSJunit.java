package com.qy.sp.fee.modules.piplecode.ct;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class CTSMSJunit {
	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://127.0.0.1:8888/SPFee";

	@Test
	public void testGetMessage(){
		try {
			String message  = "001#1#PM1052#1003#P00200#460018548284145%app";
	//		String message  = "1234";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet(baseURL+"/api/sdk/phone/syncsimcard?mobile=13113975494&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	@Test
//	public void testSubmitMessageVoCode(){
//		try {
//			String message  = "1234";
//			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
//			System.out.println(encodeMessage);
//			String result = HttpClientUtils.doGet(baseURL+"/api/sdk/phone/syncsimcard?mobile=13113975494&msg="+encodeMessage, HttpClientUtils.UTF8);
//			System.out.println("result:\n"+result);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
