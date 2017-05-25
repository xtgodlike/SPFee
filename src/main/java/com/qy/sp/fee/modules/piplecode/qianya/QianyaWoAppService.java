package com.qy.sp.fee.modules.piplecode.qianya;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MapCacheManager;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.common.utils.XMLUtils;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

@Service
public class QianyaWoAppService extends ChannelService {

	public static final String QIANYA_CP_KEY = "newqy";
	public static final String QIANYA_CP_PASSWORD = "newqy0607";
	public static final String YYSK_CP_KEY = "yunyiyingyong";
	public static final String YYSK_CP_PASSWORD = "yunyiyingyong123";
	public static final String APPID_A106 = "a106";
	public static final String APPID_A107 = "a107";
	public static final String APPID_A113 = "a113";
	private static Map<String,String> goodMap = new ConcurrentHashMap<String, String>();
	
	@Override
	public String getPipleId() {
		return "14648590456663579169855";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String iccid = requestBody.optString("iccid");
		String appId = requestBody.optString("appId");
		String ipProvince = requestBody.optString("ipProvince");
		String extData = requestBody.optString("extData");
		JSONObject result = new JSONObject();
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey) || StringUtil.isEmptyString(mobile)|| StringUtil.isEmptyString(appId) ){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		} else {
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setPipleId(pipleId);
			req.setMobile(mobile);
			req.setIpProvince(ipProvince);
			BaseResult bResult = this.accessVerify(req);
			if (bResult != null) {// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			} else {
				String groupId = KeyHelper.createKey();
				statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
				TPiple tPiple = this.tPipleDao.selectByPrimaryKey(getPipleId());

				TProduct tProduct = this.tProductDao.selectByCode(productCode);
				TChannel tChannel = this.tChannelDao.selectByApiKey(apiKey);
				TOrder order = new TOrder();
				order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
				order.setChannelId(tChannel.getChannelId());
				order.setCreateTime(DateTimeUtils.getCurrentTime());
				order.setIccid(iccid);
				order.setImei(imei);
				order.setImsi(imsi);
				order.setMobile(mobile);
				order.setOrderId(KeyHelper.createKey());
				order.setOrderStatus(GlobalConst.OrderStatus.INIT);
				order.setPipleId(getPipleId());
				order.setProductId(tProduct.getProductId());
				order.setProvinceId(req.getProvinceId());
				order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
				order.setGroupId(groupId);
				order.setExtData(extData);
				order.setAppId(appId);
				this.SaveOrderInsert(order);

				result.put("orderId",order.getOrderId());
				
//				TPipleProductKey key = new TPipleProductKey();
//				key.setPipleId(getPipleId());
//				key.setProductId(tProduct.getProductId());
//				TPipleProduct tPipleProduct = this.tPipleProductDao.selectByPrimaryKey(key);
				String pipleProductCode = goodMap.get(appId+productCode);
				if(StringUtil.isEmpty(pipleProductCode)){
					result.put("resultCode",GlobalConst.CheckResult.PRODUCT_APPID_ERROR+"");
					result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PRODUCT_APPID_ERROR));
					return result;
				}
				Map<String, String> params = new HashMap<String, String>();
				params.put("paymentUser", mobile);
				params.put("productCode",pipleProductCode);
				if(APPID_A106.equals(appId) || APPID_A107.equals(appId) || APPID_A113.equals(appId) ){ // appid=a106,a107,a113 为云逸时空的APP
					params.put("apiKey", YYSK_CP_KEY);
					params.put("apiPwd", YYSK_CP_PASSWORD);
				}else{
					params.put("apiKey", QIANYA_CP_KEY);
					params.put("apiPwd", QIANYA_CP_PASSWORD);
				}
				params.put("expansionData", extData);
				params.put("chargeType", "6");
				params.put("imsi", imsi);
				params.put("iccid", iccid);
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,tPiple.getPipleUrlA()+";"+JSONObject.fromObject(params).toString());
				try {
					String response = HttpClientUtils.doPost(tPiple.getPipleUrlA(), params, "UTF-8");
					if(!StringUtil.isEmpty(response)){
						String resultCode= "";
						String outTradeNo = "";
						String resultDescription = "";
						Document document = XMLUtils.getDocumentByStr(response);
						Element root = XMLUtils.getRootNode(document);
						Element e = (Element) root.element("resultCode");
						if(e != null){
							resultCode = e.getText();
						}
						e = (Element) root.element("outTradeNo"); 
						if(e != null){
							outTradeNo = e.getText();
						}
						e = (Element) root.element("resultDescription"); 
						if(e != null){
							resultDescription = e.getText();
						}
						statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId, response.toString());
						order.setTOrder(order);						
						order.setResultCode(resultCode);
						order.setPipleOrderId(outTradeNo);
						result.put("resultCode",resultCode);
						result.put("resultMsg",resultDescription);
						if(GlobalConst.Result.SUCCESS.equals(resultCode)){
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						}else{
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						}					
						this.SaveOrderUpdate(order);
					}else{
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","调用接口失败：接口异常");
						return result;
					}
					statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		JSONObject result = new JSONObject();
		String apiKey = requestBody.optString("apiKey");
		
		String orderId = requestBody.optString("orderId");
		String vCode = requestBody.optString("vCode");
		result.put("orderId",orderId);
		if(StringUtil.isEmpty(apiKey) || StringUtil.isEmpty(orderId) || StringUtil.isEmpty(vCode)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		}else{
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder == null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
			}else{
				// 校验用户日月限  防止第1步与第2步间隔时间内多次请求造成校验误差
				if(isUseableTradeDayAndMonth()){
					TProduct product = tProductDao.selectByPrimaryKey(tOrder.getProductId());
					// 用户日月限
					int checkResult = transCheckMobile(tOrder.getPipleId(), tOrder.getImsi(), tOrder.getMobile(),product.getProductCode());
					if(checkResult!=GlobalConst.CheckResult.PASS){
						result.put("resultCode",checkResult+"");
						result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(checkResult));
						return result;
					}
				}
				
				try {
					statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
					TPiple tPiple = this.tPipleDao.selectByPrimaryKey(getPipleId());
					Map<String, String> params = new HashMap<String, String>();
					params.put("outTradeNo", tOrder.getPipleOrderId());
					params.put("paymentcodesms", vCode);
					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), tPiple.getPipleUrlB()+";"+JSONObject.fromObject(params).toString());
					String response = HttpClientUtils.doPost(tPiple.getPipleUrlB(), params, "UTF-8");
					if(!StringUtil.isEmpty(response)){
						statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), response);
						String resultCode = "";
						String resultDescription = "";
						Document document = XMLUtils.getDocumentByStr(response);
						Element root = XMLUtils.getRootNode(document);
						Element e = (Element) root.element("resultCode"); 
						if(e != null){
							resultCode = e.getText();
						}
						e = (Element) root.element("resultDescription"); 
						if(e != null){
							resultDescription = e.getText();
						}
						if(!"0".equals(resultCode)){
							tOrder.setResultCode(resultCode);
							tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							result.put("resultCode",resultCode);
							result.put("resultMsg",resultDescription);
							this.SaveOrderUpdate(tOrder);
						}else{
							JSONObject rquestObj = new JSONObject();
							rquestObj.put("pipleOrderId", tOrder.getPipleOrderId());
							rquestObj.put("status", resultCode);
							rquestObj.put("mobile", tOrder.getMobile());
							boolean isSend = processPay(rquestObj);
							if(isSend){
								result.put("resultCode",resultCode);
								result.put("resultMsg","提交验证码成功");
							}else{
								// 扣量返回固定失败信息  
								result.put("resultCode","607");
								result.put("resultMsg","扣费失败。");
							}
						}
					}else{
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","调用接口失败：接口异常");
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","接口内部异常");
				}
				statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), JSONObject.fromObject(result).toString());
			}
		}
		return result;
	}
	
	@Override
	public String getPipleKey() {
		return "PM1023";
	}
	@Override
	public String processGetMessage(String mobile,String requestBody) throws Exception {
		String resultMsg = "";
		String args[] = requestBody.split("\\$");
		String apiKey = args[0];
		String productCode = args[1];
		String appId = args[2];
		String extData = null;
		if(args.length >3){
			extData = args[3];
			
		}
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("productCode",productCode);
		request.put("mobile",mobile);
		request.put("extData",extData);
		request.put("appId", appId);
		JSONObject result = processGetSMS(request);
		if(result != null){
			if("0".equals(result.optString("resultCode"))){
				JSONObject param = new JSONObject();
				param.put("orderId",result.optString("orderId") );
				param.put("apiKey", apiKey);
				param.put("pipleKey", getPipleKey());
				param.put("productCode", productCode);
				MapCacheManager.getInstance().getSmsOrderCache().put(mobile,param.toString());
			}
			if(StringUtil.isNotEmptyString(result.optString("orderId"))){
				TOrder tOrder = tOrderDao.selectByPrimaryKey(result.optString("orderId"));
				if(tOrder != null){
					statistics(STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"1$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
				}
			}
			logger.debug(JSONObject.fromObject(result).toString());
		}
		return resultMsg;
	}
	@Override
	public String processSubmitMessage(String mobile,String requestBody) throws Exception {
		String jsonStr = MapCacheManager.getInstance().getSmsOrderCache().get(mobile);
		JSONObject param = JSONObject.fromObject(jsonStr);
		String orderId = param.optString("orderId");
		if(StringUtil.isEmpty(orderId)){
			return "";
		}
		TOrder tOrder = tOrderDao.selectByPrimaryKey(orderId);
		if(tOrder == null)
			return "";
		statistics(STEP_SUBMIT_MESSAGE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody);
		String args[] = requestBody.split("\\$");
		if(args.length <2)
			return "";
		String apiKey = args[0];
		String vcode = args[1];
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("orderId",tOrder.getOrderId());
		request.put("vCode",vcode);
		JSONObject result = processVertifySMS(request);
		MapCacheManager.getInstance().getSmsOrderCache().remove(mobile);
		if(result != null){
			logger.debug(JSONObject.fromObject(result).toString());
			statistics(STEP_SUBMIT_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
		}
		return "";
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		return "";
	}
	public boolean processPay(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return false;
		String pipleOrderId = requestBody.optString("pipleOrderId");
		String status = requestBody.optString("status");
		String mobile = requestBody.optString("mobile");
		String extData = requestBody.optString("extData");
		TOrder order = tOrderDao.selectByPipleOrderId(pipleOrderId);
		boolean isSend = false;
		if(order!=null ){ // 同步数据正确
			if(order.getOrderStatus() == GlobalConst.OrderStatus.SUCCESS){
				return false;
			}
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return false;
			}
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			//扣量
			if("0".equals(status)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				order.setMobile(mobile);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
				notifyChannel(cp.getNotifyUrl(),order, productCode,"ok");
			}
		}
		return isSend;
	}

	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	
	public String getAppId(JSONObject requestObj){
		String appId = requestObj.optString("appId");
//		String appId = requestObj.optString("");
		return null;
	}
	static{
		goodMap.put("a101"+"P00010", "14628498450541360034061");
		goodMap.put("a101"+"P00200", "14627857109314198491649");
		goodMap.put("a101"+"P00400", "14646625469873610570119");
		goodMap.put("a101"+"P00500", "14636498990675795356240");
		goodMap.put("a101"+"P00600", "14627857435784780374087");
		goodMap.put("a101"+"P00800", "14636445032037975828447");
		goodMap.put("a101"+"P01000", "14627857913137067938378");
		goodMap.put("a101"+"P01500", "14636499251482821728496");
		goodMap.put("a101"+"P02000", "14636499343939149072290");
		
		goodMap.put("a102"+"P00010", "14629556206452657057407");
		goodMap.put("a102"+"P00100", "14633805675367833106752");
		goodMap.put("a102"+"P00200", "14630377985209365229511");
		goodMap.put("a102"+"P00300", "14633806150298155982770");
		goodMap.put("a102"+"P00400", "14633806217793113058971");
		goodMap.put("a102"+"P00500", "14633806297313554315485");
		goodMap.put("a102"+"P00600", "14630379036693603479973");
		goodMap.put("a102"+"P00700", "14633806517304766559511");
		goodMap.put("a102"+"P00800", "14630379190939856560640");
		goodMap.put("a102"+"P00900", "14633806589399653242443");
		goodMap.put("a102"+"P01000", "14630208774648821168937");
		goodMap.put("a102"+"P01100", "14633806709785076298253");
		goodMap.put("a102"+"P01200", "14633869516845262413427");
		goodMap.put("a102"+"P01300", "14633806909620545121059");
		goodMap.put("a102"+"P01500", "14633807342266466931291");
		goodMap.put("a102"+"P01600", "14633807407215531681269");
		goodMap.put("a102"+"P01800", "14633807474094863070738");
		goodMap.put("a102"+"P02000", "14629556440442179103504");
		goodMap.put("a102"+"P02300", "14633807732439877042047");
		goodMap.put("a102"+"P02500", "14633807796092938525376");
		goodMap.put("a102"+"P02800", "14633807946087657434819");
		goodMap.put("a102"+"P03000", "14633808062402038767653");
		goodMap.put("a102"+"P05000", "14633808160812373689957");
		
		goodMap.put("a103"+"P00010", "14629557950435240251210");
		goodMap.put("a103"+"P00100", "14634742092003374057122");
		goodMap.put("a103"+"P00200", "14634742184942661781860");
		goodMap.put("a103"+"P00400", "14634742285982985438334");
		goodMap.put("a103"+"P00500", "14636498990675795356240");
		goodMap.put("a103"+"P00600", "14634742486631867256184");
		goodMap.put("a103"+"P00800", "14634742717313597761297");
		goodMap.put("a103"+"P01000", "14634742823091978488929");
		goodMap.put("a103"+"P01200", "14636396191309074946459");
		goodMap.put("a103"+"P01500", "14636387432168723967930");
		goodMap.put("a103"+"P01600", "14634743079428466765059");
		goodMap.put("a103"+"P01800", "14636387908677679715092");
		goodMap.put("a103"+"P02000", "14636392031593001888530");
		
		goodMap.put("a103"+"P00010", "14629557950435240251210");
		goodMap.put("a103"+"P00100", "14634742092003374057122");
		goodMap.put("a103"+"P00200", "14634742184942661781860");
		goodMap.put("a103"+"P00400", "14634742285982985438334");
		goodMap.put("a103"+"P00500", "14636498990675795356240");
		goodMap.put("a103"+"P00600", "14634742486631867256184");
		goodMap.put("a103"+"P00800", "14634742717313597761297");
		goodMap.put("a103"+"P01000", "14634742823091978488929");
		goodMap.put("a103"+"P01200", "14636396191309074946459");
		goodMap.put("a103"+"P01500", "14636387432168723967930");
		goodMap.put("a103"+"P01600", "14634743079428466765059");
		goodMap.put("a103"+"P01800", "14636387908677679715092");
		goodMap.put("a103"+"P02000", "14636392031593001888530");
		
		goodMap.put("a104"+"P00010", "14653494839256706042322");
		goodMap.put("a104"+"P00500", "14653494942991619603792");
		goodMap.put("a104"+"P01000", "14653495116948232967651");
		goodMap.put("a104"+"P01500", "14653495502416646581502");
		goodMap.put("a104"+"P02000", "14653495310786753077610");
		
		goodMap.put("a105"+"P00010", "14653495689413070262548");
		goodMap.put("a105"+"P00100", "14657125017051314927964");
		goodMap.put("a105"+"P00200", "14653578245379095132881");
		goodMap.put("a105"+"P00400", "14653578473951497427148");
		goodMap.put("a105"+"P00500", "14653496652619449987875");
		goodMap.put("a105"+"P00600", "14653578632159494552342");
		goodMap.put("a105"+"P00800", "14653496218111341449037");
		goodMap.put("a105"+"P01000", "14653496313494327156742");
		goodMap.put("a105"+"P01200", "14653578799970767463750");
		goodMap.put("a105"+"P01500", "14653578973841120242593");
		goodMap.put("a105"+"P01600", "14657126234798367767594");
		goodMap.put("a105"+"P02000", "14653496496072326036173");
		
		goodMap.put("a106"+"P00010", "14657149647558097668223");
		goodMap.put("a106"+"P00100", "14657149763009879692130");
		goodMap.put("a106"+"P00200", "14657149834527028465715");
		goodMap.put("a106"+"P00400", "14657149943974575227524");
		goodMap.put("a106"+"P00500", "14657150820057201893859");
		goodMap.put("a106"+"P00600", "14657151114194146583765");
		goodMap.put("a106"+"P00800", "14657151191558730175380");
		goodMap.put("a106"+"P01000", "14657151266369086973493");
		goodMap.put("a106"+"P01200", "14657151337399405841300");
		goodMap.put("a106"+"P01500", "14657151872755086148864");
		goodMap.put("a106"+"P02000", "14657151968905059960556");
		
		goodMap.put("a107"+"P00010", "14683926211371339094776");
		goodMap.put("a107"+"P00100", "14683922287383992697108");
		goodMap.put("a107"+"P00200", "14683922526187807233956");
		goodMap.put("a107"+"P00300", "14683922620379575135727");
		goodMap.put("a107"+"P00400", "14683922746039066825708");
		goodMap.put("a107"+"P00500", "14683922852120041760933");
		goodMap.put("a107"+"P00600", "14683923031553255524545");
		goodMap.put("a107"+"P00700", "14683923153903540895089");
		goodMap.put("a107"+"P00800", "14683923246920039824885");
		goodMap.put("a107"+"P00900", "14683923331818190123085");
		goodMap.put("a107"+"P01000", "14683923436180556255902");
		goodMap.put("a107"+"P01100", "14683923571722199405994");
		goodMap.put("a107"+"P01200", "14683923681324217472786");
		goodMap.put("a107"+"P01300", "14683923802615233771700");
		goodMap.put("a107"+"P01500", "14683923976764476539708");
		goodMap.put("a107"+"P01600", "14683924057601472105059");
		goodMap.put("a107"+"P01800", "14683924245734528186112");
		goodMap.put("a107"+"P02000", "14683924440994886447520");
					
		goodMap.put("a113"+"P00010", "14683903985546106312183");
		goodMap.put("a113"+"P00100", "14683904169924150322997");
		goodMap.put("a113"+"P00200", "14683904301353989532495");
		goodMap.put("a113"+"P00300", "14683914388920949036233");
		goodMap.put("a113"+"P00400", "14683915386527455246002");
		goodMap.put("a113"+"P00500", "14683915825264189078815");
		goodMap.put("a113"+"P00600", "14683915919108694482842");
		goodMap.put("a113"+"P00700", "14683916024006491759699");
		goodMap.put("a113"+"P00800", "14683916167965475112593");
		goodMap.put("a113"+"P00900", "14683916417835145070018");
		goodMap.put("a113"+"P01000", "14683916539592862705887");
		goodMap.put("a113"+"P01100", "14683916812630913369475");
		goodMap.put("a113"+"P01200", "14683918395969124278696");
		goodMap.put("a113"+"P01300", "14683918481330199793366");
		goodMap.put("a113"+"P01500", "14683920785706345735702");
		goodMap.put("a113"+"P01600", "14683920901817494699233");
		goodMap.put("a113"+"P01800", "14683921175127895030770");
		goodMap.put("a113"+"P02000", "14683921501083938443400");
		
	}
	
	public static void main(String[] args) {
	}
}
