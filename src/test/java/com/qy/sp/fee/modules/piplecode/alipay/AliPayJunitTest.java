package com.qy.sp.fee.modules.piplecode.alipay;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

public class AliPayJunitTest {
	String baseURL = "http://192.168.0.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	@Test
	public void testGetOrder() throws Exception{
		 Map<String,String> param = new HashMap<String,String>();
		param.put("apiKey","1003");
        param.put("apiPwd","B97FED4E9994E33353DFAA8A31428E11BD7AE59");
        param.put("appId","a102");
        param.put("body","测试");
        param.put("subject","测试");
        param.put("price","0.2");
        String result = HttpClientUtils.doPost(baseURL+"/channel/alipay/order", param, HttpClientUtils.UTF8);
        System.out.println(result);
	}
	@Test
	public void testNotify() throws Exception{
		/*
		 * 	{"buyer_id":"2088802295533740","trade_no":"2016103121001004740223488953","body":"测试","use_coupon":"N","notify_time":"2016-10-31 18:36:03","subject":"测试","sign_type":"RSA","is_total_fee_adjust":"N","notify_type":"trade_status_sync","out_trade_no":"14779101555849546603473","gmt_payment":"2016-10-31 18:36:02","trade_status":"TRADE_SUCCESS","discount":"0.00","sign":"DJa6myvBLZ/vps1BiOQuNxGLbN2F6t1H66J2LZUJienmQFs07qGZbATGYoi4Oa9WXPtF6OdeMFx/9I5d9Z8nFH6PHHqGxWTPQu7PXwEo3fRBwami3ArxO2RaKu0h/ul2C2gtSsN+v03mYJ/5kbUETF/gz6J/9mrow7XnbK7nTgY=","buyer_email":"18073070117","gmt_create":"2016-10-31 18:36:02","price":"0.10","total_fee":"0.10","quantity":"1","seller_id":"2088411889864894","notify_id":"5e54e8b17d0bc36cc654c865ba1e3edlpm","seller_email":"tiansongsong@newqy.net","payment_type":"1"}
		 */
		String requestUrl = baseURL+"/piple/alipay/notify"+"?discount=0.00&payment_type=1&subject="+StringUtil.urlEncodeWithUtf8("测试")+"&trade_no=2016103121001004740223488953&buyer_email="+StringUtil.urlEncodeWithUtf8("18073070117")+"&gmt_create="+StringUtil.urlEncodeWithUtf8("2016-10-31 18:36:02")+"&notify_type=trade_status_sync&quantity=1&out_trade_no=14779101555849546603473&seller_id=2088411889864894&notify_time="+StringUtil.urlEncodeWithUtf8("2016-10-31 18:36:03")+"&body="+StringUtil.urlEncodeWithUtf8("测试")+"&trade_status=TRADE_SUCCESS&is_total_fee_adjust=N&total_fee=0.10&gmt_payment="+StringUtil.urlEncodeWithUtf8("2016-10-31 18:36:02")+"&seller_email="+StringUtil.urlEncodeWithUtf8("tiansongsong@newqy.net")+"&price=0.10&buyer_id=2088802295533740&notify_id=5e54e8b17d0bc36cc654c865ba1e3edlpm&use_coupon=N&sign_type=RSA&sign="+StringUtil.urlEncodeWithUtf8("DJa6myvBLZ/vps1BiOQuNxGLbN2F6t1H66J2LZUJienmQFs07qGZbATGYoi4Oa9WXPtF6OdeMFx/9I5d9Z8nFH6PHHqGxWTPQu7PXwEo3fRBwami3ArxO2RaKu0h/ul2C2gtSsN+v03mYJ/5kbUETF/gz6J/9mrow7XnbK7nTgY=");
		String result = HttpClientUtils.doGet(requestUrl,HttpClientUtils.UTF8);
		System.out.println(result);
	}
	@Test
	public void testQuery() throws Exception{
		String orderId= "14782302649494136626226";
		String requestUrl = baseURL+"/channel/alipay/query?orderId="+orderId;
		String result = HttpClientUtils.doGet(requestUrl,HttpClientUtils.UTF8);
		System.out.println(result);
	}
}
