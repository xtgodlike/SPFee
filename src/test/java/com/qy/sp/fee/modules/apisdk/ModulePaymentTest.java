package com.qy.sp.fee.modules.apisdk;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;

import net.sf.json.JSONObject;

public class ModulePaymentTest {
	
	String baseUrl = "http://192.168.0.200:8001/SPFee";
//	String baseUrl = "http://139.196.27.18/spfee";
	DesUtil desUtil = new DesUtil();
	HashMap<String, String> header = new HashMap<String, String>();
	String desKey = "31553543";
	String imei = "869634020694200";
	@Before
	public void init(){
		desUtil.setKey(desKey);
		header.put("desKey",desKey );
		header.put("imei", imei);
	}
	@Test
	public void testApplyPayment(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", imei);
		bodyObj.put("imsi", "460011935302848");
		bodyObj.put("number", "15651938912");
		bodyObj.put("number_address", "10");
		bodyObj.put("money", "P00200");
		bodyObj.put("channelId", "1003");
		bodyObj.put("appId", "a102");
		bodyObj.put("payType", "0");
		String url = baseUrl+"/api/sdk/payment/apply";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url, header, desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("request:"+desUtil.Encode(bodyObj.toString())+";result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testDownloadPayment(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("paymentId", "14521362692725053460194");
		bodyObj.put("imei", "1");
		String url = baseUrl+"/api/sdk/payment/download";
		try {
			HttpClientUtils.downloadFile("c:\\1.zip", url, header, desUtil.Encode(bodyObj.toString()),HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	@Test
	public void teststaticsPayment(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", "1");
		bodyObj.put("channelId", "1");
		bodyObj.put("appId", "1");
		bodyObj.put("flowId", "1");
		bodyObj.put("stepId","1");
		bodyObj.put("content", "test");
		bodyObj.put("orderId", "1");
		bodyObj.put("subStatus", "2");
		String url = baseUrl+"/api/sdk/payment/statistics";
		try {
			HttpClientUtils.doPost(url, header, desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	@Test
	public void testUploadPayments(){
		String url = baseUrl+"/api/manager/payment/upload";
		try {
			JSONObject bodyObj = new JSONObject();
			bodyObj.put("filePath", "D:\\project\\android\\payment\\source\\SDK\\payment\\Qianya_SDK\\build\\target\\payment\\woqianya.zip");
			bodyObj.put("fileName", "woqianya");
			bodyObj.put("pipleId", "14521362692725053460194");
			bodyObj.put("version", "4");
			String result = HttpClientUtils.doPost(url,bodyObj.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	@Test
	public void testCreateKey(){
		String str = KeyHelper.createTime()+KeyHelper.getRandomNumber(6);
		System.out.println(str);
		System.out.println(KeyHelper.createID());
	}
}
