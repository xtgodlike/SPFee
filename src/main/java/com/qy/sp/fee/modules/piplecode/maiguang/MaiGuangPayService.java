package com.qy.sp.fee.modules.piplecode.maiguang;

import java.math.BigDecimal;
import java.util.Date;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
@Service
public class MaiGuangPayService extends ChannelService{
	public static final String PAY_SUCCESS = "0";
	@Override
	public String getPipleId() {
		return "14775599243830695760815";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		JSONObject result = new JSONObject();
		logger.info("MGService 支付同步数据:"+requestBody);
		String productCode = requestBody.optString("productCode");
		String appId = requestBody.optString("appId");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String orderStatus = requestBody.optString("status");
		String orderId = requestBody.optString("orderId");
		String msg = requestBody.optString("statusMsg");
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
			TLocation location = null;
			location = mobileSegmentService.getLocationByMobile(req.getMobile());
			if(location != null){
				req.setProvinceId(location.getProvinceId());
				req.setHostId(location.getHostId());
			}
			String groupId = KeyHelper.createKey();
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			TOrder order = new TOrder();
			order.setAppId(appId);
			order.setChannelId(apiKey);
			order.setOrderId(orderId);
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			Date CurrentTime=DateTimeUtils.getCurrentTime();
			order.setCreateTime(CurrentTime);
			order.setModTime(CurrentTime);
			order.setCompleteTime(CurrentTime);
			order.setResultCode(orderStatus);
			try{
				result.put("orderId",order.getOrderId());
				if(PAY_SUCCESS.equals(orderStatus)){
					order.setResultCode(GlobalConst.Result.SUCCESS);
					order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
					order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
					order.setCreateTime(CurrentTime);
					order.setModTime(CurrentTime);
					order.setCompleteTime(CurrentTime);
					result.put("resultCode", GlobalConst.Result.SUCCESS);
					result.put("resultMsg","请求成功。");
				}else{
					order.setResultCode(orderStatus);
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
					order.setCreateTime(CurrentTime);
					order.setModTime(CurrentTime);
					result.put("resultCode", orderStatus);
					result.put("resultMsg","请求失败。");
				}
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,order.toString());
				SaveOrderInsert(order);	
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}catch(Exception e){
				e.printStackTrace();
				order.setResultCode(GlobalConst.Result.ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultCode","服务器异常");
				return result;
			}
		}

	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		return "";
	}
}
