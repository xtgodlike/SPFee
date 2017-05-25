package com.qy.sp.fee.modules.piplecode.kongmeng;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class YiYeDongmanJunitTest {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testGetSMS(){
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
		String response = "<response><status>0</status><!-- 是否计费状态，0为成功，其他表示失败 --><transIDO>05c766505e6a4c0bb28cd04f1588f8e1</transIDO><!-- 流水号， --><ConumeCode>1234</ConumeCode><!-- 计费代码 --><amount>20.0</amount><!-- 计费价格 --><serviceCode>200040000001</serviceCode><!-- 业务代码 --><phone>13058411561</phone><!--电话号码，11位 --><cParam>130142</cParam><!-- 渠道自定参数，6位 --></response>";
		
		try {
			String result = HttpClientUtils.doPost("http://139.196.27.18/spfee/piple/yiyedm/sync", response, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
