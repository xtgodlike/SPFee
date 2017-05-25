package com.qy.sp.fee.modules.piplecode.kongmeng;
import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class ShiJiDongmanJunit {
	//192.168.1.200:8001/SPFee
	//139.196.27.18/spfee
	@Test
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "18313024197");
		parameters.put("imsi", "460021251261945");
		parameters.put("productCode", "P00800");
		parameters.put("imei", "352343059415610");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14624344227957916493355");
		
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
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/piple/sjdm/sync?LinkID=lg91461509051210000694&RptStat=DELIVRD&ExtData=14641600013920872193512&FeeCode=800&GameID=10000&City=%e5%8c%97%e4%ba%ac&Mobile=18313024197", HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testDecode(){
		String str = "MDAwMDkwNzczNVUzNDMvIjk0MTU2MTA0ZjAwYzEyNTFORzF5NDVnMTF2aTRKZDhDakx6Zm5UR2twMDhnPT0zOHptMTR8MmdJMzZMeWQxMjApUm0wMjdlNjU4MzFlTTAwMHwwSDAwMDAwVTJYMSZjIiN0M0MlaEdsMzJKdnw5VHZNY3plPg==";
		System.out.println(new String(Base64.decode(str)));;
	}

}
