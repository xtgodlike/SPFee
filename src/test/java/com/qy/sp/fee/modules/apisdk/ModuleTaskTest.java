package com.qy.sp.fee.modules.apisdk;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.HttpClientUtils;

import net.sf.json.JSONObject;

public class ModuleTaskTest {
	
	String baseUrl = "http://192.168.1.200:8001/SPFee";
	DesUtil desUtil = new DesUtil();
	HashMap<String, String> header = new HashMap<String, String>();
	String desKey = "31553543";
	String imei = "869634020694200";
	@Before
	public void init(){
		desUtil.setKey(desKey);
		header.put("desKey",desKey );
		header.put("imei", imei);
	}
	@Test
	public void testLoadTasks(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("imei", "869634020694200");
		bodyObj.put("channelId", "1003");
//		bodyObj.put("appId", "1");
//		bodyObj.put("provinceId", "1");
		bodyObj.put("stepId", "STEP_KEY_INIT");
		String url = baseUrl+"/api/sdk/task/load";
		String result = "";
		try {
			result = HttpClientUtils.doPost(url,header, desUtil.Encode(bodyObj.toString()), HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:\n"+desUtil.Decode(result));
		
	}
	@Test
	public void testDownloadTasks(){
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("taskId", "1");
		bodyObj.put("imei", "1");
		String url = baseUrl+"/api/sdk/task/download";
		try {
			HttpClientUtils.downloadFile("c:\\1.zip", url, header, desUtil.Encode(bodyObj.toString()),HttpClientUtils.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//14600212476068845039635 打印并且删掉任务
	//14599978731169512259841 打印demo
	//14605427582889639119730 广告 轮训
	//14606002127134267179251 广告监听优化
	//14606934156599842010750 success
	@Test
	public void testUploadTasks(){
		String url = baseUrl+"/api/manager/task/upload";
		try {
			String result = HttpClientUtils.doPost(url,"", HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
