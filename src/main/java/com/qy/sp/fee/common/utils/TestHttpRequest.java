package com.qy.sp.fee.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;


public class TestHttpRequest {

 public static void main(String[] args) throws UnsupportedEncodingException {


	 	JSONObject parameters = new JSONObject();
	 	parameters.put("apiKey", "3026");
//	 	parameters.put("apiPwd", "KSTG123");
//		parameters.put("pipleId", "14775389896200102268388");
	 	parameters.put("pipleKey", "PM1079");
//		parameters.put("appId", "a102");
//		parameters.put("productCode", "P00400");
//		parameters.put("mobile", "13602163400");
//		parameters.put("imei", "869722026793124");
//		parameters.put("imsi", "460019305092881");
//		parameters.put("iccid", "89860085101151142361");
//		parameters.put("ua", "test");
//		parameters.put("appName", "测试APP"); 
//		parameters.put("chargeName", "测试商品"); 
//		parameters.put("imei", "866231025922455");
//		parameters.put("extData", "jsqy_rxbwt_api4y");
//////		parameters.put("ip", "115.238.54.105"); 
//////		parameters.put("iccid", "123456789012345"); 
//		parameters.put("extData", "asdggasd"); 
////		// 提交验证码
		parameters.put("orderId", "15029331336479642726727");
		parameters.put("verifyCode", "928511");
//	 	
//	 	// 其他测试
//	 	parameters.put("apiKey", "1003");
//	 	parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
//////		parameters.put("appId", "a105");
//	 	parameters.put("pipleId", "14709878328508528319181");
//	 	parameters.put("productCode", "P01000");
////	 parameters.put("imsi", "460016878515303");
//////	 parameters.put("imei", "A00000364882B3");
//	 	parameters.put("mobile", "18586878646");
//	 	parameters.put("extData", "test");
//	 	String msg = "yzw#csdy2#7002#y1003qweq"; 
//	 	String ftime = "2016-08-11 10:40:26";
//	 	msg = StringUtil.urlEncodeWithUtf8(msg);
//	 	ftime = StringUtil.urlEncodeWithUtf8(ftime);
//	 	String url = "http://localhost:8888/SPFee/piple/hsdyst/sync?mobile=13550263892&linkid=201608081740101742362&port=106616080102&param=y1003qweq&status=0000";
//	 	url = url+"&msg="+msg+"&ftime="+ftime;
	 	try {// 139.196.27.18
//	 		String result = HttpClientUtils.doPostp("http://localhost:8088/SPFee/piple/woshop/sync", "<callbackReq>测试</callbackReq>", HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/spfee/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://www.chinaunigame.net:8120/spfee/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
	 		String result = HttpClientUtils.doPost("http://localhost:8888/spfee/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/SPFee/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
//			String result = HttpClientUtils.doPost("http://localhost:8888/SPFee/api/sdk/phone/getMongodbUserGroup", parameters.toString(), HttpClientUtils.UTF8);
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
