package com.qy.sp.fee.modules.piplecode.qianya;
import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;

import net.sf.json.JSONObject;

public class QianyaWoAppJunit {
	String baseURL = "http://192.168.0.200:8001/SPFee";
//	String baseURL = "http://139.196.27.18:8090/spfee";
//	String baseURL = "http://www.chinaunigame.net:8100/spfee";
//	String baseURL = "http://123.56.158.156/spfee";
	@Test
	public void testGetMessage(){
		try {
			//10690583026594
//			String message  = "001$1$PM1023$1003$P00010$a106";
			String message  = "001#1#PM1023#2020#P00500#a104#AEE247411635";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet(baseURL+"/api/sdk/phone/syncsimcard?mobile=15651938912&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessageVoCode(){
		try {
			//10690583026594
			String message  = "123";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=15651938912&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSubmitMessage(){
		try {
			//10690583026594
			String message  = "001$2$PM1023$1003$123";
			String encodeMessage = StringUtil.urlEncodeWithUtf8(message);//Base64.encodeBytes(message.getBytes());
			System.out.println(encodeMessage);
			String result = HttpClientUtils.doGet("http://192.168.1.200:8001/SPFee/api/sdk/phone/syncsimcard?mobile=15651938912&msg="+encodeMessage, HttpClientUtils.UTF8);
			System.out.println("result:\n"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testNotify(){
		String url = "http://192.168.1.200:8001/SPFee/piple/qianyawoapp/test?Timespan=20160602202850&Mobile=15651938912&ProductCode=14629556206452657057407&Thefee=0.1&TheOutTradeNo=10021465983998367186442782802875715&OrderStatus=0&ApiKey=shkm2016";
//		String url = "http://192.168.1.200:8001/SPFee/piple/qianyawoapp/sync";
//		String notifyUrl = url+"?Timespan=20160421103343&Mobile=15651938912&ProductCode=14628498450541360034061&Thefee=0.1&TheOutTradeNo=10021464868572365362270867146855249&OrderStatus=0&ApiKey=testc&ExpansionData=xxxxxx";
		try {
			String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test//获取验证码
	public void testGetSMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("mobile", "15651938912");
			parameters.put("pipleId", "14648590456663579169855");
			parameters.put("productCode", "P00010");
			parameters.put("imei", "865521016687625");
			parameters.put("imsi", "460011935302848");
			parameters.put("appId", "a104");
			parameters.put("ipProvince", "江苏");
			
			try {
				String result = HttpClientUtils.doPost(baseURL+"/channel/getSms", parameters.toString(), HttpClientUtils.UTF8);
				System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	@Test//提交验证码
	public void testVertifySMS(){
			JSONObject parameters = new JSONObject();
			parameters.put("apiKey", "1003");
			parameters.put("apiPwd", "B97FED4E9994E33353DFAA8A31428E11BD7AE59");
			parameters.put("pipleId", "14648590456663579169855");
			parameters.put("orderId", "14586131703095654319859");
			parameters.put("vCode", "vCode");
			try {
//		String result = HttpClientUtils.doPost("http://192.168.1.200:8001/SPFee/channel/vertifySms", parameters.toString(), HttpClientUtils.UTF8);
		String result = HttpClientUtils.doPost("http://www.chinaunigame.net/spfee/channel/vertifySms","{\"apiKey\":\"3024\",\"apiPwd\":\"mg12345\",\"pipleId\":\"14648590456663579169855\",\"orderId\":\"14763272554192336521005\",\"vCode\":\"912\"}", HttpClientUtils.UTF8);
		System.out.println("result:\n"+result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	@Test
	public void synch(){
		String urls[] = new String[]{
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706455858540609049651&mobile=13036079007&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706456263032665750295&mobile=13111935933&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706456480608320213743&mobile=13185698999&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706456725308826794137&mobile=13058804265&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457004956313200983&mobile=18658110628&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457255487276034237&mobile=15558221578&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457255777598081720&mobile=15685834113&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457407699625941367&mobile=13056889274&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457503688959921226&mobile=13221155823&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457717181894022835&mobile=13282267359&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457783559668027497&mobile=18658791656&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457783876209699688&mobile=13093759097&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457936852214185936&mobile=13008958966&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706457967483697571396&mobile=13106009310&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706458217245691059044&mobile=13185984848&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706458337542008975648&mobile=13175979018&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706458713818311532180&mobile=13291672025&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706458743432676429779&mobile=15519518663&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706459246827055377445&mobile=18657723161&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706459590452098280793&mobile=13067606718&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706459841682436439881&mobile=13157383236&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706459900796260816719&mobile=13291952141&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706459995735108076082&mobile=18689717368&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706460177776058655821&mobile=13136572631&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706460397303414762366&mobile=13073817776&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706460747600993044384&mobile=18698520192&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706460777769465716688&mobile=13136346773&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706461549767145445212&mobile=18658133221&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706461858953206008388&mobile=13209521084&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706462295314285069189&mobile=13282929651&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706462324739061665174&mobile=13252320163&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706462975844848533735&mobile=13282218702&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463102026466809078&mobile=13035518603&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463227033050836676&mobile=13157925018&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463347931609118201&mobile=13067701709&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463479433180717770&mobile=13185337218&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463695945134487411&mobile=13018912016&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463879038441299527&mobile=18657275721&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706463944248086563979&mobile=13185859366&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706464104728108953456&mobile=13014256391&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706464231125045848194&mobile=13065628519&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706464539626545507133&mobile=13221961255&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023",
				"http://wfwf.appleflying.info/sp/paychunyao_dx_mr?orderId=14706466981225484167926&mobile=13023736331&status=ok&spnumber=P00400&pipleId=14648590456663579169855&productCode=P00400&extData=cyxx_swzz&pipleKey=PM1023"
		};
		for(String url : urls){
			try {
				
				String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
