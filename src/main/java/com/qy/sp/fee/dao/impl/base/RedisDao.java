package com.qy.sp.fee.dao.impl.base;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class RedisDao extends AbstractDao{

	private static Logger logger = LoggerFactory.getLogger(RedisDao.class);
	@Resource
	protected RedisTemplate<String, String> redisTemplate;
	
	public void put(final String mapKey,final String key,final String value){
		try{
			HashOperations<String,String,String> hashOPeration =  redisTemplate.opsForHash();
			hashOPeration.put(mapKey, key, value);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
	}
	public String get(final String mapKey,final String key){
		try{
			HashOperations<String,String,String> hashOPeration =  redisTemplate.opsForHash();
			return hashOPeration.get(mapKey, key);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
		return null;
	}
	public void remove(String mapKey,String key){
		try{
			HashOperations<String,String,String> hashOPeration =  redisTemplate.opsForHash();
			hashOPeration.delete(mapKey, key);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
	}
	public void clear(String mapKey){
		try{
			redisTemplate.delete(mapKey);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
	}
	public boolean contains(String key){
		try{
			return redisTemplate.hasKey(key);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
		return false;
	}
	public void put(String key,String value){
		try{
			ValueOperations<String, String> operation = redisTemplate.opsForValue();
			operation.set(key, value);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
	}
	public String get(String key){
		try{
			ValueOperations<String, String> operation = redisTemplate.opsForValue();
			return operation.get(key);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
		return null;
	}
	public void expire(String key, final long timeout){
		try{
			redisTemplate.expire(key, timeout,TimeUnit.MILLISECONDS);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("redisError:"+e.toString());
		}
	}
}
