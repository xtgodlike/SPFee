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
import com.qy.sp.fee.modules.piplecode.hs.HSRdoService.HSRdoOrder;

import net.sf.json.JSONObject;
@Service
public class GGZFRdoService extends ChannelService{
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	public final static String PAY_SUCCESS = "0000";  // 扣费成功
	public final static String PORT = "1069009216288";
	private  Logger log = Logger.getLogger(GGZFRdoService.class);		
	@Override
	public String getPipleId() {
		return "14785890270023978022243";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("GGZFRdoService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
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
			GGZFRdoOrder order = new GGZFRdoOrder();
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
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String param = pipleProduct.getPipleProductCode();  
				String reqUrl = piple.getPipleUrlA()+"?"+"mobile="+order.getMobile()+"&channel="+piple.getPipleAuthA()+"&param="+param;
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				log.info("GGZFRdoService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObject = JSONObject.fromObject(pipleResult);
					String status= jsonObject.optString("status");
					String message = jsonObject.optString("message");
					String orderNo = jsonObject.optString("orderNo");
					order.setPipleOrderId(orderNo);
					statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,jsonObject.toString());
					if(RES_SUCCESS.equals(status)){
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
						order.setResultCode(status);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败。"+message);
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
		log.info("GGZFRdoService processVertifySMS requestBody:"+requestBody);
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
					GGZFRdoOrder newOrder = new GGZFRdoOrder();
					newOrder.setTOrder(tOrder);
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					ppkey.setProductId(tProduct.getProductId());
					TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					String param = pipleProduct.getPipleProductCode();  
					String reqUrl = piple.getPipleUrlB()+"?"+"smscode="+verifyCode+"&param="+param+"&channel="+piple.getPipleAuthA();
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl);
					String payResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8); 
					log.info("GGZFRdoService payResult:"+  payResult+",orderId="+newOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payResult);
					if(payResult != null && !"".equals(payResult)){
						JSONObject jsonObject = JSONObject.fromObject(payResult);
						String status= jsonObject.optString("status");
						String message = jsonObject.optString("message");
						String orderNo = jsonObject.optString("orderNo");
						newOrder.setPipleOrderId(orderNo);
						log.info("GGZFRdoService payResult="+ payResult+",orderId="+newOrder.getOrderId());
						if(RES_SUCCESS.equals(status)){
							newOrder.setResultCode(RES_SUCCESS);
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
							result.put("resultMsg","订购失败");
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
		}catch(Exception e){
			e.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
		
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("GGZFRdoService 支付同步数据:"+requestBody);
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
					if(GlobalConst.FromType.FROM_TYPE_SMS.equals(order.getFromType())){
						notifyChannelSMS(cp.getNotifyUrl(), order,PORT, "ok");
					}else{
						notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
					}
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
	
	public class GGZFRdoOrder extends TOrder{
		
	}
}
