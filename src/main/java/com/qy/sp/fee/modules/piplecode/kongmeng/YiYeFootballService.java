package com.qy.sp.fee.modules.piplecode.kongmeng;

import java.math.BigDecimal;
import java.util.HashMap;

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
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class YiYeFootballService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;

	@Override
	public String getPipleId() {
		return "14591308675936084686599";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		logger.info("addRdoSms params: productcode="+productCode+",apiKey="+apiKey+",mobile="+mobile);
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)  || StringUtil.isEmpty(mobile)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		}else{
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			
			req.setImsi(imsi);
			req.setProductCode(productCode);
			// 调用合法性校验
			BaseResult bResult = this.accessVerify(req,pipleId);
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			}
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			
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
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			String pipleUrl = "http://182.92.189.63/m.api?_mt=1001&cpid=LY0000&appid=300000007624&fee=%s&type=401&cpparm=%s&imsi=%s&imei=%s&fmt=json&ib=0";
//			String pipleUrl = "http://121.41.116.161/dm/sms?imsi=%s&imei=%s&sid=%s&cpparam=%s&chid=%s&os=19";
			String extData = DateTimeUtils.getFormatTime(DateTimeUtils.HHMMSS);
			pipleUrl = StringUtil.format(pipleUrl, pipleProduct.getPipleProductCode(),394+extData,imsi,imei);
//			pipleUrl = StringUtil.format(pipleUrl, imsi,imei,pipleProduct.getPipleProductCode(),394+extData,"lvyin");
			if(StringUtil.isEmpty(pipleUrl)){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求参数不能匹配");
				return result;
			}
			tOrderDao.insert(order);
			result.put("orderId",order.getOrderId());
			//调用HAO
			JSONObject resultObj = null;
			try{
				String pipleResult= HttpClientUtils.doPost(pipleUrl,new HashMap<String,String>(),HttpClientUtils.UTF8);
				resultObj = JSONObject.fromObject(pipleResult);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(resultObj != null){
					// 更新订单信息
				order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
				order.setSubStatus(GETCODE_SUCCESS);
				order.setResultCode(GlobalConst.Result.SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				String pipleOrderId = resultObj.optString("transId");
				order.setPipleOrderId(pipleOrderId);
				//设置返回结果
				result.put("resultCode",GlobalConst.Result.SUCCESS);
				result.put("resultMsg","请求成功");
				result.put("orderId",order.getOrderId());
				result.put("smsPort", resultObj.optString("initPort"));
				result.put("smsMessage",resultObj.optString("initSms"));
				SaveOrderUpdate(order);
			}else{
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败");
			}
		}
		return result;
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		String status = requestBody.optString("status");
		String transIDO = requestBody.optString("transIDO");
		String ConumeCode = requestBody.optString("ConumeCode");
		String amount = requestBody.optString("amount");
		String serviceCode = requestBody.optString("serviceCode");
		String phone = requestBody.optString("phone");
		String cParam = requestBody.optString("cParam");
		String pipleOrderId = transIDO;
		TOrder order = tOrderDao.selectByPipleOrderId(pipleOrderId);
		if(order!=null ){ // 同步数据正确
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
			if("0".equals(status)){
				order.setPipleOrderId(transIDO);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderUpdate(order);
			if(isSend){ // 不扣量 通知渠道
				notifyChannel(cp.getNotifyUrl(), order.getMobile(),order.getOrderId(), productCode, "ok",null);
			}
		}
		return "ok";
	}
}
