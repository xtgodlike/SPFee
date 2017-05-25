package com.qy.sp.fee.modules.piplecode.kongmeng;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class KongmengRDOJunitTest {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testDONotify(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "18313024197");
		parameters.put("imsi", "460021251261945");
		parameters.put("productCode", "P02000");
		parameters.put("imei", "352343059415610");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("extData", "extData");
		parameters.put("pipleId", "14591308675936084686599");
		
		try {
			String result = HttpClientUtils.doPost("http://139.196.27.18/spfee/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testLFRDONotify(){
		try {
			String result = HttpClientUtils.doGet("http://data.yolotone.com:8020/statsynd/interface/shanghaikongmeng/momr_rdo_eight.jsp?mobile="+"15130237032"+"&orderId=14601241369168191984007&amount=6.0&orderStatus=2&productCode=P00600", HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
