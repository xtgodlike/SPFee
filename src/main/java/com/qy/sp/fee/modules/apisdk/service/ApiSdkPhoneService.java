package com.qy.sp.fee.modules.apisdk.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.ClientProperty;
import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.DesUtil;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TChannelDao;
import com.qy.sp.fee.dao.TImsiMobileDao;
import com.qy.sp.fee.dao.TSdkAppDao;
import com.qy.sp.fee.dao.TSdkPhoneDao;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TImsiMobile;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TSdkApp;
import com.qy.sp.fee.dto.TSdkPhone;
import com.qy.sp.fee.modules.piplecode.base.ChannelManager;
import com.qy.sp.fee.service.BaseService;
import com.qy.sp.fee.service.MobileSegmentService;

import net.sf.json.JSONObject;

@Service
public class ApiSdkPhoneService {

	private Logger logger = LoggerFactory.getLogger(ApiSdkPhoneService.class);
	@Resource
	private TImsiMobileDao tImsiMobileDao;
	@Resource
	private TSdkPhoneDao tSdkPhoneDao;
	@Resource
	private MobileSegmentService mobileSegmentService;
	@Resource
	private TChannelDao tChannelDao;
	@Resource
	private TSdkAppDao tSdkAppDao;
	@Resource 
	ApiSdkOperationService apiSdkOperationService;
	private Map<String,DesUtil> desUtilMap  = new ConcurrentHashMap<String, DesUtil>();
	private Map<String,DesUtil> desUtilDeletedMap  = new ConcurrentHashMap<String, DesUtil>();
	private String[] desKeys = new String[]{"84751150","47642510","38232831","31553543","91414439","18642266","54808912","49589256","75206325","01561997"};
	@PostConstruct
	public void init(){
		for(String desKey : desKeys){
			DesUtil util = new DesUtil();
			util.setKey(desKey);
			desUtilMap.put(desKey, util);
		}
	}
	/**
	 * 获取号码的指令。
	 * @param requestJsonObject
	 * @return
	 */
	public JSONObject querySimcardCommond(JSONObject requestJsonObject) {
		JSONObject result = new JSONObject();
		String imsi = requestJsonObject.optString("imsi_sim");
		result.put("messageToNumber", "1069083616288");
		DesUtil util = new DesUtil();
		util.setKey(IMSI_ENCODE_KEY);
		if(StringUtil.isNotEmptyString(imsi)){
			String message = COMPANY_QIANYA+"$"+BUSINESS_SIM+"$"+util.Encode(imsi);
			result.put("messageContent",message);
		}
		return result;
	}
	/**
	 * 上传可靠的imsi和mobile关系。或者imei和mobile关系
	 * @param requestJsonObject
	 */
	public void uploadSimcardInfo(JSONObject requestJsonObject) {
		String imsi = requestJsonObject.optString("imsi_sim");
		String number = requestJsonObject.optString("number_sim");
		String imei = requestJsonObject.optString("imei");
		String appId = requestJsonObject.optString("appId");
		String channelId = requestJsonObject.optString("channelId");
		if(StringUtil.isNotEmptyString(imsi) && StringUtil.isNotEmptyString(number)){
			TImsiMobile imsiInfo = tImsiMobileDao.selectByPrimaryKey(imsi);
			if(imsiInfo == null){
				imsiInfo = new TImsiMobile();
				imsiInfo.setMobile(number);
				imsiInfo.setImsi(imsi);
				imsiInfo.setImporttime(DateTimeUtils.getCurrentDate());
				tImsiMobileDao.insert(imsiInfo);
			}
		}
		if(StringUtil.isNotEmptyString(imei) && StringUtil.isNotEmptyString(number)){
			TSdkPhone phone = tSdkPhoneDao.selectByPrimaryKey(imei);
			if(phone != null && StringUtil.isNotEmptyString(number) && !StringUtil.equals(number, phone.getSimNumber1())){
				phone.setSimNumber1(number);
				phone.setSimProvince1(mobileSegmentService.getProvinceIdByMobile(number));
				tSdkPhoneDao.updateByPrimaryKeySelective(phone);
			}
		}
		if(StringUtil.isNotEmptyString(channelId) && StringUtil.isNotEmptyString(appId) && StringUtil.isNotEmptyString(imei) && StringUtil.isNotEmptyString(number)){
			staticsGetPhone(imei, appId, channelId,number);
		}
	}
	
