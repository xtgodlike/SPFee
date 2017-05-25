package com.qy.sp.fee.modules.piplecode.kongmeng;
import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class VideoJunitTest {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "18313024197");
		parameters.put("imsi", "460021251261945");
		parameters.put("productCode", "P00600");
		parameters.put("imei", "352343059415610");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14603673704623627130605");
		parameters.put("ip", "192.168.1.100");
		parameters.put("iccid", "89860032246591219455");
		parameters.put("hwId", "2200105122");
//		parameters.put("ua", "chrome");
//		parameters.put("video_ua", "molize");
		
		
		try {
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testLFRDONotify(){
		String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<request>"
				+ "<ctype>1</ctype>#1音乐 4 动漫  5 视频<channel_id>120001171</channel_id>"
				+ "<sid>e1233bde6edc063258b6f8693c84127a1c2d08fb</sid>"
				+ "<item_id>800000001546</item_id>"
				+ "<imsi>460026176314597</imsi>"
				+ "<imei>357143040437334</imei>"
				+ "<price>0.1</price>"
				+ "<status>0</status>"
				+ "<phone_number>18313024197</phone_number>"
				+ "<message>Success</message>"
				+ "<trans_id>54afdf63b8aa451a8c3aae8920b1745b</trans_id><result></result>"
				+ "<player_url>aHR0cDovL3ZvZC5nc2xiLmNtdmlkZW8uY24vNjk5MDEwLzIwMTUwOTE1LzE2LzIyMDI1Mzc2MzEv"
				+ "ODU2MTY5MzgvY2pieGVneWdkc3pial81NC5tcDQubTN1OD9tc2lzZG49MTgyNTE5ODE0MTYmbWRz"
				+ "cGlkPSZzcGlkPTY5OTAxMCZuZXRUeXBlPTQmc2lkPTIyMDI1Mzc2MzEmcGlkPTIwMjg1OTY4NzMm"
				+ "dGltZXN0YW1wPTIwMTYwMTE1MTYzOTIyJkNoYW5uZWxfSUQ9MDEwOV8wNDA5MDIwMC05OTAwMC0z"
				+ "MDMwMDAxODAwMDAwMDAmUHJvZ3JhbUlEPTYwODA5NDA0MSZQYXJlbnROb2RlSUQ9MTAxODU5NTYm"
				+ "Y2xpZW50X2lwPTIyMi45NC4yMTguMjQ5JmFzc2VydElEPTIyMDI1Mzc2MzEmZW5jcnlwdD1kZDUx"
				+ "YjU5ZWRiMDFiM2EwZDNmMmMxZmUzYjZmMTA2Yw=="
				+ "</player_url>"
				+ "<cpparam>14604281584968718258642</cpparam>#客户计费传进的 cpparam值 "
				+ "<excode>12345</excode>#音乐基地参数回调"
				+ "</request>";
		
		try {
			String result = HttpClientUtils.doPost("http://139.196.27.18/spfee/piple/video/sync", response, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testBAse64(){
		String base64 = "MDAwMDQ3MzczNVUzNDMvIjk0MTU2MTA0ZjAwYzEyNTFORzF5NDV6NWtaUVVYRG52ay96a0Z2ZmZR\nZW53PT1haTliODIvL2RCWmQ5OW1RMjApUm0xMTZlMjU5MTBlTTAwMHwwSDAwMDAwNldNMk9IKSdp\ndlFFbndyVTlWdFJ4VEdbKytbPg==";
		String result = new String(Base64.decode(base64));
		System.out.println(result);
	}


}
