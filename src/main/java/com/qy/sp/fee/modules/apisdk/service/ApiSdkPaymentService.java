package com.qy.sp.fee.modules.apisdk.service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.CommonServiceUtil;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TBlobContentDao;
import com.qy.sp.fee.dao.TChannelDao;
import com.qy.sp.fee.dao.TMobileBlacklistDao;
import com.qy.sp.fee.dao.TOrderDao;
import com.qy.sp.fee.dao.TPipleDao;
import com.qy.sp.fee.dao.TProductDao;
import com.qy.sp.fee.dao.TSdkAppDao;
import com.qy.sp.fee.dao.TSdkConfigDao;
import com.qy.sp.fee.dto.TBlobContent;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TMobileBlacklist;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleApply;
import com.qy.sp.fee.dto.TPipleFilterKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.dto.TSdkApp;
import com.qy.sp.fee.dto.TSdkConfig;
import com.qy.sp.fee.dto.TSdkConfigQueryKey;
import com.qy.sp.fee.service.CommonService;
import com.qy.sp.fee.service.MobileSegmentService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ApiSdkPaymentService {
	public static final String PAY_TYPE_MOBILE = "0";
    public static final String PAY_TYPE_WEIXIN = "1";
    public static final String PAY_TYPE_ALIPAY = "2";
    public static final String PAY_TYPE_BLACK_WHITE = "3";
	public static final String CONFIG_NEW_PIPLE_PLUGIN= "newPiplePlugin";
	public static final String CONFIG_IS_FUZZY_MONEY_PLUGIN = "isFuzzyMoney";
	@Resource
	private TMobileBlacklistDao tMobileBlacklistDao;
	@Resource
	private TPipleDao tPipleDao;
	@Resource
	private TChannelDao tChannelDao;
	@Resource
	private TProductDao tProductDao;
	@Resource
	private TBlobContentDao tBlobContentDao;
	@Resource
	private TSdkConfigDao tSdkConfigDao;
	@Resource
	private TOrderDao tOrderDao;
	@Resource
	private MobileSegmentService mobileSegmentService;
	@Resource
	private TSdkAppDao tSdkAppDao;
	public JSONObject queryPayment(JSONObject requestObject){
		String imei = requestObject.optString("imei");
		String imsi = requestObject.optString("imsi");
		String number = requestObject.optString("number");
		String number_address = requestObject.optString("number_address");
		String hostId = "";
		String channelId = requestObject.optString("channelId");
		String appId = requestObject.optString("appId");
		String money = requestObject.optString("money");
		String productId= "";
		String ipProvince = requestObject.optString("ipProvince");
		String customPipleId = requestObject.optString("pipleId");
		String payType = requestObject.optString("payType");
		String pluginType = null;
		JSONObject resultObject = new JSONObject();
		if(StringUtil.isEmpty(channelId)){
			resultObject.put("errorMsg", "渠道ID为空");
			resultObject.put("resultCode", "-1");
			return resultObject;
		}
		if(StringUtil.isEmpty(appId)){
			resultObject.put("errorMsg", "APP ID为空");
			resultObject.put("resultCode", "-1");
			return resultObject;
		}
		TChannel channel = tChannelDao.selectByPrimaryKey(channelId);
		if(channel == null){
			resultObject.put("errorMsg", "未知渠道");
			resultObject.put("resultCode", "-1");
			return resultObject;
		}
		TSdkApp app = tSdkAppDao.selectByPrimaryKey(appId);
		if(app == null){
			resultObject.put("errorMsg", "未知APP");
			resultObject.put("resultCode", "-1");
			return resultObject;
		}
		List<TPiple> selectPiples = new ArrayList<TPiple>();
		if(PAY_TYPE_MOBILE.equals(payType)){
			if(StringUtil.isEmpty(money)){
				resultObject.put("errorMsg", "金额为空");
				resultObject.put("resultCode", "-1");
				return resultObject;
			}
			TProduct product = tProductDao.selectByCode(money);
			if(product == null){
				resultObject.put("errorMsg", "产品代码错误");
				resultObject.put("resultCode", "-1");
				return resultObject;
			}
			if(StringUtil.isEmpty(number)){
				hostId = mobileSegmentService.getHostByImsi(imsi)+"";
				if(StringUtil.isEmpty(hostId) || "0".equals(hostId)){
					resultObject.put("errorMsg", "号码运营商信息为空");
					resultObject.put("resultCode", "-1");
					return resultObject;
				}
				if(StringUtil.isEmpty(number_address) || "0".equals(number_address)){
					number_address = mobileSegmentService.getProvinceByIpProvince(ipProvince)+"";
				}
				if(StringUtil.isEmpty(number_address) || "0".equals(number_address)){
					resultObject.put("errorMsg", "号码省份为空或未知省份");
					resultObject.put("resultCode", "-1");
					return resultObject;
				}
				pluginType = GlobalConst.PluginType.PLUGIN_MOBILE_NONE;
			}else{
				TMobileBlacklist blackList = tMobileBlacklistDao.selectByPrimaryKey(number);
				if(blackList != null){
					resultObject.put("errorMsg", "黑名单");
					resultObject.put("resultCode", "-1");
					return resultObject;
				}
				TLocation location = mobileSegmentService.getLocationByMobile(number);
				if(location != null){
					hostId = location.getHostId()+"";
				}
				if(StringUtil.isEmpty(hostId) || "0".equals(hostId)){
					resultObject.put("errorMsg", "号码运营商信息为空");
					resultObject.put("resultCode", "-1");
					return resultObject;
				}
				if(StringUtil.isEmpty(number_address) || "0".equals(number_address)){
					number_address = location.getProvinceId()+"";
				}
				if(StringUtil.isEmpty(number_address) || "0".equals(number_address)){
					resultObject.put("errorMsg", "号码省份为空或未知省份");
					resultObject.put("resultCode", "-1");
					return resultObject;
				}
			}
			if(!StringUtil.isEmpty(customPipleId)){
				TPiple selectPiple = tPipleDao.selectByPrimaryKey(customPipleId);
				selectPiples.add(selectPiple);
			}
			if(selectPiples.size() == 0){
				productId = product.getProductId();
				TPipleFilterKey key = new TPipleFilterKey();
				key.setProvinceId(number_address);
				key.setHostId(hostId);
				key.setProductId(productId);
				key.setChannelId(channelId);
				key.setPluginType(pluginType);
				List<TPipleApply> piples = tPipleDao.selectPipleByFilterKey(key);
				if(piples != null && piples.size() > 0){
					for(TPipleApply p : piples){
						if(!transCheck(p.getPipleId(),channelId,money,NumberUtil.getInteger(number_address),imsi,number)){
							continue;
						}
						selectPiples.add(p);
					}
				}
				if(selectPiples.size() == 0){
					TSdkConfigQueryKey configQueryKey = new TSdkConfigQueryKey();
					configQueryKey.setChannelId(channelId);
					configQueryKey.setAppId(appId);
					configQueryKey.setProvinceId(number_address);
					configQueryKey.setConfigId(CONFIG_IS_FUZZY_MONEY_PLUGIN);
					List<TSdkConfig> config = tSdkConfigDao.selectConfigurationsByConfigQueryKey(configQueryKey);
					boolean isFuzzyMoney = false;
					if(config.size() >0){
						isFuzzyMoney = NumberUtil.getBoolean(config.get(0).getConfigValue());
					}
					if(isFuzzyMoney){
						final int wantMoney = getPrice(productId);
						TPipleFilterKey fuzzyKey = new TPipleFilterKey();
						fuzzyKey.setProvinceId(number_address);
						fuzzyKey.setHostId(hostId);
						fuzzyKey.setChannelId(channelId);
						fuzzyKey.setPluginType(pluginType);
						List<TPipleApply> fuzzyKeyPiples = tPipleDao.selectPipleByFilterKey(fuzzyKey);
						if(fuzzyKeyPiples != null && fuzzyKeyPiples.size() > 0){
							Collections.sort(fuzzyKeyPiples,new Comparator<TPipleApply>() {
								
								@Override
								public int compare(TPipleApply o1, TPipleApply o2) {
									if(Math.abs(wantMoney - getPrice(o1.getProductId())) < Math.abs(wantMoney - getPrice(o2.getProductId())))
										return -1;
									return 1;
								}
							});
							for(TPipleApply p : fuzzyKeyPiples){
								String productCode = tProductDao.selectByPrimaryKey(p.getProductId()).getProductCode();
								if(!transCheck(p.getPipleId(),channelId,productCode,NumberUtil.getInteger(number_address),imsi,number)){
									continue;
								}
								selectPiples.add(p);
								if(selectPiples.size() >=3)
									break;
							}
						}
					}
				}
				
			}
		}else if(PAY_TYPE_ALIPAY.equals(payType)){
			TPiple selectPiple = null;
			String pipleId = requestObject.optString("pipleId");
			if(!StringUtil.isEmpty(pipleId)){
				selectPiple = tPipleDao.selectByPrimaryKey(pipleId);
			}else{
				selectPiple = tPipleDao.selectByPrimaryKey("14666528254896750836859");
			}
			if(selectPiple != null){
				selectPiples.add(selectPiple);
			}
		}else if(PAY_TYPE_WEIXIN.equals(payType)){
			TPiple selectPiple = null;
			String pipleId = requestObject.optString("pipleId");
			if(!StringUtil.isEmpty(pipleId)){
				selectPiple = tPipleDao.selectByPrimaryKey(pipleId);
			}else{
				selectPiple = tPipleDao.selectByPrimaryKey("14666533668639964952093");
			}
			if(selectPiple != null){
				selectPiples.add(selectPiple);
			}
		}else if(PAY_TYPE_BLACK_WHITE.equals(payType)){
			TPiple selectPiple = null;
			String pipleId = requestObject.optString("pipleId");
			if(!StringUtil.isEmpty(pipleId)){
				selectPiple = tPipleDao.selectByPrimaryKey(pipleId);
			}else{
				selectPiple = tPipleDao.selectByPrimaryKey("14714136609207184697301");
			}
			if(selectPiple != null){
				selectPiples.add(selectPiple);
			}
		}
		if(selectPiples.size()!= 0){
			JSONArray data = new JSONArray();
			for(TPiple p : selectPiples){
				JSONObject d = new JSONObject();
				d.put("paymentId", p.getPipleId());
				d.put("paymentVersion", p.getPluginVersion());
				if(p instanceof TPipleApply){
					d.put("productCode", tProductDao.selectByPrimaryKey(((TPipleApply)p).getProductId()).getProductCode());
				}
				TSdkConfigQueryKey configQueryKey = new TSdkConfigQueryKey();
				configQueryKey.setAppId(appId);
				configQueryKey.setChannelId(channelId);
				configQueryKey.setConfigId(CONFIG_NEW_PIPLE_PLUGIN);
				configQueryKey.setPipleId(p.getPipleId());
				configQueryKey.setProvinceId(number_address);
				List<TSdkConfig> config = tSdkConfigDao.selectConfigurationsByConfigQueryKey(configQueryKey);
				if(config.size() >0){
						d.put("newPiplePlugin", config.get(0).getConfigValue());
				}
				data.add(d);
			}
			resultObject.put("resultCode", "0");
			resultObject.put("data", data);
		}else{
			resultObject.put("errorMsg", "没有支付插件");
			resultObject.put("resultCode", "-1");
		}
		return resultObject;
		
	}
	private int getPrice(String productId){
		if("001".equals(productId)){
			return 1;
		}else if("010".equals(productId)){
			return 10;
		}else {
			return NumberUtil.getInteger(productId) * 100;
		}
	}
	public boolean transCheck(String pipleId,String channelId,String productCode,int provinceId,String imsi,String mobile){
		CommonService service = CommonServiceUtil.getCommonService();
		int checkResult = GlobalConst.CheckResult.PASS;
		if(checkResult == GlobalConst.CheckResult.PASS){
			// 渠道交易限额校验
			if(StringUtil.isNotEmptyString(mobile)){
				checkResult = service.transCheckMobile(pipleId, imsi,mobile, productCode);
				if(checkResult!=GlobalConst.CheckResult.PASS){
					return false;
				}
			}
		}
		if(checkResult == GlobalConst.CheckResult.PASS){
			// 渠道交易限额校验
			checkResult = service.transCheckPipleChannel(pipleId, channelId, productCode);
			if(checkResult!=GlobalConst.CheckResult.PASS){
				return false;
			}
		}
		if(provinceId != 0){
			checkResult = service.transCheckProvince(pipleId, channelId,productCode, provinceId);
			if(checkResult!=GlobalConst.CheckResult.PASS){
				return false;
			}
		}
		if(checkResult == GlobalConst.CheckResult.PASS){
			checkResult = service.transCheckPiple(pipleId, productCode);
			if(checkResult!=GlobalConst.CheckResult.PASS){
				return false;
			}
		}
		return true;
	}
	public void downloadPayment(JSONObject requestObject, HttpServletResponse response)
			throws MalformedURLException {
		try {
			String paymentId = requestObject.optString("paymentId");
			boolean isNewPiplePlugin = false;
			if(requestObject.containsKey(CONFIG_NEW_PIPLE_PLUGIN)){
				isNewPiplePlugin = requestObject.optBoolean(CONFIG_NEW_PIPLE_PLUGIN);
			}
			TPiple piple = tPipleDao.selectByPrimaryKey(paymentId);
			if(piple == null){
				response.addHeader("status", "-1");
				return ;
			}
			String fileId = null;
			if(isNewPiplePlugin){
				fileId = piple.getTestPluginId();
			}else{
				fileId = piple.getPluginFile();
			}
			TBlobContent content = tBlobContentDao.selectByPrimaryKey(fileId);
			if(content == null){
				response.addHeader("status", "-1");
				return ;
			}
			// 以流的形式下载文件。
			byte[] buffer = content.getFileContent();
			// 清空response
			response.reset();
			response.addHeader("Content-Disposition", "attachment;filename="+ paymentId);
			response.addHeader("Content-Length", "" + buffer.length);
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void updateOrder(JSONObject requestObject){
		String orderId = requestObject.optString("orderId");
		String subStatus = requestObject.optString("subStatus");
		String flowId = requestObject.optString("flowId");
		if(StringUtil.isNotEmptyString(orderId)){
			TOrder order = tOrderDao.selectByPrimaryKey(orderId);
			if(order != null){
				if(StringUtil.isNotEmptyString(subStatus)){
					order.setSubStatus(StringUtil.getInt(subStatus, 0));
				}
				order.setFlowId(flowId);
				tOrderDao.updateByPrimaryKeySelective(order);		
			}
		}
		
	}
}
