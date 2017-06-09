package com.qy.sp.fee.modules.piplecode.dxtv;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DXTVideoService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "succ";	  // 同步计费成功
	private  Logger log = Logger.getLogger(DXTVideoService.class);
	@Override
	public String getPipleId() {
		return "14958555721785335696481";
	}

	@Override
	public String getPipleKey() {
		return "PM1065";
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("DXTVideoService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleKey = requestBody.optString("pipleKey");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleKey) || StringUtil.isEmpty(imsi) || StringUtil.isEmpty(mobile)){
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
			TPiple tPiple = tPipleDao.selectByPipleKey(pipleKey);
			String pipleId = tPiple==null?"":tPiple.getPipleId();
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
			DXTVideoOrder order = new DXTVideoOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(INIT);
			order.setSubStatus(INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			order.setGroupId(groupId);
			SaveOrderInsert(order);
			result.put("orderId",order.getOrderId());
			//请求第一条短信
			Map<String, String> params = new HashMap<String, String>();
			params.put("qid", piple.getPipleAuthA());
			params.put("imsi", order.getImsi());
			params.put("imei", order.getImei());
			params.put("cp_param", order.getOrderId());  // 我方orderId
			String reqUrl = piple.getPipleUrlA()+"?"+"qid="+piple.getPipleAuthA()+"&imsi="+order.getImsi()+"&imei="+order.getImei()+"&cp_param="+order.getOrderId();
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
//			String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			String pipleResult = HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
			log.info(" DXTVideoService getSmsResult:"+  pipleResult);
			if(pipleResult != null && !"".equals(pipleResult)){
				JSONObject jsonObj = JSONObject.fromObject(pipleResult);
				String status = null;
				String data = null;
				String sms1 = null;
				String msgtxtenc = null;
				String sms2 = null;
				String msgtxtenc2 = null;
				if(jsonObj.has("status") ){
					status = jsonObj.getString("status");
					if(RES_SUCCESS.equals(status)){ // 返回成功
						data = jsonObj.getString("data");
						JSONObject dataObj = JSONObject.fromObject(data);
						sms1 = dataObj.getString("sms1");
						msgtxtenc = dataObj.getString("msgtxtenc");
						sms2 = dataObj.getString("sms2");
						msgtxtenc2 = dataObj.getString("msgtxtenc2");
						//						 order.setPipleOrderId(billid);
						order.setSmsNumber1(sms1);
						order.setSmsContent1(msgtxtenc);
						order.setSmsNumber2(sms2);
						order.setSmsContent2(msgtxtenc2);
						result.put("smsNumber1", sms1);
						result.put("smsContent1", msgtxtenc);
						result.put("smsNumber2", sms2);
						result.put("smsContent2", msgtxtenc2);
						// 更新订单信息
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GETCODE_SUCCESS);
						order.setResultCode(GlobalConst.Result.SUCCESS);
						order.setModTime(DateTimeUtils.getCurrentTime());
						//设置返回结果
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功");
						SaveOrderUpdate(order);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}else{
						status = jsonObj.getString("status");
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(status);
						order.setModTime(DateTimeUtils.getCurrentTime());
						SaveOrderUpdate(order);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+status);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}

				}else{
					status = jsonObj.getString("status");
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GETCODE_FAIL);
					order.setResultCode(status);
					order.setModTime(DateTimeUtils.getCurrentTime());
					SaveOrderUpdate(order);
					result.put("resultCode", GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败:"+status);
					statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
					return result;
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GETCODE_FAIL);
				order.setModTime(DateTimeUtils.getCurrentTime());
				SaveOrderUpdate(order);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败，接口异常");
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}
		}
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("DXTVideoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String imsi = requestBody.optString("imsi");
		String qid = requestBody.optString("qid");
		String subscribe_time = requestBody.optString("subscribe_time");
		String cp_param = requestBody.optString("cp_param");  // 我方订单号
		String result = requestBody.optString("result");
		String tran_id = requestBody.optString("tran_id");
		TOrder order = tOrderDao.selectByPrimaryKey(cp_param);
		if(order!=null ){ // 同步数据正确
			order.setPipleOrderId(tran_id); // 通道订单号
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			//扣量
			boolean bDeducted = false;
			if(P_SUCCESS.equals(result)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(result);
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
				if(!bDeducted){ // 不扣量 通知渠道
//				notifyChannel(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
//				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
					notifyChannelAPI(cp.getNotifyUrl(),order,"ok");
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(result);
			}
			SaveOrderUpdate(order);
			return "ok";
		}else{
			return "order not exist";
		}

	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class DXTVideoOrder extends TOrder{
		private String imei;
		private String cpParam;  // 透传参数
		private String smsNumber1;
		private String smsContent1;
		private String smsNumber2;
		private String smsContent2;
		
		
		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getCpParam() {
			return cpParam;
		}

		public void setCpParam(String cpParam) {
			this.cpParam = cpParam;
		}

		public String getSmsNumber1() {
			return smsNumber1;
		}

		public void setSmsNumber1(String smsNumber1) {
			this.smsNumber1 = smsNumber1;
		}

		public String getSmsNumber2() {
			return smsNumber2;
		}

		public void setSmsNumber2(String smsNumber2) {
			this.smsNumber2 = smsNumber2;
		}

		public String getSmsContent1() {
			return smsContent1;
		}

		public void setSmsContent1(String smsContent1) {
			this.smsContent1 = smsContent1;
		}

		public String getSmsContent2() {
			return smsContent2;
		}

		public void setSmsContent2(String smsContent2) {
			this.smsContent2 = smsContent2;
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
			if(this.cpParam != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("cpParam");
				oExt.setExtValue(this.cpParam);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsNumber1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsNumber1");
				oExt.setExtValue(this.smsNumber1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsContent1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("sms1");
				oExt.setExtValue(this.smsContent1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsNumber2 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsNumber2");
				oExt.setExtValue(this.smsNumber2);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsContent2 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("sms2");
				oExt.setExtValue(this.smsContent2);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
