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
public class QYVACTService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "0";	  // 同步计费成功
	@Override
	public String getPipleId() {
		return "14827354770130719977156";
	}

	@Override
	public String getPipleKey() {
		return "PA1064";
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("QYVACTService_processPaySuccess 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		
		String linkid = requestBody.optString("linkid");  // 通道订单号
		String mo_msg = requestBody.optString("mo_msg");  // 指令内容 如 DC1*100000037320100*
		String commandid = requestBody.optString("commandid"); // 端口号
		String mobile = requestBody.optString("mobile");
		String code = requestBody.optString("code");  // 0代表成功计费，其他值参见statemsg解释
		String statemsg = requestBody.optString("statemsg"); // 对于结果码的中文解释
		String transactionid = requestBody.optString("transactionid");
		String remark = requestBody.optString("remark");
		if(StringUtil.isEmptyString(linkid) || StringUtil.isEmptyString(mo_msg)  || StringUtil.isEmptyString(mobile)
				|| StringUtil.isEmptyString(commandid) || StringUtil.isEmptyString(code)){
						return "param error";
		}
		TOrder torder = tOrderDao.selectByPipleOrderId(linkid);
		if(torder==null){ // 数据未同步
			try {
				QYVACTOrder qyOrder = new QYVACTOrder();
				qyOrder.setGroupId(KeyHelper.createKey());
				statistics(STEP_PAY_BASE_TO_PLATFORM, qyOrder.getGroupId(), requestBody.toString());
				String[] momsgArr = mo_msg.split("\\*"); // 数据格式 DC10*325081910320100*302230381
				String pipleCode = momsgArr[0];
				String myApiKey = null;
				String myExtData = null;
				if(!StringUtil.isEmpty(momsgArr[2]) && momsgArr[2].length() >= 4){
					myApiKey = momsgArr[2].substring(4,8);
					myExtData = momsgArr[2].substring(8,momsgArr[2].length());
				}else{
					myApiKey = "1003"; // 默认公司自有渠道
				}
				TChannel channel = tChannelDao.selectByApiKey(myApiKey);
				if(channel==null){
					return "channel error";
				}
				TChannelPipleKey cpk = new TChannelPipleKey();
				cpk.setChannelId(channel.getChannelId());
				cpk.setPipleId(this.getPipleId());
				TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(cpk);
				TPipleProduct ppk = new TPipleProduct();
				ppk.setPipleId(this.getPipleId());
				ppk.setPipleProductCode(pipleCode);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppk);
				TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
				qyOrder.setResultCode(code);
				//扣量
				boolean bDeducted = false;
				if(P_SUCCESS.equals(code)){
                    qyOrder.setOrderId( KeyHelper.createKey());
                    qyOrder.setPipleId(this.getPipleId());
                    qyOrder.setChannelId(cp.getChannelId());
                    qyOrder.setProductId(pipleProduct.getProductId());
                    qyOrder.setPipleOrderId(linkid);
                    qyOrder.setAmount(new BigDecimal(product.getPrice() / 100));
                    qyOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    qyOrder.setSubStatus(QYVACTService.PAY_SUCCESS);
                    qyOrder.setCreateTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setModTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setExtData(myExtData);
                    if(mobile!=null && !"null".equals(mobile) && !"".equals(mobile)){
                        qyOrder.setMobile(mobile);
                        int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
                        qyOrder.setProvinceId(provinceId);
                    }
                    bDeducted  = qyOrder.deduct(cp.getVolt());
                    if(!bDeducted){ // 不扣量 通知渠道
                        notifyChannelSMS(cp.getNotifyUrl(),qyOrder,commandid,"ok");
                    }
                }else {
                    qyOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    qyOrder.setSubStatus(QYVACTService.PAY_FAIL);
                    qyOrder.setModTime(DateTimeUtils.getCurrentTime());
                }
				SaveOrderInsert(qyOrder);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("QYVACTService_processPaySuccess 同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order Synchronized";
		}
	}


	public class QYVACTOrder extends TOrder{

	}
}
