package com.qy.sp.fee.modules.piplecode.huozuan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
import com.qy.sp.fee.modules.piplecode.ds.DSWoService.DSWORdoOrder;
import com.qy.sp.fee.modules.piplecode.ls.LSSPService.LSSPorder;

@Service
public class HZService extends ChannelService{
	private  Logger log = Logger.getLogger(HZService.class);	
	private static final String SUCCESS_STATUS = "DELIVRD";
	
	private static final String IMSI = "460018548284145";
	@Override
	public String getPipleId() {
		return "14815351573456688034681";
	}
	public String getApiKey(){
		return "1003";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("HZService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		String ipProvince = requestBody.optString("ipProvince");
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
			TLocation location = null;
			location = mobileSegmentService.getLocationByMobile(req.getMobile());
			if(location != null){
				req.setProvinceId(location.getProvinceId());
				req.setHostId(location.getHostId());
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
			int provinceId = 0;
			if(StringUtil.isEmpty(ipProvince)){
				provinceId = mobileSegmentService.getProvinceIdByMobile(mobile);
			}else{
				provinceId = mobileSegmentService.getProvinceByIpProvince(ipProvince);
			}
			//保存订单
			HZOrder order = new HZOrder();
			order.setProvinceId(provinceId);
			String orderId = "hz"+groupId.substring(groupId.length()-9,groupId.length()) ;
			order.setOrderId(orderId);
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			Date CurrentTime=DateTimeUtils.getCurrentTime();
			order.setCreateTime(CurrentTime);
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);
			if(requestBody.containsKey("fromType")){
				if(GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)){
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_SMS));
				}else if(GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)){
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_SDK));
				}else if(GlobalConst.FromType.FROM_TYPE_API.equals(fromType)){
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_API));
				}
			}else{
				order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_API));
			}
			try{
				result.put("orderId",order.getOrderId());
				order.setResultCode(GlobalConst.Result.SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
				order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
				order.setCreateTime(CurrentTime);
				order.setModTime(CurrentTime);
				order.setCompleteTime(CurrentTime);
				result.put("resultCode", GlobalConst.Result.SUCCESS);
				result.put("resultMsg","请求成功。");
				return result;
			}catch(Exception e){
				e.printStackTrace();
				order.setResultCode(GlobalConst.Result.ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultCode","服务器异常");
				return result;
			}
		}
		
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("HZService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}else{
			String spport = requestBody.optString("spport");
			String mobile = requestBody.optString("mobile");
			String msg = requestBody.optString("msg");
			String linkid = requestBody.optString("linkid");
			String cpparam = requestBody.optString("cpparam");
			String mrtime = requestBody.optString("mrtime");
			String fee = requestBody.optString("fee");
			String groupId = KeyHelper.createKey();
			TOrder order = tOrderDao.selectByPrimaryKey("hz"+cpparam);
			order.setPipleOrderId(linkid);
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
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
				SaveOrderUpdate(order);
			}
		}
		return "ok";
	}
	public class HZOrder extends TOrder{
		
	}
	
	public synchronized static String generate9() {
		 
        int n = (int) (Math.random() * 900000000) + 100000000;
        return n + "";
    } 
	
}

