package com.qy.sp.fee.modules.piplecode.base;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;


public class RedisTest {
	  private Jedis jredis;
      /**
       * 建立与服务器连接
       * @throws Exception
       */
	  @Before
      public void prepareJRedisClient() throws Exception {
              System.out.println("** 尝试与服务器建立连接 **");
              jredis = new Jedis("192.168.1.7",6379,2000);
              jredis.auth("nj_qianya");
              System.out.println("** 连接服务器成功 **");

      }
      /**
       * 断开连接
       */
	  @After
      public void closeJredis() {
              if (jredis != null) {
                      System.out.println("** 断开与服务器的连接 **");
                      jredis.quit();
              }
      }
      /**
       * 测试set/get使用
       */
	  @Test
      public void testString() {
              try {
                      jredis.flushDB();
                      System.out.printf("** 放入值<%s=%s> **\n","foo","bar");
                      jredis.set("foo", "bar");
                      String value = jredis.get("foo");
                      System.out.printf("** 获取到的值:%s **\n",value);
              } catch (Exception e) {
                      e.printStackTrace();
              }
      }

      /**
       * 测试lpush/rpush/lrange使用
       */
	  @Test
      public void testSet() {
              try {
                      jredis.flushDB();
                      System.out.printf("** 放入集合[%s,%s,%s,%s,%s] **\n","one","two","three","four","five");
                      jredis.lpush("list", "three");
                      jredis.lpush("list", "two");
                      jredis.lpush("list", "one");

                      jredis.rpush("list", "four");
                      jredis.rpush("list", "five");
                      List<String> values = jredis.lrange("list", 0, -1);

                      System.out.println("** 获取到的集合值"+values+" **");
              } catch (Exception e) {
                      e.printStackTrace();
              }
      }
}
