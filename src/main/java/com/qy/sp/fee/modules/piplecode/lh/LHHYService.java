package com.qy.sp.fee.modules.piplecode.lh;

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
public class LHHYService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "0";	  // 同步计费成功
	private  Logger log = Logger.getLogger(LHHYService.class);
	@Override
	public String getPipleId() {
		return "15004664097248680193260";
	}

	@Override
	public String getPipleKey() {
		return "PW1073";
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
		String iccid = requestBody.optString("iccid");
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
			TChannelPipleKey cpKey = new TChannelPipleKey();
			cpKey.setPipleId(getPipleId());
			cpKey.setChannelId(tChannel.getChannelId());
			TChannelPiple channelPiple = tChannelPipleDao.selectByPrimaryKey(cpKey);

			//保存订单
			LHHYOrder order = new LHHYOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setIccid(iccid);
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
					+"&Iccid="+order.getIccid()+"&chargeCode="+pipleProduct.getPipleProductCode()+"&callbackUrl="+channelPiple.getNotifyUrl()
					+"&transmissionData="+order.getOrderId();
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
//			String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			String pipleResult = HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
			log.info(" LHHYService getSmsResult:"+  pipleResult);
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
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("LHHYService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try{
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
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
					LHHYOrder newOrder = new LHHYOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setVerifyCode(verifyCode);
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					String channel = piple.getPipleAuthA();
					String param = pipleProduct.getPipleProductCode();
					//提交验证码
					Map<String, String> params = new HashMap<String, String>();
					params.put("OrderId",tOrder.getPipleOrderId());
					params.put("AuthCode",newOrder.getVerifyCode());
//					String reqUrl = piple.getPipleUrlB()+"?channel="+channel+"&param="+param+"&smscode="+verifyCode;
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), piple.getPipleUrlB()+param.toString());
//					String payResult = HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					String payResult = HttpClientUtils.doPost(piple.getPipleUrlB(),params,HttpClientUtils.UTF8);
					log.info("LHHYService getPageResult:"+  payResult+",orderId="+newOrder.getOrderId());
					if(payResult != null && !"".equals(payResult)){
						statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payResult);
						JSONObject object = JSONObject.fromObject(payResult);
						String resultMsg = null;
						String code = null;
						String msg = null;
						if(object.has("code")){
							 code = object.getString("code");
							 msg = object.getString("msg");
							if(RES_SUCCESS.equals(code)){
								newOrder.setResultCode(code);
								newOrder.setModTime(DateTimeUtils.getCurrentTime());
								newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
								newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
								result.put("resultCode", GlobalConst.Result.SUCCESS);
								result.put("resultMsg","请求成功。");
							}else{
								newOrder.setModTime(DateTimeUtils.getCurrentTime());
								newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
								newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
								newOrder.setResultCode(code);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+msg);
							}
						}else{
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+payResult);
						}
					}else{
						newOrder.setResultCode(GlobalConst.Result.ERROR);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败");
					}
					SaveOrderUpdate(newOrder);
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("LHHYService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String code = requestBody.optString("code");
		String msg = requestBody.optString("msg");
		String orderId = requestBody.optString("orderId");
		String mobile = requestBody.optString("mobile");  // 我方订单号
		String price = requestBody.optString("price");
		String chargeCode = requestBody.optString("chargeCode");
		String transmissionData = requestBody.optString("transmissionData");
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
				logger.info("LHHYService同步处理异常："+e.getMessage());
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

	public class LHHYOrder extends TOrder{
		private String imei;
		private String verifyCode;
		
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

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.imei != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("imei");
				oExt.setExtValue(this.imei);
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

			return tOrderExts;
		}
	}
}
