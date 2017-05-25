package com.qy.sp.fee.dao;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TSdkLog;

import net.sf.json.JSONObject;
@Component
public class TSdkLogDao extends BaseDao{
	public static final String MONGODB_SDK_LOG = "mongodb_sdk_log";
	@Resource
	protected MongoTemplate mongoTemplate;
	
    public void deleteByPrimaryKey(String logId){
    	BasicDBObject bson = new BasicDBObject();
    	bson.put("logId", logId);
    	mongoTemplate.getCollection(MONGODB_SDK_LOG).remove(bson);
    }

    public void insert(TSdkLog record){
    	mongoTemplate.insert(record,MONGODB_SDK_LOG);
    }

    public TSdkLog selectByPrimaryKey(String logId){
    	BasicDBObject bson = new BasicDBObject();
    	bson.put("logId", logId);
    	DBObject result =  mongoTemplate.getCollection(MONGODB_SDK_LOG).findOne(bson);
    	return (TSdkLog)JSONObject.toBean(JSONObject.fromObject(result), TSdkLog.class);
    }
}