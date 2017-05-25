package com.qy.sp.fee.modules.piplecode.panmei;

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
import com.qy.sp.fee.common.utils.MD5;
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
public class PMWoReadService extends ChannelService{
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "DELIVRD";  // 扣费成功
	private  Logger log = Logger.getLogger(PMWoReadService.class);		
	@Override
	public String getPipleId() {
		return "14646760508779930172586";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("PMWoReadService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
//		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)  || StringUtil.isEmpty(productCode) || StringUtil.isEmpty(imsi)){
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
			PMWoReadOrder order = new PMWoReadOrder();
			order.setOrderId(KeyHelper.createKey());
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
			order.setGroupId(groupId);
			order.setExtData(extData);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				//请求短信指令内容
				
				Map<String, String> params = new HashMap<String, String>();
				String spId = piple.getPipleAuthA();
				String spCode = pipleProduct.getPipleProductCode();
				String time = DateTimeUtils.getCurrentYMDHMSNo();
				String key = piple.getPipleAuthB();
				params.put("spId", spId);
				params.put("spCode",spCode);
				params.put("time",time);
				// sgin = MD5(spId + spCode + time + key)
				String sign = MD5.getMD5(spId+spCode+time+key);
				params.put("sign",sign);
//				String reqUrl = piple.getPipleUrlA()+"?"+"spId="+spId+"&spCode="+spCode+"&time="+time+"&sign="+sign;
				log.info(" ZWRdoService reqUrl:"+  piple.getPipleUrlA()+";"+params.toString());
//				log.info(" ZWRdoService reqUrl:"+  reqUrl);
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,piple.getPipleUrlA()+";"+params.toString());
				String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlA(), params, HttpClientUtils.UTF8);
//				String pipleResult= HttpClientUtils.doGet(reqUrl, "UTF-8");
				log.info(" ZWRdoService getSmsResult:"+  pipleResult);
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObj = JSONObject.fromObject(pipleResult);
					String resultCode = null;
					String resultMsg = null;
					String longNum = null;
					String msg = null;
					String linkid = null;
					if(jsonObj.has("resultCode") ){
						resultCode = jsonObj.getString("resultCode");
						resultMsg = jsonObj.getString("resultMsg");
						if(RES_SUCCESS.equals(resultCode)){// 返回成功
							 longNum = jsonObj.getString("longNum");
							 msg = jsonObj.getString("msg");
							 linkid = jsonObj.getString("linkid");
							 order.setPipleOrderId(linkid);
							 order.setResultCode(resultCode);
							 order.setPort(longNum);
							 order.setContent(msg);
							 order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("port", longNum);
							 result.put("content", msg);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
						}else{
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(resultCode);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+resultMsg);
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
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}
		}
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("PMWoReadService processPaySuccess 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String linkid = requestBody.optString("linkid");
		String spId = requestBody.optString("spId");
		String spCode = requestBody.optString("spCode");
		String mobile = requestBody.optString("mobile");
		String msg = requestBody.optString("msg");
		String longNum = requestBody.optString("longNum");
		String payMoney = requestBody.optString("payMoney");
		String payTime = requestBody.optString("payTime");
		String status = requestBody.optString("status");
		String statusDesc = requestBody.optString("statusDesc");
		String spParams = requestBody.optString("spParams");
		String province = requestBody.optString("province");
		String operator = requestBody.optString("operator");
		String sign = requestBody.optString("sign");
		TOrder order = tOrderDao.selectByPipleOrderId(linkid);
		if(order!=null && order.getOrderStatus()!=GlobalConst.OrderStatus.SUCCESS){ // 成功同步过成功
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
			if(P_SUCCESS.equals(status)){
				if(mobile!=null && !"".equals(mobile)){
					order.setMobile(mobile);
					int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
					order.setProvinceId(provinceId);
				}
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else{
				if(mobile!=null && !"".equals(mobile)){
					order.setMobile(mobile);
					int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
					order.setProvinceId(provinceId);
				}
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
//				n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
				notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
			}
		}
		JSONObject parameters = new JSONObject();
		parameters.put("resultCode", "1000");
		return parameters.toString();
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class PMWoReadOrder extends TOrder{
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
