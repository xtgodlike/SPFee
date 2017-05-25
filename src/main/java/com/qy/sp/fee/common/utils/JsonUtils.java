package com.qy.sp.fee.common.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONTokener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * JSON处理工具�?.
 *
 * @author <a href="http://www.jiangzezhou.com">jiangzezhou</a>
 * @version 1.0.0.0, 6/16/15 09::55
 */
public final class JsonUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonUtils.class.getName());

    private JsonUtils() {
    }

    public static Map<String, String> jsonMapString(final String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        JSONTokener jtokener = new JSONTokener(jsonStr);
        try {
        	org.json.JSONObject json = new org.json.JSONObject(jtokener);
            Iterator it = json.keys();
            while (it.hasNext()) {
                final String key = (String) it.next();
                map.put(key, String.valueOf(json.get(key)));
            }
        } catch (JSONException e) {
            LOGGER.info("json to map String error!!");
        }
        return map;
    }
    public static<T> String list2Json(final List<T> list){
    	return bean2Json(list);
    }
    public static<T> List<T> json2List(String jsonStr,Class<T> clazz){
    	 final Gson gson = new Gson();
    	 List<T> retList = new ArrayList<T>();
         try {
        	 List<JsonElement> list = gson.fromJson(jsonStr,new TypeToken<List<JsonElement>>(){
        		 
        	 }.getType());
        	 for(JsonElement e : list){
        		 retList.add(gson.fromJson(e, clazz));
        	 }
         } catch (JsonSyntaxException e) {
             LOGGER.info("json2Bean error" + e.getMessage());
         }
         return retList;
    }
    public static <T> T json2Bean(final String jsonStr, Class<T> clazz) {
        final Gson gson = new Gson();
        T t = null;
        try {
            t = gson.fromJson(jsonStr, clazz);
        } catch (JsonSyntaxException e) {
            LOGGER.info("json2Bean error" + e.getMessage());
        }
        return t;
    }

    public static <T> String bean2Json(final T t) {
        final Gson gson = new Gson();
        String jsonStr = null;
        try {
            jsonStr = gson.toJson(t);
        } catch (JsonSyntaxException e) {
            LOGGER.info("json2Bean error" + e.getMessage());
        }
        return jsonStr;
    }

    /**
     * 将map结构转为key=value&key=value格式.
     *
     * @param params map
     * @return format str
     */
    public static String map2KVStr(final Map<String, String> params,
                                   final boolean urlEncode) {
        final StringBuilder sb = new StringBuilder();
        final Set<Map.Entry<String, String>> entrySet = params.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String value = entry.getValue();
            if (urlEncode) {
                value = StringUtil.urlEncodeWithUtf8(value);
            }
            sb.append(entry.getKey() + "=" + value)
                    .append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

}
