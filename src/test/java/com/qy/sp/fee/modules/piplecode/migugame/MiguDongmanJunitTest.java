package com.qy.sp.fee.modules.piplecode.migugame;
import org.junit.Test;

import com.qy.sp.fee.common.utils.Base64;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

public class MiguDongmanJunitTest {
	String baseURL = "http://192.168.1.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
//	String baseURL = "http://www.chinaunigame.net/spfee";
	@Test
	public void testGetSMS(){
		JSONObject parameters = new JSONObject();
		parameters.put("mobile", "13666012664");
		parameters.put("imsi", "460021251261945");
		parameters.put("productCode", "P00100");
		parameters.put("imei", "352343059415610");
		parameters.put("apiKey", "1003");
		parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
		parameters.put("pipleId", "14673377178097814534461");
		
		try {
			String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
			System.out.println(result);
			JSONObject resultObj = JSONObject.fromObject(result);
			String num = resultObj.optString("sms_num");
			String message = resultObj.optString("sms_message");
			System.out.println("result:\n"+num+","+new String(Base64.decode(message)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testBAse64(){
		String base64 = "MDAwMDAwMDAzNVUzNDMvIjk0MTU2MTA0ZjAwYzEyNTFORzF5NDVjc2Mwe15mKy8xNUFxQkJiSkN2S0p3PT03bDpcNDBwezJONzc5M2w5MjApUm0yMTJkNjA3MDBlTTAwMHwwSDAwMDAwUUpQdyVnM1VWY2VDdWltRXU3bHByTUk/Y2NfPg==";
		String result = new String(Base64.decode(base64));
		System.out.println(result);//14673571416872036860522
	}

	@Test
	public void testLFRDONotify(){
		String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<request>"
				+ "<ctype>1</ctype>#1音乐 4 动漫  5 视频<channel_id>120001171</channel_id>"
				+ "<sid>e1233bde6edc063258b6f8693c84127a1c2d08fb</sid>"
				+ "<item_id>800000001546</item_id>"
				+ "<imsi>460026176314597</imsi>"
				+ "<imei>357143040437334</imei>"
				+ "<price>0.1</price>"
				+ "<status>0</status>"
				+ "<phone_number>18313024197</phone_number>"
				+ "<message>Success</message>"
				+ "<trans_id>54afdf63b8aa451a8c3aae8920b1745b</trans_id><result></result>"
				+ "<player_url>aHR0cDovL3ZvZC5nc2xiLmNtdmlkZW8uY24vNjk5MDEwLzIwMTUwOTE1LzE2LzIyMDI1Mzc2MzEv"
				+ "ODU2MTY5MzgvY2pieGVneWdkc3pial81NC5tcDQubTN1OD9tc2lzZG49MTgyNTE5ODE0MTYmbWRz"
				+ "cGlkPSZzcGlkPTY5OTAxMCZuZXRUeXBlPTQmc2lkPTIyMDI1Mzc2MzEmcGlkPTIwMjg1OTY4NzMm"
				+ "dGltZXN0YW1wPTIwMTYwMTE1MTYzOTIyJkNoYW5uZWxfSUQ9MDEwOV8wNDA5MDIwMC05OTAwMC0z"
				+ "MDMwMDAxODAwMDAwMDAmUHJvZ3JhbUlEPTYwODA5NDA0MSZQYXJlbnROb2RlSUQ9MTAxODU5NTYm"
				+ "Y2xpZW50X2lwPTIyMi45NC4yMTguMjQ5JmFzc2VydElEPTIyMDI1Mzc2MzEmZW5jcnlwdD1kZDUx"
				+ "YjU5ZWRiMDFiM2EwZDNmMmMxZmUzYjZmMTA2Yw=="
				+ "</player_url>"
				+ "<cpparam>14673558680072666465933</cpparam>#客户计费传进的 cpparam值 "
				+ "<excode>12345</excode>#音乐基地参数回调"
				+ "</request>";
		
		try {
			String result = HttpClientUtils.doPost(baseURL+"/piple/migudongman/sync", response, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testNotifyChannel(){
		String[] urls = new String[]{
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707915359106657693178&mobile=15519207236&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707916225219046049858&mobile=13007814213&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707917783553839086186&mobile=13035509692&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707920052360489618658&mobile=13057700333&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707922104821181184671&mobile=13004643229&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707924939355144525698&mobile=13208062742&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707929470936827808549&mobile=13037866374&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707930001726842644157&mobile=13007968190&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707932125479397757209&mobile=13221219086&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707934101837975110985&mobile=18667192263&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707937016929759320382&mobile=13185386138&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707937658301635668145&mobile=13185338880&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707939831691459982499&mobile=13003617222&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707944149209775682172&mobile=13157950332&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707944329245904966002&mobile=15519440800&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707948642900020997492&mobile=13221814786&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707950226749986483155&mobile=15509540611&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707951393781571721174&mobile=13037898159&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707953788079062612223&mobile=15658730203&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707956520499404791465&mobile=13184374859&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707958854184331855508&mobile=13116590699&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707959254427405705440&mobile=18658219173&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707959909550893219124&mobile=13096840369&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707960406214652932489&mobile=15606703359&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14707965740054695691452&mobile=13251030849&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023"
				};
		try {
			for(String url : urls){
				String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				System.out.println(result);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUrlEncode(){
		String extData = StringUtil.urlEncodeWithUtf8("S01 ");
		System.out.println(extData);
		
	}
}
