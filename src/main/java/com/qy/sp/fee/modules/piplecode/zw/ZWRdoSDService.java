package com.qy.sp.fee.modules.piplecode.zw;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class ZWRdoSDService extends ChannelService{
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	private  Logger log = Logger.getLogger(ZWRdoSDService.class);		
	@Override
	public String getPipleId() {
		return "14646868316619631347862";
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("ZWRdoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String ppid = requestBody.optString("ppid");
		String imsi = requestBody.optString("imsi");
		String msisdn = requestBody.optString("msisdn");
		int price = requestBody.optInt("price");
		String time = requestBody.optString("time");
		String orderid = requestBody.optString("orderid");
		String custom = requestBody.optString("custom");
		TOrder lorder = tOrderDao.selectByPipleOrderId(orderid); 
		if(lorder==null ){ // 数据未同步
			String apiKey = custom.substring(0, 4); // 透传参数前4位
			String extData = custom.substring(4, custom.length()); // 渠道透传参数
			TChannel channel = tChannelDao.selectByApiKey(apiKey);
			TProduct product = tProductDao.selectByPrice(price);
			//保存订单
			ZWRdoOrder order = new ZWRdoOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(KeyHelper.createKey());
			order.setPipleId(getPipleId());
			order.setChannelId(channel.getChannelId());
			order.setMobile(msisdn);
			order.setImsi(imsi);
//			order.setImei(imei);
			order.setProductId(product.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
			order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setModTime(DateTimeUtils.getCurrentTime());
			order.setCompleteTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(product.getPrice()/100.0));
			int  provinceId = this.getProvinceIdByMobile(msisdn, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setExtData(extData);
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			boolean bDeducted  = order.deduct(cp.getVolt());

			SaveOrderInsert(order);
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			
			if(!bDeducted){ // 不扣量 通知渠道
				TPipleProductKey ppKey = new TPipleProductKey();
				ppKey.setPipleId(order.getPipleId());
				ppKey.setProductId(order.getProductId());
				TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppKey);
//				n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
				notifyChannel(cp.getNotifyUrl(), order, pipleProduct.getPipleProductCode(), "ok");
			}
		}
		return "ok";
	}
	public class ZWRdoOrder extends TOrder{
		private String port;
		private String content;  
		
		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.port != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("port");
				oExt.setExtValue(this.port);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.content != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("content");
				oExt.setExtValue(this.content);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
