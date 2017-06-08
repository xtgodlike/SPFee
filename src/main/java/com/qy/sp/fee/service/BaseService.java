package com.qy.sp.fee.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MapCacheManager;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TChannelDao;
import com.qy.sp.fee.dao.TChannelPipleDao;
import com.qy.sp.fee.dao.TChannelProductDao;
import com.qy.sp.fee.dao.TChannelProvinceLimitDao;
import com.qy.sp.fee.dao.TMobileBlacklistDao;
import com.qy.sp.fee.dao.TOrderDao;
import com.qy.sp.fee.dao.TOrderExtDao;
import com.qy.sp.fee.dao.TPipleDao;
import com.qy.sp.fee.dao.TPipleMobileLimitDao;
import com.qy.sp.fee.dao.TPipleProductDao;
import com.qy.sp.fee.dao.TPipleProvinceDao;
import com.qy.sp.fee.dao.TProductDao;
import com.qy.sp.fee.dao.TSdkOperationDao;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TChannelProduct;
import com.qy.sp.fee.dto.TChannelProductKey;
import com.qy.sp.fee.dto.TChannelProvinceLimit;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TMobileBlacklist;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TOrderExtKey;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleMobileLimit;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TPipleProvince;
import com.qy.sp.fee.dto.TPipleProvinceKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.dto.TSdkOperation;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;

import net.sf.json.JSONObject;

public class BaseService {
	
	public static final String STEP_GET_MESSAGE_CHANNEL_TO_PLATFORM = "20160531105047993134";
	public static final String STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT = "20160531105103640690";
	public static final String STEP_SUBMIT_MESSAGE_CHANNEL_TO_PLATFORM = "20160531105114963137";
	public static final String STEP_SUBMIT_MESSAGE_PLATFORM_TO_CHANNEL_RESULT = "20160531105121177341";
	public static final String STEP_GET_SMS_CHANNEL_TO_PLATFORM = "20160525110829482543";
	public static final String STEP_GET_SMS_PLATFORM_TO_BASE = "20160525110843249097";
	public static final String STEP_BACK_SMS_BASE_TO_PLATFORM = "20160525103926512032";
	public static final String STEP_BACK_SMS_PLATFORM_TO_CHANNEL = "20160525103932393139";
	public static final String STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM = "20160525103938811400";
	public static final String STEP_SUBMIT_VCODE_PLARFORM_TO_BASE = "20160525103944005683";
	public static final String STEP_BACK_VCODE_BASE_TO_PLATFORM = "20160525103952745644";
	public static final String STEP_BACK_VCODE_PLATFORM_TO_CHANNEL = "20160525133354188793";
	public static final String STEP_PAY_BASE_TO_PLATFORM = "20160525105205972810";
	public static final String STEP_PAY_PLATFORM_TO_CHANNEL = "20160525105224313928";
	public static final String STEP_PAY_CHANNEL_TO_PLATFORM = "20160525105230487032";
	
	private Logger log = Logger.getLogger(BaseService.class);
	@Resource
	protected TChannelDao tChannelDao;
	
	@Resource
	protected TPipleDao tPipleDao;
	
	@Resource
	protected TChannelPipleDao tChannelPipleDao;
	
	@Resource
	protected TPipleProductDao tPipleProductDao;
	
	@Resource
	protected TPipleProvinceDao tPipleProvinceDao;
	
	@Resource
	protected TChannelProductDao tChannelProductDao;
	
	@Resource
	protected TOrderDao tOrderDao;
	
	@Resource
	protected TProductDao tProductDao;
	
	@Resource
	protected TOrderExtDao tOrderExtDao;
	
	@Resource
	protected TMobileBlacklistDao tMobileBlacklistDao;
	
	@Resource
	protected TChannelProvinceLimitDao tChannelProvinceLimitDao;
	
	@Resource
	protected TPipleMobileLimitDao tPipleMobileLimitDao;
	
	@Resource
	protected TSdkOperationDao tSdkOperationDao;
	
	@Resource
	protected MobileSegmentService mobileSegmentService;
	
	public BaseResult accessVerify(BaseChannelRequest req){
		return accessVerify(req,req.getPipleId());
	}

	public BaseResult accessVerify(BaseChannelRequest req,String pipleId){
		BaseResult result = null;
		TChannel tChannel =  tChannelDao.selectByApiKey(req.getApiKey());
		if(null==tChannel){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.UNKNOWN_APIKEY+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.UNKNOWN_APIKEY));
			return result;
		}else if(tChannel.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.CHANNEL_CLOSE+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.CHANNEL_CLOSE));
			return result;
		}
		TChannelPipleKey cpkey = new TChannelPipleKey();
		cpkey.setChannelId(tChannel.getChannelId());
		cpkey.setPipleId(pipleId);
		TChannelPiple tChannelPiple = tChannelPipleDao.selectByPrimaryKey(cpkey);
		TPiple tPiple = tPipleDao.selectByPrimaryKey(pipleId);
		if(tChannelPiple==null || tPiple==null){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.PIPLE_ERROR+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PIPLE_ERROR));
			return result;
		}else if(tPiple.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.PIPLE_CLOSE+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PIPLE_CLOSE));
			return result;
		}
		TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
		if(tProduct==null){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.PRODUCTCODE_ERROR+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PRODUCTCODE_ERROR));
			return result;
		}
		TPipleProductKey ppKey = new TPipleProductKey();
		ppKey.setPipleId(pipleId);
		ppKey.setProductId(tProduct.getProductId());
		TPipleProduct tPipleProduct = tPipleProductDao.selectByPrimaryKey(ppKey);
		if(tPipleProduct==null){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.PIPLE_PRODUCT_ERROR+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PIPLE_PRODUCT_ERROR));
			return result;
		}else if(tPipleProduct.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.PIPLE_PRODUCT_CLOSE+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PIPLE_PRODUCT_CLOSE));
			return result;
		}
		TChannelProductKey cpKey = new TChannelProductKey();
		cpKey.setChannelId(tChannel.getChannelId());
		cpKey.setProductId(tProduct.getProductId());
		TChannelProduct cProduct = tChannelProductDao.selectByPrimaryKey(cpKey);
		if(cProduct==null){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.CHANNEL_PRODUCT_ERROR+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.CHANNEL_PRODUCT_ERROR));
			return result;
		}else if(cProduct.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.CHANNEL_PRODUCT_CLOSE+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.CHANNEL_PRODUCT_CLOSE));
			return result;
		}
		//校验号码不为空或者imsi和ip省份不为空。
		
		if(StringUtil.isEmpty(req.getMobile())){
			if(StringUtil.isEmpty(req.getImsi())){
				 result = new BaseResult();
				 result.setResultCode(GlobalConst.CheckResult.IMSI_ERROR+"");
				 result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.IMSI_ERROR));
				 return result;
			}
