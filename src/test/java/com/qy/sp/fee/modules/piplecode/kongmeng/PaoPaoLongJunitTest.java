package com.qy.sp.fee.modules.piplecode.kongmeng;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class PaoPaoLongJunitTest {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "18586878646");
		parameters.put("imsi", "358534058722196");
		parameters.put("productCode", "P01000");
		parameters.put("imei", "869634020694200");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14606234070959404097244");
		parameters.put("ip", "192.1681.100");
		
		try {
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testLFRDONotify(){
		
		try {
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/piple/paopao/sync?spcode=106616041401&mobile=13456789021&linkid=12321634124321313&mo=1461229371314&status=DELIVRD", HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
