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
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TPipleProvince;
import com.qy.sp.fee.dto.TPipleProvinceKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
@Service
public class YueyouMiguDongmanService extends ChannelService{
	private  Logger log = Logger.getLogger(YueyouMiguDongmanService.class);	
	private static String PREFIX_PARAM = "qianya";
	@Override
	public String getPipleId() {
		return "14830855382615142608551";
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
				String serverIp = "218.94.146.154";
				String serverPort = "8000";
				String sid = "18915af759a06002014e9ae87798b57d650a2079";
				String itemId = pipleProduct.getPipleProductCode();
				String price = "10";
				if(!"01".equals(pipleProduct.getProductId())){
					price = NumberUtil.getInteger(pipleProduct.getProductId()) *100 +"";
				}
				String requestUrl = "http://%s:%s/o/migu2api/%s/?is_consume=0&item_id=%s";
				List<String> args = new ArrayList<String>();
				args.add(serverIp);
				args.add(serverPort);
				args.add(sid);
				args.add(itemId);
				
				if(StringUtil.isNotEmptyString(imei)){
					requestUrl += "&imei=%s";
					args.add(imei);
				}
				if(StringUtil.isNotEmptyString(imsi)){
					requestUrl += "&imsi=%s";
					args.add(imsi);
				}
				if(StringUtil.isNotEmptyString(price)){
					requestUrl += "&item_price=%s";
					args.add(price);
				}
				if(StringUtil.isNotEmptyString(order.getOrderId())){
					requestUrl += "&cpparam=%s";
					args.add(PREFIX_PARAM+order.getOrderId());
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
						XMLSerializer xmlSerializer = new XMLSerializer();
						jsonObject = (JSONObject) xmlSerializer.read(pipleResult);
						JSONObject miguObject = jsonObject.optJSONObject("migu");
						String miguSMSNumber = miguObject.optString("sms_num");
						String miguSMS = miguObject.optString("sms");
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						order.setResultCode(GlobalConst.Result.SUCCESS);
						order.setModTime(DateTimeUtils.getCurrentTime());
						//设置短信结果
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功");
						result.put("orderId",order.getOrderId());
						result.put("sms_num",miguSMSNumber);
						result.put("sms_message",miguSMS);
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
		String status = requestBody.optString("status");
		String amount = requestBody.optString("price");
		String phoneNumber = requestBody.optString("phone_number");
		String transId = requestBody.optString("trans_id");
		String cpparam = requestBody.optString("cpparam");
		cpparam = cpparam.substring(6);
		//获取订单信息
		TOrder order = tOrderDao.selectByPrimaryKey(cpparam);
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
				if(StringUtil.isNotEmptyString(phoneNumber)&& !phoneNumber.contains("[")){
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
				if(isSend){
					TPipleProvinceKey pproKey = new TPipleProvinceKey();
					 pproKey.setProvinceId(order.getProvinceId());
					 pproKey.setPipleId(order.getPipleId());
					 TPipleProvince pipleProvince = tPipleProvinceDao.selectByPrimaryKey(pproKey);
					 if(pipleProvince.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
						 isSend = false;
					 }
				}
			}else {
				if(StringUtil.isNotEmptyString(phoneNumber) && !phoneNumber.contains("[")){
					order.setMobile(phoneNumber);
					int  provinceId = this.getProvinceIdByMobile(phoneNumber, false); // 获取省份ID
					order.setProvinceId(provinceId);
				}
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
		return "ok";
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	
}
