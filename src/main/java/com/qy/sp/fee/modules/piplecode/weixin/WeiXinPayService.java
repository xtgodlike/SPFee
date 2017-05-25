package com.qy.sp.fee.modules.piplecode.weixin;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MD5;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
@Service
public class WeiXinPayService extends ChannelService{
	public static String WEIXIN_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	public static String STATUS_SUCCESS ="SUCCESS";
	@Override
	public String getPipleId() {
		return "14666533668639964952093";
	}
	public JSONObject createOrder(String apiKey,String mobile,String appId,String detail, String body, String price,String extData,String notifyUrl,String clientIp){
		JSONObject result = new JSONObject();
		try {
			TChannel tChannel =  tChannelDao.selectByApiKey(apiKey);
			if(null==tChannel){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","未知渠道代码");
				return result;
			}else if(tChannel.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","渠道状态异常");
				return result;
			}
			String orderId = KeyHelper.createKey();
			String nonceStr = KeyHelper.getRandomNumber(20);
			String myAppId = "wx0f4732f9742160eb";
			String mchId = "1366950902";
			String ip = "192.168.1.1";
			if(StringUtil.isNotEmptyString(clientIp)){
				ip = clientIp;
			}
			detail = URLDecoder.decode(detail,"UTF-8");
			body = URLDecoder.decode(body,"UTF-8");
			String signStr = sign(myAppId,tChannel.getChannelId(), mchId, nonceStr, orderId, detail, body, price, notifyUrl, ip);
			
			String orderInfo = getOrderInfo(myAppId,tChannel.getChannelId(),mchId,nonceStr
					,orderId,detail,body,price,notifyUrl,ip,signStr);
			String orderResult = HttpClientUtils.doPost(WEIXIN_URL, orderInfo,HttpClientUtils.ISO,HttpClientUtils.UTF8);
			XMLSerializer xmlSerializer = new XMLSerializer();
			JSONObject jsonObject = (JSONObject) xmlSerializer.read(orderResult);
			String resultCode = jsonObject.optString("return_code");
			if(STATUS_SUCCESS.equals(resultCode)){
				result.put("resultCode",GlobalConst.Result.SUCCESS);
				result.put("resultMsg","获取订单信息成功");
				result.put("orderId", orderId);
				result.put("wx_appid",myAppId);
				result.put("partnerid", mchId);
				String prepayid = jsonObject.optString("prepay_id");
				result.put("prepayid", prepayid);
				result.put("noncestr", nonceStr);
				String timestamp =  (System.currentTimeMillis()/1000)+"";
				result.put("timestamp",timestamp);
				String packageValue = "Sign=WXPay";
				result.put("package", packageValue);
				String signApp = signApp(myAppId,nonceStr, packageValue, mchId, prepayid, timestamp);
				result.put("sign", signApp);
			}else{
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","获取订单信息失败");
				result.put("orderId", orderId);
			}
			
			TOrder order = new TOrder();
			String groupId = KeyHelper.createKey();
			order.setOrderId(orderId);
			order.setAppId(appId);
			order.setAmount(new BigDecimal(NumberUtil.getDouble(price)/100.0));
			order.setChannelId(tChannel.getChannelId());
			order.setProductId(price);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setPipleId(getPipleId());
			order.setMobile(mobile);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setGroupId(groupId);
			order.setExtData(extData);
			this.SaveOrderInsert(order);
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, "apiKey="+apiKey+",detail="+detail+",body="+body+",price="+price+",extData="+extData);
			statistics( STEP_GET_SMS_PLATFORM_TO_BASE, groupId, orderInfo);
			statistics( STEP_BACK_SMS_BASE_TO_PLATFORM, groupId, orderResult);
			statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultMsg","请求异常");
		}
		return result;
	}
	public String toISO(String str){
		try {
			return new String(str.getBytes("utf-8"), "iso8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	private String getOrderInfo(String appId,String attach,String mchId,String nonceStr,String orderId,String detail, String body, String price,String notifyUrl,String ip,String signStr) {

		String orderInfo = "<xml>"
				+ "<appid>"+appId+"</appid>"
				+ "<attach>"+attach+"</attach>"
				+ "<body>"+body+"</body>"
				+ "<detail>"+detail+"</detail>"
				+ "<mch_id>"+mchId+"</mch_id>"
				+ "<nonce_str>"+nonceStr+"</nonce_str>"
				+ "<notify_url>"+notifyUrl+"</notify_url>"
				+ "<out_trade_no>"+orderId+"</out_trade_no>"
				+ "<spbill_create_ip>"+ip+"</spbill_create_ip>"
				+ "<total_fee>"+price+"</total_fee>"
				+ "<trade_type>APP</trade_type>"
				+ "<sign>"+signStr+"</sign>"
				+ "</xml>";
		return orderInfo;
	}
	private String sign(String appId,String attach,String mchId,String nonceStr,String orderId,String detail, String body, String price,String notifyUrl,String ip) {
		String stringA="appid="+appId+"&attach="+attach+"&body="+body+"&detail="+detail+"&mch_id="+mchId+"&nonce_str="+nonceStr+"&notify_url="+notifyUrl+"&out_trade_no="+orderId+"&spbill_create_ip="+ip+"&total_fee="+price+"&trade_type=APP";
		String stringSignTemp=stringA+"&key=14697735243809422847005695njqyly";
		stringSignTemp=MD5.getMD5(stringSignTemp).toUpperCase();
		return stringSignTemp;
	}
	private String signApp(String appid,String noncestr,String packageValue,String partnerid,String prepayid, String timestamp) {
		String stringA="appid="+appid+"&noncestr="+noncestr+"&package="+packageValue+"&partnerid="+partnerid+"&prepayid="+prepayid+"&timestamp="+timestamp;
		String stringSignTemp=stringA+"&key=14697735243809422847005695njqyly";
		stringSignTemp=MD5.getMD5(stringSignTemp).toUpperCase();
		return stringSignTemp;
	}
	public String notify(JSONObject requestObject){
		String orderId = requestObject.optString("out_trade_no");
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
		if(order == null){
			return "error";
		}
		boolean isSend = false;
		String pipleOrderId = requestObject.optString("transaction_id");
		TChannelPipleKey pkey = new TChannelPipleKey();
		pkey.setChannelId(order.getChannelId());
		pkey.setPipleId(order.getPipleId());
		TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
		order.setPipleOrderId(pipleOrderId);
		String resultCode = requestObject.optString("return_code");
		if(STATUS_SUCCESS.equals(resultCode)){
			order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
			order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
			order.setModTime(DateTimeUtils.getCurrentDate());
			order.setCompleteTime(DateTimeUtils.getCurrentDate());
			order.setResultCode(resultCode);
			doWhenPaySuccess(order);
			if(cp != null){
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}
		}else{
			order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
			order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
			order.setResultCode(resultCode);
		}
		SaveOrderUpdate(order);
		if(isSend){ // 不扣量 通知渠道
			notifyWeiXinChannel(cp.getNotifyUrl(),order,"ok");
		}
		String result = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
		return result;
	}
	public void notifyWeiXinChannel(String url,TOrder order,String status){
		try {
			String rst = "";
				String param = "orderId="+order.getOrderId()+"&status="+status+"&amount="+order.getAmount().doubleValue()+"&payType=1";
				
				if(!StringUtil.isEmpty(order.getChannelId())){
					TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
					param += "&channelApi="+tChannel.getApiKey();
				}
				if(StringUtil.isNotEmptyString(order.getPipleOrderId())){
					param += "&outTradeNo="+order.getPipleOrderId();
					
				}
				if(StringUtil.isNotEmptyString(order.getAppId())){
					param += "&appId="+order.getAppId();
					
				}
				if(!StringUtil.isEmpty(order.getExtData())){
					param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
				}
				String ackUrl = url+"?"+param;
				logger.info("sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
				statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
				rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
				logger.info("getFromChannel= " + rst + " ,orderId="+order.getOrderId());
				statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public JSONObject queryOrder(String orderId){
		JSONObject result = new JSONObject();
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);	
		if(order != null){
			if(order.getOrderStatus() == GlobalConst.OrderStatus.SUCCESS && order.getDecStatus() == GlobalConst.DEC_STATUS.UNDEDUCTED){
				result.put("resultCode",GlobalConst.Result.SUCCESS);
				result.put("resultMsg", "订单支付成功");
				result.put("orderId", order.getOrderId());
				result.put("amount", order.getAmount());
				result.put("completeTime", order.getCompleteTime());
				result.put("pipleId", order.getPipleId());
			}else{
				result.put("resultCode", GlobalConst.Result.ERROR);
				result.put("resultMsg", "订单支付失败");
			}
		}else{
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultMsg","订单不存在");
		}
		return result;
	}
}
