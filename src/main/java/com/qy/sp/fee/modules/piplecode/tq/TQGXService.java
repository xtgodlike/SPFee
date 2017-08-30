package com.qy.sp.fee.modules.piplecode.tq;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TQGXService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String OP_TYPE_DB = "1";	  // 点播
	public final static String OP_TYPE_BY = "2";	  // 包月
	public final static String OP_TYPE_TD = "3";	  // 退订
	public final static String P_SUCCESS = "1";	  // 同步订购成功
	private  Logger log = Logger.getLogger(TQGXService.class);
	@Override
	public String getPipleId() {
		return "14958555721785335696481";
	}

	@Override
	public String getPipleKey() {
		return "PM1082";
	}

	public JSONObject processPay(String params,String order_string,String orderResult) throws Exception {
		log.info("TQGXService params:"+params);
		log.info("TQGXService order_string:"+order_string);
		log.info("TQGXService orderResult:"+orderResult);
		TQGXOrder order = new TQGXOrder();
		String groupId = KeyHelper.createKey();
		if(StringUtil.isNotEmptyString(order_string)) {
			statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, groupId, order_string);
			JSONObject orderStringObj = JSONObject.fromObject(order_string);
			String sms = orderStringObj.getString("order_string");
			String mobile = orderStringObj.getString("user_id");
			String access_num = orderStringObj.getString("access_num");
//			String stream_no = orderStringObj.getString("stream_no");  // 缺失通道订单号

			String[] smsStr = sms.split("#");  // order_string 指令#后4位拓展字段定义为渠道apiKey
			String apiKey = smsStr[1];
			TChannel channel = tChannelDao.selectByApiKey(apiKey);
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(getPipleId());
			order.setChannelId(channel.getChannelId());
			order.setMobile(mobile);
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setModTime(DateTimeUtils.getCurrentTime());
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setGroupId(groupId);
			order.setOrder_string(order_string);
			order.setAccess_num(access_num);
			SaveOrderInsert(order);
		}
		if(StringUtil.isNotEmptyString(params)) {
			JSONObject paramsObj = JSONObject.fromObject(params);
			if(paramsObj.has("linkId")) { // 为点播方式
				statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, groupId, params);
				String mobile = paramsObj.getString("user_id");
				String access_num = paramsObj.getString("access_num");
				String sms = paramsObj.getString("order_string");
				String ismp_product_id = paramsObj.getString("ismp_product_id"); // 计费点代码
				String linkId = paramsObj.getString("linkId");
				String correlator = paramsObj.getString("correlator");

				String[] smsStr = sms.split("#");  // order_string 指令#后4位拓展字段定义为渠道apiKey
				String apiKey = smsStr[1];
				TChannel channel = tChannelDao.selectByApiKey(apiKey);

				TPipleProduct ppkey = new TPipleProduct();
				ppkey.setPipleId(getPipleId());
				ppkey.setPipleProductCode(ismp_product_id);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
				TProduct tProduct = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());

				order.setOrderId(KeyHelper.createKey());
				order.setPipleOrderId(linkId);
				order.setPipleId(getPipleId());
				order.setChannelId(channel.getChannelId());
				order.setMobile(mobile);
				order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
				order.setCreateTime(DateTimeUtils.getCurrentTime());
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
				int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
				order.setProvinceId(provinceId);
				order.setGroupId(groupId);
				order.setOrder_string(order_string);
				order.setAccess_num(access_num);
				order.setCorrelator(correlator);
				order.setResultCode(OP_TYPE_DB);
				SaveOrderInsert(order);
			}else if(paramsObj.has("stream_no")) {
				String mobile = paramsObj.getString("user_id");
				String access_num = paramsObj.getString("access_num");
				String sms = paramsObj.getString("order_string");
				String ismp_product_id = paramsObj.getString("ismp_product_id"); // 计费点代码
				String op_type = paramsObj.getString("op_type");
				String stream_no = paramsObj.getString("stream_no");
				String correlator = paramsObj.getString("correlator");

				TPipleProduct ppkey = new TPipleProduct();
				ppkey.setPipleId(getPipleId());
				ppkey.setPipleProductCode(ismp_product_id);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
				TProduct tProduct = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());

