package com.qy.sp.fee.modules.piplecode.ct;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class CTJunit {
//	String baseURL = "http://127.0.0.1:8888/SPFee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//获取验证码
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("mobile", "13113975494");
		parameters.put("pipleId", "14786758299343112647369");
		parameters.put("productCode", "P00200");
		parameters.put("imsi", "460018548284145");
		parameters.put("imei", "868721028101329");
		parameters.put("company", "上海联臻");
		parameters.put("gamename","超级大乐透");
		parameters.put("feename", "超值998大礼包");
		parameters.put("ip","60.18.127.254");
		parameters.put("extData", "zcz");
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
		parameters.put("pipleId", "14786758299343112647369");
		parameters.put("orderId","14788564230620396201082");
		parameters.put("verifyCode","3964");
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
			String result = HttpClientUtils.doGet(baseURL+"/piple/ct/sync?orderId=14788564230620396201082&mobile=13113975494&status=ok&amount=2.0&productCode=P00200&pipleId=14786758299343112647369&apiKey=1003&imsi=460018548284145&extData=zcz",  HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
