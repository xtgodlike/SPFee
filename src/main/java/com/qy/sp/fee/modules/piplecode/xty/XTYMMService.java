package com.qy.sp.fee.modules.piplecode.xty;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;


@Service
public class XTYMMService extends ChannelService{
	private  Logger log = Logger.getLogger(XTYMMService.class);
	public final static int INIT = 0; 
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String P_SUCCESS = "DELIVRD";	  // 同步计费成功
	@Override
	public String getPipleId() {
		return "14797841710198054897225";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("XTYMMService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String iccid = requestBody.optString("iccid");
		String ip = requestBody.optString("ip");
		String mac = requestBody.optString("mac");
//		String model = requestBody.optString("model");//String model = URLEncoder.encode(URLDecoder.decode(requestBody.optString("model"),HttpClientUtils.UTF8),HttpClientUtils.UTF8);
//		String subject = requestBody.optString("subject");//String subject = URLEncoder.encode(URLDecoder.decode(requestBody.optString("subject"),HttpClientUtils.UTF8),HttpClientUtils.UTF8);
//		String appName = requestBody.optString("appName");//String appName = URLEncoder.encode(URLDecoder.decode(requestBody.optString("appName"),HttpClientUtils.UTF8),HttpClientUtils.UTF8);
		String model = URLEncoder.encode(URLDecoder.decode(requestBody.optString("model")));
		String subject = URLEncoder.encode(URLDecoder.decode(requestBody.optString("subject")));
		String appName = URLEncoder.encode(URLDecoder.decode(requestBody.optString("appName")));
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		if(StringUtil.isEmpty(fromType)){
			fromType = GlobalConst.FromType.FROM_TYPE_API;
		}
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId) || StringUtil.isEmpty(imsi) || StringUtil.isEmpty(mobile)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		}else{
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setMobile(mobile);
			// 调用合法性校验
			BaseResult bResult = this.accessVerify(req,pipleId);
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			}
			String groupId = KeyHelper.createKey();
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			//保存订单
			XTYMMOrder order = new XTYMMOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setIccid(iccid);
			order.setIp(ip);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(INIT);
			order.setSubStatus(INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setMac(mac);
			order.setModel(model);
			order.setSubject(subject);
			order.setAppName(appName);
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			order.setGroupId(groupId);
			order.setFromType(Integer.valueOf(fromType));
			order.setExtData(extData);
			SaveOrderInsert(order);
			result.put("orderId",order.getOrderId());
			String reqUrl = piple.getPipleUrlA()+"?feeCode="+pipleProduct.getPipleProductCode()+"&appCode="+piple.getPipleAuthA()+
			"&packCode="+piple.getPipleAuthB()+"&imei="+order.getImei()+"&imsi="+order.getImsi()+"&iccid="+order.getIccid()+
			"&ip="+order.getIp()+"&mac="+order.getMac()+"&model="+order.getModel()+"&subject="+order.getSubject()+
			"&appName="+order.getAppName()+"&extData="+piple.getPipleAuthC();
			try {
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				log.info(" XTYMMService getSmsResult:"+  pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObject = JSONObject.fromObject(pipleResult);
					String resultCode = jsonObject.optString("resultCode");
					if(RES_SUCCESS.equals(resultCode)){
						JSONArray jsonArray = jsonObject.getJSONArray("smsList");
						String linkId = jsonArray.optJSONObject(0).optString("linkId");
						order.setPipleOrderId(linkId);
						for(int i=0;i<jsonArray.size();i++){
							JSONObject smsObject = jsonArray.optJSONObject(i);
							result.put("mo"+i, smsObject.optString("mo"));
							result.put("port"+i, smsObject.optString("port"));
							result.put("moType"+i, smsObject.optString("moType"));
							if(i==0){
								order.setMo0(smsObject.optString("mo"));
								order.setPort0(smsObject.optString("port"));
								order.setMoType0(smsObject.optString("moType"));
							}else if(i==1){
								order.setMo1(smsObject.optString("mo"));
								order.setPort1(smsObject.optString("port"));
								order.setMoType1(smsObject.optString("moType"));
							}
						}
						order.setSubStatus(GETCODE_SUCCESS);
						order.setResultCode(resultCode);
						order.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode", GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
					}else{
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(resultCode);
						order.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败：");
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GETCODE_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
					return result;
				}
				SaveOrderUpdate(order);
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}catch(Exception e){
				e.printStackTrace();
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
				order.setModTime(DateTimeUtils.getCurrentTime());
				SaveOrderUpdate(order);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败，接口异常:"+e.getMessage());
				return result;
			}
		}
	
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("XTYMMService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String mobile = requestBody.optString("mobile");
		String spnumber = requestBody.optString("spnumber");
		String linkid = requestBody.optString("linkid");
		String momsg = requestBody.optString("momsg");
		String status = requestBody.optString("status");
		TOrder order = tOrderDao.selectByPipleOrderId(linkid);
		order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
		if(order!=null ){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			if(P_SUCCESS.equals(status)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
				if(!bDeducted){ // 不扣量 通知渠道
					notifyChannelAll(cp.getNotifyUrl(), order, null);
				}
			}else {
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderUpdate(order);
			
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	
	public class XTYMMOrder extends TOrder{
		private String imei;
		private String ip; 
		private String mac; 
		private String model; 
		private String subject; 
		private String appName;
		private String mo0;
		private String mo1;
		private String port0;
		private String port1;
		private String moType0;
		private String moType1;
		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getMac() {
			return mac;
		}

		public void setMac(String mac) {
			this.mac = mac;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public String getMo0() {
			return mo0;
		}

		public void setMo0(String mo0) {
			this.mo0 = mo0;
		}

		public String getMo1() {
			return mo1;
		}

		public void setMo1(String mo1) {
			this.mo1 = mo1;
		}

		public String getPort0() {
			return port0;
		}

		public void setPort0(String port0) {
			this.port0 = port0;
		}

		public String getPort1() {
			return port1;
		}

		public void setPort1(String port1) {
			this.port1 = port1;
		}

		public String getMoType0() {
			return moType0;
		}

		public void setMoType0(String moType0) {
			this.moType0 = moType0;
		}

		public String getMoType1() {
			return moType1;
		}

		public void setMoType1(String moType1) {
			this.moType1 = moType1;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.imei != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("imei");
				oExt.setExtValue(this.imei);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.ip != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("ip");
				oExt.setExtValue(this.ip);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.mac != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("mac");
				oExt.setExtValue(this.mac);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.model != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("model");
				oExt.setExtValue(this.model);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.subject != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("subject");
				oExt.setExtValue(this.subject);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.appName != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("appName");
				oExt.setExtValue(this.appName);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.mo0 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("mo0");
				oExt.setExtValue(this.mo0);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.mo1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("mo1");
				oExt.setExtValue(this.mo1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.port0 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("port0");
				oExt.setExtValue(this.port0);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.port1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("port1");
				oExt.setExtValue(this.port1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.moType0 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("moType0");
				oExt.setExtValue(this.moType0);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.moType1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("moType1");
				oExt.setExtValue(this.moType1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
