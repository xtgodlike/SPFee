package com.qy.sp.fee.modules.apisdk.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TSdkOperationDao;
import com.qy.sp.fee.dto.TSdkOperation;
import com.qy.sp.fee.service.MobileSegmentService;

import net.sf.json.JSONObject;

@Service
public class ApiSdkOperationService {
	private Logger logger = Logger.getLogger(ApiSdkConfigService.class);
	public static final String FLOW_MYSELF = "20160721160123236592";//平台流程Id
	public static final String STEP_MYSELF_ACTIVATION = "20160722172222172559";//平台上线记录点
	public static final String STEP_MYSELF_NEW_USER = "20160722172222172560";//平台新用户记录点
	public static final String STEP_MYSELF_REQUEST_PAY = "20160722172222172561";//平台请求支付记录点
	public static final String STEP_MYSELF_PAY_SUCCESS = "20160722172222172562";//平台支付成功记录点
	public static final String STEP_MYSELF_GET_PHONE = "20160722172222172563";//用户通过短信池获取到号码统计
	public static final String MONGODB_SDK_PHONE = "mongodb_sdk_phone";
	public static final String MONGODB_SDK_OPERATION = "mongodb_sdk_operation";
	private Map<String,Statics> staticsMap = new ConcurrentHashMap<String, Statics>();
	@Resource
	private TSdkOperationDao tSdkOperationDao;
	@Resource
	private MobileSegmentService mobileSegmentService;
	@Resource
	protected MongoTemplate mongoTemplate;
	@PostConstruct
	public void init(){
		StaticsActivation staticsActivation = new StaticsActivation();
		staticsMap.put(staticsActivation.getFlowId()+"_"+staticsActivation.getStepId(), staticsActivation);
		
		StaticsNewUser staticsNewUser = new StaticsNewUser();
		staticsMap.put(staticsNewUser.getFlowId()+"_"+staticsNewUser.getStepId(), staticsNewUser);
		
		StaticsPayRequest staticsPayRequest = new StaticsPayRequest();
		staticsMap.put(staticsPayRequest.getFlowId()+"_"+staticsPayRequest.getStepId(), staticsPayRequest);
		
		StaticsPaySuccess staticsPaySuccess = new StaticsPaySuccess();
		staticsMap.put(staticsPaySuccess.getFlowId()+"_"+staticsPaySuccess.getStepId(), staticsPaySuccess);
		
		StaticsGetPhone staticsGetPhone = new StaticsGetPhone();
		staticsMap.put(staticsGetPhone.getFlowId()+"_"+staticsGetPhone.getStepId(), staticsGetPhone);
	}
	public void staticsUserOperation(JSONObject jsonObject){
		String operationId = KeyHelper.createID();
		String appId = jsonObject.optString("appId");
		String channelId = jsonObject.optString("channelId");
		String imei = jsonObject.optString("imei");
		String content = jsonObject.optString("content");
		String stepId = jsonObject.optString("stepId");
		String flowId = jsonObject.optString("flowId");
		TSdkOperation operation = new TSdkOperation();
		operation.setOperationId(operationId);
		operation.setAppId(appId);
		operation.setChannelId(channelId);
		operation.setImei(imei);
		operation.setCreateTime(new Date());
		operation.setOperationContent(content);
		operation.setOperationStep(stepId);
		operation.setFlowId(flowId);
		tSdkOperationDao.insert(operation);
	}
	/**
	 * 
	 * @author Administrator
	 * mongodb_sdk_phone 
	 * {
	 * 		imei:"imei",
	 * 		appId:"appId",
	 * 		channelId:"channelId",
	 * 		activationTime:[{operationId:"operationId",time:"time"}],
	 * 		registerTime:"2016-10-10 09:00:01",
	 * 		lastestActivationTime:"2016-10-10 09:00:01",
	 * 		mobile:"mobile",
	 * 		host:"host",
	 * 		province:"province",
	 * 		isPay:"false"
	 * 		payRequestTime:[{operationId:"operationId",time:"time"}]
	 *      paySuccessTime:[{operationId:"operationId",orderId:"orderId",time:"time"}]
	 * 		
	 * }
	 * 
	 */
	public void staticsUserOperationCustom(JSONObject requestObj){
		String appId = requestObj.optString("appId");
		String channelId = requestObj.optString("channelId");
		String imei = requestObj.optString("imei");
		if(StringUtil.isEmpty(appId) || StringUtil.isEmpty(imei) || StringUtil.isEmpty(channelId)){
			return ;
		}
			
		BasicDBObject jsonObject = parseJsonObjectToDBObject(requestObj);
		jsonObject.put("createTime", DateTimeUtils.getCurrentTime());
		mongoTemplate.insert(jsonObject,MONGODB_SDK_OPERATION);
		String stepId = jsonObject.getString("stepId");
		String flowId = jsonObject.getString("flowId");
		String staticsKey = flowId+"_"+stepId;
		Statics statics = staticsMap.get(staticsKey);
		if(statics != null){
			statics.staticsOperation(jsonObject);
		}
		logger.debug("statics point:   "+jsonObject.toString());
	}
	private BasicDBObject parseJsonObjectToDBObject(JSONObject requestObj){
		BasicDBObject basicDBObject = new BasicDBObject();
		Iterator<String> iterator = requestObj.keys();
		while(iterator.hasNext()){
			String key = iterator.next();
			basicDBObject.put(key, requestObj.get(key));
		}
		return basicDBObject;
	}
	public interface Statics{
		public String getFlowId();
		public String getStepId();
		public void staticsOperation(BasicDBObject jsonObject);
	}
	public abstract class AbstractStatics implements Statics{
		
