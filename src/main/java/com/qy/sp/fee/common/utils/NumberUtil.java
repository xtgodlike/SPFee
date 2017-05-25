package com.qy.sp.fee.common.utils;


public class NumberUtil {


    /**
     * 转整型
     *
     * @param obj
     * @return
     */
    public static int getInteger(Object obj) {
        return getInteger(obj, 0);
    }

    /**
     * 转整型
     *
     * @param obj
     * @param def
     * @return
     */
    public static int getInteger(Object obj, int def) {
        String str = obj == null ? "" : obj.toString();
        int result = def;
        try {
            result = Integer.valueOf(str);
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * 转长整型
     *
     * @param obj
     * @return
     */
    public static long getLong(Object obj) {
        return getLong(obj, 0);
    }

    /**
     * 转长整型
     *
     * @param obj
     * @param def
     * @return
     */
    public static long getLong(Object obj, long def) {
        String str = obj == null ? "" : obj.toString();

        long result = def;
        try {
            result = Long.valueOf(str);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 转double,如果为null 为默认值为0
     *
     * @param obj
     * @return
     */
    public static double getDouble(Object obj) {
        return getDouble(obj, 0);
    }

    /**
     * 转double,如果为null 为默认值
     *
     * @param obj
     * @param def
     * @return
     */
    public static double getDouble(Object obj, int def) {
        String str = obj == null ? "" : obj.toString();
        double result = def;
        try {
            result = Double.valueOf(str);
        } catch (Exception e) {
        }

        return result;
    }

    public static boolean getBoolean(Object obj, boolean def) {
        String str = obj == null ? "" : obj.toString();
        boolean result = def;
        try {
            result = Boolean.valueOf(str);
        } catch (Exception e) {
        }

        return result;
    }

    public static boolean getBoolean(Object obj) {
        return getBoolean(obj, false);
    }

    /**
     * 是否是数字
     *
     * @param strVal
     * @return
     */
    public static boolean isNumber(String strVal) {
        try {
            if (StringUtil.isEmpty(strVal)) {
                return false;
            }

            Integer.valueOf(strVal);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
