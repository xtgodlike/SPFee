package com.qy.sp.fee.modules.piplecode.hs;

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
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class HSdystService extends ChannelService{
	
	private final static String REQ_SUCCESS = "0000";   // 扣费成功
	
	@Override
	public String getPipleId() {
		return "14709084979760324483320";
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
		String mobile = requestBody.optString("mobile");
		String port = requestBody.optString("port");
		String msg = requestBody.optString("msg");  // 完整上量指令 如：yzw#csdy2#7002#y(apiKey)(extData)
		String status = requestBody.optString("status");
		String param  = requestBody.optString("param");  // y(apiKey)(extData)
		String ftime  = requestBody.optString("ftime");
		TOrder nOrder = tOrderDao.selectByPipleOrderId(linkid);
		if(nOrder==null){ // 未同步过
			String[] msgs = msg.split("#");
			String pipleProductCode = msgs[1];
			String apiKey =param.substring(1, 5);
			String extData =param.substring(5, param.length());
			TChannel channel = tChannelDao.selectByApiKey(apiKey);
			TPipleProduct ppkey = new TPipleProduct();
			ppkey.setPipleId(getPipleId());
			ppkey.setPipleProductCode(pipleProductCode);
			TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
			
			HSdystOrder order = new HSdystOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setGroupId(groupId);
			order.setMobile(mobile);
			order.setPipleId(getPipleId());
			order.setChannelId(channel.getChannelId());
			order.setProductId(pipleProduct.getProductId());
			order.setPipleOrderId(linkid);
			if(!StringUtil.isEmpty(mobile)){
				order.setProvinceId(getProvinceIdByMobile(mobile, false));
			}
			order.setAmount(new BigDecimal(pipleProduct.getProductId()));
			order.setExtData(extData);
			order.setPort(port);
			order.setMsg(msg);
			
			TChannelPipleKey key = new TChannelPipleKey();
			key.setChannelId(channel.getChannelId());
			key.setPipleId(getPipleId());
			TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(key);
			boolean isSend = false;
			if(REQ_SUCCESS.equals(status)){ // 扣费成功
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
					TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
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
	public class HSdystOrder extends TOrder{
		private String msg;  // 指令
		private String port;  // 端口号
		
		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
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
			if(this.port != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("port");
				oExt.setExtValue(this.port);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
