package com.qy.sp.fee.modules.piplecode.hs;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class MoService extends ChannelService{
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	public final static String PAY_SUCCESS = "0000";  // 扣费成功
	public final static String PORT = "1069009216288";
	@Override
	public String getPipleId() {
		return "14804877010144720325631";
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("MoService 支付同步数据:"+requestBody);
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
		String groupId = KeyHelper.createKey();
		if(StringUtil.isEmpty(linkid))
			return error;
		TOrder order = tOrderDao.selectByPipleOrderId(linkid);
		boolean isNew =false;
		if(order == null){
			order = new TOrder();
			isNew = true;
		}else if(order.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
			return "ok";
		}
		statistics(STEP_PAY_BASE_TO_PLATFORM, groupId, requestBody.toString());
		TChannelPiple cp = null;
		List<TChannelPiple> cps =  tChannelPipleDao.getListByPipleId(getPipleId());
		if(cps.size() <=0){
			return "channel error";
		}
		cp = cps.get(0);
		boolean bDeducted = false; // 扣量标识
		order.setOrderId(KeyHelper.createKey());
		order.setPipleOrderId(linkid);
		order.setResultCode(status);
		order.setChannelId(cp.getChannelId());
		order.setPipleId(getPipleId());
		order.setGroupId(groupId);
		order.setMobile(mobile);
		order.setProductId("1");
		order.setAmount(new BigDecimal("1"));
		order.setProvinceId(mobileSegmentService.getProvinceIdByMobile(mobile));
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
				notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
			}
		}else{
			order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
			order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
			order.setModTime(DateTimeUtils.getCurrentTime());
		}
		if(isNew){
			SaveOrderInsert(order);
		}else{
			SaveOrderUpdate(order);
		}
		return "ok";
	}
	
}
