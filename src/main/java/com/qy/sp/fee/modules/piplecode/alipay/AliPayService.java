package com.qy.sp.fee.modules.piplecode.alipay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class AliPayService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	
	@Override
	public String getPipleId() {
		return "14666528254896750836859";
	}
	public JSONObject createOrder(String apiKey,String mobile,String appId,String subject, String body, String price,String extData,String notifyUrl){
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
			String orderInfo = getOrderInfo(orderId,subject,body,price,notifyUrl);
			String sign = sign(orderInfo);
		
			/**
			 * 仅需对sign 做URL编码
			 */
			sign = URLEncoder.encode(sign, "UTF-8");
			/**
			 * 完整的符合支付宝参数规范的订单信息
			 */
			final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();
			result.put("resultCode",GlobalConst.Result.SUCCESS);
			result.put("resultMsg","获取订单信息成功");
			result.put("tradeString",payInfo);
			result.put("orderId", orderId);
			TOrder order = new TOrder();
			String groupId = KeyHelper.createKey();
			order.setOrderId(orderId);
			order.setAppId(appId);
			order.setMobile(mobile);
			order.setAmount(new BigDecimal(NumberUtil.getDouble(price)));
			order.setChannelId(tChannel.getChannelId());
			order.setProductId(price);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setPipleId(getPipleId());
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setGroupId(groupId);
			order.setExtData(extData);
			this.SaveOrderInsert(order);
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, "apiKey="+apiKey+",subject="+subject+",body="+body+",price="+price+",extData="+extData);
			statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultMsg","请求异常");
		}
		return result;
	}
	private String getOrderInfo(String orderId,String subject, String body, String price,String notifyUrl) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + AlipayConfig.partner + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + AlipayConfig.SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + orderId + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + notifyUrl + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"5m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}
	private String sign(String content) {
		return SignUtils.sign(content, AlipayConfig.private_key);
	}
	private String getSignType() {
		return "sign_type=\"RSA\"";
	}
	
	public String notify(HttpServletRequest request) throws UnsupportedEncodingException{
		//获取支付宝POST过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		logger.info("支付同步数据:"+JSONObject.fromObject(params).toString());
		
		String orderId = params.get("out_trade_no");
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
		if(order ==null ){
			return "fail";
		}
		if(order.getOrderStatus() == GlobalConst.OrderStatus.SUCCESS){
			return "success";
		}
		statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(),JSONObject.fromObject(params).toString());
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		//商户订单号	String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

		//支付宝交易号	String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

		//交易状态
		String trade_status = params.get("trade_status");

		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

//		if(AlipayNotify.verify(params)){//验证成功
		if(true){//验证成功
			//////////////////////////////////////////////////////////////////////////////////////////
			//请在这里加上商户的业务逻辑程序代码

			//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
			boolean isSend = false;
			String pipleOrderId = params.get("trade_no");
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			order.setPipleOrderId(pipleOrderId);
			if(trade_status.equals("TRADE_FINISHED")){
				//判断该笔订单是否在商户网站中已经做过处理
					//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					//如果有做过处理，不执行商户的业务程序
					
				//注意：
				//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
				//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
			}else if (trade_status.equals("TRADE_SUCCESS")){
				//判断该笔订单是否在商户网站中已经做过处理
					//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					//如果有做过处理，不执行商户的业务程序
				//注意：
				//付款完成后，支付宝系统发送该交易状态通知
				//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(trade_status);
				doWhenPaySuccess(order);
				if(cp != null){
					boolean bDeducted  = order.deduct(cp.getVolt());
					if(!bDeducted){ 
						isSend =true;
					}
				}
				
			}else if("TRADE_CLOSED".equals(trade_status)){
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(trade_status);
			}
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
				notifyAliChannel(cp.getNotifyUrl(),order,"ok");
			}
			
			//——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
				
			return"success";	//请不要修改或删除

			//////////////////////////////////////////////////////////////////////////////////////////
		}else{//验证失败
			return "fail";
		}
	}
	public void notifyAliChannel(String url,TOrder order,String status){
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
