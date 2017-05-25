package com.qy.sp.fee.modules.piplecode.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.modules.piplecode.base.ChannelManager;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.AddPrefixFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.CopyValueFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.EqualsFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.Filter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.OrderFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.ProductFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.ProvinceFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.SpiltFilter;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.SubstringFilter;

@Service
public class InitCommonPiples implements BeanFactoryAware {
	private BeanFactory factory;
	@PostConstruct
	public void init(){
		initDemo();
		initLensu();
		initKuandai();
		
	}
	private void initDemo(){
		CommonChannelService service1 = (CommonChannelService)factory.getBean("commonChannelService");
		service1.setPipleId("14817848519723436323596");
		service1.setPaySuccessHttpDataType(GlobalConst.GetDataHttpType.HTTP_GET);
		
		Map<String,String> getSmsPipleExtMap = new ConcurrentHashMap<String, String>();
		getSmsPipleExtMap.put(CommonChannelService.KEY_GETSMSHTTPTYPE,GlobalConst.GetDataHttpType.HTTP_GET);
		getSmsPipleExtMap.put(CommonChannelService.KEY_GETSMSDATATYPE,GlobalConst.DataType.JSON);
		getSmsPipleExtMap.put(CommonChannelService.KEY_GETSMSURL,"http://120.25.83.178:8099/rdo_16122001_f.php");
		getSmsPipleExtMap.put(CommonChannelService.KEY_GETSMS_RESULT_CODE,"code");
		getSmsPipleExtMap.put(CommonChannelService.KEY_GETSMS_PIPLE_ORDER_ID,"linkid");
		
		Map<String,String> getSmsPipleValueMap = new ConcurrentHashMap<String, String>();
		getSmsPipleValueMap.put("mobile", "");
		getSmsPipleValueMap.put("param", "");
		getSmsPipleValueMap.put("fee", "");
		getSmsPipleValueMap.put("channel", "hefeng");
		getSmsPipleValueMap.put("prov", "");
		getSmsPipleValueMap.put("appname", "appname");
		getSmsPipleValueMap.put("itemname", "itemname");
		
		Map<String,List<Filter>> getSmsPipleValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
		List<Filter> mobileFilters = new ArrayList<Filter>();
		CopyValueFilter mobileCopyFilter = new CopyValueFilter();
		mobileCopyFilter.setRequestKey("mobile");
		mobileFilters.add(mobileCopyFilter);
		getSmsPipleValueFilterMap.put("mobile",mobileFilters);
		List<Filter> paramFilters = new ArrayList<Filter>();
		OrderFilter paramOrderFilter = new OrderFilter();
		paramFilters.add(paramOrderFilter);
		AddPrefixFilter addPrefixFilter = new AddPrefixFilter();
		addPrefixFilter.setPrefix("mh01");
		paramFilters.add(addPrefixFilter);
		getSmsPipleValueFilterMap.put("param", paramFilters);
		List<Filter> productFilters = new ArrayList<Filter>();
		ProductFilter productFilter = new ProductFilter();
		productFilters.add(productFilter);
		getSmsPipleValueFilterMap.put("fee", productFilters);
		List<Filter> provinceFilters = new ArrayList<Filter>();
		ProvinceFilter provinceFilter = new ProvinceFilter();
		provinceFilters.add(provinceFilter);
		getSmsPipleValueFilterMap.put("prov", provinceFilters);
		
		Map<String,String> getSmsResultPlatformValueMap = new ConcurrentHashMap<String, String>();
		getSmsResultPlatformValueMap.put("resultCode", "");
		getSmsResultPlatformValueMap.put("resultMsg", "");
		getSmsResultPlatformValueMap.put("nexturl", "");
		getSmsResultPlatformValueMap.put("type", "");
		
		Map<String,List<Filter>> getSmsResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
		List<Filter> resultCodeFilters = new ArrayList<Filter>();
		CopyValueFilter resultCodeCopyFilter = new CopyValueFilter();
		resultCodeCopyFilter.setRequestKey("code");
		resultCodeFilters.add(resultCodeCopyFilter);
		EqualsFilter resultCodeFilter = new EqualsFilter();
		resultCodeFilter.setData("00000");
		resultCodeFilter.setEqData(GlobalConst.Result.SUCCESS);
		resultCodeFilter.setNotEqualData(GlobalConst.Result.ERROR);
		resultCodeFilters.add(resultCodeFilter);
		getSmsResultPlatformValueFilterMap.put("resultCode", resultCodeFilters);
		List<Filter> resultMsgFilters = new ArrayList<Filter>();
		CopyValueFilter resultMsgFilter = new CopyValueFilter();
		resultMsgFilter.setRequestKey("desc","reMsg");
		resultMsgFilters.add(resultMsgFilter);
		getSmsResultPlatformValueFilterMap.put("resultMsg", resultMsgFilters);
		List<Filter> nextUrlFilters = new ArrayList<Filter>();
		CopyValueFilter nextUrlFilter = new CopyValueFilter();
		nextUrlFilter.setRequestKey("nexturl");
		nextUrlFilters.add(nextUrlFilter);
		getSmsResultPlatformValueFilterMap.put("nexturl", nextUrlFilters);
		List<Filter> typeFilters = new ArrayList<Filter>();
		CopyValueFilter typeFilter = new CopyValueFilter();
		typeFilter.setRequestKey("type");
		typeFilters.add(typeFilter);
		getSmsResultPlatformValueFilterMap.put("type", typeFilters);
		
		List<String> getSmsPipleExtOrderInfos = new ArrayList<String>();
		List<String> getSmsResultExtOrderInfos = new ArrayList<String>();
		
		
		Map<String,String> vertifyPipleExtMap = new ConcurrentHashMap<String, String>();
		Map<String,String> vertifyPipleValueMap = new ConcurrentHashMap<String, String>();
		Map<String,List<Filter>> vertifyPipleValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
		List<String> vertifyPipleExtOrderInfos = new ArrayList<String>();
		Map<String,String> vertifyResultPlatformValueMap = new ConcurrentHashMap<String, String>();
		Map<String,List<Filter>> vertifyResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
		List<String> vertifyResultExtOrderInfos = new ArrayList<String>();
		
		
		Map<String,String> processSuccessPipleExtMap = new ConcurrentHashMap<String, String>();
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_SUCCESS_RESULT_CODE, "status");
		
