package com.qy.sp.fee.modules.piplecode.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MondbTest {
	
	 private MongoClient mongoClient;
	 private DB mongoDatabase;
     /**
      * 建立与服务器连接
      * @throws Exception
      */
	  @Before
     public void prepareMongoClient() throws Exception {
	     try{   
	         // 连接到 mongodb 服务
	    	 System.out.println("** 尝试与服务器建立连接 **");
	    	 mongoClient = new MongoClient( "192.168.1.7" , 27017 );
	           // 连接到数据库
	    	 mongoDatabase = mongoClient.getDB("admin");  
	    	 mongoDatabase.authenticate("root", "nj_qianya".toCharArray());
	    	 mongoDatabase = mongoClient.getDB("spfee");
	    	 System.out.println("** 连接服务器成功 **");
	        }catch(Exception e){
	          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	       }
     }
     /**
      * 断开连接
      */
	  @After
     public void closeMongo() {
         if (mongoClient != null) {
              System.out.println("** 断开与服务器的连接 **");
              mongoClient.close();
         }
     }
	
	  @Test
     public void testInsert() {
         try {
        	 DBCollection collections = mongoDatabase.getCollection("spfee");
        	 DBObject document = new BasicDBObject("helloKey","helloValue");
        	 collections.insert(document); 
        	 System.out.println("插入成功");
         } catch (Exception e) {
                 e.printStackTrace();
         }
     }
	  @Test
	  public void testQueryString(){
		  try {
			  DBCollection collections = mongoDatabase.getCollection("spfee");
			  DBObject document = new BasicDBObject("helloKey","helloValue");
			  DBObject result = collections.findOne(document);  
	                System.out.println(result);  
	         } catch (Exception e) {
	                 e.printStackTrace();
	         }
	  }
	 @Test
     public void testDelete() {
         try {
        	 DBCollection collections = mongoDatabase.getCollection("spfee");
        	 DBObject document = new BasicDBObject("helloKey","helloValue");
        	 WriteResult result = collections.remove(document); 
         } catch (Exception e) {
                 e.printStackTrace();
         }
     }

}
