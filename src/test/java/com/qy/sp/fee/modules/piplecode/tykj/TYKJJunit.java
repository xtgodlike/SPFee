package com.qy.sp.fee.modules.piplecode.tykj;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class TYKJJunit {
	String baseURL = "http://www.chinaunigame.net/spfee";
	//String baseURL = "http://127.0.0.1:8888/SPFee";
	@Test//调试回调接口
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		//parameters.put("mobile", "18305990175");
		parameters.put("mobile", "13851528062");
		parameters.put("pipleId", "14792679130017873324206");
		parameters.put("productCode", "P01000");
		parameters.put("imsi", "460018548284145");

		try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	@Test//验证验证码
	public void testVertifySMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14792679130017873324206");
		parameters.put("orderId","14793526778524407538025");
		parameters.put("verifyCode","11111");
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
			String result = HttpClientUtils.doGet(baseURL+"/piple/tykj/sync?mobile=13851528062&port=106616110501&linkid=99999932299&status=0000&param=2910314793526778524407538025&ftime=2016-11-14+14%3A03%3A25&msg=DM10331250",  HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
