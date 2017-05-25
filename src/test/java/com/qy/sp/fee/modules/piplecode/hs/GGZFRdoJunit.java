package com.qy.sp.fee.modules.piplecode.hs;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class GGZFRdoJunit {
	//String baseURL = "http://127.0.0.1:8888/SPFee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("mobile", "13541311221");
			parameters.put("pipleId", "14785890270023978022243");
			parameters.put("productCode", "P00100");
			parameters.put("imsi", "460018548284145");
			parameters.put("extData", "zcz");
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
		parameters.put("pipleId", "14785890270023978022243");
		parameters.put("orderId", "14785942759836016233681");
		parameters.put("verifyCode", "739894");
		
		try {
	String result = HttpClientUtils.doPost(baseURL+"/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test//调试回调接口
	public void testSync(){
		try {
				String url = baseURL+"/piple/ggzf/sync?mobile=15193691199&port=10661388&linkid=16111212355988992205&msg=2&status=0000&param=1003P00200extData";
				String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
