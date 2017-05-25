package com.qy.sp.fee.modules.piplecode.others;
import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.RSAUtil;

import net.sf.json.JSONObject;

public class JiFengDuihuanJunitTest2 {
//	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	String baseURL = "http://www.chinaunigame.net/spfee";

	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("pipleId", "14737597021721395692283");
			parameters.put("mobile", "18073070117");
			parameters.put("code", "04bd2x8aad2x4e45ax6a9c3");
			
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	@Test
	public void testEncrpt(){
		try {
			byte bytes[] = RSAUtil.encryptByPublicKey("9e1f6x68ce2xef413x8bcaf".getBytes(),JiaHeDuiHuanService.publicKey);
			String code = Base64.encodeBytes(bytes,Base64.NO_OPTIONS);	
			System.out.println(code);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
