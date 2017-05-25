package com.qy.sp.fee.modules.piplecode.others;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class LengShiIvrJunitTest {
	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";

	@Test
	public void testNotify(){
		try {
			String url= baseURL+"/piple/lengshiivr/sync";
			url = url+"?mobile=13407143397&longnumber=1259066210&linkid=1&fees=1&stime=20150611140054&etime=20150611140112";
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
