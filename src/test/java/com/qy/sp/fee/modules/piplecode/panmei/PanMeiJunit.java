package com.qy.sp.fee.modules.piplecode.panmei;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class PanMeiJunit {
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
	String baseURL = "http://123.56.158.156/spfee";
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
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "2019");
			parameters.put("apiPwd", "NYHY2016RDO0608QY");
			parameters.put("mobile", "15651938912");
			parameters.put("pipleId", "14646760508779930172586");
			parameters.put("productCode", "P00100");
			parameters.put("imei", "865521016687625");
			parameters.put("imsi", "460018548284145");
			parameters.put("extData", "C21");
			
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

}
