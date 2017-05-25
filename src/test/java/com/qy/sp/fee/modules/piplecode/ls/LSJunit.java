package com.qy.sp.fee.modules.piplecode.ls;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;


public class LSJunit {
	//String baseURL = "http://127.0.0.1:8888/SPFee";
	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "2009");
			parameters.put("mobile", "15852945770");
			parameters.put("pipleId", "14810939333460048050876");
			parameters.put("productCode", "P01000");
			parameters.put("imsi","460028529457924");
			parameters.put("ipProvince", "江苏");
			//parameters.put("moeny", "P01000");
			parameters.put("imei","861262034481337");
			parameters.put("ua", "ALE-UL00-6.0");
			//parameters.put("prov","s25");
			parameters.put("extData", "zcz");
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	@Test//调试回调接口
	public void testSync(){
		JSONObject parameters = new JSONObject();
		parameters.put("code", "0");
		parameters.put("msg", "扣费成功");
		parameters.put("orderId", "10431483");
		parameters.put("price","4.00");
		parameters.put("chargeCode","zl22098A00400");
		parameters.put("mobile", "13044208484");
		parameters.put("transmissionData","14781604929965326430665");
		try {
				String result = HttpClientUtils.doPost(baseURL+"/piple/dswo/sync", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
