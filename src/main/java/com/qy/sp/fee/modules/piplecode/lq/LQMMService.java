package com.qy.sp.fee.modules.piplecode.lq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
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

import net.sf.json.JSONObject;
@Service
public class LQMMService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "0";	  // 同步计费成功
	private  Logger log = Logger.getLogger(LQMMService.class);		
	@Override
	public String getPipleId() {
		return "14797949784636409309867";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("LQMMService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String iccid = requestBody.optString("iccid");
		String ip = requestBody.optString("ip");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		if(StringUtil.isEmpty(fromType)){
			fromType = GlobalConst.FromType.FROM_TYPE_API;
		}
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId) || StringUtil.isEmpty(imsi) || StringUtil.isEmpty(mobile)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		}else{
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setMobile(mobile);
			// 调用合法性校验
			BaseResult bResult = this.accessVerify(req,pipleId);
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			}
			String groupId = KeyHelper.createKey();
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			//保存订单
			LQMMOrder order = new LQMMOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setIccid(iccid);
			order.setIp(ip);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(INIT);
			order.setSubStatus(INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			int provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			order.setGroupId(groupId);
			order.setFromType(Integer.valueOf(fromType));
			SaveOrderInsert(order);
			result.put("orderId",order.getOrderId());
			//请求第一条短信
			Map<String, String> params = new HashMap<String, String>();
			params.put("appid", piple.getPipleAuthA());
			params.put("payid", pipleProduct.getPipleProductCode());
			params.put("imsi", order.getImsi());
			params.put("imei", order.getImei());
			params.put("mobile", order.getMobile());
			params.put("iccid", order.getIccid());
			params.put("ip", order.getIp());
			params.put("extra", order.getOrderId());  // 透传订单号
			try {
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,piple.getPipleUrlA()+";"+params.toString());
				String pipleResult1= HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult1);
				log.info(" LQMMService getSmsResult1:"+  pipleResult1);
				if(pipleResult1 != null && !"".equals(pipleResult1)){
					JSONObject jsonObj = JSONObject.fromObject(pipleResult1);
					String state = null;
					String msg = null;
					String sn = null;
					String smstype = null;
					String addr = null;
					String sms = null;
					if(jsonObj.has("state") ){
						state = jsonObj.getString("state");
						if(RES_SUCCESS.equals(state)){// 返回成功
							sn = jsonObj.getString("sn");	// 通道订单号
							smstype = jsonObj.getString("smstype");	// 短信发送方式，text为文本方式，data为二进制方式
							addr = jsonObj.getString("addr");  // 发送端口
							sms = jsonObj.getString("sms"); // 发送内容
							order.setResultCode(state);
							order.setPipleOrderId(sn);	
							order.setSmstype(smstype);
							order.setAddr(addr);
							order.setSms(sms);
							result.put("resultCode", GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功。");
							result.put("smsType", smstype);
							result.put("port", addr);
							result.put("sms", sms);
							return result;
						}else{
							sn = jsonObj.getString("sn");	// 通道订单号
							msg = jsonObj.getString("msg");
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GETCODE_FAIL);
							order.setResultCode(state);
							order.setPipleOrderId(sn);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败："+msg);
							
						}
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GETCODE_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
					return result;
				}
				SaveOrderUpdate(order);
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
				order.setModTime(DateTimeUtils.getCurrentTime());
				SaveOrderUpdate(order);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败，接口异常:"+e.getMessage());
				return result;
			}
		}	
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("LQMMService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String appid = requestBody.optString("appid");
		String price = requestBody.optString("price");
		String state = requestBody.optString("state");
		String sn = requestBody.optString("sn");	// 通道订单号
		String imsi = requestBody.optString("imsi");
		String tel = requestBody.optString("tel");
		String cpparam = requestBody.optString("cpparam"); // 订单号
		TOrder order = tOrderDao.selectByPrimaryKey(cpparam);
		order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
		if(order!=null ){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			if(P_SUCCESS.equals(state)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(state);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
				if(!bDeducted){ // 不扣量 通知渠道
					notifyChannelAll(cp.getNotifyUrl(), order, null);
				}
			}else {
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(state);
			}
			SaveOrderUpdate(order);
			
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class LQMMOrder extends TOrder{
		private String imei;
		private String ip; 
		private String smstype; 
		private String addr; 
		private String sms; 
		
		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getSmstype() {
			return smstype;
		}

		public void setSmstype(String smstype) {
			this.smstype = smstype;
		}

		public String getAddr() {
			return addr;
		}

		public void setAddr(String addr) {
			this.addr = addr;
		}

		public String getSms() {
			return sms;
		}

		public void setSms(String sms) {
			this.sms = sms;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.imei != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("imei");
				oExt.setExtValue(this.imei);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.ip != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("ip");
				oExt.setExtValue(this.ip);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.smstype != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("smstype");
				oExt.setExtValue(this.smstype);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.addr != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("addr");
				oExt.setExtValue(this.addr);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.sms != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("sms");
				oExt.setExtValue(this.sms);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
