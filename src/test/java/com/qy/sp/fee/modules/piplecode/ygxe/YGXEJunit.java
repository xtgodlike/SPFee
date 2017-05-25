package com.qy.sp.fee.modules.piplecode.ygxe;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class YGXEJunit {
	//String baseURL = "http://www.chinaunigame.net/spfee";
	String baseURL = "http://127.0.0.1:8888/SPFee";
	@Test//调试回调接口
	public void testSync(){
		try {
				String url = baseURL+"/piple/ygxe/sync?spnumber=10658008101815775&linkid=20161122163417&mobile=13800000000&momsg=080%238%23B77n%23merexpand%23351%23e569be49a1be3f43&flag=delivrd";
				String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
