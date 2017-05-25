package com.qy.sp.fee.modules.piplecode.hs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MapCacheManager;
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
public class HSSJRdoService extends ChannelService{
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	public final static String PAY_SUCCESS = "0000";  // 扣费成功
	public final static String SYNC_SUCCESS = "DELIVRD";  // 同步交易成功状态
	private  Logger log = Logger.getLogger(HSSJRdoService.class);		
	@Override
	public String getPipleId() {
		return "14731438084385780523959";
	}
	
	@Override
	public String getPipleKey() {
		return "PM1026";
	}
	
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("HSSJRdoService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
//		String imei = requestBody.optString("imei");
//		String iccid = requestBody.optString("iccid");
//		String ip = requestBody.optString("ip");
		String extData = requestBody.optString("extData");   //  imsi$extdata
		if(extData.indexOf("&")> -1){ // 拓展字段包含$则为短信方式接口
			String[] extd = extData.split("&");
			imsi = extd[0];
			extData = extd[1];
		}
			
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)   || StringUtil.isEmpty(pipleId)
				|| StringUtil.isEmpty(mobile)){
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
			HSSJRdoOrder order = new HSSJRdoOrder();
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
			order.setExtData(extData);
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				String param = pipleProduct.getPipleProductCode()+order.getOrderId();  // 计费代码（5位）+透传参数（订单号）
				//1.获取请求报文
				String reqUrl = piple.getPipleUrlA()+"?"+"channel="+piple.getPipleAuthA()+"&imsi="+imsi+"&param="+param;
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				log.info(" HSSJRdoService getPageResult:"+  pipleResult+",orderId="+order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				Document document1 = XMLUtils.getDocumentByStr(pipleResult);
				if(StringUtil.isNotEmptyString(pipleResult) && document1!=null){
					Element root1 = XMLUtils.getRootNode(document1);
					Element e = (Element) root1.selectSingleNode("/Response/ResultCode");
					String resultCode = e.getText();
					result.put("resultCode", resultCode);
					if(resultCode.equals(RES_SUCCESS)){ // 请求成功
						Element e1 = (Element) root1.selectSingleNode("/Response/Order/Submit0/GetSMSVerifyCodeUrl"); 
						String getSmsUrl = e1.getText();
						order.setGetSMSVerifyCodeUrl(getSmsUrl);
						// 2.获取验证码
						String url_B = getSmsUrl+"&msisdn="+order.getMobile();
						String getCodeRst = HttpClientUtils.doGet(url_B, "utf-8");
						log.info("HSSJRdoService getSmsXML：getCodeRst="+ getCodeRst+",orderId="+order.getOrderId());
						Document document2 = XMLUtils.getDocumentByStr(getCodeRst);
						Element root2 = XMLUtils.getRootNode(document2);
						Element e2 = (Element) root2.selectSingleNode("/Response/ResultCode");
						String resultCode2 = e2.getText();
						result.put("resultCode", resultCode2);
						if(resultCode2.equals(RES_SUCCESS)){ // 请求成功
							Element e3 = (Element) root2.selectSingleNode("/Response/Order/Submit0/ButtonTag/SubmitUrl"); 
							String submitUrl = e3.getText().replaceAll("amp;", "");
							order.setSubmitUrl(submitUrl);
							order.setModTime(DateTimeUtils.getCurrentTime());
						    order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						    order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							result.put("resultCode", GlobalConst.Result.SUCCESS);
							result.put("resultMsg","请求成功");
						}else{
							Element e3 = (Element) root2.selectSingleNode("/Response/ResultMsg"); 
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(e3.getText());
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+e3.getText());
						}
					}else{
						Element e4 = (Element) root1.selectSingleNode("/Response/ResultMsg"); 
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode(e4.getText());
						order.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+e4.getText());
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
		log.info("HSSJRdoService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
			
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
					HSSJRdoOrder newOrder = new HSSJRdoOrder();
					newOrder.setTOrder(tOrder);
					newOrder.setVerifyCode(verifyCode);
					
//					TPipleProductKey ppkey = new TPipleProductKey();
//					ppkey.setPipleId(getPipleId());
//					ppkey.setProductId(tProduct.getProductId());
					TOrderExtKey oExtKey = new TOrderExtKey();
					oExtKey.setOrderId(orderId);
					oExtKey.setExtKey("submitUrl");
					TOrderExt urlExt = tOrderExtDao.selectByPrimaryKey(oExtKey);
					String submitUrl = urlExt.getExtValue();
					Map<String, String> params = new HashMap<String, String>();
					params.put("verifyCode",verifyCode);
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), submitUrl+";"+params.toString());
					// 3.提交验证码
					String pipleResult = HttpClientUtils.doPost(submitUrl, params, HttpClientUtils.UTF8); 
					log.info(" HSSJRdoService confirmResult:"+  pipleResult+",orderId="+newOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						String payResult = HttpClientUtils.doGet(pipleResult, HttpClientUtils.UTF8);
						log.info(" HSSJRdoService payResult:"+  payResult+",orderId="+newOrder.getOrderId());
						if(!StringUtil.isEmpty(payResult)){
							Document document = XMLUtils.getDocumentByStr(payResult);
							Element root = XMLUtils.getRootNode(document);
							Element rcEle = (Element) root.selectSingleNode("/Response/ResultCode");
							Element rmEle = (Element) root.selectSingleNode("/Response/ResultMsg"); 
							String resultCode = rcEle==null?"":rcEle.getText();
							String resultMsg = rmEle==null?"":rmEle.getText();
							if(resultCode.equals(RES_SUCCESS)){
								newOrder.setResultCode(resultCode);
								newOrder.setModTime(DateTimeUtils.getCurrentTime());
								newOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
								newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。");
							}else{
								newOrder.setResultCode(resultCode);
								newOrder.setModTime(DateTimeUtils.getCurrentTime());
								newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
								newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败："+resultMsg);
							}
						}else{
							newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
							newOrder.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode",GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败，接口异常");
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
	
	/**
	 *	双短方式 
	 * */
	@Override
	public String processGetMessage(String mobile,String requestBody) throws Exception {
		String resultMsg = "";
		String args[] = requestBody.split("\\$");
		String apiKey = args[0];
		String productCode = args[1];
		String extData = null;
		if(args.length >2){
			extData = args[2];
		}
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("productCode",productCode);
		request.put("mobile",mobile);
		request.put("extData",extData);
		JSONObject result = processGetSMS(request);
		if(result != null){
			if("0".equals(result.optString("resultCode"))){
				JSONObject param = new JSONObject();
				param.put("orderId",result.optString("orderId") );
				param.put("apiKey", apiKey);
				param.put("pipleKey", getPipleKey());
				param.put("productCode", productCode);
				MapCacheManager.getInstance().getSmsOrderCache().put(mobile,param.toString());
			}
			if(StringUtil.isNotEmptyString(result.optString("orderId"))){
				TOrder tOrder = tOrderDao.selectByPrimaryKey(result.optString("orderId"));
				if(tOrder != null){
					statistics(STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"1$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
				}
			}
			logger.debug(JSONObject.fromObject(result).toString());
		}
		return resultMsg;
	}
	
	@Override
	public String processSubmitMessage(String mobile,String requestBody) throws Exception {
		String jsonStr = MapCacheManager.getInstance().getSmsOrderCache().get(mobile);
		JSONObject param = JSONObject.fromObject(jsonStr);
		String orderId = param.optString("orderId");
		if(StringUtil.isEmpty(orderId)){
			return "";
		}
		TOrder tOrder = tOrderDao.selectByPrimaryKey(orderId);
		if(tOrder == null)
			return "";
		statistics(STEP_SUBMIT_MESSAGE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody);
		String args[] = requestBody.split("\\$");
		if(args.length <2)
			return "";
		String apiKey = args[0];
		String verifyCode = args[1];
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getPipleId());
		request.put("orderId",tOrder.getOrderId());
		request.put("verifyCode",verifyCode);
		JSONObject result = processVertifySMS(request);
		MapCacheManager.getInstance().getSmsOrderCache().remove(mobile);
		if(result != null){
			logger.debug(JSONObject.fromObject(result).toString());
			statistics(STEP_SUBMIT_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
		}
		return "";
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("HSSJRdoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String linkid = requestBody.optString("linkid");
		String mobile   = requestBody.optString("mobile");
		String port = requestBody.optString("port");
		String msg = requestBody.optString("msg");
		String status = requestBody.optString("status");
		String param = requestBody.optString("param");  // 透传参数
		String ftime = requestBody.optString("ftime"); 
		String orderId = param.substring(param.length()-23, param.length());   //  透传参数后23位
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
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
					
//					n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
					notifyChannelSMS(cp.getNotifyUrl(), order,port,SYNC_SUCCESS);
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
	public class HSSJRdoOrder extends TOrder{
		
		private String getSMSVerifyCodeUrl ;  	// 获取验证码URL
		private String submitUrl;  	// 提交验证码URL
		private String verifyCode; 			// 验证码
		

		public String getGetSMSVerifyCodeUrl() {
			return getSMSVerifyCodeUrl;
		}


		public void setGetSMSVerifyCodeUrl(String getSMSVerifyCodeUrl) {
			this.getSMSVerifyCodeUrl = getSMSVerifyCodeUrl;
		}


		public String getSubmitUrl() {
			return submitUrl;
		}


		public void setSubmitUrl(String submitUrl) {
			this.submitUrl = submitUrl;
		}


		public String getVerifyCode() {
			return verifyCode;
		}


		public void setVerifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
		}


		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.getSMSVerifyCodeUrl != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("getSMSVerifyCodeUrl");
				oExt.setExtValue(this.getSMSVerifyCodeUrl);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.submitUrl != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("submitUrl");
				oExt.setExtValue(this.submitUrl);
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
