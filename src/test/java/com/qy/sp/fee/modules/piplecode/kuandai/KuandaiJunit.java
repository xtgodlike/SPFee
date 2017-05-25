package com.qy.sp.fee.modules.piplecode.kuandai;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class KuandaiJunit {
	String baseURL = "http://192.168.0.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";

	@Test
	public void test(){
		String str = "1*1";
		String[] strs = str.split("\\*");
		System.out.println(strs[0]);
	}
	@Test
	public void testNotify(){
		String url = baseURL+"/piple/common/14827354770130719977156/sync?params=&mobile=18586878646&content=DC1*325081301320100*1003&linkid=1227112554510287015&smgw=1065556131&res=OK&remark=鉴权批价成功&orderid=1227112554510287067&Linkid=1227112554510287067&Status=DELIVRD&Spid=WOGAME&Src=&Cmd=DC1*325081301320100*1&mo_msg=DC1*325081301320100*1003&commandid=1065556131&transactionid=9C6AA51168594190B0EAA423066BF28A&code=0&statemsg=%E9%89%B4%E6%9D%83%E6%89%B9%E4%BB%B7%E6%88%90%E5%8A%9F";
		try {
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
