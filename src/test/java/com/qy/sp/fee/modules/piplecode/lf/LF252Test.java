package com.qy.sp.fee.modules.piplecode.lf;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class LF252Test {
	@Test
	public void testLFRDONotify(){
		Map<String,String> parameters = new HashMap<String, String>();
		parameters.put("CPParam", "1234567890");
		parameters.put("stat", "1");
		parameters.put("linkid", "1234567890");
		parameters.put("mobile", "15651938912");
		try {
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/piple/lf252/sync", parameters, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
