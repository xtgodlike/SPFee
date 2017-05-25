package com.qy.sp.fee.modules.piplecode.hs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.common.utils.XMLUtils;
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

import net.sf.json.JSONObject;
@Service
public class HSTYRdoService extends ChannelService{
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String PAY_SUCCESS = "0000";  // 扣费成功
	private  Logger log = Logger.getLogger(HSTYRdoService.class);		
	@Override
	public String getPipleId() {
		return "14778824250714011226744";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("HSTYRdoService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String appName = requestBody.optString("appName");
		String chargeName = requestBody.optString("chargeName");
//		String imei = requestBody.optString("imei");
//		String iccid = requestBody.optString("iccid");
//		String ip = requestBody.optString("ip");
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
			HSTYRdoOrder order = new HSTYRdoOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			order.setAppName(appName);
			order.setChargeName(chargeName);
			order.setExtData(extData);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String param = pipleProduct.getPipleProductCode()+order.getOrderId();  // 计费代码（7位）+透传参数（订单号）
				//请求短信验证码地址
//				Map<String, String> params = new HashMap<String, String>();
//				params.put("channel", piple.getPipleAuthA());
//				params.put("imsi", order.getImsi());
//				params.put("mobile", order.getMobile());
//				params.put("param", param);
				String reqUrl = piple.getPipleUrlA()+"?"+"channel="+piple.getPipleAuthA()+"&mobile="+order.getMobile()+"&param="+param
						+"&appName="+StringUtil.urlEncodeWithUtf8(appName)+"&chargeName="+StringUtil.urlEncodeWithUtf8(chargeName);
//				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, piple.getPipleUrlA()+";"+params.toString());
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
//				String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlA(), params, HttpClientUtils.UTF8);
				log.info(" HSTYRdoService pipleResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					Document document1 = XMLUtils.getDocumentByStr(pipleResult);
					Element root1 = XMLUtils.getRootNode(document1);
					Element e1 = (Element) root1.selectSingleNode("/root/RetCode");
					if(e1!=null){
						String retCode = e1.getText();
						Element e2 = (Element) root1.selectSingleNode("/root/RetMsg");
						String retMsg = e2.getText();
						if(retCode.equals(RES_SUCCESS)){ // 请求成功
							 Element e3 = (Element) root1.selectSingleNode("/root/OrderId");
							 String pipleOrderId = e3.getText();
							 order.setPipleOrderId(pipleOrderId);
							 order.setResultCode(retCode);
							 order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
							}else{
								order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
								order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
								order.setResultCode(retCode);
								order.setModTime(DateTimeUtils.getCurrentTime());
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+retMsg);
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
		log.info("HSTYRdoService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			
			String orderId = requestBody.optString("orderId");
			String vCode = requestBody.optString("vCode");
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
					HSTYRdoOrder newOrder = new HSTYRdoOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setvCode(vCode);
					
					TPipleProductKey ppkey = new TPipleProductKey();
					ppkey.setPipleId(getPipleId());
					ppkey.setProductId(tProduct.getProductId());
					TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
					TPiple piple = tPipleDao.selectByPrimaryKey(getPipleId());
					//提交验证码
					TOrderExtKey oExtKey = new TOrderExtKey();
					oExtKey.setOrderId(orderId);
					oExtKey.setExtKey("chargeName");
					TOrderExt ext = tOrderExtDao.selectByPrimaryKey(oExtKey);
					String chargeName = ext.getExtValue();
					String param = pipleProduct.getPipleProductCode()+tOrder.getOrderId();  // 计费代码（7位）+透传参数（订单号）
					String reqUrl = piple.getPipleUrlB()+"?"+"channel="+piple.getPipleAuthA()+"&orderId="+tOrder.getPipleOrderId()+"&param="+param
							+"&vcode="+vCode+"&chargeName="+StringUtil.urlEncodeWithUtf8(chargeName);
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), reqUrl);
//					String confirmResult= HttpClientUtils.doGet(payUrl, HttpClientUtils.UTF8);
					String confirmResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
					log.info(" HSTYRdoService confirmResult:"+  confirmResult+",orderId="+newOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), confirmResult);
					if(confirmResult != null && !"".equals(confirmResult)){
						Document document1 = XMLUtils.getDocumentByStr(confirmResult);
						Element root1 = XMLUtils.getRootNode(document1);
						Element e1 = (Element) root1.selectSingleNode("/root/RetCode");
						if(e1!=null){
							String retCode = e1.getText();
							Element e2 = (Element) root1.selectSingleNode("/root/RetMsg");
							String retMsg = e2.getText();
							if(retCode.equals(RES_SUCCESS)){ // 请求成功
								 newOrder.setResultCode(retCode);
								 newOrder.setModTime(DateTimeUtils.getCurrentTime());
								 newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
								 newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。");
								}else{
									newOrder.setResultCode(retCode);
									newOrder.setModTime(DateTimeUtils.getCurrentTime());
									newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
									newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
									
									result.put("resultCode", GlobalConst.Result.ERROR);
									result.put("resultMsg","请求失败:"+retMsg);
							}
						}else{
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+confirmResult);
						}
					}else{
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
					}
					SaveOrderUpdate(newOrder);
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
	
	
//	@Override
//	public String getPipleKey() {
//		return "PM1046";
//	}
//	
//	/**
//	 *	双短方式 
//	 * */
//	@Override
//	public String processGetMessage(String mobile,String requestBody) throws Exception {
//		String resultMsg = "";
//		String args[] = requestBody.split("\\$");
//		String apiKey = args[0];
//		String productCode = args[1];
//		String extData = null;
//		if(args.length >2){
//			extData = args[2];
//		}
//		TChannel channel = tChannelDao.selectByApiKey(apiKey);
//		if(channel == null)
//			return "";
//		JSONObject request = new JSONObject();
//		request.put("apiKey",channel.getApiKey());
//		request.put("apiPwd",channel.getApiPwd());
//		request.put("pipleId",getPipleId());
//		request.put("productCode",productCode);
//		request.put("mobile",mobile);
//		request.put("extData",extData);
//		JSONObject result = processGetSMS(request);
//		if(result != null){
//			if("0".equals(result.optString("resultCode"))){
//				JSONObject param = new JSONObject();
//				param.put("orderId",result.optString("orderId") );
//				param.put("apiKey", apiKey);
//				param.put("pipleKey", getPipleKey());
//				param.put("productCode", productCode);
//				MapCacheManager.getInstance().getSmsOrderCache().put(mobile,param.toString());
//			}
//			if(StringUtil.isNotEmptyString(result.optString("orderId"))){
//				TOrder tOrder = tOrderDao.selectByPrimaryKey(result.optString("orderId"));
//				if(tOrder != null){
//					statistics(STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"1$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
//				}
//			}
//			logger.debug(JSONObject.fromObject(result).toString());
//		}
//		return resultMsg;
//	}
//	
//	@Override
//	public String processSubmitMessage(String mobile,String requestBody) throws Exception {
//		String jsonStr = MapCacheManager.getInstance().getSmsOrderCache().get(mobile);
//		JSONObject param = JSONObject.fromObject(jsonStr);
//		String orderId = param.optString("orderId");
//		if(StringUtil.isEmpty(orderId)){
//			return "";
//		}
//		TOrder tOrder = tOrderDao.selectByPrimaryKey(orderId);
//		if(tOrder == null)
//			return "";
//		statistics(STEP_SUBMIT_MESSAGE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody);
//		String args[] = requestBody.split("\\$");
//		if(args.length <2)
//			return "";
//		String apiKey = args[0];
//		String vcode = args[1];
//		TChannel channel = tChannelDao.selectByApiKey(apiKey);
//		if(channel == null)
//			return "";
//		JSONObject request = new JSONObject();
//		request.put("apiKey",channel.getApiKey());
//		request.put("apiPwd",channel.getApiPwd());
//		request.put("pipleId",getPipleId());
//		request.put("orderId",tOrder.getOrderId());
//		request.put("vCode",vcode);
//		JSONObject result = processVertifySMS(request);
//		MapCacheManager.getInstance().getSmsOrderCache().remove(mobile);
//		if(result != null){
//			logger.debug(JSONObject.fromObject(result).toString());
//			statistics(STEP_SUBMIT_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
//		}
//		return "";
//	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("HSTYRdoService 支付同步数据:"+requestBody);
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
//		String orderId = param.substring(7, param.length());   //  前7位为固定参数,订单号从第8位开始
		TOrder order = tOrderDao.selectByPipleOrderId(linkid);
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
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				if(!bDeducted){ // 不扣量 通知渠道
					TProduct tProduct = this.tProductDao.selectByPrimaryKey(order.getProductId());
					String productCode = tProduct.getProductCode();
//					n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
					notifyChannel(cp.getNotifyUrl(), order, productCode, "ok");
				}
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
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
	public class HSTYRdoOrder extends TOrder{

		private String appName;
		private String chargeName;
		private String vCode;
		
		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public String getChargeName() {
			return chargeName;
		}

		public void setChargeName(String chargeName) {
			this.chargeName = chargeName;
		}

		public String getvCode() {
			return vCode;
		}

		public void setvCode(String vCode) {
			this.vCode = vCode;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.appName != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("appName");
				oExt.setExtValue(this.appName);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.chargeName != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("chargeName");
				oExt.setExtValue(this.chargeName);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.vCode != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("vCode");
				oExt.setExtValue(this.vCode);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
