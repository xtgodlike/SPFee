package com.qy.sp.fee.modules.piplecode.qianya;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class QianyaWoJunit {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testGetMessage(){
		try {
			//10690583026594
			String message  = "002$1$PW1006$1003$P00010 ";
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
			String message  = "002$2$PW1006$1003$123";
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
		String url = "http://192.168.1.200:8001/SPFee/piple/qianyawo/sync";
		String notifyUrl = url+"?Timespan=20160421103343&Mobile=15651938912&ProductCode=14628498450541360034061&Thefee=0.1&TheOutTradeNo=10021464665871415667361932136316007&OrderStatus=0&ApiKey=testc&ExpansionData=xxxxxx";
		try {
			String result = HttpClientUtils.doGet(notifyUrl, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testGetSMS(){
		Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("apiKey", "1003");
        parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
        parameters.put("productCode", "P00010");
        parameters.put("phone", "15651938912");
        parameters.put("iccid", "89860032246591219455");
        parameters.put("imsi", "460021251261945");
		try {
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/qywo/getPayment", parameters, HttpClientUtils.UTF8);
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
	        parameters.put("orderId", "14646657062685316539919");
	        parameters.put("verifyCode", "1234");
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/qywo/confirmPayment",parameters, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}
