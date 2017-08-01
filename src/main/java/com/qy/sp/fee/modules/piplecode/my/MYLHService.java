package com.qy.sp.fee.modules.piplecode.my;

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
import java.util.List;

@Service
public class MYLHService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "200000";  // 请求通道成功
	public final static String P_SUCCESS = "200000";	  // 同步计费成功
	private  Logger log = Logger.getLogger(MYLHService.class);
	@Override
	public String getPipleId() {
		return "15015662200607996511803";
	}

	@Override
	public String getPipleKey() {
		return "PW1075";
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("MYLHService requestBody:"+requestBody);
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
//			Map<String, String> params = new HashMap<String, String>();
//			params.put("tag", piple.getPipleAuthA());
//			params.put("imsi", order.getImsi());
//			params.put("imei", order.getImei());
//			params.put("extraData", piple.getPipleAuthB());  // 我方orderId
			String reqUrl = piple.getPipleUrlA()+"?"+"a="+piple.getPipleAuthA()+"&tel="+order.getMobile()+"&cpparam="+piple.getPipleAuthB()+order.getOrderId();
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
//			String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			String pipleResult = HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
			log.info(" MYLHService getSmsResult:"+  pipleResult);
			if(pipleResult != null && !"".equals(pipleResult)){
				JSONObject jsonObj = JSONObject.fromObject(pipleResult);
				String msg = null;
				String code = null;
				String upnumber = null;
				String sms = null;
				if(jsonObj.has("code") ){
					code = jsonObj.getString("code");
					if(RES_SUCCESS.equals(code)){ // 返回成功
						msg = jsonObj.getString("msg");
						upnumber = jsonObj.getString("upnumber");
						sms = jsonObj.getString("sms");
						order.setResultCode(code);
						order.setSmsNumber(upnumber);
						order.setSmsContent(sms);

						result.put("smsNumber", upnumber);
						result.put("smsContent", sms);
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
						msg = jsonObj.getString("msg");
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
		logger.info("MYLHService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String exData = requestBody.optString("exData"); // 从第6位开始是我方订单号
		String orderTime = requestBody.optString("orderTime");
		String phone = requestBody.optString("phone");
		String spnumber = requestBody.optString("spnumber");
		String linkid = requestBody.optString("linkid");
		String message = requestBody.optString("message");
		String msgstatus = requestBody.optString("msgstatus");
		String orderId = exData.substring(5,exData.length());
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
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
				if(P_SUCCESS.equals(msgstatus)){
                    order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    order.setSubStatus(PAY_SUCCESS);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setCompleteTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(msgstatus);
                    doWhenPaySuccess(order);
                    bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
                    if(!bDeducted){ // 不扣量 通知渠道
                        notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
                    }
                }else {
                    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(msgstatus);
                }
				SaveOrderUpdate(order);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("MYLHService同步处理异常："+e.getMessage());
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
	public class DXTVideoOrder extends TOrder{
		private String imei;
		private String cpParam;  // 透传参数
		private String smsNumber;
		private String smsContent;

		@Override
		public String getImei() {
			return imei;
		}

		@Override
		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getCpParam() {
			return cpParam;
		}

		public void setCpParam(String cpParam) {
			this.cpParam = cpParam;
		}

		public String getSmsNumber() {
			return smsNumber;
		}

		public void setSmsNumber(String smsNumber) {
			this.smsNumber = smsNumber;
		}

		public String getSmsContent() {
			return smsContent;
		}

		public void setSmsContent(String smsContent) {
			this.smsContent = smsContent;
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
			if(this.smsNumber != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsNumber");
				oExt.setExtValue(this.smsNumber);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsContent != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsContent");
				oExt.setExtValue(this.smsContent);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}

			return tOrderExts;
		}
	}
}
