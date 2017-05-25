package com.qy.sp.fee.modules.piplecode.hs;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

public class HuaShuRdoV2Junit {
	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	
	@Test
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "13451818167");
		parameters.put("imsi", "460021251261945");
		parameters.put("productCode", "P00200");
		parameters.put("imei", "352343059415610");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14673525835107348590762");
		
		try {
			String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test//提交验证码
	public void testVertifySMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14673525835107348590762");
		parameters.put("orderId", "14676224202205503505821");
		parameters.put("vCode", "vCode");
		
		try {
	String result = HttpClientUtils.doPost(baseURL+"/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testNotify(){
		try {
			String url = baseURL+"/piple/huashuo/sync"+"?mobile=15193691199&port=10661388&linkid=16111212355988992205&msg=2&status=0000&param=kdxf02A14676224202205503505821";
//		String url = "http://www.chinaunigame.net/spfee/piple/huashusms/sync?mobile=15193691199&port=10661388&linkid=16111212355988992205&msg=2&status=0001&param=mchay51003extData";
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetMessage(){
		try {
			String message  = "001#1#PM1018#1003#P00200";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet(baseURL+"/api/sdk/phone/syncsimcard?mobile=18313024197&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessageVoCode(){
		try {
			String message  = "123";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=18313024197&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
