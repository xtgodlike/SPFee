package com.qy.sp.fee.modules.piplecode.jm;

import com.qy.sp.fee.common.utils.*;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JMADMService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String P_SUCCESS = "0";	  	// 计费成功
	public final static String OT_P_SUCCESS = "1";	// 超时计费成功
	private  Logger log = Logger.getLogger(JMADMService.class);
	@Override
	public String getPipleId() {
		return "15096166741349528197460";
	}

	@Override
	public String getPipleKey() {
		return "PM1089";
	}


	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("JMADMService 支付同步数据:"+requestBody);
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return "requestBody is null";
		}
		String requestData = requestBody.optString("requestData");
		Document document = XMLUtils.getDocumentByStr(requestData);
		Element root = XMLUtils.getRootNode(document);
		String sign =  root.selectSingleNode("/RequestBody/Sign").getText();
		String behavior =  root.selectSingleNode("/RequestBody/Behavior").getText();		// 行为 (订购 order 退订 unsubscribe)
		String tradeStatus =  root.selectSingleNode("/RequestBody/Trade_status").getText();	// 交易状态(0-计费成功；1-超时默认计费成功)
		String tradeNo =  root.selectSingleNode("/RequestBody/Trade_no").getText();			// 交易流水号
		String buyerId =  root.selectSingleNode("/RequestBody/Buyer_id").getText();			// 用户号码
		String productId =  root.selectSingleNode("/RequestBody/Product_id").getText();		// 产品ID
		String productName =  root.selectSingleNode("/RequestBody/Product_name").getText();	// 产品名称
		String price =  root.selectSingleNode("/RequestBody/Price").getText();				// 产品价格，元
		String appId =  root.selectSingleNode("/RequestBody/App_id").getText();				// 应用ID
		String extension =  root.selectSingleNode("/RequestBody/Extension").getText();		// 拓展字段 透传实际的短信内容
		if(StringUtil.isEmpty(tradeNo)){
			return "Trade_no is empty";
		}
		if(StringUtil.isEmpty(extension)){
			return "Extension is empty";
		}
		TOrder nOrder = tOrderDao.selectByPipleOrderId(tradeNo);
		if(nOrder==null ){ // 未同步过
			try {
				String groupId = KeyHelper.createKey();
				statistics(STEP_PAY_BASE_TO_PLATFORM, groupId, requestBody.toString());
				// 解析指令数据 131000TF000001B002pf001211003xxxx  131000TF000001B002pf00121{apiKey}{extData}
				String pipleProductCode = "";
				String apiKey = "";
				if(extension.length()>24){
					pipleProductCode = extension.substring(0,25);
				}
				if(extension.length()>28){
					apiKey = extension.substring(25,29);
				}
				if(StringUtil.isEmpty(pipleProductCode) || StringUtil.isEmpty(apiKey)){
					return "Extension is error";
				}
				logger.info("JMADMService pipleProductCode="+pipleProductCode);
				logger.info("JMADMService apiKey="+apiKey);
				TPipleProduct ppKey = new TPipleProduct();
				ppKey.setPipleId(getPipleId());
				ppKey.setPipleProductCode(pipleProductCode);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppKey);
				TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
				TChannel channel = tChannelDao.selectByApiKey(apiKey);
				if(pipleProduct==null){
					return "pipleProduct is null";
				}
				if(channel==null){
					return "channel is null";
				}
				JMADMOrder order = new JMADMOrder();
				order.setOrderId(KeyHelper.createKey());
				order.setCreateTime(DateTimeUtils.getCurrentTime());
				order.setGroupId(groupId);
				order.setMobile(buyerId);
				order.setPipleId(getPipleId());
				order.setChannelId(channel.getChannelId());
				order.setProductId(product.getProductId());
				order.setPipleOrderId(tradeNo);
				if(!StringUtil.isEmpty(buyerId)){
					order.setProvinceId(getProvinceIdByMobile(buyerId, false));
				}
				order.setAmount(new BigDecimal(product.getProductId()));
				order.setExtData(extension);
				order.setProductId(productId);
				order.setProductName(productName);
				order.setPrice(price);
				order.setAppId(appId);
				TChannelPipleKey key = new TChannelPipleKey();
				key.setChannelId(channel.getChannelId());
				key.setPipleId(getPipleId());
				TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(key);
				//扣量
				boolean bDeducted = false;
				if("order".equals(behavior)){ // 是订购行为    退订行为不考虑
					if(P_SUCCESS.equals(tradeStatus) || OT_P_SUCCESS.equals(tradeStatus)){
						order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
						order.setSubStatus(PAY_SUCCESS);
						order.setModTime(DateTimeUtils.getCurrentTime());
						order.setCompleteTime(DateTimeUtils.getCurrentTime());
						order.setResultCode(tradeStatus);
						doWhenPaySuccess(order);
						bDeducted  = order.deduct(cp.getVolt());  // 是否扣量
						if(!bDeducted){ // 不扣量 通知渠道
							notifyChannelAPIForKey(cp.getNotifyUrl(),order,"ok");
						}
					}else {
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
						order.setModTime(DateTimeUtils.getCurrentTime());
						order.setResultCode(tradeStatus);
					}
					SaveOrderUpdate(order);
				}
				// 应答串

				String responseXml = "<ResponseBody>" +
						"<Status>"+"0"+"</Status>" +
						"<Trade_no>"+tradeNo+"</Trade_no>" +
						"</ResponseBody>";
				return responseXml;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("JMADMService 同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order Synchronized";
		}
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}

	public class JMADMOrder extends TOrder{
		private String productId;

		private String productName;

		private String price;

		private String appId;

		@Override
		public String getProductId() {
			return productId;
		}

		@Override
		public void setProductId(String productId) {
			this.productId = productId;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public String getPrice() {
			return price;
		}

		public void setPrice(String price) {
			this.price = price;
		}

		@Override
		public String getAppId() {
			return appId;
		}

		@Override
		public void setAppId(String appId) {
			this.appId = appId;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.productId != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("productId");
				oExt.setExtValue(this.productId);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.productName != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("productName");
				oExt.setExtValue(this.productName);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.price != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("price");
				oExt.setExtValue(this.price);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.appId != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("appId");
				oExt.setExtValue(this.appId);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}

			return tOrderExts;
		}
	}

}