//				String sms = paramsObj.getString("order_string");  // 缺失指令
				TOrder oldOrder = tOrderDao.selectByPipleOrderId(stream_no);
				TQGXOrder tqgxOrder = new TQGXOrder();
				tqgxOrder.setTOrder(oldOrder);
				tqgxOrder.setOrderStatus(GlobalConst.OrderStatus.INIT);
				tqgxOrder.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
				tqgxOrder.setModTime(DateTimeUtils.getCurrentTime());
				tqgxOrder.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
				int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
				tqgxOrder.setProvinceId(provinceId);
				tqgxOrder.setCorrelator(correlator);
				tqgxOrder.setResultCode(op_type);
				SaveOrderUpdate(tqgxOrder);
			}
			// 返回响应数据
			JSONObject resultJson = new JSONObject();
			resultJson.put("content","");
			resultJson.put("blackflag","0");   // 0正常  1黑名单
			return resultJson;
		}
		// 同步交易结果
		if(StringUtil.isNotEmptyString(orderResult)) {
			JSONObject orderResultObj = JSONObject.fromObject(params);
			String linkId = null;
			if(orderResultObj.has("linkId")) {
				linkId = orderResultObj.getString("linkId");
			}else if(orderResultObj.has("stream_no")) {
				linkId = orderResultObj.getString("stream_no");
			}
			TOrder nowOrder = tOrderDao.selectByPipleOrderId(linkId);
			if(nowOrder!=null) {
				statistics(STEP_PAY_BASE_TO_PLATFORM, nowOrder.getGroupId(), orderResult);
				TChannelPipleKey pkey = new TChannelPipleKey();
				pkey.setChannelId(order.getChannelId());
				pkey.setPipleId(order.getPipleId());
				TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
				String result = orderResultObj.getString("result");
				if(result.equals(P_SUCCESS)) {
						if(nowOrder.getResultCode().equals(OP_TYPE_DB) || nowOrder.getResultCode().equals(OP_TYPE_BY)) {
							boolean bDeducted = false; // 扣量
							order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
							if(nowOrder.getResultCode().equals(OP_TYPE_DB)) {
								order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
							}else {
								order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS_DG);
							}

							order.setModTime(DateTimeUtils.getCurrentTime());
							order.setCompleteTime(DateTimeUtils.getCurrentTime());
							order.setResultCode(result);
							doWhenPaySuccess(order);
							bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
							if(!bDeducted){ // 不扣量 通知渠道
								notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
							}
						}else if(nowOrder.getResultCode().equals(OP_TYPE_TD)) {
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
							order.setModTime(DateTimeUtils.getCurrentTime());
						}
					}else {
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR_TG);
						order.setModTime(DateTimeUtils.getCurrentTime());
					}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
		}
		return null;
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}

	public class TQGXOrder extends TOrder{
		private String access_num;	// 接入号
		private String order_string;  // 订购命令字
		private String correlator;	// 随机码
		private String op_type;	// 订购类型(点播值是1，包月值是2，退订值是3)


		public String getAccess_num() {
			return access_num;
		}

		public void setAccess_num(String access_num) {
			this.access_num = access_num;
		}

		public String getOrder_string() {
			return order_string;
		}

		public void setOrder_string(String order_string) {
			this.order_string = order_string;
		}

		public String getCorrelator() {
			return correlator;
		}

		public void setCorrelator(String correlator) {
			this.correlator = correlator;
		}

		public String getOp_type() {
			return op_type;
		}

		public void setOp_type(String op_type) {
			this.op_type = op_type;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.access_num != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("access_num");
				oExt.setExtValue(this.access_num);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.order_string != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("order_string");
				oExt.setExtValue(this.order_string);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.correlator != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("correlator");
				oExt.setExtValue(this.correlator);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.op_type != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("op_type");
				oExt.setExtValue(this.op_type);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}

			return tOrderExts;
		}
	}
}
