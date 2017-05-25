package com.qy.sp.fee.modules.piplecode.lf;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

public class LefuRdoJunit {
//	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	@Test
	public void testGetMessage(){
		try {
			//10690583026594
//			String message  = "001$1$PM1023$1003$P00010$a106";
			String message  = "001#1#PM1029#1003#P00200#extData";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet(baseURL+"/api/sdk/phone/syncsimcard?mobile=15802404149&msg="+encodeMessage, HttpClientUtils.UTF8);
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
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=15802404149&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessage(){
		try {
			//10690583026594
			String message  = "001$2$PM1029$1003$123";
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
		String url = "http://192.168.1.200:8001/SPFee/piple/lefurdo/sync?status=DELIVRD&linkid=1785444571514880&msg=xxxxxx&mobile=15802404149";
		try {
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("mobile", "15802404149");
			parameters.put("pipleId", "14707207010883672273133");
			parameters.put("productCode", "P00200");
			parameters.put("imei", "868754021639466");
			parameters.put("imsi", "460028024670681");
			
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
			parameters.put("pipleId", "14707207010883672273133");
			parameters.put("orderId", "14707934667633757236656");
			parameters.put("vCode", "870552");
			try {
		String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
//		String result = HttpClientUtils.doPost("http://www.chinaunigame.net/spfee/channel/vertifySms","{\"apiKey\":\"2032\",\"apiPwd\":\"SYTD123\",\"appId\":\"a106\",\"vCode\":\"908\",\"orderId\":\"14671901993252079591745\"}", HttpClientUtils.UTF8);
		System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
