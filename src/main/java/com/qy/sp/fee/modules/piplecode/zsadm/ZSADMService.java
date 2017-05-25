package com.qy.sp.fee.modules.piplecode.zsadm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MD5;
import com.qy.sp.fee.common.utils.MapUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TOrderExtKey;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import com.qy.sp.fee.modules.piplecode.ds.DSWoService.DSWORdoOrder;

@Service
public class ZSADMService extends ChannelService{
	private Logger log = Logger.getLogger(ZSADMService.class);
	
	private final static String REQ_SUCCESS = "SUCCESS";   // 请求成功
	private final static String SEND_FAIL = "1";   // 短信发送失败
	private final static String SEND_SUCCESS = "2";   // 短信发送成功
	private final static String PAY_FAIL = "1";   // 扣费失败
	private final static String PAY_SUCCESS = "2";   //  扣费成功
	@Override
	public String getPipleId() {
		return "14780541337500310032872";
	}
	
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception{
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)){
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
			ZSADMOrder order = new ZSADMOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			
			order.setExtData(extData);
			try{
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String reqUrl =  piple.getPipleUrlA();
				Map<String, String> params = new HashMap<String, String>();
				params.put("app_id", piple.getPipleAuthA());
				params.put("channel_id", piple.getPipleAuthB());
				params.put("fee_id", pipleProduct.getPipleProductCode());
				params.put("tel", order.getMobile());
				params.put("imei", "350000000000000");
				params.put("imsi", order.getImsi());
				params.put("custom_param", order.getOrderId());   // 透传我们的订单号
				params.put("Timestamp", System.currentTimeMillis() / 1000+"");
				params.put("Sign", this.getSign(params, piple.getPipleAuthC()));
				JSONObject requset = new JSONObject();
				requset.putAll(params);
				String json = requset.toString();
//				JSONObject json = new JSONObject();
//				json.put("app_id", piple.getPipleAuthA());
//				json.put("channel_id", piple.getPipleAuthB());
//				json.put("fee_id", pipleProduct.getPipleProductCode());
//				json.put("tel", order.getMobile());
//				json.put("imei", "350000000000000");
//				json.put("imsi", order.getImsi());
//				json.put("custom_param", order.getOrderId());   // 透传我们的订单号
//				json.put("Timestamp", order.getCreateTime());
//				json.put("Sign", order.getCreateTime());
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl+json);
				String pipleResult = HttpClientUtils.doPost(reqUrl, json.toString(), HttpClientUtils.UTF8);
				log.info(" DSWOService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject object = JSONObject.fromObject(pipleResult);
					String return_code = object.getString("return_code");
					if(REQ_SUCCESS.equals(return_code)){
						String order_id = object.getString("order_id");
						String smsContent1 = object.getString("message_content");
						String smsPort1 = object.getString("message_destination");
//						JSONArray smsList = object.getJSONArray("sms");
//						JSONObject sms1 = smsList.getJSONObject(0);
//						JSONObject sms2 = smsList.getJSONObject(1);
//						String smsContent1 = sms1.getString("message_content");
//						String smsPort1 = sms1.getString("message_destination");
//						String smsContent2 = sms2.getString("message_content");
//						String smsPort2 = sms2.getString("message_destination");
						order.setResultCode(return_code);
						order.setPipleOrderId(order_id);
						order.setSmsContent1(smsContent1);
						order.setSmsPort1(smsPort1);
//						order.setSmsContent2(smsContent2);
//						order.setSmsPort2(smsPort2);
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						order.setModTime(DateTimeUtils.getCurrentTime());
						
						result.put("smsContent",smsContent1);
						result.put("smsPort",smsPort1);
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
					}else{
						order.setModTime(DateTimeUtils.getCurrentTime());
					    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode("1");
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求错误："+pipleResult);
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
				}
				SaveOrderUpdate(order);
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}catch(Exception ex){
				ex.printStackTrace();
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultCode","服务器异常");
				return result;
			}
		}
		
		
	}
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody){
		log.info("ZSADMService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try{
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
			result.put("orderId",orderId);
			if(!verifyCode.equals(SEND_FAIL) && !verifyCode.equals(SEND_SUCCESS) ){
				result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL);
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL)+"。"+"发送状态有误。");
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
					ZSADMOrder newOrder =  new ZSADMOrder();
					newOrder.setSendStatus(verifyCode);
					newOrder.setTOrder(tOrder);
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					Map<String, String> params = new HashMap<String, String>();
					params.put("app_id", piple.getPipleAuthA());
					params.put("order_id", newOrder.getPipleOrderId());
					params.put("send_status", verifyCode);
					params.put("timestamp", System.currentTimeMillis() / 1000+"");
					params.put("sign", getSign(params, piple.getPipleAuthC()));
					JSONObject requset = new JSONObject();
					requset.putAll(params);
					String json = requset.toString();
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), piple.getPipleUrlB()+";"+json.toString());
					String reqResult = HttpClientUtils.doPost(piple.getPipleUrlB(), json.toString(), HttpClientUtils.UTF8);
					if(reqResult != null && !"".equals(reqResult)){
						JSONObject object = JSONObject.fromObject(reqResult);
						String return_code = object.getString("return_code");
						if(REQ_SUCCESS.equals(return_code)){
							newOrder.setResultCode(return_code);
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
							result.put("resultCode", GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功。");
						}else{
							newOrder.setResultCode("1");
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败："+reqResult);
						}
					}else{
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
					}
					SaveOrderUpdate(newOrder);
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
		logger.info("ZSADMService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String order_id = requestBody.optString("order_id");
		String app_id = requestBody.optString("app_id");
		String channel_id = requestBody.optString("channel_id");
		String fee_id = requestBody.optString("fee_id");
		String price = requestBody.optString("price");
		String phone = requestBody.optString("phone");
		String provider = requestBody.optString("provider");
		String result_status = requestBody.optString("result_status");
		String custom_param = requestBody.optString("custom_param");   // 透传订单号
		String timestamp = requestBody.optString("timestamp");
		String signature = requestBody.optString("signature");
//		TOrder order = tOrderDao.selectByPrimaryKey(custom_param);
		TOrder order = tOrderDao.selectByPipleOrderId(order_id);
		if(order!=null && order.getOrderStatus()!=GlobalConst.OrderStatus.SUCCESS){ // 订单未同步过，成功同步去重处理
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			boolean bDeducted = false; // 扣量标识
			if(result_status.equals(PAY_SUCCESS)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				if(!bDeducted){ // 不扣量 通知渠道
					notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
		}
		return "SUCCESS";
	}
	
	
	public class ZSADMOrder extends TOrder{
		private String smsContent1;	// 短信1 内容
		private String smsPort1;			// 短信1 端口
		private String smsContent2;	// 短信2 内容
		private String smsPort2;			// 短信2 端口
		private String sendStatus;	// 发送状态
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

		public String getSendStatus() {
			return sendStatus;
		}

		public void setSendStatus(String sendStatus) {
			this.sendStatus = sendStatus;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
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
			if(this.sendStatus != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("sendStatus");
				oExt.setExtValue(this.sendStatus);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
	
	public String getSign(Map<String,String> map,String signKey){
		String signStr = null;
		String signContext = "";
		Map<String,String>  newMap= MapUtil.sortMapByKey(map);
		for(Map.Entry<String, String> entry:newMap.entrySet()){
			if(!StringUtil.isEmpty(entry.getValue()))
		     System.out.println(entry.getKey()+"--->"+entry.getValue());
			signContext =signContext+ entry.getKey()+"="+entry.getValue()+"&";
			System.out.println("signContext："+signContext);
		}   
		signContext = signContext+"signkey="+signKey;
		System.out.println("allsignContext："+signContext);
		signStr = MD5.getMD5(signContext).toUpperCase();
		return signStr;
	}
	
}
