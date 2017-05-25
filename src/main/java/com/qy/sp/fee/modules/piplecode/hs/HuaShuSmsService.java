package com.qy.sp.fee.modules.piplecode.hs;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class HuaShuSmsService extends ChannelService{
	@Override
	public String getPipleId() {
		return "14665741313008076336099";
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		String groupId = KeyHelper.createID();
		statistics(STEP_PAY_BASE_TO_PLATFORM, groupId, requestBody.toString());
		String linkid = requestBody.optString("linkid");
		String status = requestBody.optString("status");
		String mobile = requestBody.optString("mobile");
		String param  = requestBody.optString("param");
		String msg  = requestBody.optString("msg");
		TOrder nOrder = tOrderDao.selectByPipleOrderId(linkid);
		if(nOrder==null){ // 未同步过
			TOrder order = new TOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setGroupId(groupId);
			order.setMobile(mobile);
			order.setPipleId(getPipleId());
			order.setPipleOrderId(linkid);
			order.setProvinceId(getProvinceIdByMobile(mobile, false));
			String apiKey = param.substring(0,4);
			String productCode = param.substring(4, 10);
			if(param.length()>10){
				String extData = param.substring(10);
				order.setExtData(extData);
			}
			TChannel channel = tChannelDao.selectByApiKey(apiKey);
			order.setChannelId(channel.getChannelId());
			TProduct product = tProductDao.selectByCode(productCode);
			order.setProductId(product.getProductId());
			order.setAmount(new BigDecimal(product.getProductId()));
			TChannelPipleKey key = new TChannelPipleKey();
			key.setChannelId(channel.getChannelId());
			key.setPipleId(getPipleId());
			TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(key);
			boolean isSend = false;
			if("0000".equals(status)){ // 同步数据正确
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				order.setMobile(mobile);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
				if(isSend){ // 不扣量 通知渠道
					notifyChannel(cp.getNotifyUrl(),order, product.getProductCode(),"ok");
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderInsert(order);
			return "ok";
		}else{
			return "order Synchronized";
		}
		
	}
}
