package com.qy.sp.fee.modules.piplecode.hs;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class MoJunit {
//	String baseURL = "http://127.0.0.1:8001/SPFee";
	String baseURL = "http://139.196.27.18/spfee";
	@Test//调试回调接口
	public void testSync(){
		try {
				String url = baseURL+"/piple/mozhou/sync?mobile=15651938912&port=10661388&linkid=1611121235598test2&msg=2&status=0000&param=1003P00200extData";
				String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
