package com.qy.sp.fee.common.utils;

public class IReadConstant {

	public final static String CPID 	= "86";
	public final static String APPID 	= "00";
	public final static String CHANNEID = "00";
	public final static String MYID 	= "00";
	
	public final static String MD5_KEY = "b13e133450f9bc36643e738e018fa29d";
	public final static String SERVER_PHONE = "400-10086";
	public final static String CPCUSTOM = "000";
	
	
	public final static String SUCCESS 	= "0";
	public final static String FAILE 	= "1";

	public final static class HRet{
		public final static int SUCCESS = 0;
		public final static int FAIL 	= 1;
	}

	public final static class Command{
		public final static String OFF_LINE = "1"; //离线
		public final static String ON_LINE 	= "2"; //在线
	}
	
	public final static class Result{
		public final static String SUCCESS 	= "0"; //成功
		public final static String ERROR 	= "1";//失败
	}
	
	//订单状态：0：初始  1：同意计费  2：计费成功  3：计费失败  4：退订
	public final static class OrderStatus{
		public final static int INIT 	= 0; //初始
		public final static int AGREE 	= 1;
		public final static int SUCCESS = 2;
		public final static int FAIL	= 3;
		public final static int UNSUB	= 4;
	}
	
	public final static class IreadServiceID{
		public final static String VALIDATE_ORDER_ID 	= "validateorderid";
		public final static String NOTITFY_RESULT 		= "notifyresult";
	}
	
	public final static String IREADER_ACCESS_NO 			= "106566660020";
	public final static String IREADER_ACCESS_NO_CONFIRM 	= "106566660020";
	
	
	
}
