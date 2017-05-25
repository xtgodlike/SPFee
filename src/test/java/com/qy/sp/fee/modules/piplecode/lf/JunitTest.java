package com.qy.sp.fee.modules.piplecode.lf;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class JunitTest {
	@Test
	public void testGetSMS(){
		Map<String,String> parameters = new HashMap<String, String>();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("mobile", "13058411561");
		parameters.put("pipleId", "14603673704623627130605");
		parameters.put("productCode", "P00100");
		parameters.put("imei", "865521016687625");
		parameters.put("imsi", "460018548284145");
		
		try {
			String result = HttpClientUtils.doPost("http://139.196.27.18/spfee/channel/getSms", parameters, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testVertifySMS(){
		Map<String,String> parameters = new HashMap<String, String>();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14603673704623627130605");
		parameters.put("orderId", "14586131703095654319859");
		parameters.put("vCode", "vCode");
		
		try {
			String result = HttpClientUtils.doPost("http://139.196.27.18/spfee/channel/vertifySms", parameters, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testNotify(){
		try {
			String channelUrl = "http://your_server_address/test.jsp";
			String orderId = "14586131703095654319859";
			String mobile = "13058411561";
			String status = "ok";
			String spnumber = "P00100";
			String pipleId = "14586131703095654319859";
			String extData = null;
			String param = "orderId="+orderId+"&mobile="+mobile+"&status="+status+"&spnumber="+spnumber+"&pipleId="+pipleId;;
			if(!StringUtil.isEmpty(extData)){
				param += "&extData="+extData;
			}
			String ackUrl = channelUrl+"?"+param;
			String rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