//			if(StringUtil.isEmpty(req.getIpProvince())){
//				 result = new BaseResult();
//				 result.setResultCode(GlobalConst.CheckResult.IP_PROVINCE_ERROR+"");
//				 result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.IP_PROVINCE_ERROR));
//				 return result;
//			}
		}
		//校验不要请求太频繁。
		String mobleUniqueKey = StringUtil.isEmpty(req.getMobile())?req.getImsi():req.getMobile() ;
		boolean isFrequentlyRequest = tPipleDao.filterFrequentlyRequest(mobleUniqueKey, pipleId);
		if(isFrequentlyRequest){
			result = new BaseResult();
			result.setResultCode(GlobalConst.CheckResult.REQ_FREQUENT+"");
			result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.REQ_FREQUENT));
			return result;
		}
		
		if(!StringUtil.isEmpty(req.getMobile())){
			TMobileBlacklist blackMobile = tMobileBlacklistDao.selectByPrimaryKey(req.getMobile());
			if(blackMobile!=null){ // 手机号码存在于黑名单中
				result = new BaseResult();
				result.setResultCode(GlobalConst.CheckResult.MOBILE_ISBLANK+"");
				result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MOBILE_ISBLANK));
				return result;
			}
			TLocation location = mobileSegmentService.getLocationByMobile(req.getMobile());
			if(location != null){
				req.setHostId(location.getHostId());
				req.setProvinceId(location.getProvinceId());
			}
			
		}
		if(req.getHostId() == 0){
			req.setHostId(mobileSegmentService.getHostByImsi(req.getImsi()));
		}
		if(req.getProvinceId() == 0){
			req.setProvinceId(mobileSegmentService.getProvinceByIpProvince(req.getIpProvince()));
		}
		if(req.getHostId() == 0 || tPiple.getHostId() != req.getHostId()){
			 result = new BaseResult();
			 result.setResultCode(GlobalConst.CheckResult.MOBLE_HOST_ERROR+"");
			 result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MOBLE_HOST_ERROR));
			 return result;
		 }
		// 省份强制校验
