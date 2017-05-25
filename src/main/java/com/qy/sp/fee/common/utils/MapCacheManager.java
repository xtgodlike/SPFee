package com.qy.sp.fee.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qy.sp.fee.dao.impl.base.RedisDao;  
  
public class MapCacheManager {  
  
	public static final String KEY_DAILY_CLEAR_MAP = "KEY_DAILY_CLEAR_MAP";
	public static final String KEY_SMS_ORDER_MAP = "KEY_SMS_ORDER_MAP";
	// 缓存实例对象  
    private volatile static MapCacheManager mapCacheObject;
    private Map<String,CacheProvider> cacheProviderMap = new ConcurrentHashMap<String, MapCacheManager.CacheProvider>();
    private MapCacheManager() {  
    }  
  
    /** 
     * 采用单例模式获取缓存对象实例 
     *  
     * @return 
     */  
    public static MapCacheManager getInstance() {  
        if (null == mapCacheObject) {  
            synchronized (MapCacheManager.class) {  
                if (null == mapCacheObject) {  
                    mapCacheObject = new MapCacheManager();  
                }  
            }  
        }  
        return mapCacheObject;  
    }  
  
  
    /** 
     * 返回缓存对象 
     *  
     * @return 
     */  
    public CacheProvider getSmsOrderCache(){
        return getCustomCacheProvider(KEY_SMS_ORDER_MAP);  
    } 
    public CacheProvider getDailyClearMapCache(){  
        return getCustomCacheProvider(KEY_DAILY_CLEAR_MAP);  
    } 
    public CacheProvider getCustomCacheProvider(String key){  
    	CacheProvider provider =  null;
    	if(!cacheProviderMap.containsKey(key)){
    		provider = new RedisCacheProvider(key);
    		cacheProviderMap.put(key, provider);
    	}else{
    		provider = cacheProviderMap.get(key);
    	}
        return provider;  
    } 
	public abstract class CacheProvider{
		private String id;
		public CacheProvider(String id) {
			this.id =id;
		}
		public String getId(){
			return this.id;
		}
		public abstract void put(String key,String value);
    	public abstract String get(String key);
    	public abstract void remove(String key);
    	public abstract void clear();
    }
    
    public class DefaultCacheProvicer extends CacheProvider{

    	Map<String,Map<String,String>> mapCache = new ConcurrentHashMap<String, Map<String,String>>();
    	public DefaultCacheProvicer(String id) {
			super(id);
		}
		public Map<String, String> getMap(String key) {
			Map<String,String> resultMap = null;
			if(!mapCache.containsKey(key)){
				resultMap = new ConcurrentHashMap<String, String>();
				mapCache.put(key, resultMap);
			}else{
				resultMap = mapCache.get(key);
			}
			return resultMap;
		}
		@Override
		public void put(String key, String value) {
			getMap(getId()).put(key, value);
		}
		@Override
		public String get(String key) {
			return getMap(getId()).get(key);
		}
		@Override
		public void remove(String key) {
			getMap(getId()).remove(key);
		}
		@Override
		public void clear() {
			getMap(getId()).clear();
			
		}
    }
    public class RedisCacheProvider extends CacheProvider{
    	
		public RedisCacheProvider(String id) {
			super(id);
		}
		@Override
		public void put(String key, String value) {
			RedisDao redisDao = CommonServiceUtil.getCommonService().getRedisDao();
			redisDao.put(getId(), key, value);
		}
		@Override
		public String get(String key) {
			RedisDao redisDao = CommonServiceUtil.getCommonService().getRedisDao();
			return redisDao.get(getId(),key);
		}
		@Override
		public void remove(String key) {
			RedisDao redisDao = CommonServiceUtil.getCommonService().getRedisDao();
			redisDao.remove(getId(),key);
		}
		@Override
		public void clear() {
			RedisDao redisDao = CommonServiceUtil.getCommonService().getRedisDao();
			redisDao.clear(getId());
			
		}
    }
}
