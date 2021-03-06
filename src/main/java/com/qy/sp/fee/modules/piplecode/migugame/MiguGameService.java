package com.qy.sp.fee.modules.piplecode.migugame;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.Base64;
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
import com.qy.sp.fee.dto.TPipleProvince;
import com.qy.sp.fee.dto.TPipleProvinceKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
@Service
public class MiguGameService extends ChannelService{
	private  Logger log = LoggerFactory.getLogger(MiguGameService.class);	
	@Override
	public String getPipleId() {
		return "14797090033600911437258";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("MiguGameService processGetSMS requestBody:"+requestBody);
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
				String requestUrl = "http://115.159.3.19:8080/InterfaceServer/serverEnter.php";

				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,requestUrl);
				
				JSONObject param = new JSONObject();
				param.put("msgID", "0");
				String base64Msg = new String(Base64.encodeBytes(param.toString().getBytes()));
				param.put("version", "1.0.0");
				param.put("data",base64Msg);
				String pipleResult= HttpClientUtils.doPost(requestUrl,param.toString(),HttpClientUtils.UTF8);
				log.info("MiguGameService getSmsResult:"+  pipleResult);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(StringUtil.isNotEmptyString(pipleResult)){
					JSONObject jsonObject = null;
					try{
						jsonObject = JSONObject.fromObject(pipleResult);
						String errorCode = jsonObject.optString("error_code");
						String guid = jsonObject.optString("guid");
						JSONObject miguObject = jsonObject.optJSONObject("migu");
						String smsreg = miguObject.optString("smsregport");
						String miguSMS = miguObject.optString("smsreg");
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						order.setResultCode(GlobalConst.Result.SUCCESS);
						order.setModTime(DateTimeUtils.getCurrentTime());
						//设置短信结果
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功");
						result.put("orderId",order.getOrderId());
						result.put("sms_num",miguSMS);
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
