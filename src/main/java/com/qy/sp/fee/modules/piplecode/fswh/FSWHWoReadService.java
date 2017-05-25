package com.qy.sp.fee.modules.piplecode.fswh;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class FSWHWoReadService extends ChannelService{
	
	private final static String PAY_SUCCESS = "DELIVRD";   // 扣费成功
	
	@Override
	public String getPipleId() {
		return "14745305180405229830811";
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		String groupId = KeyHelper.createID();
		statistics(STEP_PAY_BASE_TO_PLATFORM, groupId, requestBody.toString());
		String spnumber = requestBody.optString("spnumber");
		String mobile = requestBody.optString("mobile");
		String linkid = requestBody.optString("linkid");
		String msg = requestBody.optString("msg"); 
		String status = requestBody.optString("status");
		String fee  = requestBody.optString("fee");  // 单位分 (100~2000分)
		TOrder nOrder = tOrderDao.selectByPipleOrderId(linkid);
		if(nOrder==null){ // 未同步过
			List<TChannelPiple> cPiples = tChannelPipleDao.getListByPipleId(getPipleId());
			TChannelPiple cPiple = cPiples.get(0);
			if(cPiple==null){
				return "channel error";
			}
			TChannel channel = tChannelDao.selectByPrimaryKey(cPiple.getChannelId());
			TProduct product = tProductDao.selectByPrice(Integer.parseInt(fee));
			
			FSWHWoReadOrder order = new FSWHWoReadOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setGroupId(groupId);
			order.setMobile(mobile);
			order.setPipleId(getPipleId());
			order.setChannelId(channel.getChannelId());
			order.setProductId(product.getProductId());
			order.setPipleOrderId(linkid);
			if(!StringUtil.isEmpty(mobile)){
				order.setProvinceId(getProvinceIdByMobile(mobile, false));
			}
			order.setAmount(new BigDecimal(product.getProductId()));
//			order.setExtData(extData);
			order.setSpnumber(spnumber);
			order.setMsg(msg);
			
			TChannelPipleKey key = new TChannelPipleKey();
			key.setChannelId(channel.getChannelId());
			key.setPipleId(getPipleId());
			TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(key);
			boolean isSend = false;
			if(PAY_SUCCESS.equals(status)){ // 扣费成功
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
				if(isSend){ // 不扣量 通知渠道
					notifyChannelSMS(cp.getNotifyUrl(), order, spnumber, PAY_SUCCESS);
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
	public class FSWHWoReadOrder extends TOrder{
		private String msg;  // 指令
		private String spnumber;  // 端口号
		
		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getSpnumber() {
			return spnumber;
		}

		public void setSpnumber(String spnumber) {
			this.spnumber = spnumber;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.msg != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("msg");
				oExt.setExtValue(this.msg);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.spnumber != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("spnumber");
				oExt.setExtValue(this.spnumber);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}

}
