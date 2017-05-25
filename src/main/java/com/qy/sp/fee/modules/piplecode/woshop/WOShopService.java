package com.qy.sp.fee.modules.piplecode.woshop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.NumberUtil;
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
public class WOShopService extends ChannelService{
	public final static String RES_SUCCESS = "00000";  // 请求通道成功
	public final static String PAY_SUCCESS = "10000";  // 扣费成功
	public final static String PORT = "1065519866";
	private  Logger log = Logger.getLogger(WOShopService.class);		
	@Override
	public String getPipleId() {
		return "14821138830617686955782";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("WOShopService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String ipProvince = requestBody.optString("ipProvince");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
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
			req.setIpProvince(ipProvince);
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
			WOShopOrder order = new WOShopOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);
			if(requestBody.containsKey("fromType")){
				if(GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_SMS));
				}else if(GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_SDK));
				}else if(GlobalConst.FromType.FROM_TYPE_API.equals(fromType)){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
				}
			}else{
				order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
			}
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String param = pipleProduct.getPipleProductCode()+order.getOrderId();  // 计费代码（5位）+透传参数（订单号）
				String reqUrl = piple.getPipleUrlA()+"?"+"param="+param+"&imsi="+imsi+"&mobile="+order.getMobile()+"&channel="+piple.getPipleAuthA();
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				log.info("WOShopService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,"requrl"+reqUrl+",pipleResult:"+pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject object = JSONObject.fromObject(pipleResult);
					String code = object.getString("code");
					String msg = object.getString("msg");
					log.info("code:"+code+",msg:"+msg);
					if(RES_SUCCESS.equals(code)){
						order.setResultCode(code);
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
					}else{
					    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode(code);
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求错误："+msg);
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
			}catch (Exception e){
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
		log.info("WOShopService processVertifySMS requestBody:"+requestBody);
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
				req.setMobile(tOrder.getMobile());
				TPipleProductKey ppkey = new TPipleProductKey();
				ppkey.setPipleId(getPipleId());
				ppkey.setProductId(tProduct.getProductId());
				TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
				TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
				BaseResult bResult = this.accessVerify(req, getPipleId());
				if(bResult!=null){// 返回不为空则校验不通过
					result.put("resultCode",bResult.getResultCode());
					result.put("resultMsg",bResult.getResultMsg());
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}else{
					WOShopOrder newOrder = new WOShopOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setVerifyCode(verifyCode);
					String param = pipleProduct.getPipleProductCode()+orderId;
					String payUrl = piple.getPipleUrlB()+"?"+"param="+param+"&imsi="+newOrder.getImsi()+"&mobile="+newOrder.getMobile()+"&smscode="+verifyCode;
					String payResult= HttpClientUtils.doGet(payUrl, HttpClientUtils.UTF8);
					log.info("WOShopService getPageResult:"+  payResult+",orderId="+newOrder.getOrderId());
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), payResult);
					if(payResult != null && !"".equals(payResult)){
						JSONObject object = JSONObject.fromObject(payResult);
						if(object.containsKey("code")){
							String code= object.optString("code");
							String msg = object.optString("msg");
							if(RES_SUCCESS.equals(code)){
								newOrder.setResultCode(PAY_SUCCESS);
								newOrder.setModTime(DateTimeUtils.getCurrentTime());
								newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
								newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。"+msg);
							}else{
								newOrder.setModTime(DateTimeUtils.getCurrentTime());
								newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
								newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
								newOrder.setResultCode(code);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败。"+msg);
							}
						}else{
							String resultCode = object.optString("resultCode");
							String reMsg = object.optString("reMsg");
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							newOrder.setResultCode(resultCode);
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败"+reMsg);
						}
					}else{
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
		return result;
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
					TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
					String productCode = tProduct.getProductCode();
					notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
				}
			}else{
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
		}
		return "ok";
	}
	
	public class WOShopOrder extends TOrder{
		private String verifyCode;
		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}
		
		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
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
