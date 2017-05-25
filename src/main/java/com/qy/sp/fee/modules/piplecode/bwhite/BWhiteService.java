package com.qy.sp.fee.modules.piplecode.bwhite;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TSdkConfigDao;
import com.qy.sp.fee.dao.TSdkconfigMobileBaseDao;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.dto.TSdkConfig;
import com.qy.sp.fee.dto.TSdkConfigQueryKey;
import com.qy.sp.fee.dto.TSdkconfigMobileBase;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

@Service
public class BWhiteService extends ChannelService {

	private Logger logger = LoggerFactory.getLogger(BWhiteService.class);
	public static final String CONFIG_CODE_START_TIME ="codeStartTime";
	@Resource
	private TSdkConfigDao tSdkConfigDao;
	@Resource
	private TSdkconfigMobileBaseDao tSdkconfigMobileBaseDao;
	@Override
	public String getPipleId() {
		return "14714136609207184697301";
	}
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String appId = requestBody.optString("appId");
		String extData = requestBody.optString("extData");
		String pipleOrderId = requestBody.optString("pipleOrderId");
		String contentId = requestBody.optString("contentId");
		String releaseChannelId = requestBody.optString("releaseChannelId");
		String cpId = requestBody.optString("cpId");
		String cId = requestBody.optString("cid");
		String appVersion = requestBody.optString("appVersion");
		logger.info("BWRequest:"+requestBody.toString());
		JSONObject result = new JSONObject();
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey) || StringUtil.isEmptyString(mobile)|| StringUtil.isEmptyString(appId) ){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		} else {
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setProductCode(productCode);
			req.setPipleId(pipleId);
			req.setMobile(mobile);
			BaseResult bResult = this.accessVerify(req);
			if (bResult != null) {// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				logger.info("BWResoonse:"+result.toString());
				return result;
			} else {
				TChannel tChannel = this.tChannelDao.selectByApiKey(apiKey);
				TSdkConfigQueryKey key = new TSdkConfigQueryKey();
				key.setAppId(appId);
				key.setChannelId(tChannel.getChannelId());
				key.setPipleId(getPipleId());
				key.setProvinceId(req.getProvinceId()+"");
				key.setConfigId(CONFIG_CODE_START_TIME);
				TSdkConfig config = null;
				List<TSdkConfig> configs = tSdkConfigDao.selectConfigurationsByConfigQueryKey(key);
				if(configs.size() >0){
					config = configs.get(0); 
				}
				if(config != null){
					try{
						String codeStartTime = config.getConfigValue();
						Date currentDate = DateTimeUtils.getCurrentTime();
						currentDate.setYear(70);
						currentDate.setMonth(0);
						currentDate.setDate(1);
						String times[] = codeStartTime.split(",");
						boolean isTimeOk = false;
						for(String time: times){
							time = time.substring(1,time.length()-1);
							String start = time.split("-")[0];
							Date startDate = DateTimeUtils.toTime(start,"HH:mm:ss");
							String end = time.split("-")[1];
							Date endDate =DateTimeUtils.toTime(end,"HH:mm:ss");
							if(currentDate.getTime() > startDate.getTime() && currentDate.getTime() < endDate.getTime()){
								isTimeOk = true;
							}
						}
						if(!isTimeOk){
							result.put("resultCode","-1");
							result.put("resultMsg","不在时间段内");
							logger.info("BWResoonse:"+result.toString());
							return result;
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				TSdkconfigMobileBase tSdkconfigMobileBase = new TSdkconfigMobileBase();
				tSdkconfigMobileBase.setAppId(appId);
				tSdkconfigMobileBase.setContentId(contentId);
				tSdkconfigMobileBase.setCpId(cpId);
				tSdkconfigMobileBase.setReleaseChannelId(releaseChannelId);
				TSdkconfigMobileBase sdkconfigMobileConfig = tSdkconfigMobileBaseDao.selectByPrimaryKey(tSdkconfigMobileBase);
				if(sdkconfigMobileConfig != null){
					boolean isOPen = NumberUtil.getBoolean(sdkconfigMobileConfig.getIsOpen());
					if(!isOPen){
						result.put("resultCode","-1");
						result.put("resultMsg","基地该渠道未打开，联系管理员");
						logger.info("BWResoonse:"+result.toString());
						return result;
					}
					boolean isBWhite = NumberUtil.getBoolean(sdkconfigMobileConfig.getIsUseBWhite());
					if(!isBWhite){
						result.put("resultCode","-1");
						result.put("resultMsg","基地该渠道黑白包未开启");
						logger.info("BWResoonse:"+result.toString());
						return result;
					}
					String startCodeTime = sdkconfigMobileConfig.getStartCodeTime();
					if(StringUtil.isNotEmptyString(startCodeTime)){
						Date currentDate = DateTimeUtils.getCurrentTime();
						currentDate.setYear(70);
						currentDate.setMonth(0);
						currentDate.setDate(1);
						String times[] = startCodeTime.split(",");
						boolean isTimeOk = false;
						for(String time: times){
							time = time.substring(1,time.length()-1);
							String start = time.split("-")[0];
							Date startDate = DateTimeUtils.toTime(start,"HH:mm:ss");
							String end = time.split("-")[1];
							Date endDate =DateTimeUtils.toTime(end,"HH:mm:ss");
							if(currentDate.getTime() > startDate.getTime() && currentDate.getTime() < endDate.getTime()){
								isTimeOk = true;
							}
						}
						if(!isTimeOk){
							result.put("resultCode","-1");
							result.put("resultMsg","该基地渠道开放,不在时间段内");
							logger.info("BWResoonse:"+result.toString());
							return result;
						}
					}else{
						result.put("resultCode","-1");
						result.put("resultMsg","该省份时间段未开通。请开通");
						logger.info("BWResoonse:"+result.toString());
						return result;
					}
					
				}else{
					result.put("resultCode","-1");
					result.put("resultMsg","该版本没有配置策略。");
					logger.info("BWResoonse:"+result.toString());
					return result;
				}
				String groupId = KeyHelper.createKey();
				TProduct tProduct = this.tProductDao.selectByCode(productCode);
				statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
				TOrder order = new TOrder();
				order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
				order.setChannelId(tChannel.getChannelId());
				order.setCreateTime(DateTimeUtils.getCurrentTime());
				order.setMobile(mobile);
				order.setOrderId(KeyHelper.createKey());
				order.setOrderStatus(GlobalConst.OrderStatus.INIT);
				order.setPipleId(getPipleId());
				order.setPipleOrderId(pipleOrderId);
				order.setProductId(tProduct.getProductId());
				order.setProvinceId(req.getProvinceId());
				order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
				order.setGroupId(groupId);
				order.setExtData(extData);
				order.setAppId(appId);
				this.SaveOrderInsert(order);
				result.put("resultCode",GlobalConst.Result.SUCCESS);
				result.put("orderId",order.getOrderId());
				result.put("resultMsg","获取成功");
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
			}
		}
		logger.info("BWResoonse:"+result.toString());
		return result;
	}
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
}
