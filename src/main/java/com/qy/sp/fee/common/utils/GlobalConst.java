package com.qy.sp.fee.common.utils;

import java.util.HashMap;
import java.util.Map;


public class GlobalConst {
	
	//数据库中所有op_status字段的值，都套用这个
	public final static class OP_STATUS{
		public final static int OPEN 	= 1;
		public final static int CLOSE 	= 0;
	}
	
	//交易是否被扣量
	public final static class DEC_STATUS{
		public final static int DEDUCTED 	= 1;
		public final static int UNDEDUCTED 	= 0;
	}
	
	// 缓存KEY
	public final static class MapCache_Key{
		public final static String  DAY_TRADE = "DayTrade"; 				// 日交易金额
		public final static String  MONTH_TRADE = "MonthTrade";	// 月交易金额
		public final static String  DAY_TRADE_COUNT = "DayTradeCount";	// 日成功次数
		public final static String  MONTH_TRADE_COUNT = "MonthTradeCount";	// 月成功次数
		public final static String  DAY_REQUEST_COUNT = "DayRequestCount";	// 日请求次数
		public final static String  MONTH_REQUEST_COUNT = "MonthRequestCount";	// 月请求次数
		public final static String  DAY_GLOABL_TRADE = "DayGlobalTrade";	// 全局日金额
		public final static String  MONTH_GLOABAL_TRADE = "MonthGlobalTrade";	// 全局月金额
		public final static String  PIPLE_DAY_TRADE = "PipleDayTrade"; 				// 交易通道日交易金额
		public final static String  PIPLE_MONTH_TRADE = "PipleMonthTrade";	// 交易通道月交易金额
	}
		
	// 交易校验结果码
	public static final class CheckResult{
		public final static int PASS = 0; 							// 通过
		public final static int ERROR = 1; 						// 校验异常
		public final static int LIMIT_DAY = 2001; 			// 超日限
		public final static int LIMIT_MONTH = 2002; 		// 超月限
		public final static int LIMIT_DAY_PROVINCE = 2003; 		// 超省份日限
		public final static int LIMIT_MONTH_PROVINCE = 2004; 		// 超省份月限
		public final static int LIMIT_DAY_MOBILE = 2005; 		// 用户超扣费金额日限
		public final static int LIMIT_MONTH_MOBILE = 2006; 		// 用户超扣费金额月限
		public final static int LIMIT_DAY_COUNT_MOBILE = 2007; 		// 用户超扣费次数日限
		public final static int LIMIT_MONTH_COUNT_MOBILE = 2008; 		// 用户超扣费次数月限
		public final static int LIMIT_DAY_REQUEST_COUNT_MOBILE = 2009; 		// 用户超扣费次数月限
		public final static int LIMIT_MONTH_REQUEST_COUNT_MOBILE = 2010; 		// 用户超扣费次数月限
		public final static int LIMIT_DAY_PIPLE = 2011; 		// 通道跑满
		public final static int LIMIT_MONTH_PIPLE = 2012; 		// 通道月线跑满
		