		@Override
		public String getFlowId() {
			return FLOW_MYSELF;
		}
		@Override
		public String getStepId() {
			return "";
		}
		@Override
		public void staticsOperation(BasicDBObject jsonObject) {
			String appId = jsonObject.getString("appId");
			String channelId = jsonObject.getString("channelId");
			String imei = jsonObject.getString("imei");
			String mobile = jsonObject.getString("mobile");
			String host = jsonObject.getString("host");
			String province = jsonObject.getString("province");
			BasicDBObject dbObject = new BasicDBObject();
			dbObject.put("imei", imei);
			dbObject.put("channelId", channelId);
			dbObject.put("appId", appId);
			DBObject result = mongoTemplate.getCollection(MONGODB_SDK_PHONE).findOne(dbObject);
			if(result == null){
				result = new BasicDBObject();
				result.put("imei", imei);
				result.put("channelId", channelId);
				result.put("appId", appId);
				result.put("mobile", mobile);
				result.put("host", host);
				result.put("province", province);
				processResultSave(jsonObject,result);
				mongoTemplate.getCollection(MONGODB_SDK_PHONE).save(result);
			}else{
				if(StringUtil.isNotEmptyString(mobile)){
					result.put("mobile",mobile);
				}
				if(StringUtil.isNotEmptyString(province)){
					result.put("province", province);
				}
				if(StringUtil.isNotEmptyString(host)){
					result.put("host", host);
				}
				processResultUpdate(jsonObject,result);
				mongoTemplate.getCollection(MONGODB_SDK_PHONE).update(dbObject, result);
				
			}
		}
		protected void processResultSave(BasicDBObject jsonObject,DBObject result){
		}
		protected void processResultUpdate(BasicDBObject jsonObject,DBObject result){
		}
	}
	public class StaticsNewUser extends AbstractStatics{

