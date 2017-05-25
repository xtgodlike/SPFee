package com.qy.sp.fee.modules.apisdk.service;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.dao.impl.base.RedisDao;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.service.BaseService;

@Service
public class ApiSdkTestService extends BaseService{

	@Resource
	private RedisDao redisDao;
	
	public String query(String operation,String key,String value){
		
		try {
			if("set".equals(operation)){
				redisDao.put(key, value);
				return "setOk";
			}else if("get".equals(operation)){
				redisDao.get(key);
				return "getOk";
			}else if("none".equals(operation)){
				return "noneOk";
			}else if("order".equals(operation)){
				updateOrder();
				return "updateOrderOK";
			}
			return "NOT"+operation+"Operation";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return operation+"Exception";
	}
	
	public void updateOrder(){
		String orderId = "14815463734207720020723";
		TOrder order = tOrderDao.selectByPrimaryKey(orderId);
		if(order == null){
			order = new TOrder();
			order.setOrderId(orderId);
			order.setChannelId(1003+"");
			order.setProvinceId(10);
			SaveOrderInsert(order);
		}else{
			order.setAmount(new BigDecimal("10"));
			order.setCreateTime(DateTimeUtils.getCurrentDate());
			SaveOrderUpdate(order);
		}
	}
}
