package com.qy.sp.fee.modules.piplecode.mrb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

@Service
public class MRBService extends ChannelService{
	private  Logger log = Logger.getLogger(MRBService.class);	
	private static final String SUCCESS_STATUS = "DELIVRD";
	private static final String SPNUMBER_TWO = "2222";
	private static final String SPNUMBER_FOUR = "2224";
	private static final String CODE_TWO = "P00200";
	private static final String CODE_FOUR = "P00400";
	private static final String IMSI = "460018548284145";
	@Override
	public String getPipleId() {
		return "14797820864560203766352";
	}
	public String getApiKey(){
		return "1003";
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("MRBService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}else{
			String linkid = requestBody.optString("linkid");
			String mobile = requestBody.optString("mobile");
			String spnumber = requestBody.optString("spnumber");
			String msg = requestBody.optString("msg");
			String status = requestBody.optString("status");
			String groupId = KeyHelper.createKey();
			String productCode = null;
			if(SPNUMBER_TWO.equals(spnumber)){
				productCode = CODE_TWO;
			}else if(SPNUMBER_FOUR.equals(spnumber)){
				productCode = CODE_FOUR;
			}
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(getApiKey());
			req.setImsi(IMSI);
			req.setProductCode(productCode);
			req.setMobile(mobile);			
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(getPipleId());
			ppkey.setProductId(tProduct.getProductId());			
			if(StringUtil.isEmptyString(productCode)){
				return error;
			}	
			MRBOrder order = new MRBOrder();
			order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(getPipleId());
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(IMSI);
			order.setProductId(tProduct.getProductId());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			Date CurrentTime=DateTimeUtils.getCurrentTime();
			order.setCreateTime(CurrentTime);
			order.setModTime(CurrentTime);
			order.setCompleteTime(CurrentTime);
			order.setResultCode(status);
			order.setPipleId(linkid);
			order.setMsg(msg);
			SaveOrderInsert(order);
			if(SUCCESS_STATUS.equals(status)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				TChannelPipleKey pkey = new TChannelPipleKey();
				pkey.setChannelId(order.getChannelId());
				pkey.setPipleId(order.getPipleId());
				doWhenPaySuccess(order);
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setCreateTime(CurrentTime);
				order.setModTime(CurrentTime);
				
			}
			SaveOrderUpdate(order);
		}
		return "ok";
	}
	
	private class MRBOrder extends TOrder{
		private String msg;

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
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
			return tOrderExts;
		}
	}
}

