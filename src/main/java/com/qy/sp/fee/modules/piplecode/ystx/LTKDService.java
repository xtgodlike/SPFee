package com.qy.sp.fee.modules.piplecode.ystx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;
@Service
public class LTKDService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	@Override
	public String getPipleId() {
		return "14609584790890726707592";
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;
		
		String upport = requestBody.optString("upport");
		String order = requestBody.optString("order");
		String mobile = requestBody.optString("mobile");
		String linkId = requestBody.optString("linkId");
		String state = requestBody.optString("state");
		if(StringUtil.isEmptyString(linkId) || StringUtil.isEmptyString(state)  || StringUtil.isEmptyString(order)){
						return "param error";
		}
		TOrder torder = tOrderDao.selectByPipleOrderId(linkId);
		if(torder==null){ // 数据未同步
//			List<TChannelPiple> channelPiples = tChannelPipleDao.getListByPipleId(this.getPipleId());
//			TChannelPiple cp = channelPiples.get(0); // 只能配置给一家渠道
			String customParm = order.substring(7, 9); // order为上行指令如 1008@66a1  1008@66b1
			// 固定参数值 分配固定渠道
			String channelId = "";
			if(customParm.equals("a1")){ // 1011星象游
				channelId = "1011";
			}
			if(customParm.equals("b1")){ // 14537918881576747077596 有乐互娱
				channelId = "14537918881576747077596";
			}
			TChannelPipleKey cpk = new TChannelPipleKey();
			cpk.setChannelId(channelId);
			cpk.setPipleId(this.getPipleId());
			TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(cpk);
			TPipleProduct ppk = new TPipleProduct();
			ppk.setPipleId(this.getPipleId());
			ppk.setPipleProductCode(upport);
			TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppk);
			TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
			YSTXLTOrder ysOrder = new YSTXLTOrder();
			//扣量
			boolean isSend = false;
			if("1".equals(state)){
				ysOrder.setOrderId( KeyHelper.createKey());
				ysOrder.setPipleId(this.getPipleId());
				ysOrder.setChannelId(cp.getChannelId());
				ysOrder.setProductId(pipleProduct.getProductId());
				
				ysOrder.setPipleOrderId(linkId);
				ysOrder.setAmount(new BigDecimal(product.getPrice() / 100));
				ysOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				ysOrder.setSubStatus(LTKDService.PAY_SUCCESS);
				ysOrder.setCreateTime(DateTimeUtils.getCurrentTime());
				ysOrder.setModTime(DateTimeUtils.getCurrentTime());
				ysOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
				if(mobile!=null && !"null".equals(mobile) && !"".equals(mobile)){
					ysOrder.setMobile(mobile);
					int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
					ysOrder.setProvinceId(provinceId);
				}
				ysOrder.setUpport(upport);
				ysOrder.setOrder(order);
				boolean bDeducted  = ysOrder.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
			}else {
				ysOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				ysOrder.setSubStatus(LTKDService.PAY_FAIL);
				ysOrder.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderInsert(ysOrder);
			if(isSend){ // 不扣量 通知渠道
				notifyChannel(cp.getNotifyUrl(), ysOrder.getMobile(),null,ysOrder.getOrderId(), product.getProductCode(),this.getPipleId(), "ok",order);
			}
			return "ok";
		}else{
			return "order Synchronized";
		}
	}
	public class YSTXLTOrder extends TOrder{
		
		private String upport;  	// 同步端口(用以区分不同通道的不同资费，1元=711 2元=712 4元=714 5元=715 6元=716 8元=718)
		private String order; 		// 透传（同步指令，值=上行指令）

		public String getUpport() {
			return upport;
		}


		public void setUpport(String upport) {
			this.upport = upport;
		}


		public String getOrder() {
			return order;
		}


		public void setOrder(String order) {
			this.order = order;
		}


		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.upport != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("upport");
				oExt.setExtValue(this.upport);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.order != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("order");
				oExt.setExtValue(this.order);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