//		if(req.getProvinceId() == 0){
//			 result = new BaseResult();
//			 result.setResultCode(GlobalConst.CheckResult.PIPLE_PROVINCE_CLOSE+"");
//			 result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PIPLE_PROVINCE_CLOSE));
//			 return result;
//		}
		if(req.getProvinceId() != 0){
			 TPipleProvinceKey pproKey = new TPipleProvinceKey();
			 pproKey.setProvinceId(req.getProvinceId());
			 pproKey.setPipleId(pipleId);
			 TPipleProvince pipleProvince = tPipleProvinceDao.selectByPrimaryKey(pproKey);
			 if(pipleProvince.getOpStatus()!=GlobalConst.OP_STATUS.OPEN){
				 result = new BaseResult();
				 result.setResultCode(GlobalConst.CheckResult.PIPLE_PROVINCE_CLOSE+"");
				 result.setResultMsg(GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.PIPLE_PROVINCE_CLOSE));
				 return result;
			 }
		}
		if(isUseableTradeDayAndMonth()){
			// 用户代码日月限
			int checkResult = transCheckMobile(pipleId,req.getImsi(), req.getMobile(),req.getProductCode());
			if(checkResult!=GlobalConst.CheckResult.PASS){
				result = new BaseResult();
				result.setResultCode(checkResult+"");
				result.setResultMsg(GlobalConst.CheckResultDesc.message.get(checkResult));
				return result;
			}
		}
		if(isUseableTradeDayAndMonth()){
			// 渠道交易限额校验
			int checkResult = this.transCheckPipleChannel(pipleId, tChannel.getChannelId(), req.getProductCode());
			if(checkResult!=GlobalConst.CheckResult.PASS){
				result = new BaseResult();
				result.setResultCode(checkResult+"");
				result.setResultMsg(GlobalConst.CheckResultDesc.message.get(checkResult));
				return result;
			}
		}
		if(req.getProvinceId()!= 0 && isUseableTradeDayAndMonth()){
			// 渠道省份日月限控制
			int checkResult = transCheckProvince(pipleId, tChannel.getChannelId(),req.getProductCode(), req.getProvinceId());
			if(checkResult!=GlobalConst.CheckResult.PASS){
				result = new BaseResult();
				result.setResultCode(checkResult+"");
				result.setResultMsg(GlobalConst.CheckResultDesc.message.get(checkResult));
				return result;
			}
			
		}
		if(isUseableTradeDayAndMonth()){
			// 通道总量金额限量校验
			int checkResult = transCheckPiple(pipleId,req.getProductCode());
			if(checkResult!=GlobalConst.CheckResult.PASS){
				result = new BaseResult();
				result.setResultCode(checkResult+"");
				result.setResultMsg(GlobalConst.CheckResultDesc.message.get(checkResult));
				return result;
			}
			
		}
		return result;
	}
	
	public void SaveOrderInsert(TOrder order){
		this.tOrderDao.insert(order);
		
		for(TOrderExt te : order.gettOrderExts()){
			this.tOrderExtDao.insert(te);
		}
	}
	
	public void SaveOrderUpdate(TOrder order){
		
//		// 交易成功 更新日月限
//		if(order.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
//			this.updateCacheTrans(order.getPipleId(), order.getChannelId(), order.getAmount().doubleValue());
//		}
		
		this.tOrderDao.updateByPrimaryKeySelective(order);		
		SaveOrderExtUpdate(order);
	}
	
	public void SaveOrderExtUpdate(TOrder order){
		for(TOrderExt te : order.gettOrderExts()){
			TOrderExtKey key = new TOrderExtKey();
			key.setOrderId(te.getOrderId());
			key.setExtKey(te.getExtKey());
			this.tOrderExtDao.deleteByPrimaryKey(key);
			this.tOrderExtDao.insert(te);
		}
	}
	
	protected int getProvinceIdByMobile(String mobile, boolean bMust){
		return mobileSegmentService.getProvinceIdByMobile(mobile);
	}
	public int transCheckPiple(String pipleId,String productCode){
		try {
			TPipleMobileLimit tPipleMobileLimit = new TPipleMobileLimit();
			tPipleMobileLimit.setPipleId(pipleId);
			tPipleMobileLimit = tPipleMobileLimitDao.selectByPrimaryKey(tPipleMobileLimit);
			if(tPipleMobileLimit == null){
				return GlobalConst.CheckResult.PASS;
			}
			TProduct product = tProductDao.selectByCode(productCode);
			TOrder okey = new TOrder();
			okey.setPipleId(pipleId);
			
			String dPipleTradeKey =  getCacheKeyPiple(pipleId,GlobalConst.MapCache_Key.PIPLE_DAY_TRADE);
			String mPIpleTradeKey =  getCacheKeyPiple(pipleId,GlobalConst.MapCache_Key.PIPLE_MONTH_TRADE);
			// 校验通道日限跑的金额
			String dPipleTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dPipleTradeKey);
			log.info("mobile dPipleTradeKey="+dPipleTradeKey+";dPipleTrade="+dPipleTrade==null?0:dPipleTrade);
			if(StringUtil.isEmpty(dPipleTrade)){
				dPipleTrade = tOrderDao.getAmountForPipleDay(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(dPipleTradeKey, dPipleTrade);
			}
	    	double nowPipleDayA = NumberUtil.getDouble(dPipleTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
			double dPipleRestrict = tPipleMobileLimit.getPipleTradeDay(); // 日限
			if(nowPipleDayA > dPipleRestrict && dPipleRestrict!=0){
				return GlobalConst.CheckResult.LIMIT_DAY_PIPLE;
			}
			// 校验通道月限跑的金额
			String mPIpleTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mPIpleTradeKey);
			log.info("mobile mPIpleTradeKey="+mPIpleTradeKey+";mPIpleTrade="+mPIpleTrade==null?0:mPIpleTrade);
			if(StringUtil.isEmpty(mPIpleTrade)){
				mPIpleTrade = tOrderDao.getAmountForPipleMonth(okey)+""; // 通道月成功金额
				MapCacheManager.getInstance().getDailyClearMapCache().put(mPIpleTradeKey, mPIpleTrade);
			}
			double nowPipleMonthA = NumberUtil.getDouble(mPIpleTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
			double mPipleRestrict = tPipleMobileLimit.getPipleTradeMonth(); // 月限
			if(nowPipleMonthA > mPipleRestrict && mPipleRestrict!=0){
				return GlobalConst.CheckResult.LIMIT_MONTH_PIPLE;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GlobalConst.CheckResult.ERROR;
		}
		return GlobalConst.CheckResult.PASS;
	}
	public int transCheckMobile(String pipleId,String imsi,String mobile,String productCode){
		// 日、月限校验
		try {
			TPipleMobileLimit tPipleMobileLimit = new TPipleMobileLimit();
			tPipleMobileLimit.setPipleId(pipleId);
			tPipleMobileLimit = tPipleMobileLimitDao.selectByPrimaryKey(tPipleMobileLimit);
			if(tPipleMobileLimit == null){
				return GlobalConst.CheckResult.PASS;
			}
			TProduct product = tProductDao.selectByCode(productCode);
			String mobileUniqueKey = mobile;
			TOrder okey = new TOrder();
			okey.setPipleId(pipleId);
			if(StringUtil.isNotEmptyString(mobile)){
				okey.setMobile(mobile);
			}else{
				okey.setImsi(imsi);
				mobileUniqueKey = imsi;
			}
			
			String dTradeKey =  getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.DAY_TRADE);
			String mTradeKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_TRADE);
			String dTradeCountKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.DAY_TRADE_COUNT);
			String mTradeCountKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_TRADE_COUNT);
			String dRequestCountKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.DAY_REQUEST_COUNT);
			String mRequestCountKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_REQUEST_COUNT);
			
			String dTradeGlobalKey = getCacheKeyMobile("global_",mobileUniqueKey,GlobalConst.MapCache_Key.DAY_GLOABL_TRADE);
			String mTradeGlobalKey = getCacheKeyMobile("global_",mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_GLOABAL_TRADE);
			
			// 校验日请求次数
			String dayRequestCount = MapCacheManager.getInstance().getDailyClearMapCache().get(dRequestCountKey);
			log.info("mobile dCountKey="+dTradeCountKey+";dayCount="+dayRequestCount==null?0:dayRequestCount);
			// 缓存中没有则数据查询
			if(StringUtil.isEmpty(dayRequestCount)){
				dayRequestCount = tOrderDao.getCountForMobileDay(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(dRequestCountKey, dayRequestCount);
			}
	    	int nowDayRequestCountA = NumberUtil.getInteger(dayRequestCount) + 1 ;
	    	int dRequestCount = tPipleMobileLimit.getRequestDayCount(); // 日限
			if(nowDayRequestCountA > dRequestCount && dRequestCount!=0){
				return GlobalConst.CheckResult.LIMIT_DAY_REQUEST_COUNT_MOBILE;
			}else{
				MapCacheManager.getInstance().getDailyClearMapCache().put(dRequestCountKey, nowDayRequestCountA+"");
			}
			// 校验月请求次数
			String monthRequestCount = MapCacheManager.getInstance().getDailyClearMapCache().get(mRequestCountKey);
			log.info("mobile mCountKey="+mRequestCountKey+";monthCount="+monthRequestCount==null?0:monthRequestCount);
			// 缓存中没有则数据查询
			if(StringUtil.isEmpty(monthRequestCount)){
				monthRequestCount = tOrderDao.getCountForMobileMonth(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(mRequestCountKey, monthRequestCount);
			}
	    	int nowMonthRequestCountA = NumberUtil.getInteger(monthRequestCount) + 1 ;
	    	int mRequestCount = tPipleMobileLimit.getRequestMonthCount(); // 日限
			if(nowMonthRequestCountA > mRequestCount && mRequestCount!=0){
				return GlobalConst.CheckResult.LIMIT_MONTH_REQUEST_COUNT_MOBILE;
			}else{
				MapCacheManager.getInstance().getDailyClearMapCache().put(mRequestCountKey, nowMonthRequestCountA+"");
			}
						
			
			// 校验日成功次数
			String dayTradeCount = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeCountKey);
			log.info("mobile dCountKey="+dTradeCountKey+";dayCount="+dayTradeCount==null?0:dayTradeCount);
			// 缓存中没有则数据查询
			if(StringUtil.isEmpty(dayTradeCount)){
				dayTradeCount = tOrderDao.getCountForMobileDay(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeCountKey, dayTradeCount);
			}
	    	int nowDayCountA = NumberUtil.getInteger(dayTradeCount) + 1 ;
	    	int dRestrictCount = tPipleMobileLimit.getTradeDayCount(); // 日限
			if(nowDayCountA > dRestrictCount && dRestrictCount!=0){
				return GlobalConst.CheckResult.LIMIT_DAY_COUNT_MOBILE;
			}
			// 校验月成功次数
			String monthTradeCount = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeCountKey);
			log.info("mobile mCountKey="+mTradeCountKey+";monthCount="+monthTradeCount==null?0:monthTradeCount);
			// 缓存中没有则数据查询
			if(StringUtil.isEmpty(monthTradeCount)){
				monthTradeCount = tOrderDao.getCountForMobileMonth(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeCountKey, monthTradeCount);
			}
	    	int nowMonthCountA = NumberUtil.getInteger(monthTradeCount) + 1 ;
	    	int mRestrictCount = tPipleMobileLimit.getTradeMonthCount(); // 日限
			if(nowMonthCountA > mRestrictCount && mRestrictCount!=0){
				return GlobalConst.CheckResult.LIMIT_MONTH_COUNT_MOBILE;
			}
			
			
			if(tPipleMobileLimit.getLimitType() == GlobalConst.LimitType.LIMIT_GLOABAL){
				// 校验日全局成功金额
				String dayGlobalTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeGlobalKey);
				log.info("mobile dTradeGlobalKey="+dTradeGlobalKey+";dayGlobalTrade="+dayGlobalTrade==null?0:dayGlobalTrade);
				if(StringUtil.isEmpty(dayGlobalTrade)){
					dayGlobalTrade = tOrderDao.getAmountForMobileGlobalDay(okey)+"";
					MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeGlobalKey, dayGlobalTrade);
				}
				double nowGloabalDayA = NumberUtil.getDouble(dayGlobalTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
				double dGlobalRestrict = tPipleMobileLimit.getTradeDay(); // 日限
				if(nowGloabalDayA > dGlobalRestrict && dGlobalRestrict!=0){
					return GlobalConst.CheckResult.LIMIT_DAY_MOBILE;
				}
				
				//校验月全局成功金额
				String monthGlobalTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeGlobalKey);
				log.info("mobile mTradeGlobalKey="+mTradeGlobalKey+";monthGlobalTrade="+monthGlobalTrade==null?0:monthGlobalTrade);
				if(StringUtil.isEmpty(monthGlobalTrade)){
					monthGlobalTrade = tOrderDao.getAmountForMobileGlobalMonth(okey)+""; // 当日之前的月成功金额
					MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeGlobalKey, monthGlobalTrade);
				}
				double nowGlobalMonthA = NumberUtil.getDouble(monthGlobalTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
				double mGlobalRestrict = tPipleMobileLimit.getTradeMonth(); // 月限
				if(nowGlobalMonthA > mGlobalRestrict && mGlobalRestrict!=0){
					return GlobalConst.CheckResult.LIMIT_MONTH_MOBILE;
				}
			}else{
				// 校验日限成功金额
				String dayTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeKey);
				log.info("mobile dTradeKey="+dTradeKey+";dayTrade="+dayTrade==null?0:dayTrade);
				// 缓存中没有则数据查询
				if(StringUtil.isEmpty(dayTrade)){
					dayTrade = tOrderDao.getAmountForMobileDay(okey)+"";
					MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeKey, dayTrade);
				}
		    	double nowDayA = NumberUtil.getDouble(dayTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
				double dRestrict = tPipleMobileLimit.getTradeDay(); // 日限
				if(nowDayA > dRestrict && dRestrict!=0){
					return GlobalConst.CheckResult.LIMIT_DAY_MOBILE;
				}
				
				// 校验月成功金额
				String monthTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeKey);
				log.info("mobile mTradeKey="+mTradeKey+";monthTrade="+monthTrade==null?0:monthTrade);
				if(StringUtil.isEmpty(monthTrade)){
					monthTrade = tOrderDao.getAmountForMobileMonth(okey)+""; // 当日之前的月成功金额
					MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeKey, monthTrade);
				}
				double nowMonthA = NumberUtil.getDouble(monthTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
				double mRestrict = tPipleMobileLimit.getTradeMonth(); // 月限
				if(nowMonthA > mRestrict && mRestrict!=0){
					return GlobalConst.CheckResult.LIMIT_MONTH_MOBILE;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GlobalConst.CheckResult.ERROR;
		}
		return GlobalConst.CheckResult.PASS;
	}
	public int transCheckProvince(String pipleId,String channelId,String productCode,int provinceId){
		// 日、月限校验
		TOrder okey = new TOrder();
		okey.setPipleId(pipleId);
		okey.setChannelId(channelId);
		okey.setProvinceId(provinceId);
		TProduct product = tProductDao.selectByCode(productCode);
		TChannelProvinceLimit selectKey = new TChannelProvinceLimit();
		selectKey.setChannelId(channelId);
		selectKey.setPipleId(pipleId);
		selectKey.setProvinceId(provinceId);
		TChannelProvinceLimit tChannelProvinceLimit = tChannelProvinceLimitDao.selectByPrimaryKey(selectKey);
		if(tChannelProvinceLimit == null){
			return GlobalConst.CheckResult.PASS;
		}
		try {
			String dTradeKey =  getCacheKeyProvince(pipleId,channelId,provinceId,GlobalConst.MapCache_Key.DAY_TRADE);
			String mTradeKey = getCacheKeyProvince(pipleId,channelId,provinceId,GlobalConst.MapCache_Key.MONTH_TRADE);
			// 校验日限
			String dayTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeKey);
			log.info("province dTradeKey="+dTradeKey+";dayTrade="+dayTrade==null?0:dayTrade);
			// 缓存中没有则数据查询
			if(dayTrade==null || "".equals(dayTrade)){
				dayTrade = tOrderDao.getAmountForChannelProvinceDay(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeKey, dayTrade);
			}
	    	double nowDayA = NumberUtil.getDouble(dayTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
			double dRestrict = tChannelProvinceLimit.getTradeDay(); // 日限
			if(nowDayA > dRestrict && dRestrict!=0){
				return GlobalConst.CheckResult.LIMIT_DAY_PROVINCE;
			}
			
			// 校验月限
			String monthTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeKey);
			log.info("province mTradeKey="+mTradeKey+";dayTrade="+monthTrade==null?0:monthTrade);
			if(monthTrade==null || "".equals(monthTrade)){
				double monthAmout = tOrderDao.getAmountForChannelProvinceMonth(okey); // 当日之前的月成功金额
				monthTrade = monthAmout + NumberUtil.getDouble(dayTrade)+""; // 当前月成功金额 = 当日之前的月成功金额 + 当日成功金额
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeKey, monthTrade);
			}
			double nowMonthA = NumberUtil.getDouble(monthTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
			double mRestrict = tChannelProvinceLimit.getTradeMonth(); // 月限
			if(nowMonthA > mRestrict && mRestrict!=0){
				return GlobalConst.CheckResult.LIMIT_MONTH_PROVINCE;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GlobalConst.CheckResult.ERROR;
		}
		return GlobalConst.CheckResult.PASS;
	}
	public int transCheckPipleChannel(String pipleId, String channelId,String productCode){
		// 日、月限校验
		TOrder okey = new TOrder();
		okey.setPipleId(pipleId);
		okey.setChannelId(channelId);
		TProduct product = tProductDao.selectByCode(productCode);
		TChannelPipleKey cpk = new TChannelPipleKey();
		cpk.setChannelId(channelId);
		cpk.setPipleId(pipleId);
		TChannelPiple cPiple = tChannelPipleDao.selectByPrimaryKey(cpk);
		if(cPiple == null){
			return GlobalConst.CheckResult.PASS;
		}
		try {
			String dTradeKey =  getCacheKeyPipleChannel(pipleId,channelId,GlobalConst.MapCache_Key.DAY_TRADE);
			String mTradeKey = getCacheKeyPipleChannel(pipleId,channelId,GlobalConst.MapCache_Key.MONTH_TRADE);
			// 校验日限
			String dayTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeKey);
			log.info("channel dTradeKey="+dTradeKey+";dayTrade="+dayTrade==null?0:dayTrade);
			// 缓存中没有则数据查询
			if(dayTrade==null || "".equals(dayTrade)){
				dayTrade = tOrderDao.getAmountForDay(okey)+"";
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeKey, dayTrade);
			}
	    	double nowDayA = NumberUtil.getDouble(dayTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
			double dRestrict = cPiple.getTradeDay()==null?0:cPiple.getTradeDay(); // 日限
			if(nowDayA > dRestrict && dRestrict!=0){
				return GlobalConst.CheckResult.LIMIT_DAY;
			}
			
			// 校验月限
			String monthTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeKey);
			log.info("channel mTradeKey="+mTradeKey+";dayTrade="+monthTrade==null?0:monthTrade);
			if(monthTrade==null || "".equals(monthTrade)){
				double monthAmout = tOrderDao.getAmountForMonth(okey); // 当日之前的月成功金额
				monthTrade = monthAmout + NumberUtil.getDouble(dayTrade)+""; // 当前月成功金额 = 当日之前的月成功金额 + 当日成功金额
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeKey, monthTrade);
			}
			double nowMonthA = NumberUtil.getDouble(monthTrade) + NumberUtil.getDouble(product.getPrice()/100) ;
			double mRestrict = cPiple.getTradeMonth()==null?0:cPiple.getTradeMonth(); // 月限
			if(nowMonthA > mRestrict && mRestrict!=0){
				return GlobalConst.CheckResult.LIMIT_MONTH;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GlobalConst.CheckResult.ERROR;
		}
		return GlobalConst.CheckResult.PASS;
	}
	public void doWhenPaySuccess(TOrder order){
		try{
			if(isUseableTradeDayAndMonth()){
				updateCacheTransProvince(order.getPipleId(), order.getChannelId(), order.getProvinceId(), order.getAmount().doubleValue());
				updateCacheTrans(order.getPipleId(), order.getChannelId(), order.getAmount().doubleValue());
				updateCacheTransMobile(order.getPipleId(), order.getImsi(),order.getMobile(), order.getAmount().doubleValue());
				updateCacheTransPiple(order.getPipleId(), order.getAmount().doubleValue());
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error("支付成功后发生错误:"+e.getMessage());
		}
	}
	public void updateCacheTransProvince(String pipleId,String channelId,int provinceId,double amout){
		try {
			String dTradeKey =  getCacheKeyProvince(pipleId,channelId,provinceId,GlobalConst.MapCache_Key.DAY_TRADE);
			String mTradeKey = getCacheKeyProvince(pipleId,channelId,provinceId,GlobalConst.MapCache_Key.MONTH_TRADE);
			String dayTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeKey);
			String monthTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeKey);
			if(StringUtil.isNotEmptyString(dayTrade)){
				double newDayTrade = NumberUtil.getDouble(dayTrade) + amout;
				log.info("MapCacheManager dTradeKey="+dTradeKey+",nowDayTrade="+newDayTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeKey, newDayTrade+"");
			}
			if(StringUtil.isNotEmptyString(monthTrade)){
				double newMonthTrade =NumberUtil.getDouble(monthTrade) + amout;
				log.info("MapCacheManager mTradeKey="+mTradeKey+",nowMonthTrade="+newMonthTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeKey, newMonthTrade+"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	public void updateCacheTrans(String pipleId,String channelId,double amout){
		try {
			String dTradeKey =  getCacheKeyPipleChannel(pipleId,channelId,GlobalConst.MapCache_Key.DAY_TRADE);
			String mTradeKey = getCacheKeyPipleChannel(pipleId,channelId,GlobalConst.MapCache_Key.MONTH_TRADE);
			String dayTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeKey);
			String monthTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeKey);
			if(StringUtil.isNotEmptyString(dayTrade)){
				double newDayTrade = NumberUtil.getDouble(dayTrade) + amout;
				log.info("MapCacheManager dTradeKey="+dTradeKey+",nowDayTrade="+newDayTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeKey, newDayTrade+"");
			}
			if(StringUtil.isNotEmptyString(monthTrade)){
				double newMonthTrade = NumberUtil.getDouble(monthTrade) + amout;
				log.info("MapCacheManager mTradeKey="+mTradeKey+",nowMonthTrade="+newMonthTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeKey, newMonthTrade+"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void updateCacheTransMobile(String pipleId,String imsi,String mobile,double amout){
		try {
			String mobileUniqueKey = mobile;
			if(StringUtil.isEmpty(mobile)){
				mobileUniqueKey = imsi;
			}
			String dTradeKey =  getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.DAY_TRADE);
			String mTradeKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_TRADE);
			String dTradeCountKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.DAY_TRADE_COUNT);
			String mTradeCountKey = getCacheKeyMobile(pipleId,mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_TRADE_COUNT);
			String dTradeGlobalKey = getCacheKeyMobile("global_",mobileUniqueKey,GlobalConst.MapCache_Key.DAY_GLOABL_TRADE);
			String mTradeGlobalKey = getCacheKeyMobile("global_",mobileUniqueKey,GlobalConst.MapCache_Key.MONTH_GLOABAL_TRADE);
			String dayTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeKey);
			String monthTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeKey);
			String dayTradeCount = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeCountKey);
			String monthTradeCount = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeCountKey);
			String dayGlobalTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dTradeGlobalKey);
			String monthGlobalTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mTradeGlobalKey);
			
			//成功日交易金额
			if(StringUtil.isNotEmptyString(dayTrade)){
				double newDayTrade = NumberUtil.getDouble(dayTrade) + amout;
				log.info("MapCacheManager dTradeKey="+dTradeKey+",nowDayTrade="+newDayTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeKey, newDayTrade+"");
			}
			//成功月交易金额
			if(StringUtil.isNotEmptyString(monthTrade)){
				double newMonthTrade =NumberUtil.getDouble(monthTrade) + amout;
				log.info("MapCacheManager mTradeKey="+mTradeKey+",nowMonthTrade="+newMonthTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeKey, newMonthTrade+"");
			}
			//成功日交易成功次数
			if(StringUtil.isNotEmptyString(dayTradeCount)){
				int newDayTradeCount = NumberUtil.getInteger(dayTradeCount) + 1;
				log.info("MapCacheManager dTradeKey="+dTradeCountKey+",nowDayTradeCount="+newDayTradeCount);
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeCountKey, newDayTradeCount+"");
			}
			//成功月交易成功次数
			if(StringUtil.isNotEmptyString(monthTradeCount)){
				int newMonthTradeCount = NumberUtil.getInteger(monthTradeCount) + 1;
				log.info("MapCacheManager mTradeCountKey="+mTradeCountKey+",newMonthTradeCount="+newMonthTradeCount);
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeCountKey, newMonthTradeCount+"");
			}
			//全局日交易成功金额
			if(StringUtil.isNotEmptyString(dayGlobalTrade)){
				double newDayGlobalTrade = NumberUtil.getDouble(dayGlobalTrade) + amout;
				log.info("MapCacheManager dTradeGlobalKey="+dTradeGlobalKey+",newDayGlobalTrade="+newDayGlobalTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(dTradeGlobalKey, newDayGlobalTrade+"");
			}
			//全局月交集金额
			if(StringUtil.isNotEmptyString(monthGlobalTrade)){
				double newMonthGlobalTrade =NumberUtil.getDouble(monthGlobalTrade) + amout;
				log.info("MapCacheManager mTradeGlobalKey="+mTradeGlobalKey+",newMonthGlobalTrade="+newMonthGlobalTrade);
				MapCacheManager.getInstance().getDailyClearMapCache().put(mTradeGlobalKey, newMonthGlobalTrade+"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void updateCacheTransPiple(String pipleId,double amout){
		String dPipleTradeKey =  getCacheKeyPiple(pipleId,GlobalConst.MapCache_Key.PIPLE_DAY_TRADE);
		String mPIpleTradeKey =  getCacheKeyPiple(pipleId,GlobalConst.MapCache_Key.PIPLE_MONTH_TRADE);
		String dPipleTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(dPipleTradeKey);
		String mPIpleTrade = MapCacheManager.getInstance().getDailyClearMapCache().get(mPIpleTradeKey);
		//通道日限已经跑的成功金额
		if(StringUtil.isNotEmptyString(dPipleTrade)){
			double newDayPipleTrade = NumberUtil.getDouble(dPipleTrade) + amout;
			log.info("MapCacheManager dPipleTradeKey="+dPipleTradeKey+",newDayPipleTrade="+newDayPipleTrade);
			MapCacheManager.getInstance().getDailyClearMapCache().put(dPipleTradeKey, newDayPipleTrade+"");
		}
		//通道月限已经跑的成功金额
		if(StringUtil.isNotEmptyString(mPIpleTrade)){
			double newMonthPipleTrade =NumberUtil.getDouble(mPIpleTrade) + amout;
			log.info("MapCacheManager mPIpleTradeKey="+mPIpleTradeKey+",newMonthPipleTrade="+newMonthPipleTrade);
			MapCacheManager.getInstance().getDailyClearMapCache().put(mPIpleTradeKey, newMonthPipleTrade+"");
		}
	}
	private String getCacheKeyPipleChannel(String pipleId,String channelId,String key){
		return key+"|"+pipleId+"|"+channelId;
	}
	private String getCacheKeyProvince(String pipleId,String channelId,int provinceId,String key){
		return key+"|"+pipleId+"|"+channelId+"|"+provinceId;
	}
	private String getCacheKeyMobile(String pipleId,String mobile,String key){
		return key+"|"+pipleId+"|"+mobile;
	}
	private String getCacheKeyPiple(String pipleId,String key){
		return key+"|"+pipleId;
	}
	protected boolean isUseableTradeDayAndMonth(){
		return false;
	}
	protected void statistics(String stepId,String groupId,String content){
		try {
			TSdkOperation operation = new TSdkOperation();
			operation.setCreateTime(DateTimeUtils.getCurrentTime());
			operation.setFlowId(groupId);
			operation.setOperationId(KeyHelper.createID());
			operation.setOperationStep(stepId);
			operation.setOperationContent(content);
			tSdkOperationDao.insert(operation);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("statistics error:"+e.toString());
		}
		
	}
	@Deprecated
	public String notifyChannel(String url,String mobile,String orderId,String spnumber,String status,String extData){
		return notifyChannel(url, mobile,null, orderId, spnumber, null, status, extData);
	}
	@Deprecated
	public String notifyChannel(String url,String mobile,String imsi,String orderId,String spnumber,String pipleId,String status,String extData){
		String rst = "";
		try {
			String param = "orderId="+orderId+"&mobile="+mobile+"&status="+status+"&spnumber="+spnumber;
			if(!StringUtil.isEmpty(pipleId)){
				param += "&pipleId="+pipleId;
			}
			if(!StringUtil.isEmpty(imsi)){
				param += "&imsi="+imsi;
			}
			if(!StringUtil.isEmpty(extData)){
				param += "&extData="+StringUtil.urlEncodeWithUtf8(extData);
			}
			String ackUrl = url+"?"+param;
			log.info("sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
			rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
			log.info("getFromChannel= " + rst + " ,orderId="+orderId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rst;
	}
	/**
	 * 使用这个
	 * @param url
	 * @param order
	 * @param spnumber
	 * @param status
	 */
	public String notifyChannel(String url,TOrder order,String spnumber,String status){
		String rst = "";
		try {
				String param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&status="+status+"&spnumber="+spnumber+"&amount="+order.getAmount().doubleValue();
				if(!StringUtil.isEmpty(order.getPipleId())){
					param += "&pipleId="+order.getPipleId();
				}
				if(!StringUtil.isEmpty(spnumber)){
					param += "&productCode="+spnumber;
				}
				if(!StringUtil.isEmpty(order.getImsi())){
					param += "&imsi="+order.getImsi();
				}
				if(!StringUtil.isEmpty(order.getExtData())){
					param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
				}
				TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
				if(tPiple != null && StringUtil.isNotEmptyString(tPiple.getPipleNumber())){
					param += "&pipleKey="+tPiple.getPipleNumber();
				}
				if(!StringUtil.isEmpty(order.getAppId())){
					param += "&appId="+order.getAppId();
				}
				if(!StringUtil.isEmpty(order.getChannelId())){
					TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
					param += "&channelApi="+tChannel.getApiKey();
				}
				String ackUrl = url+"?"+param;
				log.info("sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
				statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
				rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
				log.info("getFromChannel= " + rst + " ,orderId="+order.getOrderId());
				statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return rst;
	}
	public String notifyChannelSDK(String url,TOrder order){
		return "ok";
		
	}

	/***
	 * API方式交易同步
	 * 同步参数：orderId（订单号）,mobile（手机号）,status（状态）,amout（金额）,productCode（产品代码）,pipleKey（业务代码）,apiKey（渠道代码）,imsi,extData
	 * @param url
	 * @param order
	 * @param status
	 */
	public String notifyChannelAPIForKey(String url,TOrder order,String status){
		String rst = "";
		try {
			String param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&status="+status
					+"&amount="+order.getAmount().doubleValue();
			TProduct product = tProductDao.selectByPrimaryKey(order.getProductId());
			TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
			TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
			param += "&productCode="+product.getProductCode()+"&pipleKey="+tPiple.getPipleNumber()+"&apiKey="+tChannel.getApiKey();
			if(!StringUtil.isEmpty(order.getImsi())){
				param += "&imsi="+order.getImsi();
			}
			if(!StringUtil.isEmpty(order.getExtData())){
				param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
			}
			String ackUrl = url+"?"+param;
			log.info("notifyChannelSMS sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
			statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
			rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
			log.info("notifyChannelSMS getFromChannel= " + rst + " ,orderId="+order.getOrderId());
			statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rst;
	}


	/***
	 * API方式交易同步
	 * 同步参数：orderId（订单号）,mobile（手机号）,status（状态）,amout（金额）,productCode（产品代码）,pipleId（业务ID）,apiKey（渠道代码）,imsi,extData
	 * @param url
	 * @param order
	 * @param status
	 */
	public String notifyChannelAPI(String url,TOrder order,String status){
		String rst = "";
		try {
				String param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&status="+status
						+"&amount="+order.getAmount().doubleValue();
				TProduct product = tProductDao.selectByPrimaryKey(order.getProductId());
				TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
				TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
				param += "&productCode="+product.getProductCode()+"&pipleId="+tPiple.getPipleId()+"&apiKey="+tChannel.getApiKey();
				if(!StringUtil.isEmpty(order.getImsi())){
					param += "&imsi="+order.getImsi();
				}
				if(!StringUtil.isEmpty(order.getExtData())){
					param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
				}
				String ackUrl = url+"?"+param;
				log.info("notifyChannelSMS sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
				statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
				rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
				log.info("notifyChannelSMS getFromChannel= " + rst + " ,orderId="+order.getOrderId());
				statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return rst;
	}
	
	
	/***
	 * 短信方式交易同步
	 * SMS同步参数：orderId（订单号）,mobile（手机号）,port（端口号）,status（状态）,amout（金额）,productCode（产品代码）,pipleKey（业务代码）,apiKey（渠道代码）,imsi,extData
	 * @param url
	 * @param order
	 * @param port
	 * @param status
	 */
	public String notifyChannelSMS(String url,TOrder order,String port,String status){
		String rst = "";
		try {
				String param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&port="+port+"&status="+status
						+"&amount="+order.getAmount().doubleValue();
				TProduct product = tProductDao.selectByPrimaryKey(order.getProductId());
				TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
				TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
				param += "&productCode="+product.getProductCode()+"&pipleKey="+tPiple.getPipleNumber()+"&apiKey="+tChannel.getApiKey();
				if(!StringUtil.isEmpty(order.getImsi())){
					param += "&imsi="+order.getImsi();
				}
				if(!StringUtil.isEmpty(order.getExtData())){
					param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
				}
				String ackUrl = url+"?"+param;
				log.info("notifyChannelSMS sendToChannel:"+this.getClass().getName()+" ackUrl:" + ackUrl);
				statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
				rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
				log.info("notifyChannelSMS getFromChannel= " + rst + " ,orderId="+order.getOrderId());
				statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return rst;
	}
	
	/***
	 * 交易同步统一入口
	 * API同步参数：orderId（订单号）,mobile（手机号）,status（状态）,amout（金额）,productCode（产品代码）,pipleId（业务ID）,apiKey（渠道代码）,imsi,extData
	 * SMS同步参数：orderId（订单号）,mobile（手机号）,port（端口号）,status（状态）,amout（金额）,productCode（产品代码）,pipleKey（业务代码）,apiKey（渠道代码）,imsi,extData
	 * SDK同步参数：暂无  暂不同步
	 * @param url
	 * @param order
	 * @param port
	 */
	public String notifyChannelAll(String url,TOrder order,String port){
		String rst = "";
		try {
				String param = "";
				if(GlobalConst.FromType.FROM_TYPE_API.equals(order.getFromType()+"")){
					param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&status=ok"
							+"&amount="+order.getAmount().doubleValue();
					TProduct product = tProductDao.selectByPrimaryKey(order.getProductId());
					TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
					TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
					param += "&productCode="+product.getProductCode()+"&pipleId="+tPiple.getPipleId()+"&apiKey="+tChannel.getApiKey();
					if(!StringUtil.isEmpty(order.getImsi())){
						param += "&imsi="+order.getImsi();
					}
					if(!StringUtil.isEmpty(order.getExtData())){
						param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
					}
				}else if(GlobalConst.FromType.FROM_TYPE_SMS.equals(order.getFromType()+"")){
					param = "orderId="+order.getOrderId()+"&mobile="+order.getMobile()+"&port="+port+"&status=ok"
							+"&amount="+order.getAmount().doubleValue();
					TProduct product = tProductDao.selectByPrimaryKey(order.getProductId());
					TPiple tPiple = tPipleDao.selectByPrimaryKey(order.getPipleId());
					TChannel tChannel = tChannelDao.selectByPrimaryKey(order.getChannelId());
					param += "&productCode="+product.getProductCode()+"&pipleKey="+tPiple.getPipleNumber()+"&apiKey="+tChannel.getApiKey();
					if(!StringUtil.isEmpty(order.getImsi())){
						param += "&imsi="+order.getImsi();
					}
					if(!StringUtil.isEmpty(order.getExtData())){
						param += "&extData="+StringUtil.urlEncodeWithUtf8(order.getExtData());
					}
				}else if(GlobalConst.FromType.FROM_TYPE_SDK.equals(order.getFromType()+"")){
					
				}
				String ackUrl = url+"?"+param;
				log.info("notifyChannel getFromTpye= " + order.getFromType() + " ,ackUrl="+ackUrl + " ,orderId="+order.getOrderId());
				statistics(STEP_PAY_PLATFORM_TO_CHANNEL, order.getGroupId(),ackUrl);
				if(!StringUtil.isEmpty(ackUrl) && !StringUtil.isEmpty(param)){ // 同步地址和参数都不为空 则进行同步
					rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
					log.info("notifyChannel getFromTpye= " + order.getFromType() + " ,rst="+rst + " ,orderId="+order.getOrderId());
				}
				statistics(STEP_PAY_CHANNEL_TO_PLATFORM, order.getGroupId(),rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return rst;
	}
	
	public static String getFullMessage(String mobile,String company,String business,String code){
//		001$1$PM1023$1003$P00010$a101
//		001$2$PM1023$1003$123
		String jsonStr = MapCacheManager.getInstance().getSmsOrderCache().get(mobile);
		JSONObject param = JSONObject.fromObject(jsonStr);
		String apiKey = param.optString("apiKey");
		String pipleKey = param.optString("pipleKey");
		String decodeMsg = company+"$"+business+"$"+pipleKey+"$"+apiKey+"$"+code;
		return decodeMsg;
	}
	
}
