package com.qy.sp.fee.modules.piplecode.zt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import net.sf.json.JSONObject;
@Service
public class ZTVideoService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "0";	  // 同步计费成功
	private  Logger log = Logger.getLogger(ZTVideoService.class);		
	@Override
	public String getPipleId() {
		return "14618970150295520769216";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("ZTVideoService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
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
			ZTVideoOrder order = new ZTVideoOrder();
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
			params.put("channel", piple.getPipleAuthA());
			params.put("flag", pipleProduct.getPipleProductCode());
			params.put("imsi", order.getImsi());
			params.put("cpparam", order.getExtData());
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,piple.getPipleUrlA()+";"+params.toString());
			String pipleResult1= HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult1);
			log.info(" ZTVideoService getSmsResult1:"+  pipleResult1);
			if(pipleResult1 != null && !"".equals(pipleResult1)){
				JSONObject jsonObj = JSONObject.fromObject(pipleResult1);
				String status1 = null;
				String resulttext1 = null;
				String port1 = null;
				String sms1 = null;
				String billid = null;
				if(jsonObj.has("result") ){
					status1 = jsonObj.getString("result");
					if(RES_SUCCESS.equals(status1)){// 返回成功
						 port1 = jsonObj.getString("port");
						 sms1 = jsonObj.getString("sms");
						 billid = jsonObj.getString("billid");
						 order.setPipleOrderId(billid);
						 order.setSmsNumber1(port1);
						 order.setSms1(sms1);
						 result.put("smsNumber1", port1);
						 result.put("sms1", sms1);
					}else{
						resulttext1 = jsonObj.getString("resulttext");
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(status1);
						order.setModTime(DateTimeUtils.getCurrentTime());
						SaveOrderUpdate(order);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+resulttext1);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}
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
			
			//请求第二条短信
			Map<String, String> params2 = new HashMap<String, String>();
			params2.put("channel", piple.getPipleAuthA());
			params2.put("flag", pipleProduct.getPipleProductCode());
			params2.put("imsi", order.getImsi());
			params2.put("billid", order.getPipleOrderId());
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,piple.getPipleUrlB()+";"+params.toString());
			String pipleResult2= HttpClientUtils.doPost(piple.getPipleUrlB(),params2,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult2);
			log.info(" ZTVideoService getSmsResult2:"+  pipleResult2);
			if(pipleResult2 != null && !"".equals(pipleResult2)){
				JSONObject jsonObj2 = JSONObject.fromObject(pipleResult2);
				String status2 = null;
				String resulttext2 = null;
				String port2 = null;
				String sms2 = null;
				if(jsonObj2.has("result") ){
					status2 = jsonObj2.getString("result");
					if(RES_SUCCESS.equals(status2)){// 返回成功
						 port2 = jsonObj2.getString("port");
						 sms2 = jsonObj2.getString("sms");
						 order.setSmsNumber1(port2);
						 order.setSms1(sms2);
						 result.put("smsNumber2", port2);
						 result.put("sms2", sms2);
					}else{
						resulttext2 = jsonObj2.getString("resulttext");
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(resulttext2);
						order.setModTime(DateTimeUtils.getCurrentTime());
						SaveOrderUpdate(order);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+resulttext2);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}
				}			
				
				// 更新订单信息
			order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
			order.setSubStatus(GETCODE_SUCCESS);
			order.setResultCode(GlobalConst.Result.SUCCESS);
			order.setModTime(DateTimeUtils.getCurrentTime());
			//设置返回结果
			result.put("resultCode",GlobalConst.Result.SUCCESS);
			result.put("resultMsg","请求成功");
			result.put("orderId",order.getOrderId());
			SaveOrderUpdate(order);
			statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
			return result;
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
		logger.info("ZTVideoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String channel = requestBody.optString("channel");
		String flag = requestBody.optString("flag");
		String billid = requestBody.optString("billid");
		String status = requestBody.optString("status");
		String imsi = requestBody.optString("imsi");
		String amount = requestBody.optString("amount");
		String cpparam = requestBody.optString("cpparam");
		String mobile = requestBody.optString("mobile");
		TOrder order = tOrderDao.selectByPipleOrderId(billid);
		if(order!=null ){ // 同步数据正确
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
			boolean isSend = false;
			if(P_SUCCESS.equals(status)){
				if(mobile!=null && !"".equals(mobile)){
					order.setMobile(mobile);
				}
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
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
//				notifyChannel(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
			}
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class ZTVideoOrder extends TOrder{
		private String imei;
		private String extData;  // 扩展字段
		private String smsNumber1;
		private String sms1;
		private String smsNumber2;
		private String sms2;
		
		
		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getExtData() {
			return extData;
		}

		public void setExtData(String extData) {
			this.extData = extData;
		}

		public String getSmsNumber1() {
			return smsNumber1;
		}

		public void setSmsNumber1(String smsNumber1) {
			this.smsNumber1 = smsNumber1;
		}

		public String getSms1() {
			return sms1;
		}

		public void setSms1(String sms1) {
			this.sms1 = sms1;
		}

		public String getSmsNumber2() {
			return smsNumber2;
		}

		public void setSmsNumber2(String smsNumber2) {
			this.smsNumber2 = smsNumber2;
		}

		public String getSms2() {
			return sms2;
		}

		public void setSms2(String sms2) {
			this.sms2 = sms2;
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
			if(this.extData != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("extData");
				oExt.setExtValue(this.extData);
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
			if(this.sms1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("sms1");
				oExt.setExtValue(this.sms1);
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
			if(this.sms2 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("sms2");
				oExt.setExtValue(this.sms2);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
