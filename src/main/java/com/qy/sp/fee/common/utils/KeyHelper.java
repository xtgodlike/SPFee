package com.qy.sp.fee.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;



/**
 * 主键生成策略
 *
 */
public class KeyHelper {
	
	
	/**
	 * @param String
	 * 功能描述 ：生成一个不重复的字符串
	 */

	public static String createKey() 
	{
		String key = "";
		String s = getRandomNumber(10);
		key = new Date().getTime() + s;
		return key;
	}
	
	public static String createID(){ //32位
		StringBuffer ID = new StringBuffer();
		String s = getRandomNumber(19);
		ID.append(new Date().getTime()).append(s);
		return ID.toString();
	}
	
	public static String createCode(){
		String s = getRandomNumber(4);
		String d = createDate();
		String code = "";
		code =  d+s;
		return code;
	}
	
	public static String HEX(){
		return Long.toHexString(new Date().getTime());
	}
	
	public static String creatKey(int len){
		String key = getRandomNumber(len);
		return key;
	}
	
	public static String createDate(){
		Date cDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String str = format.format(cDate);
		return str;
	}
	
	public static String createTime(){
		Date cDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String str = format.format(cDate);
		return str;
	}
	
	/**
	 * @param String
	 * 功能描述 ：得到一个cnt位的随机
	 */
	public static String getRandomNumber(int cnt) {
		int j = cnt;
		long[] random = new long[j];
		for (int i = 0; i < j; i++) 
		{
			random[i] = Math.round(Math.floor((Math.random() * 10)));
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < random.length; i++) 
		{
			String temp = Long.toString(random[i]);
			sb.append(temp);
		}
		return sb.toString();
	}
	
	/**
	 * 获取0～100之间的随机数
	 * @return
	 */
	public static int getRandomValue(int min,int max){
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
        return s;
	}
	
	/**
	 * 获取1～100之间的随机数
	 * @return
	 */
	public static int randomVal(){
		return getRandomValue(1,100);
	}
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 100; i++) {
			System.out.println((i+1)+"号密码："+"qy"+KeyHelper.creatKey(21));
		}
		
	}
	
}
