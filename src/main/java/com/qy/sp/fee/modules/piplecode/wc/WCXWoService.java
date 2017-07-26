package com.qy.sp.fee.modules.piplecode.wc;

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
public class WCXWoService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "10000";  // 请求通道成功
	public final static String P_SUCCESS = "10000";	  // 同步计费成功
	private  Logger log = Logger.getLogger(WCXWoService.class);
	@Override
	public String getPipleId() {
		return "14998309713781409450145";
	}

	@Override
	public String getPipleKey() {
		return "PM1070";
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("WCXWoService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String apiKey = requestBody.optString("apiKey");
		String pipleKey = requestBody.optString("pipleKey");
		String productCode = requestBody.optString("productCode");
		String mobile = requestBody.optString("mobile");
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
			WCXWoOrder order = new WCXWoOrder();
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

			String reqUrl = piple.getPipleUrlA()+"?"+"channelId="+piple.getPipleAuthA()+"&cm="+piple.getPipleAuthB()+"&imsi="+order.getImsi()+"&imei="+order.getImei()
					+"&mobile="+order.getMobile()+"&productId="+piple.getPipleAuthC()
					+"&price="+tProduct.getPrice()+"&exData="+order.getOrderId();
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
//			String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			String pipleResult = HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
			log.info(" WCXWoService getSmsResult:"+  pipleResult);
			if(pipleResult != null && !"".equals(pipleResult)){
				JSONObject jsonObj = JSONObject.fromObject(pipleResult);
				String sms = null;
				String port = null;
				String smsType = null;
				String resultCode = null;
				String resultMsg = null;
				String orderId = null;
				if(jsonObj.has("resultCode") ){
					resultCode = jsonObj.getString("resultCode");
					resultMsg = jsonObj.getString("resultMsg");
					if(RES_SUCCESS.equals(resultCode)){ // 返回成功
						sms = jsonObj.getString("sms");
						port = jsonObj.getString("port");
						smsType = jsonObj.getString("smsType");
						orderId = jsonObj.getString("orderId");
						//	order.setPipleOrderId(billid);
						order.setSmsNumber1(port);
						order.setSmsContent1(sms);
						order.setResultCode(resultCode);
						order.setPipleOrderId(orderId);
						result.put("smsNumber1", port);
						result.put("smsContent1", sms);
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
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(resultCode);
						order.setModTime(DateTimeUtils.getCurrentTime());
						SaveOrderUpdate(order);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+resultMsg);
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
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("WCXWoService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
			result.put("orderId",orderId);
			if(StringUtil.isEmptyString(apiKey) || StringUtil.isEmptyString(orderId)   || StringUtil.isEmpty(verifyCode)) {
				result.put("resultCode", GlobalConst.CheckResult.MUST_PARAM_ISNULL + "");
				result.put("resultMsg", GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
				return result;
			}
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder==null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
				return result;
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
				return result;
			}else{
				statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
				TProduct tProduct = this.tProductDao.selectByPrimaryKey(tOrder.getProductId());
				BaseChannelRequest req = new BaseChannelRequest();
				req.setApiKey(apiKey);
				req.setImsi(tOrder.getImsi());
				req.setProductCode(tProduct.getProductCode());
				BaseResult bResult = this.accessVerify(req, getPipleId());
				if(bResult!=null){// 返回不为空则校验不通过
					result.put("resultCode",bResult.getResultCode());
					result.put("resultMsg",bResult.getResultMsg());
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}else{
					WCXWoOrder newOrder =  new WCXWoOrder();
					newOrder.setVerifyCode(verifyCode);
					newOrder.setTOrder(tOrder);

					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());

					//提交验证码
					String reqUrl = piple.getPipleUrlB()+"?"+"orderId="+tOrder.getPipleOrderId()+"&verifyCode="+verifyCode
							+"&mobile="+tOrder.getMobile()+"&imsi="+tOrder.getImsi()+"&channelId="+piple.getPipleAuthB();
					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl);
					String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					log.info(" WCXWoService confirmResult:"+  pipleResult);
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						JSONObject jsonObj = JSONObject.fromObject(pipleResult);
						String resultCode = null;
						String resultMsg = null;
						String port = null;
						String content = null;
						if(jsonObj.has("resultCode") ){
							resultCode = jsonObj.getString("resultCode");
							resultMsg = jsonObj.getString("resultMsg");
							if(RES_SUCCESS.equals(resultCode)){// 返回成功
								tOrder.setResultCode(resultCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
								result.put("resultCode", GlobalConst.Result.SUCCESS);
								result.put("resultMsg","请求成功。");
							}else{
								tOrder.setResultCode(resultCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+resultMsg);
							}
						}else{
							tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
							tOrder.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+pipleResult);
						}

					}else{
						tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
						tOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
					}
					SaveOrderUpdate(tOrder);
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("WCXWoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String imsi = requestBody.optString("imsi");
		String mobile = requestBody.optString("mobile");
		String productId = requestBody.optString("productId");
		String price = requestBody.optString("price");
		String timestamp = requestBody.optString("timestamp");
		String exData = requestBody.optString("exData");  // 我方订单号
		String orderId = requestBody.optString("orderId");
		String province = requestBody.optString("province");
		String resultCode = requestBody.optString("resultCode");

		TOrder order = tOrderDao.selectByPipleOrderId(orderId);
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

				//扣量
				boolean bDeducted = false;
				if(P_SUCCESS.equals(resultCode)){
                    order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    order.setSubStatus(PAY_SUCCESS);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setCompleteTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(resultCode);
                    doWhenPaySuccess(order);
                    bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
                    if(!bDeducted){ // 不扣量 通知渠道
    //				notifyChannel(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
    //				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
                        notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
                    }
                }else {
                    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(resultCode);
                }
				SaveOrderUpdate(order);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("WCXWoService同步处理异常："+e.getMessage());
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

	public class WCXWoOrder extends TOrder{
		private String imei;
		private String cpParam;  // 透传参数
		private String smsNumber1;
		private String smsContent1;
		private String smsNumber2;
		private String smsContent2;
		private String verifyCode;

		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}

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
			if(this.verifyCode != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("verifyCode");
				oExt.setExtValue(this.verifyCode);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
