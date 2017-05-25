package com.qy.sp.fee.modules.piplecode.ygxe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
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
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import com.qy.sp.fee.modules.piplecode.ct.CTSWService;

@Service
public class YGXEService extends ChannelService {
	private  Logger log = Logger.getLogger(YGXEService.class);	
	
	public String getPipleId() {
		return "14797000529389710091718";
	}
	public String getApiKey(){
		return "1003";
	}
	private static final String IMSI = "460018548284145";
	private static final String PROUCECODE = "P00800";
	
	

	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("YGXEService 支付同步数据:"+requestBody.toString());
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String linkid = requestBody.optString("linkid");
		String mobile = requestBody.optString("mobile");
		String spnumber = requestBody.optString("spnumber");
		String momsg = requestBody.optString("momsg");
		String exdata = requestBody.optString("exdata");
		BaseChannelRequest req = new BaseChannelRequest();
		req.setApiKey(getApiKey());
		req.setImsi(IMSI);
		req.setProductCode(PROUCECODE);
		req.setMobile(mobile);
		// 调用合法性校验
		BaseResult bResult = this.accessVerify(req,getPipleId());
		if(bResult!=null){// 返回不为空则校验不通过
			return error;
		}
		TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
		TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
		TPipleProductKey ppkey = new TPipleProductKey();
		ppkey.setPipleId(getPipleId());
		ppkey.setProductId(tProduct.getProductId());
		TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
		TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
		YGXEOrder order = new YGXEOrder();
		String groupId = KeyHelper.createKey();
		order.setOrderId(KeyHelper.createKey());
		order.setGroupId(groupId);
		order.setPipleId(getPipleId());
		order.setSpnumber(spnumber);
		order.setPipleOrderId(linkid);
		order.setMomsg(momsg);
		order.setChannelId(tChannel.getChannelId());
		order.setMobile(mobile);
		order.setImsi(IMSI);
		order.setProductId(tProduct.getProductId());
		order.setOrderStatus(GlobalConst.OrderStatus.INIT);
		order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
		order.setCreateTime(DateTimeUtils.getCurrentTime());
		order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
		order.setProvinceId(req.getProvinceId());
		SaveOrderInsert(order);
		order.setResultCode(GlobalConst.Result.SUCCESS);
		order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
		order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
		order.setModTime(DateTimeUtils.getCurrentTime());
		order.setCompleteTime(DateTimeUtils.getCurrentTime());
		boolean bDeducted = false; // 扣量标识
		TChannelPipleKey pkey = new TChannelPipleKey();
		pkey.setChannelId(order.getChannelId());
		pkey.setPipleId(order.getPipleId());
		doWhenPaySuccess(order);
		TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
		if(cp == null){
			return "channel error";
		}
		bDeducted  = order.deduct(cp.getVolt());  
		if(!bDeducted){ // 不扣量 通知渠道
			notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
			
		}
		SaveOrderUpdate(order);
		return "ok";	
	}
	 
	 private class YGXEOrder extends TOrder {
		private String spnumber;
		private String momsg;
		private String linkid;
		public String getSpnumber() {
			return spnumber;
		}
		public void setSpnumber(String spnumber) {
			this.spnumber = spnumber;
		}
		public String getMomsg() {
			return momsg;
		}
		public void setMomsg(String momsg) {
			this.momsg = momsg;
		}
		public String getLinkid() {
			return linkid;
		}
		public void setLinkid(String linkid) {
			this.linkid = linkid;
		}
		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.spnumber != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("spnumber");
				oExt.setExtValue(this.spnumber);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.momsg != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("momsg");
				oExt.setExtValue(this.momsg);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.linkid != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("company");
				oExt.setExtValue(this.linkid);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	 }
}
