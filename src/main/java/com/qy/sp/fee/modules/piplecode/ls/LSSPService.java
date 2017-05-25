package com.qy.sp.fee.modules.piplecode.ls;

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
import com.qy.sp.fee.common.utils.MapCacheManager;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TOrderExtKey;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import com.qy.sp.fee.service.MobileSegmentService;

import net.sf.json.JSONObject;
@Service
public class LSSPService extends ChannelService{
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String PAY_SUCCESS = "0000";  // 扣费成功
	
	private  Logger log = Logger.getLogger(LSSPService.class);		
	@Override
	public String getPipleId() {
		return "14810939333460048050876";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("LSSPService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String ipProvince = requestBody.optString("ipProvince");
		String ua = requestBody.optString("ua");
		//String prov = requestBody.optString("prov");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		}else{
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setMobile(mobile);
			req.setIpProvince(ipProvince);
			// 调用合法性校验
			BaseResult bResult = this.accessVerify(req,pipleId);
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			}
			int provinceId = req.getProvinceId();
			String prov = getProv(provinceId);
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
			LSSPorder order = new LSSPorder();
			order.setProvinceId(req.getProvinceId());
			order.setUa(ua);
			order.setProv(prov);
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setImei(imei);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);
			if(requestBody.containsKey("fromType")){
				if(GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_SMS));
				}else if(GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_SDK));
				}else if(GlobalConst.FromType.FROM_TYPE_API.equals(fromType)){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
				}
			}else{
				order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
			}
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String param = pipleProduct.getPipleProductCode();  
				//请求短信验证码地址
				String reqUrl = piple.getPipleUrlA()+"?"+"param="+param+"&channel="+piple.getPipleAuthA()+"&imei="+imei+"&imsi="+imsi+"&ua="+ua+"&prov="+prov;
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
//				String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlA(), params, HttpClientUtils.UTF8);
				log.info(" LSSPService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject object = JSONObject.fromObject(pipleResult);
					String code = object.getString("code");
					
						if(RES_SUCCESS.equals(code)){
							String shortcode = object.optString("shortcode");
							String basecontent = object.optString("basecontent");
							String shortcode2 = object.optString("shortcode2");
							String basecontent2 = object.optString("basecontent2");
							String transactionid = object.optString("transactionid");
							String cooperid = object.optString("cooperid");
							order.setPipleOrderId(transactionid);
							order.setResultCode(code);
							order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							result.put("shortcode", shortcode);
							result.put("basecontent", basecontent);
							result.put("shortcode2", shortcode2);
							result.put("basecontent2", basecontent2);
							result.put("resultCode",GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功。");
						}else{
							String msg = object.getString("message");
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(code);
							result.put("resultCode",GlobalConst.Result.ERROR);
							result.put("resultMsg","请求错误："+msg);
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
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("LSSPService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String linkid = requestBody.optString("linkid");
		String mobile   = requestBody.optString("mobile");
		String port = requestBody.optString("port");
		String msg = requestBody.optString("msg");
		String status = requestBody.optString("status");
		String param = requestBody.optString("param");  // 透传订单号
		String ftime = requestBody.optString("ftime"); 
		TOrder order = tOrderDao.selectByPipleOrderId(linkid);
		order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
		if(order!=null && order.getOrderStatus()!=GlobalConst.OrderStatus.SUCCESS){ // 订单未同步过，成功同步去重处理
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			boolean bDeducted = false; // 扣量标识
			order.setPipleOrderId(linkid);
			order.setResultCode(status);
			if(PAY_SUCCESS.equals(status)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				if(!bDeducted){ // 不扣量 通知渠道
					TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
					notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
		}
		return "ok";
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	public class LSSPorder extends TOrder{
		private String ua;
		private String prov;
		
		
		public String getUa() {
			return ua;
		}


		public void setUa(String ua) {
			this.ua = ua;
		}


		public String getProv() {
			return prov;
		}


		public void setProv(String prov) {
			this.prov = prov;
		}


		


		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.ua != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("ua");
				oExt.setExtValue(this.ua);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.prov != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("prov");
				oExt.setExtValue(this.prov);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
	
			return tOrderExts;
		}
	}
	private String getProv(int provinceId){
		String prov = null;
		switch(provinceId){
			case 1:
				prov = "s10";
				break;
			case 2:
				prov = "s22";
				break;
			case 9:
				prov = "s21";
				break;
			case 22:
				prov = "s23";
				break;
			case 3:
				prov = "s311";
				break;
			case 14:
				prov = "s791";
				break;
			case 15:
				prov = "s531";
				break;
			case 4:
				prov = "s351";
				break;
			case 5:
				prov = "s471";
				break;
			case 16:
				prov = "s371";
				break;
			case 6:
				prov = "s24";
				break;
			case 17:
				prov = "s27";
				break;
			case 7:
				prov = "s431";
				break;
			case 18:
				prov = "s731";
				break;
			case 8:
				prov = "s451";
				break;
			case 19:
				prov = "s20";
				break;
			case 10:
				prov = "s25";
				break;
			case 20:
				prov = "s771";
				break;
			case 21:
				prov = "s898";
				break;
			case 23:
				prov = "s28";
				break;
			case 11:
				prov = "s571";
				break;
			case 24:
				prov = "s851";
				break;
			case 12:
				prov = "s551";
				break;
			case 25:
				prov = "s871";
				break;
			case 13:
				prov = "s591";
				break;
			case 27:
				prov = "s29";
				break;
			case 31:
				prov = "s991";
				break;
			case 30:
				prov = "s951";
				break;
			case 28:
				prov = "s931";
				break;
			case 29:
				prov = "s971";
				break;
			case 26:
				prov = "s891";
				break;
		}
		return prov;
	}
}