		@Override
		public String getStepId() {
			return STEP_MYSELF_NEW_USER;
		}
		protected void processResultSave(BasicDBObject jsonObject,DBObject result){
			Date currentDate = DateTimeUtils.getCurrentTime();
			result.put("regsiterTime", currentDate);
			result.put("lastestActivationTime", currentDate);
			List<BasicDBObject> activationTime = new ArrayList<BasicDBObject>();
			BasicDBObject time = new BasicDBObject();
			time.put("time", currentDate);
			time.put("operationId", jsonObject.get("_id"));
			activationTime.add(time);
			result.put("lastestActivationTime", currentDate);
			result.put("activationTime",activationTime );
		}
	}
	public class StaticsActivation extends AbstractStatics{

		@Override
		public String getStepId() {
			return STEP_MYSELF_ACTIVATION;
		}
		@Override
		protected void processResultSave(BasicDBObject jsonObject,DBObject result) {
			Date currentDate = DateTimeUtils.getCurrentTime();
			List<BasicDBObject> activationTime = new ArrayList<BasicDBObject>();
			BasicDBObject time = new BasicDBObject();
			time.put("time", currentDate);
			time.put("operationId", jsonObject.get("_id"));
			activationTime.add(time);
			result.put("regsiterTime", currentDate);
			result.put("lastestActivationTime", currentDate);
			result.put("activationTime",activationTime );
		}
		@Override
		protected void processResultUpdate(BasicDBObject jsonObject,DBObject result) {
			Date currentDate = DateTimeUtils.getCurrentTime();
			result.put("lastestActivationTime", currentDate);
			List<BasicDBObject> activationTime = (List<BasicDBObject>)result.get("activationTime");
			BasicDBObject time = new BasicDBObject();
			time.put("time", currentDate);
			time.put("operationId", jsonObject.get("_id"));
			activationTime.add(time);
			if(activationTime.size() >10){
				activationTime = activationTime.subList(activationTime.size()-10,activationTime.size());
			}
			result.put("activationTime",activationTime );
		}
		
	}
	public class StaticsGetPhone extends AbstractStatics{
		
		@Override
		public String getStepId() {
			return STEP_MYSELF_GET_PHONE;
		}
		
	}
	public class StaticsPayRequest extends AbstractStatics{

		@Override
		public String getStepId() {
			return STEP_MYSELF_REQUEST_PAY;
		}
		@Override
		protected void processResultUpdate(BasicDBObject jsonObject,DBObject result) {
			Date currentDate =DateTimeUtils.getCurrentTime();
			List<BasicDBObject> payRequestTimes = (List<BasicDBObject>)result.get("payRequestTime");
			if(payRequestTimes == null){
				payRequestTimes =  new ArrayList<BasicDBObject>();
			}
			BasicDBObject time = new BasicDBObject();
			time.put("time", currentDate);
			time.put("operationId", jsonObject.get("_id"));
			payRequestTimes.add(time);
			if(payRequestTimes.size() >10){
				payRequestTimes = payRequestTimes.subList(payRequestTimes.size()-10,payRequestTimes.size());
			}
			result.put("payRequestTime",payRequestTimes );
		}
	}
	public class StaticsPaySuccess extends AbstractStatics{

		@Override
		public String getStepId() {
			return STEP_MYSELF_PAY_SUCCESS;
		}
		@Override
		protected void processResultUpdate(BasicDBObject jsonObject,DBObject result) {
			Date currentDate =DateTimeUtils.getCurrentTime();
			List<BasicDBObject> paySuccessTimes = (List<BasicDBObject>)result.get("paySuccessTime");
			if(paySuccessTimes == null){
				paySuccessTimes = new ArrayList<BasicDBObject>();
			}
			BasicDBObject time = new BasicDBObject();
			time.put("time", currentDate);
			time.put("operationId", jsonObject.get("_id"));
			time.put("orderId", jsonObject.get("orderId"));
			paySuccessTimes.add(time);
			if(paySuccessTimes.size() >10){
				paySuccessTimes = paySuccessTimes.subList(paySuccessTimes.size()-10,paySuccessTimes.size());
			}
			result.put("paySuccessTime",paySuccessTimes );
		}
	}
}
