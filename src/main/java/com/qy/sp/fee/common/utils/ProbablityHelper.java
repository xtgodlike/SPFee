package com.qy.sp.fee.common.utils;

/**
 * @author wangwang
 *
 */
public class ProbablityHelper {

	/**
	 * @param probablity
	 *            概率 （0-100）
	 * @return 返回true的概率为 probablity%
	 */
	public static boolean getProbablityAction(int probablity) {

		// 0~99
		int d = (int) (Math.random() * 100);
		if (d < probablity) {
			return true;
		} else {
			return false;
		}

	}

}
