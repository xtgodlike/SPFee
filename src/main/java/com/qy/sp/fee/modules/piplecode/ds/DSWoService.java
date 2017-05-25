package com.qy.sp.fee.modules.piplecode.ds;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import com.qy.sp.fee.dto.TOrderExtKey;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import com.qy.sp.fee.service.MobileSegmentService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class DSWoService extends ChannelService{
	private Logger log = Logger.getLogger(DSWoService.class);
	public final static String PORT = "1069009216288";
	private final static String REQ_SUCCESS = "0";   // 请求成功
	@Override
	public String getPipleId() {
		return "14775389896200102268388";
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
		String fromType = requestBody.optString("fromType");
		String ipProvince = requestBody.optString("ipProvince");
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
			int provinceId = 0;
			if(StringUtil.isEmpty(ipProvince)){
				provinceId = mobileSegmentService.getProvinceIdByMobile(mobile);
			}else{
				provinceId = mobileSegmentService.getProvinceByIpProvince(ipProvince);
			}
			//保存订单
			DSWORdoOrder order = new DSWORdoOrder();
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
			Date CurrentTime=DateTimeUtils.getCurrentTime();
			order.setCreateTime(CurrentTime);
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);
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
			try{
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String reqUrl =  piple.getPipleUrlA();
				String chargeCode = pipleProduct.getPipleProductCode();
				//请求验证码接口
				Map<String, String> params = new HashMap<String, String>();
				params.put("mobile", mobile);
				params.put("chargeCode", chargeCode);
				params.put("callbackUrl", piple.getNotifyUrlA());
				params.put("transmissionData", order.getOrderId());
				JSONObject requset = new JSONObject();
				requset.putAll(params);
				String json = requset.toString();
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl+json);
				String pipleResult = HttpClientUtils.doPost(reqUrl, json, HttpClientUtils.UTF8);
				log.info(" DSWOService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject object = JSONObject.fromObject(pipleResult);
					String code = object.getString("code");
					String msg = object.getString("msg");
					log.info("code:"+code+",msg:"+msg);
					String pipleOrderId = null;//通道订单号
					String actionTarget = null;//验证接口
					String authCode =null;//验证码
					String dsOrderId = null;//外部orderId
					if(REQ_SUCCESS.equals(code)){
						JSONObject jsonResult= object.getJSONObject("result");
						JSONArray actionList= jsonResult.getJSONArray("actionList");
						JSONObject action = actionList.getJSONObject(0);
						JSONObject actionParam = action.getJSONObject("actionParam");
						pipleOrderId = actionParam.getString("orderId");
						actionTarget = action.getString("actionTarget");
						dsOrderId = jsonResult.getString("orderId");
						order.setResultCode(code);
						order.setSmsVertifyUrl(actionTarget);
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						order.setResultCode(String.valueOf(code));
						order.setPipleOrderId(pipleOrderId);
						order.setDSOrderId(dsOrderId);
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
					}else{
					    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode("1");
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求错误："+msg);
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
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
		log.info("DSWOService processVertifySMS requestBody:"+requestBody);
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
				DSWORdoOrder newOrder =  new DSWORdoOrder();
				newOrder.setTOrder(tOrder);
				newOrder.setVerifyCode(verifyCode);
				newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
				TPipleProductKey ppkey = new TPipleProductKey();
				ppkey.setPipleId(getPipleId());
				ppkey.setProductId(tProduct.getProductId());
				TOrderExtKey oExtKey = new TOrderExtKey();
				oExtKey.setOrderId(orderId);
				oExtKey.setExtKey("smsVertifyUrl");
				TOrderExt urlExt = tOrderExtDao.selectByPrimaryKey(oExtKey);
					String smsVertifyUrl = urlExt.getExtValue();
					JSONObject jsons = new JSONObject();
					jsons.put("orderId", tOrder.getPipleOrderId());
					jsons.put("authCode",verifyCode);
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), smsVertifyUrl+";"+jsons.toString());
					String payResult = HttpClientUtils.doPost(smsVertifyUrl, jsons.toString(), HttpClientUtils.UTF8);
					if(payResult != null && !"".equals(payResult)){
						JSONObject object = JSONObject.fromObject(payResult);
						String code = object.getString("code");
						String msg = object.getString("msg");
						if(REQ_SUCCESS.equals(code)){
							result.put("resultCode", GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功。");
							newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
						}else{
							newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
							newOrder.setResultCode("1");
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败："+msg);
							SaveOrderUpdate(newOrder);
						}
						
					}else{
						newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
						SaveOrderUpdate(newOrder);
					}
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				
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
		logger.info("DWOService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String code = requestBody.optString("code");
		String msg = requestBody.optString("msg");
		String orderId = "S0"+requestBody.optString("orderId");
		String mobile = requestBody.optString("mobile");
		String price = requestBody.optString("price");
		String chargeCode = requestBody.optString("chargeCode");
		String transmissionData = requestBody.optString("transmissionData");
		TOrder order = tOrderDao.selectByPrimaryKey(transmissionData);
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
			if("0".equals(code)){
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
			}
			SaveOrderUpdate(order);
		}
		return "200";
	}
	
	
	public class DSWORdoOrder extends TOrder{
		private String smsVertifyUrl;//验证接口
		private String verifyCode;//验证码
		private String DSOrderId;//东硕外部orderId
		public String getSmsVertifyUrl() {
			return smsVertifyUrl;
		}
		public void setSmsVertifyUrl(String smsVertifyUrl) {
			this.smsVertifyUrl = smsVertifyUrl;
		}
		
		public String getVerifyCode() {
			return verifyCode;
		}
		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}
		public String getDSOrderId() {
			return DSOrderId;
		}
		public void setDSOrderId(String dSOrderId) {
			DSOrderId = dSOrderId;
		}
		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.smsVertifyUrl != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smsVertifyUrl");
				oExt.setExtValue(this.smsVertifyUrl);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.verifyCode != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("verifyCode");
				oExt.setExtValue(this.verifyCode);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}if(this.DSOrderId != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("DSOrderId");
				oExt.setExtValue(this.DSOrderId);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
