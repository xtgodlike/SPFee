package com.qy.sp.fee.common.utils;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class LTTestHttpRequest {

 public static void main(String[] args) throws UnsupportedEncodingException {

	 	JSONObject parameters = new JSONObject();
	 	parameters.put("paymentUser", "15605228000");
		parameters.put("outTradeNo", "10021496651115468505439139911012300");
		parameters.put("paymentAcount", "001");
		parameters.put("totalFee", 1);
		parameters.put("subject", "test");

		 Map<String, String> authMap = new HashMap<String, String>();
		 authMap.put("appKey", "8fa68f67a6e35b462c676be1b67d74d1992176a3");
		 authMap.put("token", "413979888dfa2fe6d38b5ea8e2f4117f453c1276");

	 	try {
			String result = HttpClientUtils.doAuthPost("https://open.wo.com.cn/openapi/rpc/paymentcodesms/v1.0", parameters.toString(),authMap, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
