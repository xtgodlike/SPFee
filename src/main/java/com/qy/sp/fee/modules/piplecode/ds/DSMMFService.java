package com.qy.sp.fee.modules.piplecode.ds;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DSMMFService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "0";	  // 同步计费成功
	private  Logger log = Logger.getLogger(DSMMFService.class);
	@Override
	public String getPipleId() {
		return "15096165788525813960820";
	}

	@Override
	public String getPipleKey() {
		return "PM1088";
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("DSMMService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleKey = requestBody.optString("pipleKey");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String iccid = requestBody.optString("iccid");
		String ip = requestBody.optString("ip");
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
//			TChannelPipleKey cpKey = new TChannelPipleKey();
//			cpKey.setPipleId(getPipleId());
//			cpKey.setChannelId(tChannel.getChannelId());
//			TChannelPiple channelPiple = tChannelPipleDao.selectByPrimaryKey(cpKey);

			//保存订单
			DSMMOrder order = new DSMMOrder();
			order.setOrderId(KeyHelper.createKey());
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
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			order.setGroupId(groupId);
			SaveOrderInsert(order);
			result.put("orderId",order.getOrderId());
			//请求验证码
			Map<String, String> params = new HashMap<String, String>();
			String reqUrl = piple.getPipleUrlA()+"?"+"imsi="+order.getImsi()+"&imei="+order.getImei()+"&mobile="+order.getMobile()
					+"&iccid="+order.getIccid()+"&ip="+order.getIp()+"&chargeCode="+pipleProduct.getPipleProductCode()
					+"&callbackUrl="+piple.getNotifyUrlA()
					+"&transmissionData="+order.getOrderId();
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
//			String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			String pipleResult = HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
			log.info(" DSMMService getSmsResult:"+  pipleResult);
			if(pipleResult != null && !"".equals(pipleResult)){
				JSONObject jsonObj = JSONObject.fromObject(pipleResult);
				String resultMsg = null;
				String code = null;
				String msg = null;
				String orderId = null;
				if(jsonObj.has("code") ){
					code = jsonObj.getString("code");
					msg = jsonObj.getString("msg");
					if(RES_SUCCESS.equals(code)){ // 返回成功
						resultMsg = jsonObj.getString("result");
						JSONObject resData = JSONObject.fromObject(resultMsg);
						orderId = resData.getString("orderId");
						JSONArray actionList = resData.getJSONArray("actionList");
						JSONObject smsInfo1 = actionList.getJSONObject(0);
						String smsContent1 = smsInfo1.getString("actionParam");
						String smsPort1 = smsInfo1.getString("actionTarget");
//						String smsType1 = smsInfo1.getString("actionRemark");
						JSONObject smsInfo2 = actionList.getJSONObject(1);
						String smsContent2 = smsInfo2.getString("actionParam");
						String smsPort2 = smsInfo2.getString("actionTarget");
//						String smsType2 = smsInfo2.getString("actionRemark");

						order.setPipleOrderId(orderId);
						order.setSmsContent1(smsContent1);
						order.setSmsPort1(smsPort1);
//						order.setSmsType1(smsType1);
						order.setSmsContent2(smsContent2);
						order.setSmsPort2(smsPort2);
//						order.setSmsType2(smsType2);
						result.put("smsContent1",smsContent1);
						result.put("smsPort1",smsPort1);
//						result.put("smsType1",smsType1);
						result.put("smsContent2",smsContent2);
						result.put("smsPort2",smsPort2);
//						result.put("smsType2",smsType2);

						// 更新订单信息
						order.setPipleOrderId(orderId);
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GETCODE_SUCCESS);
						order.setResultCode(code);
						order.setCreateTime(DateTimeUtils.getCurrentTime());
						order.setModTime(DateTimeUtils.getCurrentTime());
						//设置返回结果
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功");
						SaveOrderUpdate(order);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}else{
						code = jsonObj.getString("code");
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(code);
						order.setModTime(DateTimeUtils.getCurrentTime());
						SaveOrderUpdate(order);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+msg);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}

				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GETCODE_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					SaveOrderUpdate(order);
					result.put("resultCode", GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败:"+pipleResult);
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
		logger.info("DSMMService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String code = requestBody.optString("code");
		String msg = requestBody.optString("msg");
		String orderId = requestBody.optString("orderId");
		String mobile = requestBody.optString("mobile");
		String price = requestBody.optString("price");
		String chargeCode = requestBody.optString("chargeCode");
		String transmissionData = requestBody.optString("transmissionData"); // 我方订单号
		TOrder order = tOrderDao.selectByPrimaryKey(transmissionData);
		if(order!=null ){ // 同步数据正确
			try {
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
				if(P_SUCCESS.equals(code)){
                    order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    order.setSubStatus(PAY_SUCCESS);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setCompleteTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(code);
                    doWhenPaySuccess(order);
                    bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
                    if(!bDeducted){ // 不扣量 通知渠道
                        notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
                    }
                }else {
                    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(code);
                }
				SaveOrderUpdate(order);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("DSMMService 同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order not exist";
		}

	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}

	public class DSMMOrder extends TOrder{
		private String imei;

		private String smsContent1;

		private String smsPort1;

		private String smsType1;

		private String smsContent2;

		private String smsPort2;

		private String smsType2;

		private String verifyCode;

		private String ip;

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}

		public String getSmsContent1() {
			return smsContent1;
		}

		public void setSmsContent1(String smsContent1) {
			this.smsContent1 = smsContent1;
		}

		public String getSmsPort1() {
			return smsPort1;
		}

		public void setSmsPort1(String smsPort1) {
			this.smsPort1 = smsPort1;
		}

		public String getSmsType1() {
			return smsType1;
		}

		public void setSmsType1(String smsType1) {
			this.smsType1 = smsType1;
		}

		public String getSmsContent2() {
			return smsContent2;
		}

		public void setSmsContent2(String smsContent2) {
			this.smsContent2 = smsContent2;
		}

		public String getSmsPort2() {
			return smsPort2;
		}

		public void setSmsPort2(String smsPort2) {
			this.smsPort2 = smsPort2;
		}

		public String getSmsType2() {
			return smsType2;
		}

		public void setSmsType2(String smsType2) {
			this.smsType2 = smsType2;
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
			if(this.smsContent1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsContent1");
				oExt.setExtValue(this.smsContent1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsPort1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsPort1");
				oExt.setExtValue(this.smsPort1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsType1 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsType1");
				oExt.setExtValue(this.smsType1);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsContent2 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsContent2");
				oExt.setExtValue(this.smsContent2);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsPort2 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsPort2");
				oExt.setExtValue(this.smsPort2);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsType2 != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsType2");
				oExt.setExtValue(this.smsType2);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}

			if(this.imei != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("verifyCode");
				oExt.setExtValue(this.verifyCode);
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

			return tOrderExts;
		}
	}

}
