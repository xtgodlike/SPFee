package com.qy.sp.fee.modules.piplecode.sthd;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class STHDLTService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "DELIVRD";	  // 同步计费成功
	@Override
	public String getPipleId() {
		return "15002890752889230410486";
	}

	@Override
	public String getPipleKey() {
		return "PW1071";
	}

	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		
		String telno = requestBody.optString("telno");  // 用户手机号码
		String mo = requestBody.optString("mo");		// 用户上行指令
		String dest = requestBody.optString("dest");	// 端口号 长号码 106xxxxx
		String stat = requestBody.optString("stat");	// 移动返回的状态报告,如果为-99 请参考errorcode的值
		String errorcode = requestBody.optString("errorcode");	// 错误代码
		String linkid = requestBody.optString("linkid");	// 通道订单号
		String motime = requestBody.optString("motime");	// 用户发送短信的时间 格式  YYYYMMDDHHNNSS
		if(StringUtil.isEmptyString(linkid) || StringUtil.isEmptyString(telno)  || StringUtil.isEmptyString(stat)
				|| StringUtil.isEmptyString(mo) || StringUtil.isEmptyString(dest)){
						return "param error";
		}
		TOrder torder = tOrderDao.selectByPipleOrderId(linkid);
		if(torder==null){ // 数据未同步
			try {
				STHDLTTOrder qyOrder = new STHDLTTOrder();
				qyOrder.setGroupId(KeyHelper.createKey());
				statistics(STEP_PAY_BASE_TO_PLATFORM, qyOrder.getGroupId(), requestBody.toString());
				// 该代码不能区分渠道 只能分配给一个通道
				List<TChannelPiple> channelPiples = tChannelPipleDao.getListByPipleId(getPipleId());
				TChannelPiple channelPiple = null;
				if(channelPiples==null){
					return "channel error";
				}else if(channelPiples.size()==0){
					return "channel error";
				}else {
					channelPiple = channelPiples.get(0);
				}
				// 固定计费点 1元
				TProduct product = tProductDao.selectByPrimaryKey("1");
				qyOrder.setResultCode(stat);
				//扣量
				boolean bDeducted = false;
				if(P_SUCCESS.equals(stat)){
                    qyOrder.setOrderId( KeyHelper.createKey());
                    qyOrder.setPipleId(this.getPipleId());
                    qyOrder.setChannelId(channelPiple.getChannelId());
                    qyOrder.setProductId(product.getProductId());
                    qyOrder.setPipleOrderId(linkid);
                    qyOrder.setAmount(new BigDecimal(product.getPrice() / 100));
                    qyOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    qyOrder.setSubStatus(STHDLTService.PAY_SUCCESS);
                    qyOrder.setCreateTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setModTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
//                    qyOrder.setImsi(imsi);
//                    qyOrder.setExtData(myExtData);
                    if(telno!=null && !"null".equals(telno) && !"".equals(telno)){
                        qyOrder.setMobile(telno);
                        int  provinceId = this.getProvinceIdByMobile(telno, false); // 获取省份ID
                        qyOrder.setProvinceId(provinceId);
                    }
                    bDeducted  = qyOrder.deduct(channelPiple.getVolt());
                    if(!bDeducted){ // 不扣量 通知渠道
                        notifyChannelSMS(channelPiple.getNotifyUrl(),qyOrder,"10660815","ok");
                    }
                }else {
					qyOrder.setResultCode(errorcode);
                    qyOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    qyOrder.setSubStatus(STHDLTService.PAY_FAIL);
                    qyOrder.setModTime(DateTimeUtils.getCurrentTime());
                }
				SaveOrderInsert(qyOrder);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("QYVACService同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order Synchronized";
		}
	}


	public class STHDLTTOrder extends TOrder{

	}
}