		// 有效校验错误原因细分
		public final static int MUST_PARAM_ISNULL = 1001; 						// 必填参数不能为空
		public final static int ORDER_FAIL = 1002; 										// 订单号无效
		public final static int ORDER_HASSUCCESS = 1003; 							// 订单已提交，请勿重复请求
		public final static int UNKNOWN_APIKEY = 1004;  	// 未知渠道代码
		public final static int APIPWD_ERROR = 1005;  	// 渠道密码错误
		public final static int CHANNEL_CLOSE = 1006;  	// 渠道未开通
		public final static int PIPLE_ERROR = 1007;  	// 无此计费通道或渠道未配置该计费通道
		public final static int PIPLE_CLOSE = 1008;  	// 计费通道未开通
		public final static int PRODUCTCODE_ERROR = 1009;  	// 未知产品代码
		public final static int PIPLE_PRODUCT_ERROR = 1010;  	// 计费通道未配置该产品
		public final static int PIPLE_PRODUCT_CLOSE = 1011;  	// 计费通道产品未开通
		public final static int CHANNEL_PRODUCT_ERROR = 1012;  	// 渠道未配置该产品
		public final static int CHANNEL_PRODUCT_CLOSE = 1013;  	// 渠道产品未开通
		public final static int REQ_FREQUENT = 1014;  	// 请求太频繁
		public final static int MOBILE_ISBLANK = 1015;  	// 手机号码属于黑名单
		public final static int MOBLE_HOST_ERROR = 1016;  	// 号码运营商不符
		public final static int PIPLE_PROVINCE_CLOSE = 1017;  	// 计费通道分省未开通
		public final static int PRODUCT_APPID_ERROR = 1018;		// productCode或appId不正确
		public final static int IMSI_ERROR = 1019;		// IMSI不能为空
		public final static int IP_PROVINCE_ERROR = 1020;		// IP省份不能为空
		
	}
	// 交易校验结果说明
	public static final class CheckResultDesc{
		public static Map<Integer, String> message = new HashMap<Integer, String>();
		static{
			message.put(CheckResult.PASS, "校验通过"); 
			message.put(CheckResult.ERROR, "校验异常"); 
			message.put(CheckResult.LIMIT_DAY, "渠道交易超日限"); 
			message.put(CheckResult.LIMIT_MONTH, "渠道交易超月限"); 
			message.put(CheckResult.LIMIT_DAY_PROVINCE, "渠道交易超省份日限"); 
			message.put(CheckResult.LIMIT_MONTH_PROVINCE, "渠道交易超省份月限"); 
			message.put(CheckResult.LIMIT_DAY_MOBILE, "手机用户超日限"); 
			message.put(CheckResult.LIMIT_MONTH_MOBILE, "手机用户超月限"); 
			message.put(CheckResult.LIMIT_DAY_COUNT_MOBILE, "手机用户超扣费次数日限"); 
			message.put(CheckResult.LIMIT_MONTH_COUNT_MOBILE, "手机用户超扣费次数月限"); 
			message.put(CheckResult.LIMIT_DAY_REQUEST_COUNT_MOBILE, "手机用户请求次数日限"); 
			message.put(CheckResult.LIMIT_MONTH_REQUEST_COUNT_MOBILE, "手机用户请求次数月限"); 
			
			message.put(CheckResult.MUST_PARAM_ISNULL, "必填参数不能为空"); 
			message.put(CheckResult.ORDER_FAIL, " 订单号无效"); 
			message.put(CheckResult.ORDER_HASSUCCESS, "订单已提交，请勿重复请求"); 
			message.put(CheckResult.UNKNOWN_APIKEY, "未知渠道代码"); 
			message.put(CheckResult.APIPWD_ERROR, "渠道密码错误"); 
			message.put(CheckResult.CHANNEL_CLOSE, "渠道未开通"); 
			message.put(CheckResult.PIPLE_ERROR, " 无此计费通道或渠道未配置该计费通道"); 
			message.put(CheckResult.PIPLE_CLOSE, "计费通道未开通"); 
			message.put(CheckResult.PRODUCTCODE_ERROR, "未知产品代码"); 
			message.put(CheckResult.PIPLE_PRODUCT_ERROR, "计费通道未配置该产品"); 
			message.put(CheckResult.PIPLE_PRODUCT_CLOSE, "计费通道产品未开通"); 
			message.put(CheckResult.CHANNEL_PRODUCT_ERROR, "渠道未配置该产品"); 
			message.put(CheckResult.CHANNEL_PRODUCT_CLOSE, "渠道产品未开通"); 
			message.put(CheckResult.REQ_FREQUENT, " 请求太频繁"); 
			message.put(CheckResult.MOBILE_ISBLANK, "手机号码属于黑名单"); 
			message.put(CheckResult.MOBLE_HOST_ERROR, "号码运营商与计费通道不符"); 
			message.put(CheckResult.PIPLE_PROVINCE_CLOSE, "计费通道分省未开通"); 
			message.put(CheckResult.PRODUCT_APPID_ERROR, "productCode或appId不正确"); 
			message.put(CheckResult.IMSI_ERROR, "IMSI错误或为空"); 
			message.put(CheckResult.IP_PROVINCE_ERROR, "IP省份错误或者不存在"); 
			message.put(CheckResult.LIMIT_DAY_PIPLE, "业务日限金额已满"); 
			message.put(CheckResult.LIMIT_MONTH_PIPLE, "业务月限金额已满"); 
		}
	}
	
	
	// 返回结果
	public final static class PluginType{
		public final static String PLUGIN_MOBILE_MUST 	= "50001"; //手机号必传
		public final static String PLUGIN_MOBILE_NONE 	= "50002";//手机号可空
	}
	// 返回结果
	public final static class Result{
		public final static String SUCCESS 	= "0"; //成功
		public final static String ERROR 	= "1";//失败
	}
	
	// 交易状态通用
	public final static class OrderStatus{
		public final static int INIT 	= 0; 					//开始
		public final static int TRADING 	= 1; 			//交易中
		public final static int SUCCESS = 2; 			//成功
		public final static int FAIL 	= 3; 					//失败
	}
	
	public final static class FromType{
		public static final String FROM_TYPE_SDK = "1";//SDK发起请求
		public static final String FROM_TYPE_API = "2";//API发起请求
		public static final String FROM_TYPE_SMS = "3";//短信发起请求
	}
	public final static class SyncResultType{
		public static final int SYNC_INIT= 0;//同步渠道初始化
		public static final int SYNC_SUCCESS= 1;//同步渠道结果成功
		public static final int SYNC_ERROR= 2;//同步给渠道结果失败
	}
	// 交易详细子状态
  public final static class SubStatus{
	  	public static final int PAY_INIT = 0;															// 初始化
	    public static final int PAY_GET_SMS_SUCCESS = 1;									// 获取验证码成功
	    public static final int PAY_GET_SMS_FAIL = 2;											// 获取验证码失败
	    public static final int PAY_FILTER_LOCAL_SMS_SUCCESS = 11;				// 截取本地短信成功
	    public static final int PAY_FILTER_LOCAL_SMS_FAIL = 12;						// 截取本地短信失败
	    public static final int PAY_SUBMIT_CODE_SUCCESS = 13;						// 提交验证码成功
	    public static final int PAY_SUBMIT_CODE_FAIL = 14;								// 提交验证码失败
	    public static final int PAY_SEND_MESSAGE_SUCCESS = 15;						// 发送短信指令成功
	    public static final int PAY_SEND_MESSAGE_FAIL = 16;								// 发送短信指令失败
	    public static final int PAY_OVER_FLOW = 17;								// 支付流程结束
	    public static final int PAY_SUCCESS = 3;													// 支付成功
	    public static final int PAY_SUCCESS_DG = 31;												//包月订购成功
	    public static final int PAY_ERROR = 4;														// 支付失败
	    public static final int PAY_ERROR_TG = 41;													//包月退订
  }
	public final static class LimitType{
		public final static int LIMIT_PIPLE 	= 1;
		public final static int LIMIT_GLOABAL 	= 2;
	}
	public final static class GetDataHttpType{
		public final static String HTTP_GET 	= "1";
		public final static String HTTP_POST_FORM 	= "2";
		public final static String HTTP_POST_BODY_JSON = "3";
		public final static String HTTP_POST_BODY_XML = "4";
	}
	public final static class DataType{
		public final static String JSON 	= "1";
		public final static String XML 	= "2";
	}
}

