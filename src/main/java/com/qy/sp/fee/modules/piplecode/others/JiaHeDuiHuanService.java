package com.qy.sp.fee.modules.piplecode.others;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

@Service
public class JiaHeDuiHuanService extends ChannelService {
	public String FROM_SDK_EXTDATA = "qsdk_";
	public static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy6jcojQYzzygt9Jb6Gk6ujPjm2/FTIEDMC7i+Rs1JGnj3+R8xIlSaYQSIt6gLGMK3gDP9Ok/8twvnRIt68APosoMGm/KMis/3Hjl+ux4hQ4EJ67SnqqpM8cTmIRJg+TnG1EhwPbulZ4NfpGfzhtcB93vGMfyU1tAAs3QauIkPG5L168n6RNia2cVQSrroT/7L4IsySuORBZFmc02DXWTdPB1zMKaR9QPYyqlMwNmZxng65r3Wpko7mgWpiOA0PkwB++hHdnEXcXlkuFCljLjeXo7qcUoFR2Sk39/c2vZTFgrmKPFbRO3AyabyyYfd/8RImjxQJZnMNN407Uxf5Y8mQIDAQAB";
	@Override
	public String getPipleId() {
		return "14737597021721395692283";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String extData = requestBody.optString("extData");
		String productCode = requestBody.optString("productCode");
		String fromType = requestBody.optString("fromType");
		if(StringUtil.isEmpty(fromType)){
			if(StringUtil.isNotEmptyString(extData)  && extData.startsWith(FROM_SDK_EXTDATA)){
				fromType = GlobalConst.FromType.FROM_TYPE_SDK;
			}
			else{
				fromType = GlobalConst.FromType.FROM_TYPE_API;
			}
		}
		JSONObject result = new JSONObject();
		if(StringUtil.isEmptyString(apiKey) || StringUtil.isEmpty(mobile) || StringUtil.isEmpty(productCode)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		}
		BaseChannelRequest req = new BaseChannelRequest();
		req.setApiKey(apiKey);
		
		req.setProductCode(productCode);
		req.setPipleId(getPipleId());
		req.setMobile(mobile);
		BaseResult bResult = this.accessVerify(req);
		if (bResult != null) {// 返回不为空则校验不通过
			result.put("resultCode",bResult.getResultCode());
			result.put("resultMsg",bResult.getResultMsg());
			return result;
		}
		String groupId = KeyHelper.createKey();
		statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
		TChannel tChannel = this.tChannelDao.selectByApiKey(apiKey);
		TProduct tProduct = this.tProductDao.selectByCode(productCode);
		TChannelPipleKey pkey = new TChannelPipleKey();
		pkey.setChannelId(tChannel.getChannelId());
		pkey.setPipleId(getPipleId());
		TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
	    // # 为了防止第三方通过调用该接口刷兑换码,兑换码在发送前应先使用公钥对其进行加密
//		code = Base64.encodeBytes(RSAUtil.encryptByPublicKey(code.getBytes(), publicKey));	
		TOrder order = new TOrder();
		order.setChannelId(tChannel.getChannelId());
		order.setCreateTime(DateTimeUtils.getCurrentTime());
		order.setModTime(DateTimeUtils.getCurrentTime());
		order.setOrderId(KeyHelper.createKey());
		order.setOrderStatus(GlobalConst.OrderStatus.INIT);
		order.setPipleId(getPipleId());
		order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
		order.setMobile(mobile);
		order.setProvinceId(req.getProvinceId());
		order.setGroupId(groupId);
		order.setProductId(tProduct.getProductId());
		order.setFromType(NumberUtil.getInteger(fromType));
		order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
		if("P01000".equals(productCode)){
			order.setAmount(new BigDecimal(8.98));
		}else if("P03000".equals(productCode)){
			order.setAmount(new BigDecimal(26.93));
		}
		order.setExtData(extData);
		result.put("orderId",order.getOrderId());
		result.put("resultCode",GlobalConst.Result.SUCCESS);
		result.put("resultMsg","提交成功");
		doWhenPaySuccess(order);
		boolean isSend = false;
		boolean bDeducted  = order.deduct(cp.getVolt());
		if(!bDeducted){ 
			isSend =true;
		}
		if(isSend){
			String syncRst = "";
			if(GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)){
				syncRst = notifyChannelSMS(cp.getNotifyUrl(), order, "1069009216288", "ok");
			}else if(GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)){
				syncRst = notifyChannelSDK(cp.getNotifyUrl(),order);
			}else{
				syncRst = notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
			}
			if("ok".equals(syncRst)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
			}else{
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
			}
		}
		this.SaveOrderInsert(order);
		statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
		return result;
		
	}
	@Override
	public String getPipleKey() {
		return "PA1036";
	}
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
		if(StringUtil.isNotEmptyString(extData) && extData.startsWith(FROM_SDK_EXTDATA)){
			request.put("fromType", GlobalConst.FromType.FROM_TYPE_SDK);
		}else{
			request.put("fromType", GlobalConst.FromType.FROM_TYPE_SMS);
		}
		JSONObject result = processGetSMS(request);
		logger.info("processGetMessage:"+mobile+","+requestBody+",result="+result.toString());
		if(result != null){
			if(StringUtil.isNotEmptyString(result.optString("orderId"))){
				TOrder tOrder = tOrderDao.selectByPrimaryKey(result.optString("orderId"));
				if(tOrder != null){
					statistics(STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"1$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
				}
			}
		}
		return resultMsg;
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return false;
	}
}
