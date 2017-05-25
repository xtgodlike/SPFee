package com.qy.sp.fee.modules.piplecode.base;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;

import net.sf.json.JSONObject;

public class JunitTest {
	
	@Test
	public void testRDO(){
		Map<String,String> parameters = new HashMap<String, String>();
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("mobile", "13058411561");
		parameters.put("pipleId", "14603673704623627130605");
		parameters.put("productCode", "P00100");
		parameters.put("imei", "865521016687625");
		parameters.put("imsi", "460018548284145");
		
		try {
			String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/channel/getSms", JSONObject.fromObject(parameters).toString(), HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSync(){
		try {
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/channel/syncall", HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testGetProvince(){
		try {
			String provinceStr = HttpClientUtils.doGet("http://www.ip138.com:8080/search.asp?action=mobile&mobile=15651938912", HttpClientUtils.GBK);
			int start = provinceStr.lastIndexOf("卡号归属地");
			if(start != -1){
				provinceStr = provinceStr.substring(start+5,start+400 );
				System.out.println(provinceStr);
				provinceStr = provinceStr.substring(0,provinceStr.indexOf("&nbsp;"));
				provinceStr = provinceStr.replaceAll("[^(\\u4e00-\\u9fa5)]", "");
				System.out.println(provinceStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testGetProvince2(){
		try {
			String provinceStr = HttpClientUtils.doGet("http://shouji.supfree.net/fish.asp?cat=15651938912", HttpClientUtils.GBK);
			int start = provinceStr.lastIndexOf("归属地：");
			if(start != -1){
				provinceStr = provinceStr.substring(start+5,start+400 );
				System.out.println(provinceStr);
				provinceStr = provinceStr.substring(0,provinceStr.indexOf("</p>"));
				provinceStr = provinceStr.replaceAll("[^(\\u4e00-\\u9fa5)]", "");
				int index = provinceStr.indexOf("省");
				if(index != -1){
					provinceStr = provinceStr.substring(0,index+1);
				}
			}
			System.out.println(provinceStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testBase64(){
//		String encode = "6BK39DO8DenF2yEz9apqkTiwK4RmsoDjNs6XiAMGtkROoNSrAHs7Uu23N899Lk6ONOxZbvlvdIwxypd206lS/8L/Tt4tq+pm3hzAnVTbv3j/Y8AUOs3PyOL3cPL1rwt+LHKObXsca1iwKqGIhPkThuD+zE5PokOtXPDCLApGobqVGm1TChyP1aVKvDt9+9Zhps9+BPUkAsb1djQlTogJ0tRS7W7lQub9Yz6D9GIe1rYASZf10+BtJmbrhhHsjGvKFdxvl2jZV6BMlNLoWl5A0/nus2F3l3VO90qzyZSrgHw=";
		String encode = "nus2F3l3VO90qzyZSrgHw=";
		String result = new String(Base64.decode(encode,Base64.NO_OPTIONS));
		System.out.println(result);
	}
	@Test
	public void uploadFile(){
		String url = "http://192.168.0.200:8002/SPManage/fileupload/file";
		Map<String,String> map = new HashMap<String, String>();
		map.put("dir", "appid/channelid/imei");
		map.put("fileName", "2016-09-09.log");
		String result = HttpClientUtils.uploadFile(url, map, new File("c:\\2016-09-09.log"), HttpClientUtils.UTF8);
		System.out.println(result);
	}
	@Test
	public void testRandom(){
		Random random = new Random();
		float s = (random.nextInt(350000000)%(850000000-350000000+1) + 350000000)/1000000000.0f;
		System.out.println(s);
	}
	@Test
	public void crateKey(){
		System.out.println(KeyHelper.createKey());
	}
}
