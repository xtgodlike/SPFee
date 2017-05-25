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
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
@Service
public class VideoService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	private long currentTime = System.currentTimeMillis();
	@Override
	public String getPipleId() {
		return "14603673704623627130605";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		//要返回给渠道的参数放在这个变量里面
		JSONObject result = new JSONObject();
		//获取渠道请求的参数
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String ip = requestBody.optString("ip");
		String iccid = requestBody.optString("iccid");
		String ua = requestBody.optString("ua");
		String video_ua =requestBody.optString("video_ua");
		String hwId = requestBody.optString("hwId");
		logger.info("addRdoSms params: productcode="+productCode+",apiKey="+apiKey+",mobile="+mobile);
		//判断产品代码，apiKey，apiPassword是否为空。
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)  || StringUtil.isEmpty(mobile)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		}else{
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			
			req.setImsi(imsi);
			req.setProductCode(productCode);
			//判断产品代码，apiKey，apiPassword是否合法。
			BaseResult bResult = this.accessVerify(req,pipleId);
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			}
			//获取渠道信息
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			//获取产品信息
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			//获取通道产品信息
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			//创建订单
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
			order.setIccid(iccid);
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			String pipleUrl = "http://115.159.74.129:8000/o/mgreqapi/6a4fa600b282df8274bcb60dfae31b40609ced67/?imei=%s&imsi=%s&node_id=%s&product_id=%s&content_id=%s&item_price=%s&cpparam=%s";
			if(!StringUtil.isEmpty(ua)){
				pipleUrl += "&ua="+ua;
			}
			if(!StringUtil.isEmpty(video_ua)){
				pipleUrl += "&video_ua="+video_ua;
			}
			pipleUrl+= "&start_time=%s&huawei_id=%s&channel_id=%s&public_key=%s&video_app_id=%s&video_app_key=%s&iccid=%s&ip=%s";
			String node_id = "";
			node_id = "10535082";
			String product_id = "2028595110";
			String content_id = "600063";
			String item_price = pipleProduct.getPipleProductCode();
			String channel_id ="201000100000000";
			String public_key = "079c0b1517fb9e1eb4865005dff894a0";
			String video_app_id = "43d9e04917a7a27505506933bfa4b1b0";
			String video_app_key = "2dd9890977a48c2e74d1bc81f3c05b45";
			String cpparam = order.getOrderId();
			pipleUrl = StringUtil.format(pipleUrl,imei,imsi,node_id,product_id,content_id,item_price,cpparam,currentTime,hwId,channel_id,public_key,video_app_id,video_app_key,iccid,ip);
			if(StringUtil.isEmpty(pipleUrl)){
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求参数不能匹配");
				return result;
			}
			tOrderDao.insert(order);
			result.put("orderId",order.getOrderId());
			logger.info("Request Piple Url:"+pipleUrl);
			//请求通道获取短信指令
			String pipleResult= HttpClientUtils.doGet(pipleUrl,HttpClientUtils.UTF8);
			if(pipleResult != null){
				//获取短信指令后拼接代码返回给渠道。
				JSONObject jsonObject = null;
				try{
					XMLSerializer xmlSerializer = new XMLSerializer();
					jsonObject = (JSONObject) xmlSerializer.read(pipleResult);
				}catch(Exception e){
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求错误");
					return result;
				}
				JSONObject videoObject = jsonObject.optJSONObject("video");
				JSONObject miguObject = jsonObject.optJSONObject("migu");
				if(videoObject == null || miguObject == null){
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求错误");
					return result;
				}
				String videoSMSNumber = videoObject.optString("sms_num");
				String videoSMS = videoObject.optString("sms");
				String miguSMSNumber = miguObject.optString("sms_num");
				String miguSMS = miguObject.optString("sms");
				order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
				order.setSubStatus(GETCODE_SUCCESS);
				order.setResultCode(GlobalConst.Result.SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				//设置短信结果
				result.put("resultCode",GlobalConst.Result.SUCCESS);
				result.put("resultMsg","请求成功");
				result.put("orderId",order.getOrderId());
				result.put("smsNumber1",videoSMSNumber);
				result.put("sms1",videoSMS);
				result.put("smsNumber2",miguSMSNumber);
				result.put("sms2",miguSMS);
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
		//获取通道同步给我们的数据
		String status = requestBody.optString("status");
		String amount = requestBody.optString("price");
		String phoneNumber = requestBody.optString("phone_number");
		String transId = requestBody.optString("trans_id");
		String cpparam = requestBody.optString("cpparam");
		//获取订单信息
		TOrder order = tOrderDao.selectByPrimaryKey(cpparam);
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
			//获取产品信息
			TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
			String productCode = tProduct.getProductCode();
			//判断是否扣量
			boolean isSend = false;
			if("0".equals(status)){
				order.setPipleOrderId(transId);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setMobile(phoneNumber);
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
			if(isSend){ // 不扣量 时通知渠道
				notifyChannel(cp.getNotifyUrl(), order.getMobile(),order.getOrderId(), productCode, "ok",null);
			}
		}
		return "ok";
	}
}
