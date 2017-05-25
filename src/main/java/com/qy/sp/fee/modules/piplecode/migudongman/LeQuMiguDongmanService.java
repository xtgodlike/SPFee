package com.qy.sp.fee.modules.piplecode.migudongman;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class LeQuMiguDongmanService extends ChannelService{
	private  Logger log = Logger.getLogger(LeQuMiguDongmanService.class);	
	@Override
	public String getPipleId() {
		return "14685695389177498476580";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("MiguDongmanService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String ip = requestBody.optString("ip");
		String extData = requestBody.optString("extData");
		
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)  || StringUtil.isEmpty(productCode)){
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
			//保存订单
			TOrder order = new TOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setExtData(extData);
			order.setGroupId(groupId);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String serverIp = "121.14.38.97";
				String serverPort = "25324";
				String sid = KeyHelper.getRandomNumber(15);
				String channelId = "4180001";
				String paycode = pipleProduct.getPipleProductCode();
				String operation = "102";
				String appId = "300000008119";
				String requestUrl = "http://%s:%s/argo_mtk/service?sid=%s&channel_id=%s&operation=%s";
				List<String> args = new ArrayList<String>();
				args.add(serverIp);
				args.add(serverPort);
				args.add(sid);
				args.add(channelId);
				args.add(operation);
				if(StringUtil.isNotEmptyString(imei)){
					requestUrl += "&imei=%s";
					args.add(imei);
				}
				if(StringUtil.isNotEmptyString(imsi)){
					requestUrl += "&imsi=%s";
					args.add(imsi);
				}
				if(StringUtil.isNotEmptyString(paycode)){
					requestUrl += "&paycode=%s";
					args.add(paycode);
				}
				if(StringUtil.isNotEmptyString(appId)){
					requestUrl += "&app_id=%s";
					args.add(appId);
				}
				if(StringUtil.isNotEmptyString(ip)){
					requestUrl += "&ip=%s";
					args.add(ip);
				}
				requestUrl = StringUtil.format(requestUrl, args.toArray());
				
				log.info("MiguDongmanService reqUrl:"+ requestUrl);

				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,requestUrl);
				
				String pipleResult= HttpClientUtils.doGet(requestUrl, HttpClientUtils.UTF8);
				log.info("MiguDongmanService getSmsResult:"+  pipleResult);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(StringUtil.isNotEmptyString(pipleResult)){
					JSONObject jsonObject = null;
					try{
						jsonObject = JSONObject.fromObject(pipleResult);
						String code = jsonObject.optString("result");
						if("0".equals(code)){
							String smsmsg = jsonObject.optString("smsmsg");
							String tradeid = jsonObject.optString("tradeid");
							String smsport = jsonObject.optString("smsport");
							order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							order.setResultCode(GlobalConst.Result.SUCCESS);
							order.setModTime(DateTimeUtils.getCurrentTime());
							order.setPipleOrderId(tradeid);
							result.put("resultCode",GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功");
							result.put("orderId",order.getOrderId());
							result.put("sms_num",smsport);
							result.put("sms_message",smsmsg);
						}else{
							order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(code);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode",code);
							result.put("resultMsg","请求失败");
						}
					}catch(Exception e){
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求错误");
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setModTime(DateTimeUtils.getCurrentTime());
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
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}
		}
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("MiguDongmanService processPaySuccess 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		//获取通道同步给我们的数据
		String status = "0";
		String phoneNumber = requestBody.optString("MSISDN");
		String transId = requestBody.optString("OrderID");
		//获取订单信息
		TOrder order = tOrderDao.selectByPipleOrderId(transId);
		if(order!=null && order.getOrderStatus()!=GlobalConst.OrderStatus.SUCCESS){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			//获取产品信息
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			//判断是否扣量
			boolean isSend = false;
			if("0".equals(status)){
				if(StringUtil.isNotEmptyString(phoneNumber)){
					order.setMobile(phoneNumber);
					int  provinceId = this.getProvinceIdByMobile(phoneNumber, false); // 获取省份ID
					order.setProvinceId(provinceId);
				}
				order.setPipleOrderId(transId);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 时通知渠道
				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
			}
		}
		String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SyncAppOrderResp xmlns=\"http://www.monternet.com/dsmp/schemas/\"><TransactionID>%s</TransactionID><MsgType>SyncAppOrderResp</MsgType><Version>1.0.0</Version>< hRet >0</ hRet ></SyncAppOrderResp>";
		result = StringUtil.format(result, transId);
		return result;
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	
}
