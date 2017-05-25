package com.qy.sp.fee.modules.piplecode.mrb;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class MRBJunit {
	//String baseURL = "http://www.chinaunigame.net/spfee";
	String baseURL = "http://127.0.0.1:8888/SPFee";
	@Test//调试回调接口
	public void testSync(){
		try {
				String url = baseURL+"/piple/mrb/sync?spnumber=2224&mobile=13800000000&linkid=test_20161122161836&msg=YX,260637,11,17E1,1822051,06,33056G3&status=DELIVRD";
				String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
