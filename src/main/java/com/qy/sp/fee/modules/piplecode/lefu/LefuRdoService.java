package com.qy.sp.fee.modules.piplecode.lefu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
public class LefuRdoService extends ChannelService {

	@Override
	public String getPipleId() {
		return "14707207010883672273133";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String iccid = requestBody.optString("iccid");
		String extData = requestBody.optString("extData");
		JSONObject result = new JSONObject();
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey) || StringUtil.isEmptyString(mobile)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		} else {
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setPipleId(pipleId);
			req.setMobile(mobile);
			BaseResult bResult = this.accessVerify(req);
			if (bResult != null) {// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			} else {
				String groupId = KeyHelper.createKey();
				statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
				TPiple tPiple = this.tPipleDao.selectByPrimaryKey(getPipleId());

				TProduct tProduct = this.tProductDao.selectByCode(productCode);
				TChannel tChannel = this.tChannelDao.selectByApiKey(apiKey);
				TOrder order = new TOrder();
				order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
				order.setChannelId(tChannel.getChannelId());
				order.setCreateTime(DateTimeUtils.getCurrentTime());
				order.setIccid(iccid);
				order.setImsi(imsi);
				order.setMobile(mobile);
				order.setOrderId(KeyHelper.createKey());
				order.setOrderStatus(GlobalConst.OrderStatus.INIT);
				order.setPipleId(getPipleId());
				order.setProductId(tProduct.getProductId());
				order.setProvinceId(req.getProvinceId());
				order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
				order.setGroupId(groupId);
				order.setExtData(extData);
				this.SaveOrderInsert(order);
				TPipleProductKey key = new TPipleProductKey();
				key.setPipleId(getPipleId());
				key.setProductId(tProduct.getProductId());
				TPipleProduct tPipleProduct = this.tPipleProductDao.selectByPrimaryKey(key);
				String reqUrl = "http://xlsdk.xl-game.cn/sdkServer/openapi/unionpay/createorder?phone=%s&amount=%s&transp=X01%s&op=cm&appid=4298&paychannel=cm_yqtt&iccid=898600c20915f9021376";
				List<String> args = new ArrayList<String>();
				args.add(mobile);
				args.add(tPipleProduct.getPipleProductCode());
				args.add(order.getOrderId());
				if(StringUtil.isNotEmptyString(imei)){
					reqUrl += "&imei=%s";
					args.add(imei);
				}
				if(StringUtil.isNotEmptyString(imsi)){
					reqUrl += "&imsi=%s";
					args.add(imsi);
				}
				reqUrl = String.format(reqUrl, args.toArray());
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
				try {
					String response = HttpClientUtils.doGet(reqUrl, "UTF-8");
					if(!StringUtil.isEmpty(response)){
						statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId, response.toString());
						JSONObject resultObj = JSONObject.fromObject(response);
						String errcode = resultObj.optString("errcode");
						if("0".equals(errcode)){
							String pipleOrderId = resultObj.getJSONObject("result").optString("orderid");
							order.setPipleOrderId(pipleOrderId);
							order.setResultCode(errcode);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							result.put("resultCode",errcode);
							result.put("resultMsg","获取验证码成功");
							result.put("orderId",order.getOrderId());
						}else{
							order.setResultCode(errcode);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							result.put("resultCode",errcode);
							result.put("resultMsg",resultObj.optString("err"));
						}
						this.SaveOrderUpdate(order);
					}else{
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","调用接口失败：接口异常");
						return result;
					}
					statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		JSONObject result = new JSONObject();
		String apiKey = requestBody.optString("apiKey");
		
		String orderId = requestBody.optString("orderId");
		String vCode = requestBody.optString("vCode");
		result.put("orderId",orderId);
		if(StringUtil.isEmpty(apiKey) || StringUtil.isEmpty(orderId) || StringUtil.isEmpty(vCode)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		}else{
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder == null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
			}else{
				try {
					statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
					TPiple tPiple = this.tPipleDao.selectByPrimaryKey(getPipleId());
					String reqUrl = "http://xlsdk.xl-game.cn/sdkServer/openapi/unionpay/commitverify?verifycode=%s&appid=4298&paychannel=cm_yqtt&orderid=%s";
					reqUrl = String.format(reqUrl, vCode,tOrder.getPipleOrderId());
					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl);
					String response = HttpClientUtils.doGet(reqUrl, "UTF-8");
					if(!StringUtil.isEmpty(response)){
						statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), response);
						JSONObject resultObj = JSONObject.fromObject(response);
						String resultCode = resultObj.optString("errcode");
						
						if(!"0".equals(resultCode)){
							tOrder.setResultCode(resultCode);
							tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							result.put("resultCode",resultCode);
							result.put("resultMsg",resultObj.optString("err"));
						}else{
							tOrder.setResultCode(resultCode);
							tOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
							result.put("resultCode",resultCode);
							result.put("resultMsg","提交验证码成功");
						}
						this.SaveOrderUpdate(tOrder);
					}else{
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","调用接口失败：接口异常");
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","接口内部异常");
				}
				statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), JSONObject.fromObject(result).toString());
			}
		}
		return result;
	}
	
	@Override
	public String getPipleKey() {
		return "PM1029";
	}
	@Override
	public String processGetMessage(String mobile,String requestBody) throws Exception {
		String resultMsg = "";
		String args[] = requestBody.split("\\$");
		String apiKey = args[0];
		String productCode = args[1];
		String extData = args[2];
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
		String vcode = args[1];
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("orderId",tOrder.getOrderId());
		request.put("vCode",vcode);
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
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		String linkId = requestBody.optString("linkid");
		String orderId = linkId.substring(3);
		String status = requestBody.optString("status");
		String mobile = requestBody.optString("mobile");
		String extData = requestBody.optString("msg");
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
		if(order!=null ){ // 同步数据正确
			if(order.getOrderStatus() == GlobalConst.OrderStatus.SUCCESS){
				return "ok";
			}
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			String productId = order.getProductId();
			order.setAmount(new BigDecimal(productId));
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			//扣量
			boolean isSend = false;
			if("DELIVRD".equals(status)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				if(StringUtil.isNotEmptyString(mobile))
					order.setMobile(mobile);
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
			if(isSend){ // 不扣量 通知渠道
				notifyChannel(cp.getNotifyUrl(),order, productCode,"ok");
			}
		}
		return "ok";
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
}
