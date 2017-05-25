package com.qy.sp.fee.modules.piplecode.kongmeng;

import java.math.BigDecimal;

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
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
@Service
public class ShiJiDongManService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	private  Logger log = Logger.getLogger(ShiJiDongManService.class);		
	@Override
	public String getPipleId() {
		return "14624344227957916493355";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("ShiJiDongManService requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String groupId = KeyHelper.createKey();
		statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey) ){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
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
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			}
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			//保存订单
			TOrder order = new TOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setMobile(mobile);
			order.setChannelId(tChannel.getChannelId());
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(INIT);
			order.setSubStatus(INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setImsi(imsi);
			order.setGroupId(groupId);
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			tOrderDao.insert(order);
			String pipleUrl = "http://121.52.208.188:3001/dmappsdk";
			String document = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><request><gameId>%s</gameId><imei>%s</imei><imsi>%s</imsi><extData>%s</extData></request>";
			document = String.format(document,pipleProduct.getPipleProductCode(),imei,imsi,order.getOrderId());
			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,pipleUrl+";"+document);
			String pipleResult= HttpClientUtils.doPost(pipleUrl,document,HttpClientUtils.UTF8);
			statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId, pipleResult);
			if(pipleResult != null){	
				XMLSerializer xmlSerializer = new XMLSerializer();
				xmlSerializer.setExpandableProperties(new String[]{"response"});
				JSONObject jsonObject = (JSONObject) xmlSerializer.read(pipleResult);
				String state = jsonObject.optString("state");
				String spNumber = jsonObject.optString("spNumber");
				String moContent = jsonObject.optString("moContent");
				String linkId = jsonObject.optString("linkId");
				if("0".equals(state)){
					result.put("resultCode",GlobalConst.Result.SUCCESS);
					result.put("resultMsg","请求成功");
					result.put("orderId",order.getOrderId());
					result.put("moContent", moContent);
					result.put("spNumber", spNumber);
					order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
					order.setSubStatus(GETCODE_SUCCESS);
					order.setResultCode(state);
					order.setPipleOrderId(linkId);
					order.setModTime(DateTimeUtils.getCurrentTime());
				}else{
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败");
					order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
					order.setSubStatus(GETCODE_FAIL);
					order.setResultCode(state);
					order.setModTime(DateTimeUtils.getCurrentTime());
				}
				SaveOrderUpdate(order);
			}else{
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败");
			}
		}
		statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
		return result;
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		
		String error = "error";
		if(requestBody == null)
			return error;
		String RptStat = requestBody.optString("RptStat");
		String ExtData = requestBody.optString("ExtData");
		String LinkID = requestBody.optString("LinkID");
		String Mobile = requestBody.optString("Mobile");
		TOrder order = tOrderDao.selectByPrimaryKey(ExtData);
		if(order!=null ){ // 同步数据正确
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			String productId = order.getProductId();
			order.setAmount(new BigDecimal(productId));
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			//扣量
			boolean isSend = false;
			if("DELIVRD".equals(RptStat)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setPipleOrderId(LinkID);
				order.setResultCode(RptStat);
				order.setMobile(Mobile);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(PAY_FAIL);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setPipleOrderId(LinkID);
				order.setResultCode(RptStat);
				order.setMobile(Mobile);
			}
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
				notifyChannel(cp.getNotifyUrl(),order, productCode,"ok");
			}
		}
		return "ok";
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
}
