package com.qy.sp.fee.modules.piplecode.sllk;

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
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class SLLKMdoService extends ChannelService{
	public final static String RES_SUCCESS = "200";  // 请求通道成功
	private  Logger log = Logger.getLogger(SLLKMdoService.class);		
	@Override
	public String getPipleId() {
		return "14653763608440462880800";
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("ZWRdoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String mobile = requestBody.optString("mobile");
		String linkid = requestBody.optString("linkid");
		String msg = requestBody.optString("msg");
		String spcode = requestBody.optString("spcode");
		TOrder lorder = tOrderDao.selectByPipleOrderId(linkid); 
		if(lorder==null ){ // 数据未同步
			List<TChannelPiple> cPiples = tChannelPipleDao.getListByPipleId(getPipleId());  // 此代码只能配置一家渠道
			TChannelPiple channelPiple = null;
			if(cPiples.size()!=1){
				return "channel error";
			}else{
				channelPiple = cPiples.get(0);
			}
			TChannel channel = tChannelDao.selectByPrimaryKey(channelPiple.getChannelId());
			String[] msgStr = msg.split(",");  // 指令 YX,263438,3,1cb6,1812526,619005,3391*        第4截标识计费点
			String productCode = msgStr[3].toString();
			TPipleProduct ppkey = new TPipleProduct();
			ppkey.setPipleId(getPipleId());
			ppkey.setPipleProductCode(productCode);
			TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppkey);
			TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
			//保存订单
			SLLKMdoOrder order = new SLLKMdoOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(KeyHelper.createKey());
			order.setPipleOrderId(linkid);
			order.setPipleId(getPipleId());
			order.setChannelId(channel.getChannelId());
			order.setMobile(mobile);
//			order.setImsi(imsi);
//			order.setImei(imei);
			order.setProductId(product.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
			order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setModTime(DateTimeUtils.getCurrentTime());
			order.setCompleteTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal(product.getPrice()/100.0));
			int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
			order.setProvinceId(provinceId);
			order.setSpcode(spcode);
			order.setMsg(msg);
			boolean bDeducted  = order.deduct(channelPiple.getVolt());
			SaveOrderInsert(order);
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			
			if(!bDeducted){ // 不扣量 通知渠道
//				n(cp.getNotifyUrl(), order.getMobile(),order.getImsi(),order.getOrderId(), productCode, order.getPipleId(),"ok",cpparam);
				notifyChannel(channelPiple.getNotifyUrl(), order, product.getProductCode(), "ok");
			}
		}
		return "ok";
	}
	public class SLLKMdoOrder extends TOrder{
		private String msg;
		private String spcode;  

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getSpcode() {
			return spcode;
		}

		public void setSpcode(String spcode) {
			this.spcode = spcode;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.msg != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("msg");
				oExt.setExtValue(this.msg);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.spcode != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("spcode");
				oExt.setExtValue(this.spcode);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
