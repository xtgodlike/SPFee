package com.qy.sp.fee.modules.piplecode.lefu;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class Lf252Service extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;

	@Override
	public String getPipleId() {
		return "14592369931766138014889";
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		if(requestBody == null)
			return "error";
		String spnumber = requestBody.optString("spnumber");
		String mobile = requestBody.optString("mobile");
		String linkid = requestBody.optString("linkid");
		String msg = requestBody.optString("msg");
		String status = requestBody.optString("status");
		TOrder order = new TOrder();
		order.setOrderId(KeyHelper.createKey());
		order.setMobile(mobile);
		order.setPipleId(getPipleId());
		order.setPipleOrderId(linkid);
		List<TChannelPiple> cPiples =  tChannelPipleDao.getListByPipleId(getPipleId());
		TChannelPiple cp;
		if(cPiples.size() > 0){
			cp = cPiples.get(0);  // 只能配置一家渠道
		}else{
			return "channel error";
		}
		order.setChannelId(cp.getChannelId());
		int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
		order.setProvinceId(provinceId);
		String productId = "10";
		order.setAmount(new BigDecimal(productId));
		order.setProductId(productId);
		TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
		String productCode = tProduct.getProductCode();
		order.setCreateTime(DateTimeUtils.getCurrentTime());
		//扣量
		boolean isSend = false;
		if("DELIVRD".equals(status)){
			order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
			order.setSubStatus(PAY_SUCCESS);
			order.setModTime(DateTimeUtils.getCurrentTime());
			order.setCompleteTime(DateTimeUtils.getCurrentTime());
			order.setResultCode(status);
			boolean bDeducted  = order.deduct(cp.getVolt());
			if(!bDeducted){ 
				isSend =true;
			}
		}else {
			order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
			order.setSubStatus(PAY_FAIL);
			order.setModTime(DateTimeUtils.getCurrentTime());
			order.setResultCode(status);
		}
		SaveOrderInsert(order);
		if(isSend){ // 不扣量 通知渠道
			notifyChannel(cp.getNotifyUrl(), order.getMobile(),order.getOrderId(), productCode, "ok",msg);
		}
		return "ok";
	}
}
