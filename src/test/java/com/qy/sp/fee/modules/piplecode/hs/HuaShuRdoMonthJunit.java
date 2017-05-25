package com.qy.sp.fee.modules.piplecode.hs;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

public class HuaShuRdoMonthJunit {
	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	@Test
	public void testGetMessage(){
		try {
			//10690583026594
//			String message  = "001$1$PM1023$1003$P00010$a106";
			String message  = "001#1#PM1019#1003#P00010";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet(baseURL+"/api/sdk/phone/syncsimcard?mobile=15651938912&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessageVoCode(){
		try {
			//10690583026594
			String message  = "123";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=15651938912&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessage(){
		try {
			//10690583026594
			String message  = "001$2$PM1019$1003$123";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=15651938912&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testNotify(){
		String url = "http://192.168.1.200:8001/SPFee/piple/qianyawoapp/test?Timespan=20160602202850&Mobile=15651938912&ProductCode=14629556206452657057407&Thefee=0.1&TheOutTradeNo=10021465983998367186442782802875715&OrderStatus=0&ApiKey=shkm2016";
//		String url = "http://192.168.1.200:8001/SPFee/piple/qianyawoapp/sync";
//		String notifyUrl = url+"?Timespan=20160421103343&Mobile=15651938912&ProductCode=14628498450541360034061&Thefee=0.1&TheOutTradeNo=10021464868572365362270867146855249&OrderStatus=0&ApiKey=testc&ExpansionData=xxxxxx";
		try {
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test//获取验证码
	public void testGetSMS(){
			long start =System.currentTimeMillis();
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("mobile", "18313024197");
			parameters.put("pipleId", "14677003607812044654719");
			parameters.put("productCode", "P00800");
			parameters.put("imei", "865521016687625");
			parameters.put("imsi", "460021251261945");
			
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
				
				System.out.println(System.currentTimeMillis() - start);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	@Test//提交验证码
	public void testVertifySMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("pipleId", "14677003607812044654719");
			parameters.put("orderId", "14682892621916852470651");
			parameters.put("vCode", "vCode");
			parameters.put("msg", "2001");
			
			try {
		String result = HttpClientUtils.doPost(baseURL+"/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

}