		service1.setGetSmsPipleExtMap(getSmsPipleExtMap);
		service1.setGetSmsPipleValueFilterMap(getSmsPipleValueFilterMap);
		service1.setGetSmsPipleValueMap(getSmsPipleValueMap);
		service1.setGetSmsResultPlatformValueFilterMap(getSmsResultPlatformValueFilterMap);
		service1.setGetSmsResultPlatformValueMap(getSmsResultPlatformValueMap);
		ChannelManager.getInstance().putChannelService(service1.getPipleId(), service1);
	}
	private void initLensu(){
		CommonChannelService service1 = (CommonChannelService)factory.getBean("commonChannelService");
		service1.setPipleId("14827218782973045073543");
		service1.setPaySuccessHttpDataType(GlobalConst.GetDataHttpType.HTTP_GET);
		
		
		Map<String,String> processSuccessPipleExtMap = new ConcurrentHashMap<String, String>();
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_SUCCESS_RESULT_CODE,GlobalConst.Result.SUCCESS);
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_EXIST_ORDER,"false");
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_RETURN_SUCCESS_CODE,"OK");
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_RETURN_ERROR_CODE,"ERROR");
		
		Map<String,String> processSuccessResultPlatformValueMap = new ConcurrentHashMap<String, String>();
		processSuccessResultPlatformValueMap.put("status", GlobalConst.Result.SUCCESS);
		processSuccessResultPlatformValueMap.put("orderId", "");
		processSuccessResultPlatformValueMap.put("pipleOrderId", "");
		processSuccessResultPlatformValueMap.put("mobile", "");
		processSuccessResultPlatformValueMap.put("productId", "");
		processSuccessResultPlatformValueMap.put("port", "");
		processSuccessResultPlatformValueMap.put("longnumber", "");
		processSuccessResultPlatformValueMap.put("stime", "");
		processSuccessResultPlatformValueMap.put("etime", "");
		
		Map<String,List<Filter>> processSuccessResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
		List<Filter> pipleOrderIdFilters = new ArrayList<Filter>();
		CopyValueFilter linkIdCopyFilter = new CopyValueFilter();
		linkIdCopyFilter.setRequestKey("linkid");
		pipleOrderIdFilters.add(linkIdCopyFilter);
		List<Filter> mobileFilters = new ArrayList<Filter>();
		CopyValueFilter mobileFilter = new CopyValueFilter();
		mobileFilter.setRequestKey("mobile");
		mobileFilters.add(mobileFilter);
		List<Filter> productFilters = new ArrayList<Filter>();
		CopyValueFilter productcopyFilter = new CopyValueFilter();
		productcopyFilter.setRequestKey("fees");
		ProductFilter productIdYuanFilter = new ProductFilter();
		productIdYuanFilter.setUnit(ProductFilter.UNIT_YUAN);
		productFilters.add(productcopyFilter);
		productFilters.add(productIdYuanFilter);
		List<Filter> longnumberFilters = new ArrayList<Filter>();
		CopyValueFilter longnumberFilter = new CopyValueFilter();
		longnumberFilter.setRequestKey("longnumber");
		longnumberFilters.add(longnumberFilter);
		List<Filter> stimeFilters = new ArrayList<Filter>();
		CopyValueFilter stimeFilter = new CopyValueFilter();
		stimeFilter.setRequestKey("stime");
		stimeFilters.add(stimeFilter);
		List<Filter> etimeFilters = new ArrayList<Filter>();
		CopyValueFilter etimeCopyFilter = new CopyValueFilter();
		etimeCopyFilter.setRequestKey("etime");
		etimeFilters.add(etimeCopyFilter);
		processSuccessResultPlatformValueFilterMap.put("pipleOrderId", pipleOrderIdFilters);
		processSuccessResultPlatformValueFilterMap.put("mobile", mobileFilters);
		processSuccessResultPlatformValueFilterMap.put("productId", productFilters);
		processSuccessResultPlatformValueFilterMap.put("port", longnumberFilters);
		processSuccessResultPlatformValueFilterMap.put("stime", stimeFilters); 
		processSuccessResultPlatformValueFilterMap.put("etime", etimeFilters); 
		
		List<String> processSuccessExtOrderInfos = new ArrayList<String>();
		processSuccessExtOrderInfos.add("port");
		processSuccessExtOrderInfos.add("stime");
		processSuccessExtOrderInfos.add("etime");
		
		service1.setProcessSuccessPipleExtMap(processSuccessPipleExtMap);
		service1.setProcessSuccessResultPlatformValueMap(processSuccessResultPlatformValueMap);
		service1.setProcessSuccessPlatformValueFilterMap(processSuccessResultPlatformValueFilterMap);
		service1.setProcessSuccessExtOrderInfos(processSuccessExtOrderInfos);
		ChannelManager.getInstance().putChannelService(service1.getPipleId(), service1);
	}
	private void initKuandai(){
		CommonChannelService service1 = (CommonChannelService)factory.getBean("commonChannelService");
		service1.setPipleId("14827354770130719977156");
		service1.setPaySuccessHttpDataType(GlobalConst.GetDataHttpType.HTTP_GET);
		
		
		Map<String,String> processSuccessPipleExtMap = new ConcurrentHashMap<String, String>();
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_SUCCESS_RESULT_CODE,"code");
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_EXIST_ORDER,"false");
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_RETURN_SUCCESS_CODE,"OK");
		processSuccessPipleExtMap.put(CommonChannelService.KEY_PROCESS_RETURN_ERROR_CODE,"ERROR");
		
		Map<String,String> processSuccessResultPlatformValueMap = new ConcurrentHashMap<String, String>();
		processSuccessResultPlatformValueMap.put("status","");
		processSuccessResultPlatformValueMap.put("orderId", "");
		processSuccessResultPlatformValueMap.put("pipleOrderId", "");
		processSuccessResultPlatformValueMap.put("mobile", "");
		processSuccessResultPlatformValueMap.put("productId", "");
		processSuccessResultPlatformValueMap.put("port", "");
		processSuccessResultPlatformValueMap.put("apiKey", "");
		processSuccessResultPlatformValueMap.put("extData", "");
		processSuccessResultPlatformValueMap.put("mo_msg", "");
		
		Map<String,List<Filter>> processSuccessResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
		List<Filter> statusFilters = new ArrayList<Filter>();
		CopyValueFilter statusCopyFilter = new CopyValueFilter();
		statusCopyFilter.setRequestKey("code");
		statusFilters.add(statusCopyFilter);
		EqualsFilter statusQualFilter = new EqualsFilter();
		statusQualFilter.setData("0");
		statusQualFilter.setEqData(GlobalConst.Result.SUCCESS);
		statusQualFilter.setNotEqualData(GlobalConst.Result.ERROR);
		statusFilters.add(statusQualFilter);
		List<Filter> pipleOrderIdFilters = new ArrayList<Filter>();
		CopyValueFilter linkIdCopyFilter = new CopyValueFilter();
		linkIdCopyFilter.setRequestKey("linkid");
		pipleOrderIdFilters.add(linkIdCopyFilter);
		List<Filter> mobileFilters = new ArrayList<Filter>();
		CopyValueFilter mobileFilter = new CopyValueFilter();
		mobileFilter.setRequestKey("mobile");
		mobileFilters.add(mobileFilter);
		List<Filter> productFilters = new ArrayList<Filter>();
		CopyValueFilter productcopyFilter = new CopyValueFilter();
		productcopyFilter.setRequestKey("mo_msg");
		SpiltFilter productSpiltFilter = new SpiltFilter();
		productSpiltFilter.setIndex(0);
		productSpiltFilter.setSplitStr("\\*");
		ProductFilter productIdFilter = new ProductFilter();
		productIdFilter.setUnit(ProductFilter.PRODUCT_ID_FROM_PIPLE);
		productFilters.add(productcopyFilter);
		productFilters.add(productSpiltFilter);
		productFilters.add(productIdFilter);
		List<Filter> portFilters = new ArrayList<Filter>();
		CopyValueFilter portFilter = new CopyValueFilter();
		portFilter.setRequestKey("smgw");
		portFilters.add(portFilter);
		List<Filter> apiKeyFilters = new ArrayList<Filter>();
		CopyValueFilter apiKeyCopyFilter = new CopyValueFilter();
		apiKeyCopyFilter.setRequestKey("mo_msg");
		apiKeyFilters.add(apiKeyCopyFilter);
		SpiltFilter apiKeySpiltFilter = new SpiltFilter();
		apiKeySpiltFilter.setIndex(2);
		apiKeySpiltFilter.setSplitStr("\\*");
		apiKeyFilters.add(apiKeySpiltFilter);
		SubstringFilter apiKeySubFilter = new SubstringFilter();
		apiKeySubFilter.setStart(0);
		apiKeySubFilter.setEnd(4);
		apiKeyFilters.add(apiKeySubFilter);
		List<Filter> extDataFilters = new ArrayList<Filter>();
		CopyValueFilter extDataCopyFilter = new CopyValueFilter();
		extDataCopyFilter.setRequestKey("mo_msg");
		extDataFilters.add(extDataCopyFilter);
		List<Filter> msgFilters = new ArrayList<Filter>();
		CopyValueFilter msgCopyFilter = new CopyValueFilter();
		msgCopyFilter.setRequestKey("mo_msg");
		msgFilters.add(msgCopyFilter);
		processSuccessResultPlatformValueFilterMap.put("status", statusFilters);
		processSuccessResultPlatformValueFilterMap.put("pipleOrderId", pipleOrderIdFilters);
		processSuccessResultPlatformValueFilterMap.put("mobile", mobileFilters);
		processSuccessResultPlatformValueFilterMap.put("productId", productFilters);
		processSuccessResultPlatformValueFilterMap.put("port", portFilters);
		processSuccessResultPlatformValueFilterMap.put("apiKey", apiKeyFilters); 
		processSuccessResultPlatformValueFilterMap.put("extData", extDataFilters); 
		processSuccessResultPlatformValueFilterMap.put("mo_msg", msgFilters); 
		
		List<String> processSuccessExtOrderInfos = new ArrayList<String>();
		processSuccessExtOrderInfos.add("port");
		processSuccessExtOrderInfos.add("mo_msg");
		
		service1.setProcessSuccessPipleExtMap(processSuccessPipleExtMap);
		service1.setProcessSuccessResultPlatformValueMap(processSuccessResultPlatformValueMap);
		service1.setProcessSuccessPlatformValueFilterMap(processSuccessResultPlatformValueFilterMap);
		service1.setProcessSuccessExtOrderInfos(processSuccessExtOrderInfos);
		ChannelManager.getInstance().putChannelService(service1.getPipleId(), service1);
	}
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		factory = beanFactory;
	}
}
