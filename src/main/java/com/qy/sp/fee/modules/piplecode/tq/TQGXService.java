package com.qy.sp.fee.modules.piplecode.tq;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
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
		return "15040101351485677882939";
	}

	@Override
	public String getPipleKey() {
		return "PM1082";
	}

	public JSONObject processPay(String params,String order_string,String orderResult) throws Exception {
		log.info("TQGXService params:"+params);
		log.info("TQGXService order_string:"+order_string);
		log.info("TQGXService orderResult:"+orderResult);
		JSONObject resultJson = new JSONObject();
		TQGXOrder order = new TQGXOrder();
		String groupId = KeyHelper.createKey();
		if(StringUtil.isNotEmptyString(order_string)) {
			statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, groupId, order_string);
			JSONObject orderStringObj = JSONObject.fromObject(order_string);
			String sms = orderStringObj.getString("order_string");
			String mobile = orderStringObj.getString("user_id");
			String access_num = orderStringObj.getString("access_num");
//			String stream_no = orderStringObj.getString("stream_no");  // 缺失通道订单号
			String[] smsStr = sms.split("#");  // order_string 指令#后拓展信息（前4位为apiKey，之后数据为渠道拓展）
//			String feeCode = smsStr[0];
			String apiKey = null;
			String extData = null;
			TChannel channel = null;
			if(smsStr.length>2){ // 有#扩展数据
				String extStr = smsStr[1];
				if(extStr.length()>4){
					apiKey = extStr.substring(0,4);

					extData = extStr.substring(4,extStr.length());
				}
			}else{ // 无拓展数据 默认apiKey=1003 千雅内部渠道
				apiKey = "1003";
			}
			channel = tChannelDao.selectByApiKey(apiKey);
//			TPipleProduct ppkey = new TPipleProduct();
//			ppkey.setPipleId(getPipleId());
//			ppkey.setPipleProductCode(feeCode);
//			TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
//			TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(getPipleId());
			order.setChannelId(channel==null?null:channel.getChannelId());
//			order.setProductId(product.getProductId());  // 默认10元
			order.setMobile(mobile);
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setModTime(DateTimeUtils.getCurrentTime());
//			order.setAmount(new BigDecimal(product.getPrice()/100.0));
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			order.setGroupId(groupId);
			order.setOrder_string(order_string);
			order.setAccess_num(access_num);
			order.setResultCode(OP_TYPE_BY);
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

				String[] smsStr = sms.split("#");  // order_string 指令#后拓展信息（前4位为apiKey，之后数据为渠道拓展）
				String apiKey = null;
				String extData = null;
				TChannel channel = null;
				if(smsStr.length>2){ // 有#扩展数据
					String extStr = smsStr[1];
					if(extStr.length()>4){
						apiKey = extStr.substring(0,4);

						extData = extStr.substring(4,extStr.length());
					}
				}else{ // 无拓展数据 默认apiKey=1003 千雅内部渠道
					apiKey = "1003";
				}
				channel = tChannelDao.selectByApiKey(apiKey);
				TPipleProduct ppkey = new TPipleProduct();
				ppkey.setPipleId(getPipleId());
				ppkey.setPipleProductCode(ismp_product_id);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
				TProduct tProduct = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());

				order.setOrderId(KeyHelper.createKey());
				order.setPipleOrderId(linkId);
				order.setPipleId(getPipleId());
				order.setProductId(tProduct.getProductId());
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
				order.setExtData(extData);
				SaveOrderInsert(order);
			}else if(paramsObj.has("stream_no")) { // 包月方式
				String mobile = paramsObj.getString("user_id");
				String access_num = paramsObj.getString("access_num");
				String ismp_product_id = paramsObj.getString("ismp_product_id"); // 计费点代码
				String op_type = paramsObj.getString("op_type");
				String stream_no = paramsObj.getString("stream_no");
				String correlator = paramsObj.getString("correlator");
//				String sms = paramsObj.getString("order_string");  // 缺失指令
//				TOrder oldOrder = tOrderDao.selectByPipleOrderId(stream_no);
				// 根据通道ID和手机号关联
				List<TOrder> orders = tOrderDao.getOrderByPipleIdAndMobile(getPipleId(),mobile);
				TOrder oldOrder = null;
				if(CollectionUtils.isNotEmpty(orders)){ // 短信指令订购方式
					oldOrder = orders.get(0);
					log.info("TQGXService oldOrder orderId="+oldOrder.getOrderId()+",mobile="+oldOrder.getMobile());
					statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, groupId, params);
					TPipleProduct ppkey = new TPipleProduct();
					ppkey.setPipleId(getPipleId());
					ppkey.setPipleProductCode(ismp_product_id);
					TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
					TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
					TQGXOrder tqgxOrder = new TQGXOrder();
					tqgxOrder.setTOrder(oldOrder);
					tqgxOrder.setPipleOrderId(stream_no);
					tqgxOrder.setProductId(product.getProductId());  // 默认10元
					tqgxOrder.setAmount(new BigDecimal(product.getPrice()/100.0));
					tqgxOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
					tqgxOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
					tqgxOrder.setModTime(DateTimeUtils.getCurrentTime());
					int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
					tqgxOrder.setProvinceId(provinceId);
					tqgxOrder.setCorrelator(correlator);
					tqgxOrder.setResultCode(op_type);
					SaveOrderUpdate(tqgxOrder);
				}else { // 接口订购方式 只同步一次params
					oldOrder = tOrderDao.selectByPipleOrderId(stream_no);
					if(oldOrder==null){ // 未同步过 则新增数据
						statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, groupId, params);
						String apiKey = "1003"; // 由于接口订购包月方式无区分渠道参数只能配置1个渠道，默认为自有渠道“千雅”
						TChannel channel = null;
						channel = tChannelDao.selectByApiKey(apiKey);
						TPipleProduct ppkey = new TPipleProduct();
						ppkey.setPipleId(getPipleId());
						ppkey.setPipleProductCode(ismp_product_id);
						TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
						TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
						TQGXOrder jkdgOrder = new TQGXOrder();
						jkdgOrder.setOrderId(KeyHelper.createKey());
						jkdgOrder.setPipleOrderId(stream_no);
						jkdgOrder.setPipleId(getPipleId());
						jkdgOrder.setChannelId(channel==null?null:channel.getChannelId());
						jkdgOrder.setProductId(product.getProductId());
						jkdgOrder.setMobile(mobile);
						jkdgOrder.setOrderStatus(GlobalConst.OrderStatus.INIT);
						if(op_type.equals(OP_TYPE_TD)){
							jkdgOrder.setSubStatus(GlobalConst.SubStatus.PAY_ERROR_TG);
						}else {
							jkdgOrder.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
						}
						jkdgOrder.setResultCode(op_type);
						jkdgOrder.setCreateTime(DateTimeUtils.getCurrentTime());
						jkdgOrder.setModTime(DateTimeUtils.getCurrentTime());
						jkdgOrder.setAmount(new BigDecimal(product.getPrice()/100.0));
						int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
						jkdgOrder.setProvinceId(provinceId);
						jkdgOrder.setGroupId(groupId);
						jkdgOrder.setAccess_num(access_num);
						jkdgOrder.setCorrelator(correlator);
						SaveOrderInsert(jkdgOrder);
					}
				}
			}
			// 返回响应数据
			resultJson.put("content","");
			resultJson.put("blackflag","0");   // 0正常  1黑名单
			return resultJson;
		}
		// 同步交易结果
		if(StringUtil.isNotEmptyString(orderResult)) {
			JSONObject orderResultObj = JSONObject.fromObject(orderResult);
			String linkId = null;
			if(orderResultObj.has("linkId")) {
				linkId = orderResultObj.getString("linkId");
			}
			if(StringUtil.isEmpty(linkId) && orderResultObj.has("stream_no")) {
				linkId = orderResultObj.getString("stream_no");
			}
			TOrder nowOrder = tOrderDao.selectByPipleOrderId(linkId);
			if(nowOrder!=null) {
				statistics(STEP_PAY_BASE_TO_PLATFORM, nowOrder.getGroupId(), orderResult);
				TChannelPipleKey pkey = new TChannelPipleKey();
				pkey.setChannelId(nowOrder.getChannelId());
				pkey.setPipleId(nowOrder.getPipleId());
				TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
				String result = orderResultObj.getString("result");
				if(result.equals(P_SUCCESS)) {
						if(nowOrder.getResultCode().equals(OP_TYPE_DB) || nowOrder.getResultCode().equals(OP_TYPE_BY)) {
							boolean bDeducted = false; // 扣量
							nowOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
							if(nowOrder.getResultCode().equals(OP_TYPE_DB)) {
								nowOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
							}else {
								nowOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS_DG);
							}
							nowOrder.setModTime(DateTimeUtils.getCurrentTime());
							nowOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
							doWhenPaySuccess(nowOrder);

							bDeducted  = nowOrder.deduct(cp.getVolt());  // 是否扣量
							if(!bDeducted){ // 不扣量 通知渠道
								notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
							}
						}else if(nowOrder.getResultCode().equals(OP_TYPE_TD)) {
							nowOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							nowOrder.setSubStatus(GlobalConst.SubStatus.PAY_ERROR_TG);
							nowOrder.setModTime(DateTimeUtils.getCurrentTime());
						}
					}else {
						nowOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						nowOrder.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
						nowOrder.setModTime(DateTimeUtils.getCurrentTime());
					}
			}else {
				nowOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				nowOrder.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				nowOrder.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(nowOrder);
		}
		return resultJson;
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

	public String getVerifyCode(String mobile){
		JSONObject result = new JSONObject();
		try {
			if(StringUtil.isEmpty(mobile)){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","必填参数不能为空");
			}
			HashMap<String,String> parameters = new HashMap<String, String>();
			String uri = "http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe";
			String spId = "11105199";
			String chargeId = "2001";
			String timestamp = DateTimeUtils.getCurrentYMDHMSNo();
			String sercet = "1a9311163d2a432bd4a8";
			// SHA1(chargeId+timestamp+sercet)
			String token = SecurityUtils.getSha1(chargeId+timestamp+sercet);
			parameters.put("spId", spId);
			parameters.put("chargeId", chargeId);
			parameters.put("orderType", 1+"");
			parameters.put("timestamp", timestamp);
			parameters.put("accessToken", token);
			parameters.put("imsi", "460016878515303");
			parameters.put("ip", "123.56.158.156");
			log.info("getOrderId 获取订单号接口请求：uri="+uri+",params="+parameters.toString());
			String returnData = HttpClientUtils.doPost("http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe",parameters,"utf-8");
			log.info("getOrderId 获取订单号接口响应：returnData="+returnData);
			String orderId = null;
			if(StringUtil.isNotEmptyString(returnData)){
				JSONObject object = JSONObject.fromObject(returnData);
				String resultCode = object.optString("errcode");
				String resultMsg = object.optString("errmsg");
				if("0".equals(resultCode) || "159".equals(resultCode)){ // 0为成功 159获取号码失败但是orderinfo会返回 其他失败
					String orderinfo = object.optString("orderinfo");
					JSONObject orderObj = JSONObject.fromObject(orderinfo);
					orderId = orderObj.optString("orderId");
					result.put("orderId",orderId);
					result.put("resultCode",resultCode);
					result.put("resultMsg",resultMsg);
				}else {
					result.put("resultCode",resultCode);
					result.put("resultMsg",resultMsg);
				}
			}else{
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","获取订单号接口响应为空.");

			}
			// 获取订单号成功 提交手机号获取验证码
			if(StringUtil.isNotEmptyString(orderId)){
				HashMap<String,String> getCodeParams = new HashMap<String, String>();
				getCodeParams.put("orderId",orderId);
				getCodeParams.put("phoneNum",mobile);
				log.info("getCode 获取验证码接口请求：uri="+uri+",params="+getCodeParams.toString());
				String getCodeData = HttpClientUtils.doPost("http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe",getCodeParams,"utf-8");
				log.info("getCode 获取验证码接口响应：getCodeData="+getCodeData);
				if(StringUtil.isNotEmptyString(getCodeData)){
					JSONObject object = JSONObject.fromObject(getCodeData);
					String resultCode = object.optString("errcode");
					String resultMsg = object.optString("errmsg");
					if("0".equals(resultCode)){ // 0为成功 其他失败
						result.put("resultCode",resultCode);
						result.put("resultMsg",resultMsg);
					}else {
						result.put("resultCode",resultCode);
						result.put("resultMsg",resultMsg);
					}
				}else{
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","获取验证码接口响应为空.");

				}
			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取验证码失败："+e.getMessage(),e);
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultMsg","获取验证码失败："+e.getMessage());
			return result.toString();
		}
	}


	public String submitVerifyCode(String orderId,String mobile,String verifyCode){
		JSONObject result = new JSONObject();
		try {
			if(StringUtil.isEmpty(orderId) || StringUtil.isEmpty(mobile) || StringUtil.isEmpty(verifyCode)){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","必填参数不能为空");
			}
			String uri = "http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe";
			// 获取订单号成功 提交手机号获取验证码
			HashMap<String,String> submitParams = new HashMap<String, String>();
			submitParams.put("orderId",orderId);
			submitParams.put("phoneNum",mobile);
			submitParams.put("verCode",verifyCode);
			log.info("submitCode 提交验证码接口请求：uri="+uri+",params="+submitParams.toString());
			String submitReData = HttpClientUtils.doPost("http://61.160.185.51:9250/ismp/serviceOrder?action=subscribe",submitParams,"utf-8");
			log.info("submitCode 提交验证码接口响应：submitReData="+submitReData);
			if(StringUtil.isNotEmptyString(submitReData)){
				JSONObject object = JSONObject.fromObject(submitReData);
				String resultCode = object.optString("errcode");
				String resultMsg = object.optString("errmsg");
				if("0".equals(resultCode)){ // 0为成功 其他失败
					result.put("resultCode",resultCode);
					result.put("resultMsg",resultMsg);
				}else {
					result.put("resultCode",resultCode);
					result.put("resultMsg",resultMsg);
				}
			}else{
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","提交验证码接口响应为空.");

			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("提交验证码失败："+e.getMessage(),e);
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultMsg","提交验证码失败："+e.getMessage());
			return result.toString();
		}
	}
}
