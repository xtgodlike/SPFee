package com.qy.sp.fee.modules.piplecode.migudongman;
import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class LeQuMiguDongmanJunitTest {
//	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "18313024197");
		parameters.put("imsi", "460021251261945");
		parameters.put("productCode", "P00010");
		parameters.put("imei", "352343059415610");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14685695389177498476580");
		parameters.put("ip", "192.168.1.100");
		
		try {
			String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testBAse64(){
		String base64 = "MDAwMDAwMDAzNVUzNDMvIjk0MTU2MTA0ZjAwYzEyNTFORzF5NDVjc2Mwe15mKy8xNUFxQkJiSkN2S0p3PT03bDpcNDBwezJONzc5M2w5MjApUm0yMTJkNjA3MDBlTTAwMHwwSDAwMDAwUUpQdyVnM1VWY2VDdWltRXU3bHByTUk/Y2NfPg==";
		String result = new String(Base64.decode(base64));
		System.out.println(result);//14673571416872036860522
	}

	@Test
	public void testLFRDONotify(){
		String response = "<SyncAppOrderReq xmlns=\"http://www.monternet.com/dsmp/schemas/\">"
				+ "<OrderID>YWM3YjY4NWEwYzY5NDNjZTkwYjdjM2E2ZjljNmRhMDk=</OrderID>"
				+ "<CheckID>0</CheckID>"
				+ "<ActionTime>20130619144435</ActionTime>"
				+ "<ActionID>1</ActionID>"
				+ "<MSISDN>18313024197</MSISDN>"
				+ "<TradeID>L0IF7AF2J4L5IF1B</TradeID>"
				+ "</SyncAppOrderReq>";
		
		try {
			String result = HttpClientUtils.doPost(baseURL+"/piple/lequmigudongman/sync", response, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
