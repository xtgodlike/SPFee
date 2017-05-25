package com.qy.sp.fee.modules.piplecode.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TPipleProvince;
import com.qy.sp.fee.dto.TPipleProvinceKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
@Component
@Scope("prototype")
public class CommonChannelService extends ChannelService{
	public String FROM_SDK_EXTDATA = "qsdk_";
	public static final String KEY_GETSMSHTTPTYPE = "getSmsHttpType";
	public static final String KEY_GETSMSURL = "getSmsUrl";
	public static final String KEY_GETSMSDATATYPE = "getSmsDataType";
	public static final String KEY_GETSMS_RESULT_CODE = "getSmsResultCode";
	public static final String KEY_GETSMS_PIPLE_ORDER_ID = "getSmsPipleOrderId";
	public static final String KEY_VERTIFYSMSHTTPTYPE = "getSmsHttpType";
	public static final String KEY_VERTIFYSMSURL = "getSmsUrl";
	public static final String KEY_VERTIFYSMSDATATYPE = "getSmsDataType";
	public static final String KEY_VERTIFYSMS_RESULT_CODE = "getSmsResultCode";
	public static final String KEY_PROCESS_SUCCESS_RESULT_CODE = "processSuccessResultCode";
	public static final String KEY_PROCESS_EXIST_ORDER = "processSuccessExistOrder";
	public static final String KEY_PROCESS_RETURN_SUCCESS_CODE = "processSuccessReturnSuccessCode";
	public static final String KEY_PROCESS_RETURN_ERROR_CODE = "processSuccessReturnErrorCode";
	
	protected Map<String,String> getSmsPipleExtMap = new ConcurrentHashMap<String, String>();
	protected Map<String,String> getSmsPipleValueMap = new ConcurrentHashMap<String, String>();
	protected Map<String,List<Filter>> getSmsPipleValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
	protected List<String> getSmsPipleExtOrderInfos = new ArrayList<String>();
	protected Map<String,String> getSmsResultPlatformValueMap = new ConcurrentHashMap<String, String>();
	protected Map<String,List<Filter>> getSmsResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
	protected List<String> getSmsResultExtOrderInfos = new ArrayList<String>();
	
	protected Map<String,String> vertifyPipleExtMap = new ConcurrentHashMap<String, String>();
	protected Map<String,String> vertifyPipleValueMap = new ConcurrentHashMap<String, String>();
	protected Map<String,List<Filter>> vertifyPipleValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
	protected List<String> vertifyPipleExtOrderInfos = new ArrayList<String>();
	protected Map<String,String> vertifyResultPlatformValueMap = new ConcurrentHashMap<String, String>();
	protected Map<String,List<Filter>> vertifyResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
	protected List<String> vertifyResultExtOrderInfos = new ArrayList<String>();
	
	protected Map<String,String> processSuccessPipleExtMap = new ConcurrentHashMap<String, String>();
	protected Map<String,String> processSuccessResultPlatformValueMap = new ConcurrentHashMap<String, String>();
	protected Map<String,List<Filter>> processSuccessPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
	protected List<String> processSuccessExtOrderInfos = new ArrayList<String>();
	
