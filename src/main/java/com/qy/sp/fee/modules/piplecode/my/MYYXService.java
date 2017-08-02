package com.qy.sp.fee.modules.piplecode.my;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MYYXService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "200000";  // 请求通道成功
	public final static String P_SUCCESS = "0";	  // 同步计费成功
	private  Logger log = Logger.getLogger(MYYXService.class);
	@Override
	public String getPipleId() {
		return "15015662802400843217575";
	}

	@Override
	public String getPipleKey() {
		return "PW1076";
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("MYYXService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String apiKey = requestBody.optString("apiKey");
		String pipleKey = requestBody.optString("pipleKey");
		String productCode = requestBody.optString("productCode");
		String mobile = requestBody.optString("mobile");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleKey) || StringUtil.isEmpty(imsi) || StringUtil.isEmpty(mobile)){
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
			TPiple tPiple = tPipleDao.selectByPipleKey(pipleKey);
			String pipleId = tPiple==null?"":tPiple.getPipleId();
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
			WCXWoOrder order = new WCXWoOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(INIT);
			order.setSubStatus(INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			order.setGroupId(groupId);
			SaveOrderInsert(order);
			result.put("orderId",order.getOrderId());

			String reqUrl = piple.getPipleUrlA()+"?"+"userid="+piple.getPipleAuthA()+"&tel="+order.getMobile()+"&orderid="+piple.getPipleAuthB()+KeyHelper.creatKey(10)
					+"&webid="+piple.getPipleAuthC()+order.getOrderId();
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,reqUrl);
//			String pipleResult = HttpClientUtils.doPost(piple.getPipleUrlA(),params,HttpClientUtils.UTF8);
			String pipleResult = HttpClientUtils.doGet(reqUrl,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
			log.info(" MYYXService getSmsResult:"+  pipleResult);
			if(pipleResult != null && !"".equals(pipleResult)){
				JSONObject jsonObj = JSONObject.fromObject(pipleResult);
				String resultCode = null;
				String resultMsg = null;
				String orderid = null;
				if(jsonObj.has("resultCode") ){
					resultCode = jsonObj.getString("resultCode");
					resultMsg = jsonObj.getString("resultMsg");
					if(RES_SUCCESS.equals(resultCode)){ // 返回成功
						orderid = jsonObj.getString("orderid");
						//	order.setPipleOrderId(billid);
						order.setPipleOrderId(orderid);
						order.setResultCode(resultCode);
						// 更新订单信息
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GETCODE_SUCCESS);
						order.setModTime(DateTimeUtils.getCurrentTime());
						//设置返回结果
						result.put("resultCode",GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功");
						SaveOrderUpdate(order);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}else{
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GETCODE_FAIL);
						order.setResultCode(resultCode);
						order.setModTime(DateTimeUtils.getCurrentTime());
						SaveOrderUpdate(order);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+resultMsg);
						statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
						return result;
					}

				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GETCODE_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					SaveOrderUpdate(order);
					result.put("resultCode", GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败:"+pipleResult);
					statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
					return result;
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GETCODE_FAIL);
				order.setModTime(DateTimeUtils.getCurrentTime());
				SaveOrderUpdate(order);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败，接口异常");
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}
		}
	}

	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("MYYXService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
			result.put("orderId",orderId);
			if(StringUtil.isEmptyString(apiKey) || StringUtil.isEmptyString(orderId)   || StringUtil.isEmpty(verifyCode)) {
				result.put("resultCode", GlobalConst.CheckResult.MUST_PARAM_ISNULL + "");
				result.put("resultMsg", GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
				return result;
			}
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
					WCXWoOrder newOrder =  new WCXWoOrder();
					newOrder.setVerifyCode(verifyCode);
					newOrder.setTOrder(tOrder);

					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());

					//提交验证码
					String reqUrl = piple.getPipleUrlB()+"?"+"orderid="+tOrder.getPipleOrderId()+"&verifycode="+verifyCode;
					statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl);
					String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					log.info(" MYYXService confirmResult:"+  pipleResult);
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						JSONObject jsonObj = JSONObject.fromObject(pipleResult);
						String resultCode = null;
						String resultMsg = null;
						String port = null;
						String content = null;
						if(jsonObj.has("resultCode") ){
							resultCode = jsonObj.getString("resultCode");
							resultMsg = jsonObj.getString("resultMsg");
							if(RES_SUCCESS.equals(resultCode)){// 返回成功
								tOrder.setResultCode(resultCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_SUCCESS);
								result.put("resultCode", GlobalConst.Result.SUCCESS);
								result.put("resultMsg","请求成功。");
							}else{
								tOrder.setResultCode(resultCode);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SEND_MESSAGE_FAIL);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+resultMsg);
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
		logger.info("MYYXService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String message = requestBody.optString("message");
		String spnumber = requestBody.optString("spnumber");
		String linkid = requestBody.optString("linkid");
		String exData = requestBody.optString("exData");		// 从第6位开始我方订单号
		String phone = requestBody.optString("phone");
		String orderTime = requestBody.optString("orderTime");
		String msgstatus = requestBody.optString("msgstatus");
		String province = requestBody.optString("province");
		String resultCode = requestBody.optString("resultCode");

		String myOrderId = exData.substring(5,exData.length());
		TOrder order = tOrderDao.selectByPrimaryKey(myOrderId);
		if(order!=null ){ // 同步数据正确
			try {
				statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
				TChannelPipleKey pkey = new TChannelPipleKey();
				pkey.setChannelId(order.getChannelId());
				pkey.setPipleId(order.getPipleId());
				TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
				if(cp == null){
                    return "channel error";
                }

				//扣量
				boolean bDeducted = false;
				if(P_SUCCESS.equals(resultCode)){
                    order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    order.setSubStatus(PAY_SUCCESS);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setCompleteTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(resultCode);
                    doWhenPaySuccess(order);
                    bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
                    if(!bDeducted){ // 不扣量 通知渠道
                        notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
                    }
                }else {
                    order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
                    order.setModTime(DateTimeUtils.getCurrentTime());
                    order.setResultCode(resultCode);
                }
				SaveOrderUpdate(order);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("WCXWoService同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order not exist";
		}

	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}

	public class WCXWoOrder extends TOrder{
		private String imei;
		private String verifyCode;

		public String getVerifyCode() {
			return verifyCode;
		}

		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}

		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
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
			if(this.verifyCode != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("verifyCode");
				oExt.setExtValue(this.verifyCode);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
