package com.qy.sp.fee.modules.piplecode.others;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

@Service
public class LengShiIvrService extends ChannelService {

	@Override
	public String getPipleId() {
		return "14713429518867685419527";
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		String linkId = requestBody.optString("linkid");
		String fees = requestBody.optString("fees");
		String mobile = requestBody.optString("mobile");
		String longnumber = requestBody.optString("longnumber");
		String stime = requestBody.optString("stime");
		String etime = requestBody.optString("etime");
		TOrder order = tOrderDao.selectByPipleOrderId(linkId);
		if(order!=null ){ // 同步数据正确
			if(order.getOrderStatus() == GlobalConst.OrderStatus.SUCCESS){
				return "ok";
			}
		}
		order = new TOrder();
		order.setGroupId(KeyHelper.createKey());
		order.setOrderId(KeyHelper.createKey());
		statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
		List<TChannelPiple> cps =  tChannelPipleDao.getListByPipleId(getPipleId());
		TChannelPiple cp = null ;
		if(cps.size() > 0 )
			cp =  cps.get(0);
		if(cp == null){
			return "channel error";
		}
		order.setProductId(fees);
		order.setAmount(new BigDecimal(fees));
		TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
		String productCode = tProduct.getProductCode();
		//扣量
		boolean isSend = false;
		order.setChannelId(cp.getChannelId());
		order.setPipleId(getPipleId());
		order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
		order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
		order.setCreateTime(DateTimeUtils.getCurrentTime());
		order.setModTime(DateTimeUtils.getCurrentTime());
		order.setPipleOrderId(linkId);
		order.setCompleteTime(DateTimeUtils.getCurrentTime());
		
		if(StringUtil.isNotEmptyString(mobile)){
			order.setMobile(mobile);
			order.setProvinceId(mobileSegmentService.getProvinceIdByMobile(mobile));
		}
		doWhenPaySuccess(order);
		boolean bDeducted  = order.deduct(cp.getVolt());
		if(!bDeducted){ 
			isSend =true;
		}
		SaveOrderInsert(order);
		if(isSend){ // 不扣量 通知渠道
			notifyChannel(cp.getNotifyUrl(),order, productCode,"ok",fees,longnumber,stime,etime);
		}
		return "ok";
	}
	public void notifyChannel(String url,TOrder order,String spnumber,String status,String fees,String longnumber,String stime,String etime){
		try {
			String rst = "";
				String param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&status="+status+"&amount="+order.getAmount().doubleValue();
				
				if(!StringUtil.isEmpty(order.getPipleId())){
					param += "&pipleId="+order.getPipleId();
				}
				if(!StringUtil.isEmpty(spnumber)){
					param += "&productCode="+spnumber;
				}
				if(!StringUtil.isEmpty(order.getImsi())){
					param += "&imsi="+order.getImsi();
				}
				if(!StringUtil.isEmpty(order.getExtData())){
					param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
				}
				TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
				if(tPiple != null && StringUtil.isNotEmptyString(tPiple.getPipleNumber())){
					param += "&pipleKey="+tPiple.getPipleNumber();
				}
				if(!StringUtil.isEmpty(fees)){
					param += "&fees="+fees;
				}
				if(!StringUtil.isEmpty(longnumber)){
					param += "&longnumber="+longnumber;
				}
				if(!StringUtil.isEmpty(stime)){
					param += "&stime="+stime;
				}
				if(!StringUtil.isEmpty(etime)){
					param += "&etime="+etime;
				}
				
				String ackUrl = url+"?"+param;
				logger.info("sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
				statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
				rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
				logger.info("getFromChannel= " + rst + " ,orderId="+order.getOrderId());
				statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return false;
	}
}