	protected String paySuccessHttpDataType;
	protected String pipleId;
	
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imei = requestBody.optString("imei");
		String imsi = requestBody.optString("imsi");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		String ipProvince = requestBody.optString("ipProvince");
		String appId = requestBody.optString("appId");
		if(StringUtil.isEmpty(fromType)){
			if(StringUtil.isNotEmptyString(extData)  && extData.startsWith(FROM_SDK_EXTDATA)){
				fromType = GlobalConst.FromType.FROM_TYPE_SDK;
			}
			else{
				fromType = GlobalConst.FromType.FROM_TYPE_API;
			}
		}
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)  ){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		} 
		BaseChannelRequest req = new BaseChannelRequest();
		req.setApiKey(apiKey);
		req.setImsi(imsi);
		req.setProductCode(productCode);
		req.setPipleId(pipleId);
		req.setMobile(mobile);
		req.setIpProvince(ipProvince);
		BaseResult bResult = this.accessVerify(req);
		if (bResult != null) {// 返回不为空则校验不通过
			result.put("resultCode",bResult.getResultCode());
			result.put("resultMsg",bResult.getResultMsg());
			return result;
		} else {
			String groupId = KeyHelper.createKey();
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
			TProduct tProduct = this.tProductDao.selectByCode(productCode);
			TChannel tChannel = this.tChannelDao.selectByApiKey(apiKey);
			TOrder order = new TOrder();
			order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
			order.setChannelId(tChannel.getChannelId());
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setImei(imei);
			order.setImsi(imsi);
			order.setMobile(mobile);
			order.setOrderId(KeyHelper.createKey());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setPipleId(getPipleId());
			order.setProductId(tProduct.getProductId());
			order.setProvinceId(req.getProvinceId());
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setGroupId(groupId);
			order.setExtData(extData);
			order.setFromType(NumberUtil.getInteger(fromType));
			order.setAppId(appId);
			for(String key : getSmsPipleExtOrderInfos){
				String value = getSmsPipleValueMap.get(key);
				if(StringUtil.isNotEmptyString(value)){
					TOrderExt ext = new TOrderExt();
					ext.setExtKey(key);
					ext.setExtValue(value);
					ext.setOrderId(order.getOrderId());
					order.addToderExts(ext);
				}
			}
			this.SaveOrderInsert(order);
			result.put("orderId",order.getOrderId());
			requestBody.put("orderId", order.getOrderId());
			try{
				String url= "";
				String pipleUrlresult = "";
				if(GlobalConst.GetDataHttpType.HTTP_GET.equals(getSmsPipleExtMap.get(KEY_GETSMSHTTPTYPE))){
					url = getSmsPipleExtMap.get(KEY_GETSMSURL)+"?";
					for(String key:getSmsPipleValueMap.keySet()){
						String value = getSmsPipleValueMap.get(key);
						if(getSmsPipleValueFilterMap.containsKey(key)){
							List<Filter> filters = getSmsPipleValueFilterMap.get(key);
							try{
								if(filters != null){
									for(Filter f : filters){
										if(f instanceof AbstractBaseJsonObjectFilter){
											AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
											jsonFilter.setReqJsonObject(requestBody);
										}
										if(f instanceof AbstractOrderFilter){
											AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
											orderFilter.setOrder(order);
										}
										if(f instanceof AbstractGetSmsFilter){
											AbstractGetSmsFilter getSmsFilter = (AbstractGetSmsFilter) f;
											getSmsFilter.setBaseChannelRequest(req);
										}
										value = f.filter(value,this);
									}
								}
							}catch(Exception e){
								e.printStackTrace();
								result.put("resultCode","1");
								result.put("resultMsg",e.getMessage());
								return result;
							}
						}
						if(url.endsWith("?")){
							url+=key+"="+value;
						}else{
							url+="&"+key+"="+value;
						}
					}
					statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,url);
					pipleUrlresult = HttpClientUtils.doGet(url,HttpClientUtils.UTF8);
				}else if(GlobalConst.GetDataHttpType.HTTP_POST_FORM.equals(getSmsPipleExtMap.get(KEY_GETSMSHTTPTYPE))){
					url = getSmsPipleExtMap.get(KEY_GETSMSURL);
					Map<String,String> params = new HashMap<String, String>();
					for(String key:getSmsPipleValueMap.keySet()){
						String value = getSmsPipleValueMap.get(key);
						if(getSmsPipleValueFilterMap.containsKey(key)){
							List<Filter> filters = getSmsPipleValueFilterMap.get(key);
							try{
								if(filters != null){
									for(Filter f : filters){
										if(f instanceof AbstractBaseJsonObjectFilter){
											AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
											jsonFilter.setReqJsonObject(requestBody);
										}
										if(f instanceof AbstractOrderFilter){
											AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
											orderFilter.setOrder(order);
										}
										if(f instanceof AbstractGetSmsFilter){
											AbstractGetSmsFilter getSmsFilter = (AbstractGetSmsFilter) f;
											getSmsFilter.setBaseChannelRequest(req);
										}
										value = f.filter(value,this);
									}
								}
							}catch(Exception e){
								e.printStackTrace();
								result.put("resultCode","1");
								result.put("resultMsg",e.getMessage());
								return result;
							}
						}
						params.put(key, value);
					}
					statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,url+","+JSONObject.fromObject(params).toString());
					pipleUrlresult = HttpClientUtils.doPost(url,params,HttpClientUtils.UTF8);
				}else if(GlobalConst.GetDataHttpType.HTTP_POST_BODY_JSON.equals(getSmsPipleExtMap.get(KEY_GETSMSHTTPTYPE))){
					url = getSmsPipleExtMap.get(KEY_GETSMSURL);
					JSONObject params = new JSONObject();
					for(String key:getSmsPipleValueMap.keySet()){
						String value = getSmsPipleValueMap.get(key);
						if(getSmsPipleValueFilterMap.containsKey(key)){
							List<Filter> filters = getSmsPipleValueFilterMap.get(key);
							try{
								if(filters != null){
									for(Filter f : filters){
										if(f instanceof AbstractBaseJsonObjectFilter){
											AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
											jsonFilter.setReqJsonObject(requestBody);
										}
										if(f instanceof AbstractOrderFilter){
											AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
											orderFilter.setOrder(order);
										}
										if(f instanceof AbstractGetSmsFilter){
											AbstractGetSmsFilter getSmsFilter = (AbstractGetSmsFilter) f;
											getSmsFilter.setBaseChannelRequest(req);
										}
										value = f.filter(value,this);
									}
								}
							}catch(Exception e){
								e.printStackTrace();
								result.put("resultCode","1");
								result.put("resultMsg",e.getMessage());
								return result;
							}
						}
						params.put(key, value);
					}
					statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId,url+","+params.toString());
					pipleUrlresult = HttpClientUtils.doPost(url,params.toString(),HttpClientUtils.UTF8);
				}
				if(StringUtil.isNotEmptyString(pipleUrlresult)){
					statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleUrlresult);
					if(GlobalConst.DataType.JSON.equals(getSmsPipleExtMap.get(KEY_GETSMSDATATYPE))){
						JSONObject object = JSONObject.fromObject(pipleUrlresult);
						for(String key : getSmsResultPlatformValueMap.keySet()){
							String value = getSmsResultPlatformValueMap.get(key);
							List<Filter> filters = getSmsResultPlatformValueFilterMap.get(key);
							try{
								if(filters != null){
									for(Filter f : filters){
										if(f instanceof AbstractBaseJsonObjectFilter){
											AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
											jsonFilter.setReqJsonObject(object);
										}
										if(f instanceof AbstractOrderFilter){
											AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
											orderFilter.setOrder(order);
										}
										if(f instanceof AbstractGetSmsFilter){
											AbstractGetSmsFilter getSmsFilter = (AbstractGetSmsFilter) f;
											getSmsFilter.setBaseChannelRequest(req);
										}
										value = f.filter(value,this);
									}
								}
							}catch(Exception e){
								e.printStackTrace();
							}
							getSmsResultPlatformValueMap.put(key,value);
						}
						String orderResult = object.optString(getSmsPipleExtMap.get(KEY_GETSMS_RESULT_CODE));
						if(StringUtil.isNotEmptyString(orderResult)){
							order.setResultCode(orderResult);
						}
						String pipleOrderId = object.optString(getSmsPipleExtMap.get(KEY_GETSMS_PIPLE_ORDER_ID));
						if(StringUtil.isNotEmptyString(pipleOrderId)){
							order.setPipleOrderId(pipleOrderId);
						}
						
					}
				}
				String resultCode = getSmsResultPlatformValueMap.get("resultCode");
				String resultMsg = getSmsResultPlatformValueMap.get("resultMsg");
				if(GlobalConst.Result.SUCCESS.equals(resultCode)){
					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
					order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
					if(StringUtil.isEmpty(resultMsg)){
						resultMsg = "获取成功";
					}
					getSmsResultPlatformValueMap.put("resultMsg",resultMsg);
				}else{
					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					if(StringUtil.isEmpty(resultMsg)){
						resultMsg = "获取失败";
					}
					getSmsResultPlatformValueMap.put("resultMsg",resultMsg);
				}
				for(String key : getSmsResultExtOrderInfos){
					String value = getSmsResultPlatformValueMap.get(key);
					if(StringUtil.isNotEmptyString(value)){
						TOrderExt ext = new TOrderExt();
						ext.setExtKey(key);
						ext.setExtValue(value);
						ext.setOrderId(order.getOrderId());
						order.addToderExts(ext);
					}
				}
				for(String key : getSmsResultPlatformValueMap.keySet()){
					result.put(key, getSmsResultPlatformValueMap.get(key));
				}
			}catch(Exception e){
				e.printStackTrace();
				order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","调用接口失败：接口异常");
			}
			
			this.SaveOrderUpdate(order);
			statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, JSONObject.fromObject(result).toString());
		}
		return result;
	}
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		JSONObject result = new JSONObject();
		String apiKey = requestBody.optString("apiKey");
		
		String orderId = requestBody.optString("orderId");
		String vCode = requestBody.optString("vCode");
		result.put("orderId",orderId);
		if(StringUtil.isEmpty(apiKey) || StringUtil.isEmpty(orderId) || StringUtil.isEmpty(vCode)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
		}else{
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder == null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
			}else{
				// 校验用户日月限  防止第1步与第2步间隔时间内多次请求造成校验误差
				if(isUseableTradeDayAndMonth()){
					TProduct product = tProductDao.selectByPrimaryKey(tOrder.getProductId());
					// 用户日月限
					int checkResult = transCheckMobile(tOrder.getPipleId(), tOrder.getImsi(), tOrder.getMobile(),product.getProductCode());
					if(checkResult!=GlobalConst.CheckResult.PASS){
						result.put("resultCode",checkResult+"");
						result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(checkResult));
						return result;
					}
				}
				try{
					statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
					String url= "";
					String pipleUrlresult = "";
					if(GlobalConst.GetDataHttpType.HTTP_GET.equals(vertifyPipleExtMap.get(KEY_VERTIFYSMSDATATYPE))){
						url = vertifyPipleExtMap.get(KEY_VERTIFYSMSURL)+"?";
						for(String key:vertifyPipleValueMap.keySet()){
							String value = vertifyPipleValueMap.get(key);
							if(vertifyPipleValueFilterMap.containsKey(key)){
								List<Filter> filters = vertifyPipleValueFilterMap.get(key);
								try{
									if(filters != null){
										for(Filter f : filters){
											if(f instanceof AbstractBaseJsonObjectFilter){
												AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
												jsonFilter.setReqJsonObject(requestBody);
											}
											if(f instanceof AbstractOrderFilter){
												AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
												orderFilter.setOrder(tOrder);
											}
											value = f.filter(value,this);
										}
									}
								}catch(Exception e){
									e.printStackTrace();
									result.put("resultCode","1");
									result.put("resultMsg",e.getMessage());
									return result;
								}
							}
							if(url.endsWith("?")){
								url+=key+"="+value;
							}else{
								url+="&"+key+"="+value;
							}
						}
						statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), url);
						pipleUrlresult = HttpClientUtils.doGet(url,HttpClientUtils.UTF8);
					}else if(GlobalConst.GetDataHttpType.HTTP_POST_FORM.equals(vertifyPipleExtMap.get(KEY_VERTIFYSMSHTTPTYPE))){
						url = vertifyPipleExtMap.get(KEY_VERTIFYSMSURL);
						Map<String,String> params = new HashMap<String, String>();
						for(String key:vertifyPipleValueMap.keySet()){
							String value = vertifyPipleValueMap.get(key);
							if(vertifyPipleValueFilterMap.containsKey(key)){
								List<Filter> filters = vertifyPipleValueFilterMap.get(key);
								try{
									if(filters != null){
										for(Filter f : filters){
											if(f instanceof AbstractBaseJsonObjectFilter){
												AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
												jsonFilter.setReqJsonObject(requestBody);
											}
											if(f instanceof AbstractOrderFilter){
												AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
												orderFilter.setOrder(tOrder);
											}
											value = f.filter(value,this);
										}
									}
								}catch(Exception e){
									e.printStackTrace();
									result.put("resultCode","1");
									result.put("resultMsg",e.getMessage());
									return result;
								}
							}
							params.put(key, value);
						}
						statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), url+","+JSONObject.fromObject(params).toString());
						pipleUrlresult = HttpClientUtils.doPost(url,params,HttpClientUtils.UTF8);
					}else if(GlobalConst.GetDataHttpType.HTTP_POST_BODY_JSON.equals(vertifyPipleExtMap.get(KEY_VERTIFYSMSHTTPTYPE))){
						url = vertifyPipleExtMap.get(KEY_VERTIFYSMSURL);
						JSONObject params = new JSONObject();
						for(String key:vertifyPipleValueMap.keySet()){
							String value = vertifyPipleValueMap.get(key);
							if(vertifyPipleValueFilterMap.containsKey(key)){
								List<Filter> filters = vertifyPipleValueFilterMap.get(key);
								try{
									if(filters != null){
										for(Filter f : filters){
											if(f instanceof AbstractBaseJsonObjectFilter){
												AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
												jsonFilter.setReqJsonObject(requestBody);
											}
											if(f instanceof AbstractOrderFilter){
												AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
												orderFilter.setOrder(tOrder);
											}
											value = f.filter(value,this);
										}
									}
								}catch(Exception e){
									e.printStackTrace();
									result.put("resultCode","1");
									result.put("resultMsg",e.getMessage());
									return result;
								}
							}
							params.put(key, value);
						}
						statistics( STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), url+","+params.toString());
						pipleUrlresult = HttpClientUtils.doPost(url,params.toString(),HttpClientUtils.UTF8);
					}
					if(StringUtil.isNotEmptyString(pipleUrlresult)){
						statistics(STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(),pipleUrlresult);
						if(GlobalConst.DataType.JSON.equals(vertifyPipleExtMap.get(KEY_VERTIFYSMSDATATYPE))){
							JSONObject object = JSONObject.fromObject(pipleUrlresult);
							for(String key : vertifyResultPlatformValueMap.keySet()){
								String value = vertifyResultPlatformValueMap.get(key);
								List<Filter> filters = vertifyResultPlatformValueFilterMap.get(key);
								try{
									if(filters != null){
										for(Filter f : filters){
											if(f instanceof AbstractBaseJsonObjectFilter){
												AbstractBaseJsonObjectFilter jsonFilter = (AbstractBaseJsonObjectFilter) f;
												jsonFilter.setReqJsonObject(object);
											}
											if(f instanceof AbstractOrderFilter){
												AbstractOrderFilter orderFilter = (AbstractOrderFilter) f;
												orderFilter.setOrder(tOrder);
											}
											value = f.filter(value,this);
										}
									}
								}catch(Exception e){
									e.printStackTrace();
								}
								vertifyResultPlatformValueMap.put(key,value);
							}
							String orderResult = object.optString(vertifyPipleExtMap.get(KEY_VERTIFYSMS_RESULT_CODE));
							if(StringUtil.isNotEmptyString(orderResult)){
								tOrder.setResultCode(orderResult);
							}
							
						}
					}
					String resultCode = vertifyResultPlatformValueMap.get("resultCode");
					String resultMsg = vertifyResultPlatformValueMap.get("resultMsg");
					if(GlobalConst.Result.SUCCESS.equals(resultCode)){
						tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_SUCCESS);
						tOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						if(StringUtil.isEmpty(resultMsg)){
							resultMsg = "提交成功";
						}
						vertifyResultPlatformValueMap.put("resultMsg",resultMsg);
					}else{
						tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						if(StringUtil.isEmpty(resultMsg)){
							resultMsg = "提交失败";
						}
						vertifyResultPlatformValueMap.put("resultMsg",resultMsg);
					}
					for(String key : vertifyResultExtOrderInfos){
						String value = vertifyResultPlatformValueMap.get(key);
						if(StringUtil.isNotEmptyString(value)){
							TOrderExt ext = new TOrderExt();
							ext.setExtKey(key);
							ext.setExtValue(value);
							ext.setOrderId(tOrder.getOrderId());
							tOrder.addToderExts(ext);
						}
					}
					for(String key : vertifyResultPlatformValueMap.keySet()){
						result.put(key, vertifyResultPlatformValueMap.get(key));
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","接口内部异常");
					tOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
					tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				}
			}
			this.SaveOrderUpdate(tOrder);
			statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), JSONObject.fromObject(result).toString());
		}
		return result;
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		for(String key : processSuccessResultPlatformValueMap.keySet()){
			String value = processSuccessResultPlatformValueMap.get(key);
			List<Filter> filters = processSuccessPlatformValueFilterMap.get(key);
			try{
				if(filters != null){
					for(Filter f : filters){
						if(f instanceof AbstractBaseJsonObjectFilter){
							AbstractBaseJsonObjectFilter processSuccessFilter = (AbstractBaseJsonObjectFilter) f;
							processSuccessFilter.setReqJsonObject(requestBody);
						}
						value = f.filter(value,this);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			processSuccessResultPlatformValueMap.put(key,value);
		}
		String status = processSuccessResultPlatformValueMap.get("status");
		String orderId = processSuccessResultPlatformValueMap.get("orderId");
		String pipleOrderId = processSuccessResultPlatformValueMap.get("pipleOrderId");
		String mobile = processSuccessResultPlatformValueMap.get("mobile");
		String productId = processSuccessResultPlatformValueMap.get("productId");
		String port = processSuccessResultPlatformValueMap.get("port");
		String apiKey = processSuccessResultPlatformValueMap.get("apiKey");
		String extData = processSuccessResultPlatformValueMap.get("extData");
		TOrder order = null;
		
		boolean isExitOrder = NumberUtil.getBoolean(processSuccessPipleExtMap.get(KEY_PROCESS_EXIST_ORDER));
		if(!isExitOrder){
			if(StringUtil.isNotEmptyString(pipleOrderId)){
				order = tOrderDao.selectByPipleOrderId(pipleOrderId);
			}
			if(order == null){
				order = new TOrder();
				order.setOrderId(KeyHelper.createKey());
				order.setGroupId(KeyHelper.createKey());
				order.setPipleId(getPipleId());
				order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			}
		}else{
			if(StringUtil.isNotEmptyString(orderId)){
				order = tOrderDao.selectByPrimaryKey(orderId);
			}else if(StringUtil.isNotEmptyString(pipleOrderId)){
				order = tOrderDao.selectByPipleOrderId(pipleOrderId);
			}
		}
		if(order != null){
			if(order.getOrderStatus()!=GlobalConst.OrderStatus.SUCCESS){ // 订单未同步过，成功同步去重处理
				statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
				String orderResult = requestBody.optString(processSuccessPipleExtMap.get(KEY_PROCESS_SUCCESS_RESULT_CODE));
				if(StringUtil.isNotEmptyString(orderResult)){
					order.setResultCode(orderResult);
				}
				if(StringUtil.isNotEmptyString(pipleOrderId)){
					order.setPipleOrderId(pipleOrderId);
				}
				if(StringUtil.isNotEmptyString(mobile)){
					order.setMobile(mobile);
					order.setProvinceId(mobileSegmentService.getProvinceIdByMobile(mobile));
				}
				if(StringUtil.isNotEmptyString(productId)){
					order.setProductId(productId);
					TProduct tProduct = tProductDao.selectByPrimaryKey(productId);
					order.setAmount(new BigDecimal(tProduct.getPrice()/100.0));
				}
				if(StringUtil.isNotEmptyString(apiKey)){
					TChannel channel = tChannelDao.selectByApiKey(apiKey);
					order.setChannelId(channel.getChannelId());
				}
				if(order.getFromType() == 0){
					order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
				}
				if(StringUtil.isNotEmptyString(extData)){
					order.setExtData(extData);
				}
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
				TChannelPiple cp = null;
				if(StringUtil.isNotEmptyString(order.getChannelId())){
					TChannelPipleKey pkey = new TChannelPipleKey();
					pkey.setChannelId(order.getChannelId());
					pkey.setPipleId(order.getPipleId());
					cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
				}else{
					List<TChannelPiple> channelPiples = tChannelPipleDao.getListByPipleId(getPipleId());
					if(channelPiples .size() >0){
						cp = channelPiples.get(0);
						order.setChannelId(cp.getChannelId());
					}
				}
				for(String key : processSuccessExtOrderInfos){
					String value = processSuccessResultPlatformValueMap.get(key);
					if(StringUtil.isNotEmptyString(value)){
						TOrderExt ext = new TOrderExt();
						ext.setExtKey(key);
						ext.setExtValue(value);
						ext.setOrderId(order.getOrderId());
						order.addToderExts(ext);
					}
				}
				boolean isSend = false; // 扣量标识
				if(GlobalConst.Result.SUCCESS.equals(status)){
					order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
					order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
					order.setModTime(DateTimeUtils.getCurrentTime());
					order.setCompleteTime(DateTimeUtils.getCurrentTime());
					doWhenPaySuccess(order);
					if(cp != null){
						boolean bDeducted  = order.deduct(cp.getVolt());  
						if(!bDeducted){ // 不扣量 通知渠道
							isSend = true;
							
						}
					}
				}else{
					order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				}
				if(isSend){
					String channelResult = notifyChannelAll(cp.getNotifyUrl(), order, port);
					if("ok".equals(channelResult)){
						order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
					}else{
						order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
					}
				}
				if(isExitOrder){
					SaveOrderUpdate(order);
				}else{
					SaveOrderInsert(order);
				}
			}
			return processSuccessPipleExtMap.get(KEY_PROCESS_RETURN_SUCCESS_CODE);
		}else{
			return processSuccessPipleExtMap.get(KEY_PROCESS_RETURN_ERROR_CODE);
		}
	}
	@Override
	public String getPipleId() {
		return this.pipleId;
	}
	public void setPipleId(String pipleId) {
		this.pipleId = pipleId;
	}
	
	public String getPaySuccessHttpDataType() {
		return paySuccessHttpDataType;
	}
	public void setPaySuccessHttpDataType(String paySuccessHttpDataType) {
		this.paySuccessHttpDataType = paySuccessHttpDataType;
	}
	
	public Map<String, String> getGetSmsPipleExtMap() {
		return getSmsPipleExtMap;
	}
	public void setGetSmsPipleExtMap(Map<String, String> getSmsPipleExtMap) {
		this.getSmsPipleExtMap = getSmsPipleExtMap;
	}
	public Map<String, String> getGetSmsPipleValueMap() {
		return getSmsPipleValueMap;
	}
	public void setGetSmsPipleValueMap(Map<String, String> getSmsPipleValueMap) {
		this.getSmsPipleValueMap = getSmsPipleValueMap;
	}
	public Map<String, List<Filter>> getGetSmsPipleValueFilterMap() {
		return getSmsPipleValueFilterMap;
	}
	public void setGetSmsPipleValueFilterMap(Map<String, List<Filter>> getSmsPipleValueFilterMap) {
		this.getSmsPipleValueFilterMap = getSmsPipleValueFilterMap;
	}
	public Map<String, String> getGetSmsResultPlatformValueMap() {
		return getSmsResultPlatformValueMap;
	}
	public void setGetSmsResultPlatformValueMap(Map<String, String> getSmsResultPlatformValueMap) {
		this.getSmsResultPlatformValueMap = getSmsResultPlatformValueMap;
	}
	public Map<String, List<Filter>> getGetSmsResultPlatformValueFilterMap() {
		return getSmsResultPlatformValueFilterMap;
	}
	public void setGetSmsResultPlatformValueFilterMap(Map<String, List<Filter>> getSmsResultPlatformValueFilterMap) {
		this.getSmsResultPlatformValueFilterMap = getSmsResultPlatformValueFilterMap;
	}
	public Map<String, String> getProcessSuccessPipleExtMap() {
		return processSuccessPipleExtMap;
	}
	public void setProcessSuccessPipleExtMap(Map<String, String> processSuccessPipleExtMap) {
		this.processSuccessPipleExtMap = processSuccessPipleExtMap;
	}
	
	public Map<String, String> getProcessSuccessResultPlatformValueMap() {
		return processSuccessResultPlatformValueMap;
	}
	public void setProcessSuccessResultPlatformValueMap(Map<String, String> processSuccessResultPlatformValueMap) {
		this.processSuccessResultPlatformValueMap = processSuccessResultPlatformValueMap;
	}
	public Map<String, List<Filter>> getProcessSuccessPlatformValueFilterMap() {
		return processSuccessPlatformValueFilterMap;
	}
	public void setProcessSuccessPlatformValueFilterMap(Map<String, List<Filter>> processSuccessPlatformValueFilterMap) {
		this.processSuccessPlatformValueFilterMap = processSuccessPlatformValueFilterMap;
	}
	
	public List<String> getGetSmsPipleExtOrderInfos() {
		return getSmsPipleExtOrderInfos;
	}
	public void setGetSmsPipleExtOrderInfos(List<String> getSmsPipleExtOrderInfos) {
		this.getSmsPipleExtOrderInfos = getSmsPipleExtOrderInfos;
	}
	public List<String> getGetSmsResultExtOrderInfos() {
		return getSmsResultExtOrderInfos;
	}
	public void setGetSmsResultExtOrderInfos(List<String> getSmsResultExtOrderInfos) {
		this.getSmsResultExtOrderInfos = getSmsResultExtOrderInfos;
	}
	public List<String> getProcessSuccessExtOrderInfos() {
		return processSuccessExtOrderInfos;
	}
	public void setProcessSuccessExtOrderInfos(List<String> processSuccessExtOrderInfos) {
		this.processSuccessExtOrderInfos = processSuccessExtOrderInfos;
	}
	
	public Map<String, String> getVertifyPipleExtMap() {
		return vertifyPipleExtMap;
	}
	public void setVertifyPipleExtMap(Map<String, String> vertifyPipleExtMap) {
		this.vertifyPipleExtMap = vertifyPipleExtMap;
	}
	public Map<String, String> getVertifyPipleValueMap() {
		return vertifyPipleValueMap;
	}
	public void setVertifyPipleValueMap(Map<String, String> vertifyPipleValueMap) {
		this.vertifyPipleValueMap = vertifyPipleValueMap;
	}
	public Map<String, List<Filter>> getVertifyPipleValueFilterMap() {
		return vertifyPipleValueFilterMap;
	}
	public void setVertifyPipleValueFilterMap(Map<String, List<Filter>> vertifyPipleValueFilterMap) {
		this.vertifyPipleValueFilterMap = vertifyPipleValueFilterMap;
	}
	public List<String> getVertifyPipleExtOrderInfos() {
		return vertifyPipleExtOrderInfos;
	}
	public void setVertifyPipleExtOrderInfos(List<String> vertifyPipleExtOrderInfos) {
		this.vertifyPipleExtOrderInfos = vertifyPipleExtOrderInfos;
	}
	public Map<String, String> getVertifyResultPlatformValueMap() {
		return vertifyResultPlatformValueMap;
	}
	public void setVertifyResultPlatformValueMap(Map<String, String> vertifyResultPlatformValueMap) {
		this.vertifyResultPlatformValueMap = vertifyResultPlatformValueMap;
	}
	public Map<String, List<Filter>> getVertifyResultPlatformValueFilterMap() {
		return vertifyResultPlatformValueFilterMap;
	}
	public void setVertifyResultPlatformValueFilterMap(Map<String, List<Filter>> vertifyResultPlatformValueFilterMap) {
		this.vertifyResultPlatformValueFilterMap = vertifyResultPlatformValueFilterMap;
	}
	public List<String> getVertifyResultExtOrderInfos() {
		return vertifyResultExtOrderInfos;
	}
	public void setVertifyResultExtOrderInfos(List<String> vertifyResultExtOrderInfos) {
		this.vertifyResultExtOrderInfos = vertifyResultExtOrderInfos;
	}
	public static interface Filter{
		String filter(String filter,CommonChannelService service)throws Exception;
	}
	public static abstract class AbstractFilter implements Filter{
		@Override
		public String filter(String filter,CommonChannelService service)throws Exception {
			return null;
		}
	}
	public static abstract class AbstractGetSmsFilter extends AbstractOrderFilter{

		protected TOrder order;
		protected BaseChannelRequest baseChannelRequest;
		public BaseChannelRequest getBaseChannelRequest() {
			return baseChannelRequest;
		}
		public void setBaseChannelRequest(BaseChannelRequest baseChannelRequest) {
			this.baseChannelRequest = baseChannelRequest;
		}

		public TOrder getOrder() {
			return order;
		}

		public void setOrder(TOrder order) {
			this.order = order;
		}

		@Override
		public String filter(String filter,CommonChannelService service)throws Exception {
			return null;
		}
	}
	public static abstract class AbstractOrderFilter extends AbstractBaseJsonObjectFilter{

		protected TOrder order;

		public TOrder getOrder() {
			return order;
		}

		public void setOrder(TOrder order) {
			this.order = order;
		}

		@Override
		public String filter(String filter,CommonChannelService service)throws Exception {
			return null;
		}
	}
	public static abstract class AbstractBaseJsonObjectFilter implements Filter{

		protected JSONObject reqJsonObject;

		public JSONObject getReqJsonObject() {
			return reqJsonObject;
		}

		public void setReqJsonObject(JSONObject reqJsonObject) {
			this.reqJsonObject = reqJsonObject;
		}

		@Override
		public String filter(String filter,CommonChannelService service)throws Exception {
			return null;
		}
	}
	public static class EqualsFilter extends AbstractFilter{
		private String data;
		private String eqData;
		private String notEqualData;
		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}
		

		public String getEqData() {
			return eqData;
		}

		public void setEqData(String eqData) {
			this.eqData = eqData;
		}

		public String getNotEqualData() {
			return notEqualData;
		}

		public void setNotEqualData(String notEqualData) {
			this.notEqualData = notEqualData;
		}

		@Override
		public String filter(String filter,CommonChannelService service) throws Exception {
			if(StringUtil.equals(filter, this.data)){
				return eqData;
			}
			return notEqualData;
		}
	}
	public static class MustFilter extends AbstractFilter{

		@Override
		public String filter(String filter,CommonChannelService service) throws Exception {
			if(StringUtil.isEmpty(filter)){
				throw new Exception("参数不可以为空");
			}
			return filter;
		}
	}
	public static class SpiltFilter extends AbstractFilter{
		private String splitStr;
		private int index;

		public String getSplitStr() {
			return splitStr;
		}

		public void setSplitStr(String splitStr) {
			this.splitStr = splitStr;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}
		@Override
		public String filter(String filter,CommonChannelService service) {
			return filter.split(this.splitStr)[index];
		}
	}
	public static class SubstringFilter extends AbstractFilter{
		private int start;
		private int end ;

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getEnd() {
			return end;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		@Override
		public String filter(String filter,CommonChannelService service) {
			if(end == 0){
				return filter.substring(start);
			}else{
				return filter.substring(start,end);
			}
		}
	}
	public static class AddPrefixFilter extends AbstractFilter{
		public static int POSITION_LEFT = 1;
		public static int POSITION_RIGHT = 2;
		private int position = POSITION_LEFT;
		private String prefix;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public String filter(String filter,CommonChannelService service) {
			if(this.position == POSITION_LEFT){
				return this.prefix+filter;
			}else{
				return filter+this.prefix;
			}
		}
	}
	public static class OrderFilter extends AbstractOrderFilter{

		@Override
		public String filter(String filter,CommonChannelService service) {
			return order.getOrderId();
		}
	}
	public static class ProductFilter extends AbstractBaseJsonObjectFilter{

		public static final int UNIT_YUAN = 1;
		public static final int UNIT_FEN = 2;
		public static final int PIPLE_PRODUCT_CODE = 3;
		public static final int PRODUCT_ID_FROM_PIPLE = 4;
		private int unit;
		
		public int getUnit() {
			return unit;
		}
		public void setUnit(int unit) {
			this.unit = unit;
		}
		@Override
		public String filter(String filter,CommonChannelService service) {
			String result = "";
			int price = 0;
			if(this.unit == UNIT_YUAN){
				price = NumberUtil.getInteger(filter)*100;
				if(price == 1){
					result = "001";
				}else if(price == 10){
					result = "01";
				}else{
					result = (price/100)+"";
				}
			}else if(this.unit == UNIT_FEN){
				price = NumberUtil.getInteger(filter);
				if(price == 1){
					result = "001";
				}else if(price == 10){
					result = "01";
				}else{
					result = (price/100)+"";
				}
			}else if(this.unit == PIPLE_PRODUCT_CODE){
				
				TProduct tProduct = service.tProductDao.selectByCode(reqJsonObject.optString("productCode"));
				TPipleProductKey key = new TPipleProductKey();
				key.setPipleId(service.getPipleId());
				key.setProductId(tProduct.getProductId());
				TPipleProduct pipleProduct = service.tPipleProductDao.selectByPrimaryKey(key);
				result = pipleProduct.getPipleProductCode();
			}else if(this.unit == PRODUCT_ID_FROM_PIPLE){
				TPipleProduct key = new TPipleProduct();
				key.setPipleId(service.getPipleId());
				key.setPipleProductCode(filter);
				TPipleProduct pipleProduct = service.tPipleProductDao.selectByPipleProductCode(key);
				result = pipleProduct.getProductId();
			}
			return result;
		}
	}
	public static class ProvinceFilter extends AbstractGetSmsFilter{

		@Override
		public String filter(String filter,CommonChannelService service) {
		    String pipleProvinceCode = filter;
		    TPipleProvinceKey key = new TPipleProvinceKey();
		    key.setProvinceId(baseChannelRequest.getProvinceId());
		    key.setPipleId(baseChannelRequest.getPipleId());
			TPipleProvince tPipleProvince = service.tPipleProvinceDao.selectByPrimaryKey(key);
			if(tPipleProvince != null){
				pipleProvinceCode = tPipleProvince.getPipleProvinceCode(); 
			}
			return pipleProvinceCode;
		}
	}
	public static class CopyValueFilter extends AbstractBaseJsonObjectFilter{
		private String[] requestKey;

		public String[] getRequestKey() {
			return requestKey;
		}


		public void setRequestKey(String... requestKey) {
			this.requestKey = requestKey;
		}


		@Override
		public String filter(String filter,CommonChannelService service) {
			for(String k : requestKey){
				String value = getValue(k, reqJsonObject);
				if(StringUtil.isNotEmptyString(value)){
					return value;
				}
			}
			return filter;
		}
		
		private String getValue(String key,JSONObject object){
			if(object.containsKey(key)){
				return object.optString(key);
			}else{
				String value = "";
				for(Object obj: object.values()){
					if(obj instanceof JSONObject){
						value = getValue(key, object);
					}else if(obj instanceof JSONArray){
						JSONArray array = (JSONArray)obj;
						if(array.size() >0){
							value = getValue(key, array.getJSONObject(0));
						}
					}
				}
				return value;
			}
		}
	}
}
