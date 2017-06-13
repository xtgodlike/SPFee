package com.qy.sp.fee.modules.piplecode.fy;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.*;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FYLTService extends ChannelService{
	public final static int INIT = 0; 
	public final static int GETCODE_SUCCESS 	= 1; 
	public final static int GETCODE_FAIL 	= 2; 
	public final static int PAY_SUCCESS = 3;
	public final static int PAY_FAIL = 4;
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String P_SUCCESS = "DELIVRD";	  // 同步计费成功
	@Override
	public String getPipleId() {
		return "14968067402400448748543";
	}

	@Override
	public String getPipleKey() {
		return "PM1067";
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody == null)
			return error;

		
		String msg = requestBody.optString("msg");  // 上行信息 固定指令：BK*22+透传参数（apiKey）
		String linkid = requestBody.optString("linkid");
		String spnumber = requestBody.optString("spnumber");
		String mobile = requestBody.optString("mobile");
		String status = requestBody.optString("status");
		String ctime = requestBody.optString("ctime");

		if(StringUtil.isEmptyString(msg) || StringUtil.isEmptyString(linkid)  || StringUtil.isEmptyString(spnumber)
				|| StringUtil.isEmptyString(mobile) || StringUtil.isEmptyString(status)){
						return "param error";
		}
		TOrder torder = tOrderDao.selectByPipleOrderId(linkid);
		if(torder==null){ // 数据未同步
			try {
				statistics(STEP_PAY_BASE_TO_PLATFORM, torder.getGroupId(), requestBody.toString());
				String piplePCode = msg.substring(0,5);
				String myApiKey = msg.substring(5,msg.length());
				TChannel channel = tChannelDao.selectByApiKey(myApiKey);
				TChannelPipleKey cpk = new TChannelPipleKey();
				cpk.setChannelId(channel.getChannelId());
				cpk.setPipleId(this.getPipleId());
				TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(cpk);
				TPipleProduct ppk = new TPipleProduct();
				ppk.setPipleId(this.getPipleId());
				ppk.setPipleProductCode(piplePCode);
				TPipleProduct pipleProduct = tPipleProductDao.selectByPipleProductCode(ppk);
				TProduct product = tProductDao.selectByPrimaryKey(pipleProduct.getProductId());
				FYLTOrder qyOrder = new FYLTOrder();
				qyOrder.setMsg(msg);
				qyOrder.setSpnumber(spnumber);
				qyOrder.setCtime(ctime);
				qyOrder.setResultCode(status);
				//扣量
				boolean bDeducted = false;
				if(P_SUCCESS.equals(status)){
                    qyOrder.setOrderId( KeyHelper.createKey());
                    qyOrder.setPipleId(this.getPipleId());
                    qyOrder.setChannelId(cp.getChannelId());
                    qyOrder.setProductId(pipleProduct.getProductId());
                    qyOrder.setPipleOrderId(linkid);
                    qyOrder.setAmount(new BigDecimal(product.getPrice() / 100));
                    qyOrder.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
                    qyOrder.setSubStatus(FYLTService.PAY_SUCCESS);
                    qyOrder.setCreateTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setModTime(DateTimeUtils.getCurrentTime());
                    qyOrder.setCompleteTime(DateTimeUtils.getCurrentTime());
                    if(mobile!=null && !"null".equals(mobile) && !"".equals(mobile)){
                        qyOrder.setMobile(mobile);
                        int  provinceId = this.getProvinceIdByMobile(mobile, false); // 获取省份ID
                        qyOrder.setProvinceId(provinceId);
                    }
                    bDeducted  = qyOrder.deduct(cp.getVolt());
                    if(!bDeducted){ // 不扣量 通知渠道
                        notifyChannelSMS(cp.getNotifyUrl(),qyOrder,spnumber,"ok");
                    }
                }else {
                    qyOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
                    qyOrder.setSubStatus(FYLTService.PAY_FAIL);
                    qyOrder.setModTime(DateTimeUtils.getCurrentTime());
                }
				SaveOrderInsert(qyOrder);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("FYLTService同步处理异常："+e.getMessage());
				return "sync error";
			}
		}else{
			return "order Synchronized";
		}
	}


	public class FYLTOrder extends TOrder{
		private String msg;  // 上行内容
		private String spnumber;  // 通道长号码
		private String ctime;  // 订购时间

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getSpnumber() {
			return spnumber;
		}

		public void setSpnumber(String spnumber) {
			this.spnumber = spnumber;
		}

		public String getCtime() {
			return ctime;
		}

		public void setCtime(String ctime) {
			this.ctime = ctime;
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
			if(this.spnumber != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("spnumber");
				oExt.setExtValue(this.spnumber);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.ctime != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("ctime");
				oExt.setExtValue(this.ctime);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}

			return tOrderExts;
		}
	}
}
