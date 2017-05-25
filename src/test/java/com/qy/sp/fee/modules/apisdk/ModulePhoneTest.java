package com.qy.sp.fee.modules.apisdk;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;

import net.sf.json.JSONObject;

public class ModulePhoneTest {
	
	String baseUrl = "http://192.168.0.200:8001/SPFee";
//	String baseUrl = "http://139.196.27.18/spfee";
//	String baseUrl = "http://www.chinaunigame.net:8120/spfee";
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
	public void testaddSimcard(){
//		baseUrl = "http://139.196.27.18/spfee/api/sdk/phone/syncsimcard";
//		baseUrl = "http://139.196.27.18/spfee";
		String msg = "MDAyJDAkMUE0MjZGQjlCMUNFNjdDNDE2N0VBMTUxMzE4QUIwOTA=";
		String url = baseUrl+"/api/sdk/phone/syncsimcard?mobile=18313024197&msg="+msg;
		String result = "";
		try {
			result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+result);
	}
	@Test
	public void testSendSimcard(){
		//接收号码10690583026594
		String mobile = "15651938912";
		String message = "69FA075583A8208D42AE398B09D5FD1D";
		String url = "http://222.73.117.158/msg/HttpBatchSendSM?account=sx-qyly&pswd=sx-qyly&mobile=%s&msg=%s&needstatus=true";
		url = String.format(url, mobile,message);
		String result = "";
		try {
			result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+result);
	}
	@Test
	public void testgetSimcard(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imsi_sim", "460021251261945");
		String url = baseUrl+"/api/sdk/phone/simcard";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url,header,desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testLoadConfigures(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", "869634020694200");
		bodyObj.put("channelId", "1003");
		bodyObj.put("appId", "a103");
		bodyObj.put("provinceId", "1");
		bodyObj.put("configId", "isFuzzyMoney");
		String url = baseUrl+"/api/sdk/phone/configuration";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url, header,desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testLoadUserCenterSdkInfo(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", imei);
		bodyObj.put("channelId", "1003");
		bodyObj.put("appId", "a102");
		bodyObj.put("cpuModel", "x64");
		String url = baseUrl+"/api/resource/sdk/sdkinfo";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url, header,desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("request:"+desUtil.Encode(bodyObj.toString())+";result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testuploadPhoneInfo(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", imei);
		bodyObj.put("appId", "a102");
		bodyObj.put("channelApi", "1003");
		bodyObj.put("number_sim1", "15651938912");
		bodyObj.put("imsi_sim1", "460011935302848");
		bodyObj.put("province", "江苏省");
		bodyObj.put("screenWidth", "1024");
		bodyObj.put("screenHeight", "768");
		bodyObj.put("processName", "net.apptest3");
		String url = baseUrl+"/api/sdk/phone/mobileInfo";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url, header,desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("request:"+desUtil.Encode(bodyObj.toString())+";result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testStaticsUserOperation(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", "1");
		bodyObj.put("channelId", "1");
		bodyObj.put("appId", "1");
		bodyObj.put("flowId", "1");
		bodyObj.put("stepId","1");
		bodyObj.put("content", "test9");
		String url = baseUrl+"/api/sdk/phone/statistics";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url, header,desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+desUtil.Decode(result));
	}
	@Test
	public void testLoggerException(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", "1");
		bodyObj.put("level", "error");
		bodyObj.put("appId", "1");
		bodyObj.put("channelId", "1");
		bodyObj.put("exception", "testexception");
		String url = baseUrl+"/api/sdk/phone/exception";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url, header,desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testRandomDesKeys(){
		for(int i=0;i<10 ;i ++){
			System.out.print("\""+KeyHelper.getRandomNumber(8)+"\",");
		}
	}
	@Test
	public void testBase64(){
		String base64 = new String(Base64.encodeBytes("FE4323AF89DBB537".getBytes()));
		System.out.println(base64);
	}
	@Test
	public void testPressure(){
		String url = baseUrl+"/api/sdk/test/query?operation=get&key=a&value=a";
		String result = "";
		try {
			result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:"+result);
		
	}
}
