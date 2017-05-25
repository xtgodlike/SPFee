package com.qy.sp.fee.modules.piplecode.jz;

import java.math.BigDecimal;

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
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class JZWoMusicService extends ChannelService{
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String PAY_SUCCESS = "delivrd";  // 扣费成功
	private  Logger log = Logger.getLogger(JZWoMusicService.class);		
	@Override
	public String getPipleId() {
		return "14709878328508528319181";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("JZWoMusicService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
//		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)  || StringUtil.isEmpty(mobile)){
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
			TOrder order = new TOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(getPipleId());
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
//			order.setImei(imei);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(getProvinceIdByMobile(mobile, false));
			order.setExtData(extData);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				//请求短信指令内容
//				Map<String, String> params = new HashMap<String, String>();
//				params.put("ppid", piple.getPipleAuthA());
//				params.put("custom", order.getExtData());
				String reqUrl = piple.getPipleUrlA()+"?"+"cpid="+piple.getPipleAuthA()+"&appKey="+piple.getPipleAuthB()+"&mobile="+order.getMobile()+"&payPoint="+pipleProduct.getPipleProductCode();
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				log.info(" JZWoMusicService getSmsResult:"+  pipleResult);
				// result test:{"errorCode":0,"errorDesc":"success","msg":"","ext":"18586878646"}
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObj = JSONObject.fromObject(pipleResult);
					String errorCode = null;
					String errorDesc = null;
					String msg = null;
					if(jsonObj.has("errorCode") ){
						errorCode = jsonObj.getString("errorCode");
						if(RES_SUCCESS.equals(errorCode)){// 返回成功
							errorCode = jsonObj.getString("errorCode");
							errorDesc = jsonObj.getString("errorDesc");
							msg = jsonObj.getString("msg");
//							 order.setPipleOrderId(orderid);
							 order.setResultCode(errorCode);
							 order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
						}else{
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(errorCode);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+errorDesc);
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
		log.info("JZWoMusicService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			
			String orderId = requestBody.optString("orderId");
			String vCode = requestBody.optString("vCode");
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			result.put("orderId", orderId);
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
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					
					String reqUrl = piple.getPipleUrlB()+"?"+"cpid="+piple.getPipleAuthA()+"&appKey="+piple.getPipleAuthB()+"&mobile="+tOrder.getMobile()
											+"&payPoint="+pipleProduct.getPipleProductCode()+"&validCode="+vCode+"&cpparam="+piple.getPipleAuthC()+tOrder.getOrderId();
					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl);
					String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					log.info(" JZWoMusicService confirmResult:"+  pipleResult);
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						JSONObject jsonObj = JSONObject.fromObject(pipleResult);
						String errorCode = null;
						String errorDesc = null;
						String msg = null;
						if(jsonObj.has("errorCode") ){
							errorCode = jsonObj.getString("errorCode");
							if(RES_SUCCESS.equals(errorCode)){// 返回成功
								errorDesc = jsonObj.getString("errorDesc");
								tOrder.setResultCode(errorCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。");
							}else{
								tOrder.setResultCode(errorCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+errorDesc);
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
	public String getPipleKey() {
		return "PM1032";
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
		String vCode = args[1];
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("orderId",tOrder.getOrderId());
		request.put("vCode",vCode);
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
		logger.info("JZWoMusicService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String mobile = requestBody.optString("mobile");
		String momsg = requestBody.optString("momsg");   // 透传字段    wn12+orderId
		String spnumber = requestBody.optString("spnumber");
		String linkid = requestBody.optString("linkid");
		String flag = requestBody.optString("flag");
		String orderId = "";
		if(!StringUtil.isEmpty(momsg)){
			orderId =  momsg.substring(4, momsg.length());
		}
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
		if(order!=null ){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			
			if(PAY_SUCCESS.equals(flag)){// 扣费成功
				TChannelPipleKey pkey = new TChannelPipleKey();
				pkey.setChannelId(order.getChannelId());
				pkey.setPipleId(order.getPipleId());
				TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
				if(cp == null){
					return "channel error";
				}
				boolean isSend = false; //是否同步
				order.setPipleOrderId(linkid);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
				doWhenPaySuccess(order);
				if(isSend){ // 不扣量 通知渠道
					TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
					notifyChannel(cp.getNotifyUrl(), order, tProduct.getProductCode(), "ok");
				}else {
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
					order.setModTime(DateTimeUtils.getCurrentTime());
					order.setResultCode(flag);
				}
				SaveOrderUpdate(order);
			}
			
		}else{
			return "order no found";
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
}
