package com.qy.sp.fee.modules.piplecode.qianya;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class QYVACService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "ok";	  // 同步计费成功
	@Override
	public String getPipleId() {
		return "14962881902849800414208";
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		
		String linkId = requestBody.optString("orderId");  // 通道订单号
		String pipleId = requestBody.optString("pipleId");
		String apiKey = requestBody.optString("apiKey");
		String piplePCode = requestBody.optString("productCode");
		String status = requestBody.optString("status");
		String mobile = requestBody.optString("mobile");
		String imsi = requestBody.optString("imsi");
		String amount = requestBody.optString("amount");
		String extData = requestBody.optString("extData"); // 前4位为我方apiKey 从第5位开始为透传信息
		if(StringUtil.isEmptyString(linkId) || StringUtil.isEmptyString(pipleId)  || StringUtil.isEmptyString(apiKey)
				|| StringUtil.isEmptyString(piplePCode) || StringUtil.isEmptyString(status)){
						return "param error";
		}
		TOrder torder = tOrderDao.selectByPipleOrderId(linkId);
		if(torder==null){ // 数据未同步
			String myApiKey = extData.substring(0,4);
			String myExtData = extData.substring(4,extData.length());
			TChannel channel = tChannelDao.selectByApiKey(myApiKey);
			TChannelPipleKey cpk = new TChannelPipleKey();
			cpk.setChannelId(channel.getChannelId());
			cpk.setPipleId(this.getPipleId());
			TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(cpk);
			TPipleProduct ppk = new TPipleProduct();
			ppk.setPipleId(this.getPipleId());
			ppk.setPipleProductCode(piplePCode);
			TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppk);
			TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
			QYVACTOrder qyOrder = new QYVACTOrder();
			//扣量
			boolean bDeducted = false;
			if(P_SUCCESS.equals(status)){
				qyOrder.setOrderId( KeyHelper.createKey());
				qyOrder.setPipleId(this.getPipleId());
				qyOrder.setChannelId(cp.getChannelId());
				qyOrder.setProductId(pipleProduct.getProductId());
				qyOrder.setPipleOrderId(linkId);
				qyOrder.setAmount(new BigDecimal(product.getPrice() / 100));
				qyOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				qyOrder.setSubStatus(QYVACService.PAY_SUCCESS);
				qyOrder.setCreateTime(DateTimeUtils.getCurrentTime());
				qyOrder.setModTime(DateTimeUtils.getCurrentTime());
				qyOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
				qyOrder.setImsi(imsi);
				qyOrder.setExtData(myExtData);
				if(mobile!=null && !"null".equals(mobile) && !"".equals(mobile)){
					qyOrder.setMobile(mobile);
					int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
					qyOrder.setProvinceId(provinceId);
				}
				bDeducted  = qyOrder.deduct(cp.getVolt());
				if(!bDeducted){ // 不扣量 通知渠道
					notifyChannelSMS(cp.getNotifyUrl(),qyOrder,"1065556131","ok");
				}
			}else {
				qyOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				qyOrder.setSubStatus(QYVACService.PAY_FAIL);
				qyOrder.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderInsert(qyOrder);
			return "ok";
		}else{
			return "order Synchronized";
		}
	}


	public class QYVACTOrder extends TOrder{

	}
}