	public JSONObject querySimcardInfo(JSONObject requestJsonObject) {
		JSONObject result = new JSONObject();
		String imsi = requestJsonObject.optString("imsi_sim");
		if(StringUtil.isNotEmptyString(imsi)){
			TImsiMobile imsiInfo = tImsiMobileDao.selectByPrimaryKey(imsi);
			if(imsiInfo != null){
				result.put("imsi_sim", imsi);
				result.put("number_sim", imsiInfo.getMobile());
			}else{
				String queryUrl = ClientProperty.getProperty("config", "SIMCARD_QUERY_URL");
				if(StringUtil.isNotEmptyString(queryUrl)){
					String url = StringUtil.format(queryUrl, imsi);
					try {
						String mobile = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
						if(StringUtil.isNotEmptyString(mobile)){
							result.put("imsi_sim", imsi);
							result.put("number_sim", mobile);
							uploadSimcardInfo(result);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		return result;
	}
	public String queryMobileByImsi(String imsi){
		if(StringUtil.isNotEmptyString(imsi)){
			TImsiMobile imsiInfo = tImsiMobileDao.selectByPrimaryKey(imsi);
			if(imsiInfo != null){
				return imsiInfo.getMobile();
			}
		}
		return "";
	}
	public static final String IMSI_ENCODE_KEY= "NJQIANYA";
	public static final String PREFIX_MSG = "\\$";
	public static final String MSG_SPILT1 = "$";
	public static final String MSG_SPILT2 = "#";
	public static final String COMPANY_QIANYA = "001";
	public static final String COMPANY_KM = "002";
	public static final String BUSINESS_SIM = "0";
	public static final String BUSINESS_GET_SMS = "1";
	public static final String BUSINESS_SUBMIT_SMS = "2";
	public static final String BUSINESS_IMEI = "3";
	public String reciiveSmsMessage(JSONObject requestJsonObject){
		String mobile = requestJsonObject.optString("mobile");
		String msg = requestJsonObject.optString("msg");
		logger.info("mobile:"+mobile+",msg="+msg);
		if(StringUtil.isEmpty(msg)){
			return null;
		}
		String decodeMsg = msg;
		decodeMsg = decodeMsg.replace(MSG_SPILT2,MSG_SPILT1);
		String args[] = decodeMsg.split(PREFIX_MSG);
		String company =args[0];
		String business = null;
		if(args.length >1)
			business = args[1];
		if(COMPANY_QIANYA.equals(company)){
			startHandlerMessage(requestJsonObject, decodeMsg, company, business);
		}else if(COMPANY_KM.equals(company)){
			try {
				String url = ClientProperty.getProperty("config","SIMCARD_DISPATCHER_URL");
				if(StringUtil.isNotEmptyString(url)){
					url = String.format(url, mobile,msg);
					HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String code = msg;
			company = COMPANY_QIANYA;
			business = BUSINESS_SUBMIT_SMS;
			String fullMessage = BaseService.getFullMessage(mobile, company, business, code);
			requestJsonObject.put("msg", fullMessage);
			startHandlerMessage(requestJsonObject, fullMessage, company, business);
		}
		return "";
		
	}
	
	
	private void startHandlerMessage(JSONObject requestJsonObject, String decodeMsg, String company, String business) {
		if(BUSINESS_SIM.equals(business)){
			String simInfo = decodeMsg.substring(company.length()+business.length()+2);
			requestJsonObject.put("msg", simInfo);
			addSimcardInfo(requestJsonObject);
		}else if(BUSINESS_GET_SMS.equals(business)){
			try {
				String chargeMsg = decodeMsg.substring(company.length()+business.length()+2);
				requestJsonObject.put("msg", chargeMsg);
				ChannelManager.getInstance().fireGetMessage(requestJsonObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(BUSINESS_SUBMIT_SMS.equals(business)){
			try {
				String chargeMsg = decodeMsg.substring(company.length()+business.length()+2);
				requestJsonObject.put("msg", chargeMsg);
				ChannelManager.getInstance().fireSubmitMessage(requestJsonObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public String dispatcherInSmsMessage(JSONObject requestJsonObject){
		String mobile = requestJsonObject.optString("mobile");
		String msg = requestJsonObject.optString("msg");
		logger.info("mobile:"+mobile+",msg="+msg);
		if(StringUtil.isEmpty(msg)){
			return null;
		}
		String decodeMsg = msg;
		decodeMsg = decodeMsg.replace(MSG_SPILT2,MSG_SPILT1);
		String args[] = decodeMsg.split(PREFIX_MSG);
		String company =args[0];
		String business = null;
		if(args.length >1)
			business = args[1];
		startHandlerMessage(requestJsonObject, decodeMsg, company, business);
		return "";
		
	}
	public void addSimcardInfo(JSONObject requestJsonObject) {
		String mobile = requestJsonObject.optString("mobile");
		String msg = requestJsonObject.optString("msg");
		if(!StringUtil.isEmpty(msg)){
			DesUtil util = new DesUtil();
			util.setKey(IMSI_ENCODE_KEY);
			msg =  util.Decode(msg);
			TImsiMobile imsiInfo = tImsiMobileDao.selectByPrimaryKey(msg);
			if(imsiInfo == null) {
				imsiInfo = new TImsiMobile();
				imsiInfo.setImsi(msg);
				imsiInfo.setMobile(mobile);
				imsiInfo.setImporttime(new Date());
				tImsiMobileDao.insertSelective(imsiInfo);
			}
		}
	}
	public JSONObject uploadMobilePhone(JSONObject requestJsonObject) {
		String simImsi1 = requestJsonObject.optString("imsi_sim1");
		String simIccid1 = requestJsonObject.optString("iccid_sim1");
		String simNumber1 = requestJsonObject.optString("number_sim1");
		String simImsi2 = requestJsonObject.optString("imsi_sim2");
		String simIccid2 = requestJsonObject.optString("iccid_sim");
		String simNumber2 = requestJsonObject.optString("number_sim2");
		String simImsi3 = requestJsonObject.optString("imsi_sim3");
		String simIccid3 = requestJsonObject.optString("iccid_sim3");
		String simNumber3 = requestJsonObject.optString("number_sim3");
		String osVersion = requestJsonObject.optString("osVersion");
		String imei = requestJsonObject.optString("imei");
		String model = requestJsonObject.optString("model");
		String cpuModel = requestJsonObject.optString("cpuModel");
		int screenWidth = requestJsonObject.optInt("screenWidth");
		int screenHeight = requestJsonObject.optInt("screenHeight");
		int isWifi = requestJsonObject.optInt("isWifi");
		String macAddress = requestJsonObject.optString("macAddress");
		String manufacture = requestJsonObject.optString("manufacture");
		int isRoot = requestJsonObject.optInt("isRoot");
		String appPacketName = requestJsonObject.optString("appPacketName");
		String appSigin = requestJsonObject.optString("appSigin");
		String appName = requestJsonObject.optString("appName");
		String appSize = requestJsonObject.optString("appSize");
		String channelApi = requestJsonObject.optString("channelApi");
		String appId = requestJsonObject.optString("appId");
		String country = requestJsonObject.optString("country");
		String province = requestJsonObject.optString("province");
		String city = requestJsonObject.optString("city");
		String processName = requestJsonObject.optString("processName");
		String processName2 = requestJsonObject.optString("processName2");
		String key = desKeys[Math.abs(imei.hashCode())%desKeys.length];
		boolean isNewUser = false;
		JSONObject result = new JSONObject();
		TSdkPhone phone = tSdkPhoneDao.selectByPrimaryKey(imei);
		if (phone == null) {
			phone = new TSdkPhone();
			phone.setCreateTime(new Date());
			phone.setImei(imei);
			phone.setIsRoot(isRoot);
			phone.setIsWifi(isWifi);
			phone.setMacAddress(macAddress);
			phone.setManufacture(manufacture);
			phone.setModel(model);
			phone.setCpuModel(cpuModel);
			phone.setModifyTime(new Date());
			phone.setCreateTime(new Date());
			phone.setOsVersion(osVersion);
			phone.setPhoneId(imei);
			phone.setReqKey(key);
			phone.setScreenHeight(screenHeight);
			phone.setScreenWidth(screenWidth);
			phone.setSimIccid1(simIccid1);
			phone.setSimIccid2(simIccid2);
			phone.setSimIccid3(simIccid3);
			phone.setSimImsi1(simImsi1);
			phone.setSimImsi2(simImsi2);
			phone.setSimImsi3(simImsi3);
			phone.setProcessName(processName);
			phone.setProcessName2(processName2);
			phone.setCountry(country);
			phone.setProvince(province);
			phone.setCity(city);
			if (StringUtil.isNotEmptyString(simNumber1)) {
				checkSimCardInfo(simImsi1, simNumber1);
				phone.setSimNumber1(simNumber1);
				phone.setSimProvince1(mobileSegmentService.getProvinceIdByMobile(simNumber1));
			}
			if (StringUtil.isNotEmptyString(simNumber2)) {
				phone.setSimNumber2(simNumber2);
				phone.setSimProvince2(mobileSegmentService.getProvinceIdByMobile(simNumber2));
			}
			if (StringUtil.isNotEmptyString(simNumber3)) {
				phone.setSimNumber3(simNumber3);
				phone.setSimProvince3(mobileSegmentService.getProvinceIdByMobile(simNumber3));
			}
			tSdkPhoneDao.insertSelective(phone);
			isNewUser = true;
		} else {
			boolean isUpdate = false;
			if (isRoot != 0 && isRoot != phone.getIsRoot()) {
				isUpdate = true;
				phone.setIsRoot(isRoot);
			}
			if (isWifi != 0 && isWifi != phone.getIsWifi()) {
				isUpdate = true;
				phone.setIsWifi(isWifi);
			}
			if (screenHeight != 0 && screenHeight != phone.getScreenHeight()) {
				isUpdate = true;
				phone.setScreenHeight(screenHeight);
			}
			if (screenWidth != 0 && screenWidth != phone.getScreenWidth()) {
				isUpdate = true;
				phone.setScreenWidth(screenWidth);
			}
			if (StringUtil.isNotEmptyString(macAddress) && !StringUtil.equals(macAddress, phone.getMacAddress())) {
				isUpdate = true;
				phone.setMacAddress(macAddress);
			}
			if (StringUtil.isNotEmptyString(osVersion) && !StringUtil.equals(osVersion, phone.getOsVersion())) {
				isUpdate = true;
				phone.setOsVersion(osVersion);
			}
			if (StringUtil.isNotEmptyString(manufacture) && !StringUtil.equals(manufacture, phone.getManufacture())) {
				isUpdate = true;
				phone.setManufacture(manufacture);
			}
			if (StringUtil.isNotEmptyString(model) && !StringUtil.equals(model, phone.getModel())) {
				isUpdate = true;
				phone.setModel(model);
			}
			if(StringUtil.isNotEmptyString(cpuModel) && !StringUtil.equals(cpuModel,phone.getCpuModel())){
				isUpdate = true;
				phone.setCpuModel(cpuModel);
			}
			if (StringUtil.isNotEmptyString(simImsi1) && !StringUtil.equals(simImsi1, phone.getSimImsi1())) {
				isUpdate = true;
				phone.setSimImsi1(simImsi1);
				phone.setSimIccid1(simIccid1);
			}
			if (StringUtil.isNotEmptyString(simNumber1) && !StringUtil.equals(simNumber1, phone.getSimNumber1())) {
				checkSimCardInfo(simImsi1, simNumber1);
				isUpdate = true;
				phone.setSimNumber1(simNumber1);
			}
			if(StringUtil.isNotEmptyString(phone.getSimNumber1()) && phone.getSimProvince1() == 0){
				isUpdate = true;
				phone.setSimProvince1(mobileSegmentService.getProvinceIdByMobile(phone.getSimNumber1()));
			}
			if (StringUtil.isNotEmptyString(simImsi2) && !StringUtil.equals(simImsi2, phone.getSimImsi2())) {
				isUpdate = true;
				phone.setSimImsi2(simImsi2);
				phone.setSimIccid2(simIccid2);
			}
			if (StringUtil.isNotEmptyString(simNumber2) && !StringUtil.equals(simNumber2, phone.getSimNumber2())) {
				isUpdate = true;
				phone.setSimNumber2(simNumber2);
			}
			if(StringUtil.isNotEmptyString(phone.getSimNumber2())  && phone.getSimProvince2() == 0){
				isUpdate = true;
				phone.setSimProvince2(mobileSegmentService.getProvinceIdByMobile(phone.getSimNumber2()));
			}
			if (StringUtil.isNotEmptyString(simImsi3) && !StringUtil.equals(simImsi3, phone.getSimImsi3())) {
				isUpdate = true;
				phone.setSimImsi3(simImsi3);
				phone.setSimIccid3(simIccid3);
			}
			if (StringUtil.isNotEmptyString(simNumber3) && !StringUtil.equals(simNumber3, phone.getSimNumber3())) {
				isUpdate = true;
				phone.setSimNumber3(simNumber3);
			}
			if(StringUtil.isNotEmptyString(phone.getSimNumber3()) && phone.getSimProvince3() == 0){
				isUpdate = true;
				phone.setSimProvince3(mobileSegmentService.getProvinceIdByMobile(phone.getSimNumber3()));
			}
			if (StringUtil.isNotEmptyString(key) && !StringUtil.equals(key, phone.getReqKey())) {
				isUpdate = true;
				phone.setReqKey(key);
			}
			if(StringUtil.isEmpty(phone.getProcessName()) && StringUtil.isNotEmptyString(processName)){
				isUpdate = true;
				phone.setProcessName(processName);
			}else if(StringUtil.isEmpty(phone.getProcessName2()) && StringUtil.isNotEmptyString(processName)){
				isUpdate = true;
				phone.setProcessName2(processName);
			}else if (!StringUtil.equals(processName, phone.getProcessName()) && !StringUtil.equals(processName, phone.getProcessName2())) {
				isUpdate = true;
				phone.setProcessName(processName);
			}
			if (StringUtil.isNotEmptyString(processName2) &&!StringUtil.equals(processName2, phone.getProcessName2())) {
				isUpdate = true;
				phone.setProcessName2(processName2);
			}
			if (StringUtil.isNotEmptyString(country) && !StringUtil.equals(country, phone.getCountry())) {
				isUpdate = true;
				phone.setCountry(country);
			}
			if (StringUtil.isNotEmptyString(province) && !StringUtil.equals(province, phone.getProvince())) {
				isUpdate = true;
				phone.setProvince(province);
			}
			if (StringUtil.isNotEmptyString(city) && !StringUtil.equals(city, phone.getCity())) {
				isUpdate = true;
				phone.setCity(city);
			}
			if(isUpdate){
				phone.setModifyTime(new Date());
				tSdkPhoneDao.updateByPrimaryKeySelective(phone);
			}
		}
		result.put("imsi_sim1", phone.getSimImsi1());
		result.put("iccid_sim1", phone.getSimIccid1());
		result.put("number_sim1", phone.getSimNumber1());
		result.put("address_sim1", phone.getSimProvince1()+"");
		result.put("imsi_sim2", phone.getSimImsi2());
		result.put("iccid_sim2", phone.getSimIccid2());
		result.put("number_sim2", phone.getSimNumber2());
		result.put("address_sim2", phone.getSimProvince2()+"");
		result.put("imsi_sim3", phone.getSimImsi3());
		result.put("iccid_sim3", phone.getSimIccid3());
		result.put("number_sim3", phone.getSimNumber3());
		result.put("address_sim3", phone.getSimProvince3()+"");
		result.put("osVersion", phone.getOsVersion());
		result.put("imei", phone.getImei());
		result.put("model", phone.getModel());
		result.put("cpuModel", phone.getCpuModel());
		result.put("manufacture", phone.getManufacture());
		result.put("screenWidth", phone.getScreenWidth());
		result.put("screenHeight", phone.getScreenHeight());
		result.put("isWifi", phone.getIsWifi());
		result.put("macAddress", phone.getMacAddress());
		result.put("country", phone.getCountry());
		result.put("province", phone.getProvince());
		result.put("city", phone.getCity());
		result.put("isRoot", phone.getIsRoot());
		result.put("key", phone.getReqKey());
		result.put("processName", phone.getProcessName());
		result.put("processName2", phone.getProcessName2());
		TChannel channel = tChannelDao.selectByApiKey(channelApi);
		if(channel != null){
			result.put("channelId", channel.getChannelId());
		}else{
			result.put("resultCode", "-1");
			result.put("resultMsg", "渠道ID不正确");
			return result;
		}
		TSdkApp app = tSdkAppDao.selectByPrimaryKey(appId);
		if(app ==null){
			result.put("resultCode", "-1");
			result.put("resultMsg", "appId不正确");
			return result;
		}
		result.put("resultCode", "0");
		if(isNewUser){
			staticsNewUser(phone,appId,channel.getChannelId());
		}else{
			staticsActivation(phone,appId,channel.getChannelId());
		}
		return result;
	}
	private void checkSimCardInfo(String simImsi, String simNumber) {
		try{
		TImsiMobile imsiInfo = tImsiMobileDao.selectByPrimaryKey(simImsi);
		if(imsiInfo == null){
			imsiInfo = new TImsiMobile();
			imsiInfo.setImsi(simImsi);
			imsiInfo.setMobile(simNumber);
			imsiInfo.setImporttime(new Date());
			tImsiMobileDao.insert(imsiInfo);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void staticsNewUser(TSdkPhone phone,String appId,String channelId){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("stepId", ApiSdkOperationService.STEP_MYSELF_NEW_USER);
		jsonObject.put("flowId", ApiSdkOperationService.FLOW_MYSELF);
		jsonObject.put("imei", phone.getImei());
		jsonObject.put("appId", appId);
		jsonObject.put("channelId", channelId);
		if(!StringUtil.isEmpty(phone.getSimNumber1())){
			jsonObject.put("mobile", phone.getSimNumber1());
			jsonObject.put("host", mobileSegmentService.getLocationByMobile(phone.getSimNumber1()).getHostId()+"");
			jsonObject.put("province", phone.getSimProvince1()+"");
		}else{
			jsonObject.put("mobile", "");
			jsonObject.put("host", mobileSegmentService.getHostByImsi(phone.getSimImsi1()));
			jsonObject.put("province",mobileSegmentService.getProvinceByIpProvince(phone.getProvince())+"");
		}
		apiSdkOperationService.staticsUserOperationCustom(jsonObject);
	}
	private void staticsActivation(TSdkPhone phone,String appId,String channelId){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("stepId", ApiSdkOperationService.STEP_MYSELF_ACTIVATION);
		jsonObject.put("flowId", ApiSdkOperationService.FLOW_MYSELF);
		jsonObject.put("imei", phone.getImei());
		jsonObject.put("appId", appId);
		jsonObject.put("channelId", channelId);
		if(!StringUtil.isEmpty(phone.getSimNumber1())){
			jsonObject.put("mobile", phone.getSimNumber1());
			jsonObject.put("host", mobileSegmentService.getLocationByMobile(phone.getSimNumber1()).getHostId()+"");
			jsonObject.put("province", phone.getSimProvince1()+"");
		}else{
			jsonObject.put("mobile", "");
			jsonObject.put("host", mobileSegmentService.getHostByImsi(phone.getSimImsi1()));
			jsonObject.put("province",mobileSegmentService.getProvinceByIpProvince(phone.getProvince())+"");
		}
		apiSdkOperationService.staticsUserOperationCustom(jsonObject);
	}
	private void staticsGetPhone(String phoneId,String appId,String channelId,String mobile){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("stepId", ApiSdkOperationService.STEP_MYSELF_GET_PHONE);
		jsonObject.put("flowId", ApiSdkOperationService.FLOW_MYSELF);
		jsonObject.put("imei", phoneId);
		jsonObject.put("appId", appId);
		jsonObject.put("channelId", channelId);
		if(!StringUtil.isEmpty(mobile)){
			TLocation location = mobileSegmentService.getLocationByMobile(mobile);
			jsonObject.put("mobile", mobile);
			if(location != null){
				jsonObject.put("host",location.getHostId()+"");
				jsonObject.put("province", location.getProvinceId()+"");
			}
			apiSdkOperationService.staticsUserOperationCustom(jsonObject);
		}
	}
	public String encode(String imei,String desKey,String requestBody) throws Exception{
		DesUtil util = desUtilMap.get(desKey);
		if(util == null)
			util = desUtilDeletedMap.get(desKey);
		if(util == null){
			TSdkPhone phone = tSdkPhoneDao.selectByPrimaryKey(imei);
			if(phone != null){
				util = new DesUtil();
				util.setKey(phone.getReqKey());
				desUtilDeletedMap.put(phone.getReqKey(), util);
			}
		}
		logger.debug("beforeEncode:"+requestBody);
		return util.Encode(requestBody);
	}
	public String decode(String imei,String desKey,String requestBody) throws Exception{
		DesUtil util = desUtilMap.get(desKey);
		if(util == null)
			util = desUtilDeletedMap.get(desKey);
		if(util == null){
			TSdkPhone phone = tSdkPhoneDao.selectByPrimaryKey(imei);
			if(phone != null){
				util = new DesUtil();
				util.setKey(phone.getReqKey());
				desUtilDeletedMap.put(phone.getReqKey(), util);
			}
		}
		String decodeStr = util.Decode(requestBody);
		logger.debug("afterDecode:"+decodeStr);
		return decodeStr;
	}
	
}
