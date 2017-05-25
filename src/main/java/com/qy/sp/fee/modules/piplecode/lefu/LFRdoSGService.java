package com.qy.sp.fee.modules.piplecode.lefu;

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
public class LFRdoSGService extends ChannelService{
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String PAY_SUCCESS = "DELIVRD";  // 扣费成功
	private  Logger log = Logger.getLogger(LFRdoSGService.class);		
	@Override
	public String getPipleId() {
		return "14648625543563534167390";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("ZTVideoService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String iccid = requestBody.optString("iccid");
		String ip = requestBody.optString("ip");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)  || StringUtil.isEmpty(imsi)){
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
			LFRdoSGOrder order = new LFRdoSGOrder();
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
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				//请求短信验证码
				Map<String, String> params = new HashMap<String, String>();
				params.put("channel", piple.getPipleAuthA());
				params.put("imsi", order.getImsi());
				params.put("iccid", order.getIccid());
				params.put("phone", order.getMobile());
				params.put("imei", order.getImei());
				params.put("payCode", pipleProduct.getPipleProductCode());
				params.put("ip", order.getIp());
				String extraData = pipleProduct.getPipleProductCode()+"8"+order.getExtData();
				params.put("extraData", extraData);
//				String reqUrl = piple.getPipleUrlA()+"?"+"ppid="+pipleProduct.getPipleProductCode()+"&custom="+order.getExtData();
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, piple.getPipleUrlA()+";"+params.toString());
//				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlA(), params, HttpClientUtils.UTF8);
				log.info(" LFRdoSGService getSmsResult:"+  pipleResult);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObj = JSONObject.fromObject(pipleResult);
					String retCode = null;
					String retMsg = null;
					String orderId = null;
					if(jsonObj.has("retCode") ){
						retCode = jsonObj.getString("retCode");
						retMsg = jsonObj.getString("retMsg");
						if(RES_SUCCESS.equals(retCode)){// 返回成功
							orderId = jsonObj.getString("orderId");
							 order.setPipleOrderId(orderId);
							 order.setResultCode(retCode);
							 order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
						}else{
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(retCode);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+retMsg);
						}
					}else{
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+pipleResult);
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
				}
				SaveOrderUpdate(order);
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			} catch (Exception e) {
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
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("ZTVideoService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
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
					LFRdoSGOrder newOrder = new LFRdoSGOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setVerifyCode(verifyCode);
					
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					//提交验证码
					Map<String, String> params = new HashMap<String, String>();
					params.put("channel", piple.getPipleAuthA());
					params.put("orderId", newOrder.getPipleOrderId());
					params.put("verifyCode", newOrder.getVerifyCode());
//					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), result.toString());
//					String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					statistics(STEP_GET_SMS_PLATFORM_TO_BASE, tOrder.getGroupId(), piple.getPipleUrlB()+";"+params.toString());
					String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlB(), params, HttpClientUtils.UTF8);
					log.info(" LFRdoSGService confirmResult:"+  pipleResult);
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						JSONObject jsonObj = JSONObject.fromObject(pipleResult);
						String retCode = null;
						String retMsg = null;
						if(jsonObj.has("retCode") ){
							retCode = jsonObj.getString("retCode");
							retMsg = jsonObj.getString("retMsg");
							if(RES_SUCCESS.equals(retCode)){// 返回成功
								tOrder.setResultCode(retCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。");
							}else{
								tOrder.setResultCode(retCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+retMsg);
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
		logger.info("ZWRdoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String spnumber = requestBody.optString("spnumber");
		String mobile   = requestBody.optString("mobile");
		String linkid = requestBody.optString("linkid");
		String msg = requestBody.optString("msg");
		String status = requestBody.optString("status");
		TOrder order = tOrderDao.selectByPipleOrderId(linkid);
		if(order!=null ){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			boolean isSend = false; //是否同步
			if(PAY_SUCCESS.equals(status)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			doWhenPaySuccess(order);
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
				TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
				String productCode = tProduct.getProductCode();
//				n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
			}
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class LFRdoSGOrder extends TOrder{
		private String imei;
		private String ip;  
		private String verifyCode;
		private String spnumber;
		private String msg;
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

		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}

		public String getSpnumber() {
			return spnumber;
		}

		public void setSpnumber(String spnumber) {
			this.spnumber = spnumber;
		}
		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
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
			if(this.verifyCode != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("verifyCode");
				oExt.setExtValue(this.verifyCode);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.spnumber != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("spnumber");
				oExt.setExtValue(this.spnumber);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.msg != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("msg");
				oExt.setExtValue(this.msg);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
