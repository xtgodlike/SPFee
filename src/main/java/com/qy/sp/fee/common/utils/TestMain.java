package com.qy.sp.fee.common.utils;

import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class TestMain {

 public static void main(String[] args) throws UnsupportedEncodingException {
//	 int a = 0;
//	 Long b =0l;
//	 System.out.println(a==b);
//	 System.out.println(a==b.longValue());
	 String asd = "BK*223088";
	 String piplePCode = asd.substring(0,5);
	 String myApiKey = asd.substring(5,asd.length());
	 System.out.println(piplePCode+"--"+myApiKey);
 }
 
	
}
