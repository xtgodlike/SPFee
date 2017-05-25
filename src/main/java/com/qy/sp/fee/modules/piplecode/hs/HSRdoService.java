package com.qy.sp.fee.modules.piplecode.hs;

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
import com.qy.sp.fee.common.utils.MapCacheManager;
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

import net.sf.json.JSONObject;
@Service
public class HSRdoService extends ChannelService{
	public final static String RES_SUCCESS = "00000";  // 请求通道成功
	public final static String PAY_SUCCESS = "10000";  // 扣费成功
	
	private  Logger log = Logger.getLogger(HSRdoService.class);		
	@Override
	public String getPipleId() {
		return "14651192454535636843030";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("HSRdoService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
//		String imei = requestBody.optString("imei");
//		String iccid = requestBody.optString("iccid");
//		String ip = requestBody.optString("ip");
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
			HSRdoOrder order = new HSRdoOrder();
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
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String param = pipleProduct.getPipleProductCode()+order.getOrderId();  // 计费代码（5位）+透传参数（订单号）
				//请求短信验证码地址
//				Map<String, String> params = new HashMap<String, String>();
//				params.put("channel", piple.getPipleAuthA());
//				params.put("imsi", order.getImsi());
//				params.put("mobile", order.getMobile());
//				params.put("param", param);
				if(StringUtil.isEmpty(imsi)){
					imsi = "460000000000000";  // 无IMSI  默认传值460000000000000
				}
				String reqUrl = piple.getPipleUrlA()+"?"+"channel="+piple.getPipleAuthA()+"&imsi="+imsi+"&mobile="+order.getMobile()+"&param="+param;
//				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, piple.getPipleUrlA()+";"+params.toString());
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
//				String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlA(), params, HttpClientUtils.UTF8);
				log.info(" HSRdoService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					String cmreadURI = "http://wap.cmread.com";
					List<String>  smsMsg = StringUtil.match(pipleResult, "form", "action");
					if(smsMsg.size()>0){// 包含请求获取验证码请求地址
					   String getCodeUrl = cmreadURI + smsMsg.get(0).replaceAll("amp;", "");
					   getCodeUrl = getCodeUrl+"&msisdn="+order.getMobile(); // 添加手机号组成完整请求链接
					   statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,getCodeUrl);
					   log.info(" HSRdoService getCodeUrl:"+  getCodeUrl+",orderId="+order.getOrderId());
					   order.setGetCodeUrl(getCodeUrl);
					   String getCodeRst = HttpClientUtils.doGet(getCodeUrl, "utf-8");
					   statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,getCodeRst);
					   log.info("HSRdoService getCodeRst："+getCodeRst+",orderId="+order.getOrderId());
					   if(getCodeRst.contains("发送短信验证码成功")){
						   List<String>  payMsg = StringUtil.match(getCodeRst, "form", "action");
						   String smsVertifyUrl =cmreadURI+ payMsg.get(0).replaceAll("amp;", "");
						   	 order.setSmsVertifyUrl(smsVertifyUrl);
						     order.setResultCode(RES_SUCCESS);
						     order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
					   }else{
						   	order.setModTime(DateTimeUtils.getCurrentTime());
						    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode("1");
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败。");
					   }
					}else{
						   order.setModTime(DateTimeUtils.getCurrentTime());
						    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode("1");
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:1");
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
		log.info("HSRdoService processVertifySMS requestBody:"+requestBody);
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
					HSRdoOrder newOrder = new HSRdoOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setVerifyCode(verifyCode);
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
//					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					//提交验证码
//					payRstUrl = HttpClientUtils.doPost(url, authMap, "UTF-8");
//					log.info("QYRdoSmsVerify payRstUrl="+ payRstUrl);
//					payRst = HttpClientUtils.doGet(payRstUrl, "UTF-8");
//					log.info("QYRdoSmsVerify payRst="+ payRst);
					TOrderExtKey oExtKey = new TOrderExtKey();
					oExtKey.setOrderId(orderId);
					oExtKey.setExtKey("smsVertifyUrl");
					TOrderExt urlExt = tOrderExtDao.selectByPrimaryKey(oExtKey);
					Map<String, String> params = new HashMap<String, String>();
					params.put("verifyCode",verifyCode);
					String smsVertifyUrl = urlExt.getExtValue();
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), smsVertifyUrl+";"+params.toString());
//					String confirmResult= HttpClientUtils.doGet(payUrl, HttpClientUtils.UTF8);
					String payUrl= HttpClientUtils.doPost(smsVertifyUrl, params, HttpClientUtils.UTF8);  // 返回支付确认地址
					log.info(" HSRdoService confirmResult:"+  payUrl+",orderId="+newOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payUrl);
					if(payUrl != null && !"".equals(payUrl)){
						String payResult = HttpClientUtils.doGet(payUrl, "UTF-8");
						statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payResult);
						log.info("HSRdoService payResult="+ payResult+",orderId="+newOrder.getOrderId());
						newOrder.setPayUrl(payUrl);
						if(payResult.contains("成功消费")){
							newOrder.setResultCode(RES_SUCCESS);
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
//							// 立即同步方式
//							newOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
//							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
//							result.put("resultCode", GlobalConst.Result.SUCCESS);
//							result.put("resultMsg","请求成功。");
//							TChannelPipleKey pkey = new TChannelPipleKey();
//							pkey.setChannelId(newOrder.getChannelId());
//							pkey.setPipleId(newOrder.getPipleId());
//							TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
//							boolean bDeducted  = newOrder.deduct(cp.getVolt());
//							if(bDeducted){
//								newOrder.setResultCode("1888002");
//								result.put("resultCode", GlobalConst.Result.ERROR);
//								result.put("resultMsg","订购失败：2");
//							}else{ // 不扣量 通知渠道
//								newOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
////								n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
//								notifyChannel(cp.getNotifyUrl(), newOrder, tProduct.getProductCode(), "ok");
//							}
						}else{
							newOrder.setResultCode("2");
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","订购失败：2");
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
	public String getPipleKey() {
		return "PM1011";
	}
	
	/**
	 *	双短方式 
	 * */
	@Override
	public String processGetMessage(String mobile,String requestBody) throws Exception {
		String resultMsg = "";
		String args[] = requestBody.split("\\$");
		String apiKey = args[0];
		String productCode = args[1];
		String extData = null;
		if(args.length >2){
			extData = args[2];
		}
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("productCode",productCode);
		request.put("mobile",mobile);
		request.put("extData",extData);
		JSONObject result = processGetSMS(request);
		if(result != null){
			if("0".equals(result.optString("resultCode"))){
				JSONObject param = new JSONObject();
				param.put("orderId",result.optString("orderId") );
				param.put("apiKey", apiKey);
				param.put("pipleKey", getPipleKey());
				param.put("productCode", productCode);
				MapCacheManager.getInstance().getSmsOrderCache().put(mobile,param.toString());
			}
			if(StringUtil.isNotEmptyString(result.optString("orderId"))){
				TOrder tOrder = tOrderDao.selectByPrimaryKey(result.optString("orderId"));
				if(tOrder != null){
					statistics(STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"1$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
				}
			}
			logger.debug(JSONObject.fromObject(result).toString());
		}
		return resultMsg;
	}
	
	@Override
	public String processSubmitMessage(String mobile,String requestBody) throws Exception {
		String jsonStr = MapCacheManager.getInstance().getSmsOrderCache().get(mobile);
		JSONObject param = JSONObject.fromObject(jsonStr);
		String orderId = param.optString("orderId");
		if(StringUtil.isEmpty(orderId)){
			return "";
		}
		TOrder tOrder = tOrderDao.selectByPrimaryKey(orderId);
		if(tOrder == null)
			return "";
		statistics(STEP_SUBMIT_MESSAGE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody);
		String args[] = requestBody.split("\\$");
		if(args.length <2)
			return "";
		String apiKey = args[0];
		String verifyCode = args[1];
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("orderId",tOrder.getOrderId());
		request.put("verifyCode",verifyCode);
		JSONObject result = processVertifySMS(request);
		MapCacheManager.getInstance().getSmsOrderCache().remove(mobile);
		if(result != null){
			logger.debug(JSONObject.fromObject(result).toString());
			statistics(STEP_SUBMIT_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
		}
		return "";
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("HSRdoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String linkid = requestBody.optString("linkid");
		String mobile   = requestBody.optString("mobile");
		String port = requestBody.optString("port");
		String msg = requestBody.optString("msg");
		String status = requestBody.optString("status");
		String param = requestBody.optString("param");  // 透传订单号
		String ftime = requestBody.optString("ftime"); 
		String orderId = param.substring(5, param.length());   //  前5位为固定参数,订单号从第6位开始
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
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
			order.setPipleOrderId(linkid);
			order.setResultCode(status);
			if(PAY_SUCCESS.equals(status)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				if(!bDeducted){ // 不扣量 通知渠道
					TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
					String productCode = tProduct.getProductCode();
//					n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
					notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class HSRdoOrder extends TOrder{
		private String getCodeUrl;
		private String smsVertifyUrl;
		private String payUrl;  
		private String verifyCode;

		public String getGetCodeUrl() {
			return getCodeUrl;
		}

		public void setGetCodeUrl(String getCodeUrl) {
			this.getCodeUrl = getCodeUrl;
		}

		public String getSmsVertifyUrl() {
			return smsVertifyUrl;
		}

		public void setSmsVertifyUrl(String smsVertifyUrl) {
			this.smsVertifyUrl = smsVertifyUrl;
		}

		public String getPayUrl() {
			return payUrl;
		}

		public void setPayUrl(String payUrl) {
			this.payUrl = payUrl;
		}

		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.getCodeUrl != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("getCodeUrl");
				oExt.setExtValue(this.getCodeUrl);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smsVertifyUrl != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsVertifyUrl");
				oExt.setExtValue(this.smsVertifyUrl);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.payUrl != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("payUrl");
				oExt.setExtValue(this.payUrl);
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
