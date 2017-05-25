package com.qy.sp.fee.common.utils;

import org.apache.commons.lang.StringUtils;

public class BaiDuProductIdHelper {

	public static String parserProductId(String command) {
		String result = "";
		if (StringUtils.isEmpty(command)) {
			return result;
		}
		int begin = command.indexOf(",,,");
		if (begin < 0) {
			return result;
		}
		int end = command.lastIndexOf(",,,");
		if (end < begin) {
			return result;
		}
		result = command.substring(begin + 3, end);
		return result;
	}
}
