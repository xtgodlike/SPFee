package com.qy.sp.fee.modules.piplecode.qianya;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;

public class QianyaReaderJunit {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testGetMessage(){
		try {
			//10690583026594
			String meesage  = "002$1$PM1003$1003$P00200";
			String base64Message = Base64.encodeBytes(meesage.getBytes());
			System.out.println(base64Message);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=18313024197&msg="+base64Message, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessage(){
		try {
			//10690583026594
			String meesage  = "002$2$PM1003$1003$123";
			String base64Message = Base64.encodeBytes(meesage.getBytes());
			System.out.println(base64Message);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=18313024197&msg="+base64Message, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetSMS(){
		Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("apiKey", "1003");
        parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
        parameters.put("productCode", "P00200");
        parameters.put("mobile", "18313024197");
        parameters.put("imsi", "460021251261945");
		try {
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/qy/getRdoSms", parameters, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitVCode(){
		try {
			Map<String, String> parameters = new HashMap<String, String>();
	        parameters.put("apiKey", "1003");
	        parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
	        parameters.put("orderId", "14646794077865346256440");
	        parameters.put("verifyCode", "1234");
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/qy/rdoSmsVerify",parameters, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}
