package com.qy.sp.fee.modules.piplecode.kongmeng;

import java.math.BigDecimal;

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
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class PaoPaoLongService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	@Override
	public String getPipleId() {
		return "14606234070959404097244";
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
		String ip = requestBody.optString("ip");
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
			//保存订单
			TOrder order = new TOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setPipleId(pipleId);
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setChannelId(tChannel.getChannelId());
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(INIT);
			order.setSubStatus(INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setPipleOrderId(System.currentTimeMillis()+"");
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			String pipleUrl = "http://114.80.201.9:8089/xh157_wo0414.php?channel=hongyun&imsi=%s&imei=%s&param=6414102%s&ip=%s";
			pipleUrl = StringUtil.format(pipleUrl,imsi,imei,order.getPipleOrderId(),ip);
			if(StringUtil.isEmpty(pipleUrl)){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求参数不能匹配");
				return result;
			}
			tOrderDao.insert(order);
			//调用HAO
			logger.info("Request Piple Url:"+pipleUrl);
			String pipleResult= HttpClientUtils.doGet(pipleUrl,HttpClientUtils.UTF8);
			if(pipleResult != null){								
					// 更新订单信息
				JSONObject jsonObject = null;
				try{
					jsonObject =JSONObject.fromObject(pipleResult);
				}catch(Exception e){
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求错误");
					return result;
				}
				String status = jsonObject.optString("error");
				if("0".equals(status)){
					String num1 = jsonObject.optString("num1");
					String content1 = jsonObject.optString("content1");
					String num2 = jsonObject.optString("num2");
					String content2 = jsonObject.optString("content2");
					order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
					order.setSubStatus(GETCODE_SUCCESS);
					order.setResultCode(status);
					order.setModTime(DateTimeUtils.getCurrentTime());
					//设置返回结果
					result.put("orderId",order.getOrderId());
					result.put("resultCode",GlobalConst.Result.SUCCESS);
					result.put("resultMsg","请求成功");
					result.put("orderId",order.getOrderId());
					result.put("num1",num1);
					result.put("content1",content1);
					result.put("num2",num2);
					result.put("content2",content2);
				}else{
					order.setResultCode(status);
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg",status);
				}
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
		String pipleOrderId = requestBody.optString("linkid");
		String mo = requestBody.optString("mo");
		TOrder order = tOrderDao.selectByPipleOrderId(mo);
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
			if("DELIVRD".equals(status)){
				order.setPipleOrderId(pipleOrderId);
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
		return "0";
	}
}
