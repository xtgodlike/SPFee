package com.qy.sp.fee.modules.piplecode.ct;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MD5;
import com.qy.sp.fee.common.utils.MapCacheManager;
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

@Service
public class CTSWService extends ChannelService {
	public final static String RES_OK = "ok";
	public final static String IP = "60.18.127.254";
	public final static String PAY_SUCCESS = "0";
	public final static String PORT = "1069009216288";
	private final static String IMEI = "868721028101329";
	private  Logger log = Logger.getLogger(CTSWService.class);	
	@Override
	public String getPipleId() {
		return "14786758299343112647369";
	}
	@Override
	public String getPipleKey() {
		return "PM1052";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("CTSWService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = getPipleId();
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		String company = null;
		String gamename = null;
		String feename = null;
		String ipProvince = requestBody.optString("ipProvince");
		String fromType = requestBody.optString("fromType");
		if(GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)){
			company = apiKey;
			gamename = apiKey;
			feename = apiKey;
		}else{
			company = requestBody.optString("company");
			gamename = requestBody.optString("gamename");
			feename = requestBody.optString("feename");
		}
		
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
			CTSWOrder order = new CTSWOrder();
			ppkey.setProductId(tProduct.getProductId());
			ppkey.setPipleId(pipleId);
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			if(requestBody.containsKey("fromType")){
				if(GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)){
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_SMS));
				}else if(GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)){
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_SDK));
				}else if(GlobalConst.FromType.FROM_TYPE_API.equals(fromType)){
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_API));
				}
			}else{
				order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_API));
			}
			int provinceId = 0;
			if(StringUtil.isEmpty(ipProvince)){
				provinceId = mobileSegmentService.getProvinceIdByMobile(mobile);
			}else{
				provinceId = mobileSegmentService.getProvinceByIpProvince(ipProvince);
			}
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			order.setProvinceId(provinceId);
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
			order.setPrice(String.valueOf(tProduct.getPrice()));
			order.setIp(IP);
			order.setCompany(company);
			order.setGamename(gamename);
			order.setFeename(feename);
 			order.setExtData(extData);
			try{
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String reqUrl = piple.getPipleUrlA();
				String merchantid = piple.getPipleAuthA();
				String type = piple.getPipleAuthB();
				String key = piple.getPipleAuthC();
				String pipleProductCode = pipleProduct.getPipleProductCode();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("linkid", order.getOrderId());
				jsonObject.put("type", type);
				jsonObject.put("merchantid", merchantid);
				jsonObject.put("price",tProduct.getPrice());
				jsonObject.put("phone",order.getMobile());
				jsonObject.put("imsi", order.getImsi());
				jsonObject.put("feename", feename);
				jsonObject.put("gamename", gamename);
				jsonObject.put("imei",imei);
				jsonObject.put("company",company);
				jsonObject.put("code", pipleProductCode);
				jsonObject.put("ip", IP);
				jsonObject.put("cpparam", order.getOrderId());
				Map<String,String> header = new HashMap<String,String>();
				header.put("sign", getSign(jsonObject.toString()+key));
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl+";"+jsonObject);
				String pipleResult = HttpClientUtils.doPost(reqUrl, header, jsonObject.toString(), HttpClientUtils.UTF8);
				log.info("CTSWService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject resultObject = JSONObject.fromObject(pipleResult);
					String resultStatus = resultObject.optString("result");
					String failcode = resultObject.optString("failcode");
					if(RES_OK.equals(resultStatus)){
						order.setResultCode(resultStatus);
					    order.setModTime(DateTimeUtils.getCurrentTime());
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						result.put("resultCode", GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
					}else{
						order.setModTime(DateTimeUtils.getCurrentTime());
					    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode(failcode);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败。");
					}
					SaveOrderUpdate(order);
					statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
					return result;
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
		}
	}
	
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("CTSWService processVertifySMS requestBody:"+requestBody);
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
					CTSWOrder newOrder = new CTSWOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setVerifyCode(verifyCode);
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					String reqUrl = piple.getPipleUrlB();
					String merchantid = piple.getPipleAuthA();
					String type = piple.getPipleAuthB();
					String key = piple.getPipleAuthC();
					String code = pipleProduct.getPipleProductCode();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("linkid", newOrder.getOrderId());
					jsonObject.put("type", type);
					jsonObject.put("merchantid", merchantid);
					jsonObject.put("code", code);
					jsonObject.put("param", verifyCode);
					Map<String,String> header = new HashMap<String,String>();
					header.put("sign", getSign(jsonObject.toString()+key));
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl+";"+jsonObject.toString());
					String payResult = HttpClientUtils.doPost(reqUrl, header, jsonObject.toString(), HttpClientUtils.UTF8);
					log.info("CTSWService getPageResult:"+  payResult+",orderId="+newOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payResult);
					if(payResult != null && !"".equals(payResult)){
						JSONObject object = JSONObject.fromObject(payResult);
						String resultStatus = object.optString("result");
						String failcode = object.optString("failcode");
						if(RES_OK.equals(resultStatus)){
							newOrder.setResultCode(resultStatus);
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							result.put("resultCode", GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功。");
						}else{
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						    newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							newOrder.setResultCode(failcode);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败。");
						}
					}else{
						newOrder.setResultCode(GlobalConst.Result.ERROR);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","订购失败");
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
		String extDatas[] = extData.split("\\%");
		String imsi = null;
		if(extDatas.length>1){
			imsi = extDatas[0];
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
		request.put("fromType",GlobalConst.FromType.FROM_TYPE_SMS);
		request.put("imsi", imsi);
		request.put("imei", IMEI);
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
		request.put("verifyCode", verifyCode);
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
		logger.info("CTSWService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String linkid = requestBody.optString("linkid");
		String status = requestBody.optString("status");
		String cpparam = requestBody.optString("cpparam");
		TOrder order = tOrderDao.selectByPrimaryKey(cpparam);
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
			order.setPipleOrderId(linkid);
			order.setResultCode(status);
			if(PAY_SUCCESS.equals(status)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				if(!bDeducted){ // 不扣量 通知渠道
					if(GlobalConst.FromType.FROM_TYPE_SMS.equals(order.getFromType())){
						notifyChannelSMS(cp.getNotifyUrl(), order,PORT, "ok");
					}else{
						notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
					}
				}
			}else{
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
		}
		return RES_OK;
	}
	
	
	private String getSign(String str){
		byte[] source =null;
		try {
			source = str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String stringSignTemp = MD5.getMD5(source).toUpperCase();
		return stringSignTemp;
	}
	
	
	public class CTSWOrder extends TOrder{
		private String price;
		private String ip;
		private String company;
		private String gamename;
		private String feename;
		private String verifyCode;
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public String getCompany() {
			return company;
		}
		public void setCompany(String company) {
			this.company = company;
		}
		public String getGamename() {
			return gamename;
		}
		public void setGamename(String gamename) {
			this.gamename = gamename;
		}
		public String getFeename() {
			return feename;
		}
		public void setFeename(String feename) {
			this.feename = feename;
		}
		public String getVerifyCode() {
			return verifyCode;
		}
		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}
		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.price != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("price");
				oExt.setExtValue(this.price);
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
			if(this.company != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("company");
				oExt.setExtValue(this.company);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.gamename != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("gamename");
				oExt.setExtValue(this.gamename);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.feename != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("feename");
				oExt.setExtValue(this.feename);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.verifyCode != null){
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
