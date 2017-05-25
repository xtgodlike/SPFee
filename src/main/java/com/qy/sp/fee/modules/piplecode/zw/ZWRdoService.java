package com.qy.sp.fee.modules.piplecode.zw;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
public class ZWRdoService extends ChannelService{
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	private  Logger log = Logger.getLogger(ZWRdoService.class);		
	@Override
	public String getPipleId() {
		return "14645932413593156136355";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("ZTVideoService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)  || StringUtil.isEmpty(imsi)){
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
			ZWRdoOrder order = new ZWRdoOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
//			order.setMobile(mobile);
			order.setImsi(imsi);
//			order.setImei(imei);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
//			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
//			order.setProvinceId(provinceId);
			order.setExtData(extData);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				//请求短信指令内容
//				Map<String, String> params = new HashMap<String, String>();
//				params.put("ppid", piple.getPipleAuthA());
//				params.put("custom", order.getExtData());
				String reqUrl = piple.getPipleUrlA()+"?"+"ppid="+pipleProduct.getPipleProductCode()+"&custom="+order.getExtData();
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				log.info(" ZWRdoService getSmsResult:"+  pipleResult);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObj = JSONObject.fromObject(pipleResult);
					String status = null;
					String orderid = null;
					String port = null;
					String content = null;
					if(jsonObj.has("status") ){
						status = jsonObj.getString("status");
						if(RES_SUCCESS.equals(status)){// 返回成功
							orderid = jsonObj.getString("orderid");
							port = jsonObj.getString("port");
							content = jsonObj.getString("content");
							 order.setPipleOrderId(orderid);
							 order.setResultCode(status);
							 order.setPort(port);
							 order.setContent(content);
							 order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("port", port);
							 result.put("content", content);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
						}else{
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(status);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+status);
						}
					}else{
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+pipleResult);
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
				}
				SaveOrderUpdate(order);
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			} catch (Exception e) {
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
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("ZTVideoService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			
			String orderId = requestBody.optString("orderId");
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder==null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
				return result;
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
				return result;
			}else{
				statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
				TProduct tProduct = this.tProductDao.selectByPrimaryKey(tOrder.getProductId());
				BaseChannelRequest req = new BaseChannelRequest();
				req.setApiKey(apiKey);
				
				req.setImsi(tOrder.getImsi());
				req.setProductCode(tProduct.getProductCode());
				BaseResult bResult = this.accessVerify(req, getPipleId());
				if(bResult!=null){// 返回不为空则校验不通过
					result.put("resultCode",bResult.getResultCode());
					result.put("resultMsg",bResult.getResultMsg());
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}else{
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					
					//短信发送确认
					String reqUrl = piple.getPipleUrlB()+"?"+"orderid="+tOrder.getPipleOrderId();
					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), result.toString());
					String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					log.info(" ZWRdoService confirmResult:"+  pipleResult);
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						JSONObject jsonObj = JSONObject.fromObject(pipleResult);
						String status = null;
						String orderid = null;
						String port = null;
						String content = null;
						if(jsonObj.has("status") ){
							status = jsonObj.getString("status");
							if(RES_SUCCESS.equals(status)){// 返回成功
								tOrder.setResultCode(status);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。");
							}else{
								tOrder.setResultCode(status);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+status);
							}
						}else{
							tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
							tOrder.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+pipleResult);
						}
					
					}else{
						tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
						tOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
					}
					SaveOrderUpdate(tOrder);
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("ZWRdoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String ppid = requestBody.optString("ppid");
		String imsi = requestBody.optString("imsi");
		String msisdn = requestBody.optString("msisdn");
		String price = requestBody.optString("price");
		String time = requestBody.optString("time");
		String orderid = requestBody.optString("orderid");
		String custom = requestBody.optString("custom");
		TOrder order = tOrderDao.selectByPipleOrderId(orderid);
		if(order!=null ){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			boolean isSend = false; //是否同步
			if(msisdn!=null && !"".equals(msisdn)){
				order.setMobile(msisdn);
				int  provinceId = this.getProvinceIdByMobile(msisdn, false); // 获取省份ID
				order.setProvinceId(provinceId);
			}
			order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
			order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
			order.setModTime(DateTimeUtils.getCurrentTime());
			order.setCompleteTime(DateTimeUtils.getCurrentTime());
			boolean bDeducted  = order.deduct(cp.getVolt());
			if(!bDeducted){ 
				isSend =true;
			}
			doWhenPaySuccess(order);
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
//				n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
			}
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class ZWRdoOrder extends TOrder{
		private String port;
		private String content;  
		
		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.port != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("port");
				oExt.setExtValue(this.port);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.content != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("content");
				oExt.setExtValue(this.content);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
