package com.qy.sp.fee.modules.piplecode.ky;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class KYWOService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String P_FAIL = "1";	  // 订购失败
	public final static String P_SUCCESS = "2";	  // 订购成功
	public final static String P_TD = "3";	  	  // 退订成功
	private  Logger log = Logger.getLogger(KYWOService.class);
	@Override
	public String getPipleId() {
		return "15040598002744398380007";
	}

	@Override
	public String getPipleKey() {
		return "PM1083";
	}

	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("KYWOService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String order_id = requestBody.optString("order_id");
		String app_id = requestBody.optString("app_id");
		String channel_id = requestBody.optString("channel_id");
		String fee_id = requestBody.optString("fee_id");		// 计费点ID
		String price = requestBody.optString("price");
		String phone = requestBody.optString("phone");
		String provider = requestBody.optString("provider");
		String result_status = requestBody.optString("result_status");
		String custom_param = requestBody.optString("custom_param");	// 前4位是apiKey
		String timestamp = requestBody.optString("timestamp");
		String signature = requestBody.optString("signature");
		TOrder order = tOrderDao.selectByPipleOrderId(order_id);
		if(order==null ){ // 同步数据正确
			try {
				KYWOOrder qyOrder = new KYWOOrder();
				qyOrder.setGroupId(KeyHelper.createKey());
				statistics(STEP_PAY_BASE_TO_PLATFORM, qyOrder.getGroupId(), requestBody.toString());
				String apiKey = custom_param.substring(0,4);
				TChannel channel = tChannelDao.selectByApiKey(apiKey);
				TChannelPipleKey cpk = new TChannelPipleKey();
				cpk.setChannelId(channel.getChannelId());
				cpk.setPipleId(this.getPipleId());
				TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(cpk);
				TPipleProduct ppk = new TPipleProduct();
				ppk.setPipleId(this.getPipleId());
				ppk.setPipleProductCode(fee_id);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppk);
				TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());

				//扣量
				boolean bDeducted = false;
				if(P_SUCCESS.equals(result_status)){
					qyOrder.setOrderId(KeyHelper.createKey());
					qyOrder.setPipleOrderId(order_id);
					qyOrder.setPipleId(this.getPipleId());
					qyOrder.setChannelId(cp.getChannelId());
					qyOrder.setProductId(pipleProduct.getProductId());
					qyOrder.setAmount(new BigDecimal(product.getPrice() / 100));
					qyOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
					qyOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
					qyOrder.setCreateTime(DateTimeUtils.getCurrentTime());
					qyOrder.setModTime(DateTimeUtils.getCurrentTime());
					qyOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
					if(StringUtil.isNotEmptyString(phone) && !"null".equals(phone)){
						qyOrder.setMobile(phone);
						int  provinceId = this.getProvinceIdByMobile(phone, false); // 获取省份ID
						qyOrder.setProvinceId(provinceId);
					}
					qyOrder.setResultCode(result_status);
					bDeducted  = qyOrder.deduct(cp.getVolt());
					if(!bDeducted){ // 不扣量 通知渠道
//						notifyChannelAPIForKey(cp.getNotifyUrl(),qyOrder,"ok");
						statistics(STEP_PAY_PLATFORM_TO_CHANNEL, qyOrder.getGroupId(),cp.getNotifyUrl()+"?"+requestBody.toString());
						String rst = HttpClientUtils.doPost(cp.getNotifyUrl(),requestBody.toString(),HttpClientUtils.UTF8);
						statistics(STEP_PAY_CHANNEL_TO_PLATFORM, qyOrder.getGroupId(),rst);
					}
				}else {
					qyOrder.setResultCode(result_status);
					qyOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					qyOrder.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
					qyOrder.setModTime(DateTimeUtils.getCurrentTime());
				}
				SaveOrderInsert(qyOrder);
				return "SUCCESS";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("KYWOService 同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else if(order!=null && P_TD.equals(result_status)) { // 退订
			try {
				statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
				order.setResultCode(P_TD);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR_TG);
				order.setModTime(DateTimeUtils.getCurrentTime());
				// 如果未扣量则同步退订数据给渠道
				if(order.getDecStatus().equals(GlobalConst.DEC_STATUS.UNDEDUCTED)){
                    TChannelPipleKey cpk = new TChannelPipleKey();
                    cpk.setChannelId(order.getChannelId());
                    cpk.setPipleId(getPipleId());
                    TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(cpk);
                    statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),cp.getNotifyUrl()+"?"+requestBody.toString());
                    String rst = HttpClientUtils.doPost(cp.getNotifyUrl(),requestBody.toString(),HttpClientUtils.UTF8);
                    statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
                }
                SaveOrderUpdate(order);
				return "SUCCESS";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("KYWOService 同步退订处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order error";
		}

	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}

	public class KYWOOrder extends TOrder{

	}
}
