package com.qy.sp.fee.modules.piplecode.qianya;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RHWoService extends ChannelService {
	public final static int INIT = 0;
	public final static int GETCODE_SUCCESS 	= 1;
	public final static int GETCODE_FAIL 	= 2;
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String GETSMS_SUCCESS = "0";  // 下短信接口成功
	public final static String PAYRES_SUCCESS = "4";  // 计费接口成功
	public final static String SYNC_P_SUCCESS = "6";	  // 同步计费成功
	public final static String SYNC_P_FAIL = "7";	  // 同步计费失败
	private  Logger log = Logger.getLogger(RHWoService.class);
	@Override
	public String getPipleId() {
		return "15048391738400190821775";
	}
	@Override
	public String getPipleKey() {
		return "PM1084";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("RHWoService_processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");

		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(mobile)){
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
			BaseResult bResult = this.accessVerify(req,getPipleId());
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
			RHWOrder order = new RHWOrder();
			ppkey.setProductId(tProduct.getProductId());
			ppkey.setPipleId(getPipleId());
			TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(getPipleId());
			order.setChannelId(tChannel.getChannelId());
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			if(mobile!=null && !"null".equals(mobile) && !"".equals(mobile)){
				order.setMobile(mobile);
				int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
				order.setProvinceId(provinceId);
			}
 			order.setExtData(extData);
			try{
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());

				// 请求参数处理
				long timestamp = Calendar.getInstance().getTimeInMillis()/1000;
//				SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				long timestamp = format.parse(DateTimeUtils.getFormatTime(DateTimeUtils.yyyyMMddHHmmss)).getTime();
				// 必填的请求参数都按照名称字符升序排列

				// channelCode=1&cpId=1&cpOrderId=20150805123&goodsCode=1&mobile=18611011043&orderType=0&sourceType=1&timestamp=1460700196&key=XXXXXXXXXXXXXXXX
				String params = "channelCode=1"+"&cpId="+piple.getPipleAuthA()+"&cpOrderId="+order.getOrderId()
						+"&goodsCode="+pipleProduct.getPipleProductCode()+"&mobile="+mobile
						+"&orderType=0"+"&sourceType=2"
						+"&timestamp="+timestamp+"&key="+piple.getPipleAuthB();
				String signStr = MD5.getMD5(params).toUpperCase();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("channelCode", 1);
				jsonObject.put("cpId", piple.getPipleAuthA());
				jsonObject.put("cpOrderId", order.getOrderId());
				jsonObject.put("goodsCode",pipleProduct.getPipleProductCode());
				jsonObject.put("orderType",0);
				jsonObject.put("mobile",mobile);
				jsonObject.put("sourceType",2);
				jsonObject.put("timestamp", timestamp);
				jsonObject.put("sign", signStr);
				jsonObject.put("scope", "");
				Map<String, String> reqParams = new HashMap<String,String>();
				reqParams.put("data",jsonObject.toString());
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, piple.getPipleUrlA()+";"+jsonObject.toString());
				String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(), reqParams, HttpClientUtils.UTF8);
				log.info("RHWoService pipleResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject resultObject = JSONObject.fromObject(pipleResult);
					String resultCode = resultObject.optString("resultCode");
					String resultMsg = resultObject.optString("resultMsg");
					String pipleOrderId = resultObject.optString("orderId");
					if(GETSMS_SUCCESS.equals(resultCode)){
						order.setResultCode(resultCode);
						order.setPipleOrderId(pipleOrderId);
					    order.setModTime(DateTimeUtils.getCurrentTime());
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						result.put("resultCode", GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
					}else{
						order.setModTime(DateTimeUtils.getCurrentTime());
					    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode(resultCode);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败。"+resultMsg);
					}

				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
					return result;
				}
			}catch(Exception ex){
				ex.printStackTrace();
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
				order.setModTime(DateTimeUtils.getCurrentTime());
				SaveOrderUpdate(order);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败，接口异常:"+ex.getMessage());
				return result;
			}
			SaveOrderUpdate(order);
			statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
			return result;
		}
	}
	
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("RHWoService_processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String apiKey = requestBody.optString("apiKey");
		String orderId = requestBody.optString("orderId");
		String verifyCode = requestBody.optString("verifyCode");
		result.put("orderId",orderId);
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
            req.setMobile(tOrder.getMobile());
			req.setImsi(tOrder.getImsi());
			req.setProductCode(tProduct.getProductCode());
			BaseResult bResult = this.accessVerify(req, getPipleId());
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
				return result;
			}else{
				RHWOrder newOrder = new RHWOrder();
				newOrder.setTOrder(tOrder);
				newOrder.setVerifyCode(verifyCode);
				TPipleProductKey ppkey = new TPipleProductKey();
				ppkey.setPipleId(getPipleId());
				ppkey.setProductId(tProduct.getProductId());
				TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
				TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());

				try {
					// 请求参数处理
					long timestamp = Calendar.getInstance().getTimeInMillis()/1000;
					// 必填的请求参数都按照名称字符升序排列
					// cpId=1&goodsCode=1&orderId=20150805123&timestamp=1460700196&verifyCode=159635&key=XXXXXXXXXXXXXXXX
					String params = "cpId="+piple.getPipleAuthA()+"&goodsCode="+pipleProduct.getPipleProductCode()
							+"&orderId="+newOrder.getPipleOrderId()+"&timestamp="+timestamp
							+"&verifyCode="+verifyCode+"&key="+piple.getPipleAuthB();
					String signStr = MD5.getMD5(params).toUpperCase();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("orderId", newOrder.getPipleOrderId());
					jsonObject.put("cpOrderId", newOrder.getOrderId());
					jsonObject.put("cpId", piple.getPipleAuthA());
					jsonObject.put("goodsCode",pipleProduct.getPipleProductCode());
					jsonObject.put("verifyCode",verifyCode);
					jsonObject.put("timestamp", timestamp);
					jsonObject.put("sign", signStr);
					String reqUrl = piple.getPipleUrlB();
					Map<String, String> reqParams = new HashMap<String,String>();
					reqParams.put("data",jsonObject.toString());
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl+";"+jsonObject.toString());
					String payResult = HttpClientUtils.doPost(reqUrl,reqParams, HttpClientUtils.UTF8);
					log.info("CTSWService getPageResult:"+  payResult+",orderId="+newOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payResult);
					if(payResult != null && !"".equals(payResult)){
						JSONObject object = JSONObject.fromObject(payResult);
						String resultCode = object.optString("resultCode");
						String resultMsg = object.optString("resultMsg");
						if(PAYRES_SUCCESS.equals(resultCode)){
							newOrder.setResultCode(resultCode);
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
							result.put("resultCode", GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功。");
						}else{
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							newOrder.setResultCode(resultMsg);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败。"+resultMsg);
						}
					}else{
						newOrder.setResultCode(GlobalConst.Result.ERROR);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","订购失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
					newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
					newOrder.setModTime(DateTimeUtils.getCurrentTime());
					SaveOrderUpdate(newOrder);
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常:"+e.getMessage());
					return result;
				}
				SaveOrderUpdate(newOrder);
				statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
				return result;
			}
		}
	}
	

	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("RHWoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String cpOrderId = requestBody.optString("cpOrderId");  		// 我方订单号
		String pipleOrderId = requestBody.optString("pipleOrderId");	// 通道订单号
		String status = requestBody.optString("status");  // 6-成功  7-失败
		TOrder order = tOrderDao.selectByPrimaryKey(cpOrderId);
		order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
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
//			order.setPipleOrderId(linkid);
			order.setResultCode(status);
			if(SYNC_P_SUCCESS.equals(status)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				if(!bDeducted){ // 不扣量 通知渠道
					notifyChannelAPIForKey(cp.getNotifyUrl(), order, "ok");
				}
			}else{
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
		}else if(order==null){
			return "order not exist";
		}
		return "0";
	}
	
	
//	private String getSign(String str){
//		byte[] source =null;
//		try {
//			source = str.getBytes("utf-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String stringSignTemp = MD5.getMD5(source).toUpperCase();
//		return stringSignTemp;
//	}
	
	
	public class RHWOrder extends TOrder{
		private String verifyCode;

		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if (this.verifyCode != null) {
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
