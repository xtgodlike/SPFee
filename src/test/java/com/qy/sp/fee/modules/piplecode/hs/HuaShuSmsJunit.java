package com.qy.sp.fee.modules.piplecode.hs;
import java.net.URLEncoder;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class HuaShuSmsJunit {
//	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	@Test
	public void testNotify(){
		try {
		String url = "http://www.chinaunigame.net/spfee/piple/huashusms/sync?mobile=15193691199&port=10661388&linkid=16111212355988992205&msg=2&status=0000&param=1003P00200extData";
//			String url = "http://192.168.1.200:8001/SPFee/piple/huashusms/sync?mobile=15193691199&port=10661388&linkid=16111212355988992205&msg=2&status=0000&param="+URLEncoder.encode("A1003P00200","UTF-8");
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
