package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.OrderDao;
import com.qy.sp.fee.dto.PipleOrderIdParam;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TOrder;

@Component
public class TOrderDao {
	@Resource
	private OrderDao orderDao;
    public int deleteByPrimaryKey(String orderId){
    	return orderDao.deleteByPrimaryKey(orderId);
    }

    public int insert(TOrder record){
    	return orderDao.insert(record);
    }

    public int insertSelective(TOrder record){
    	return orderDao.insertSelective(record);
    }

    public TOrder selectByPrimaryKey(String orderId){
    	return orderDao.selectByPrimaryKey(orderId);
    }
    
    public TOrder selectByPipleKey(PipleOrderIdParam key){
    	return orderDao.selectByPipleKey(key);
    }
    
    public TOrder selectByPipleOrderId(String pipleOrderId){
    	return orderDao.selectByPipleOrderId(pipleOrderId);
    }

    public int updateByPrimaryKeySelective(TOrder record){
    	return orderDao.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(TOrder record){
    	return orderDao.updateByPrimaryKey(record);
    }
    
    public List<String> getUnmatchProvinceMobiles(@Param("num") int num){
    	return orderDao.getUnmatchProvinceMobiles(num);
    }
    
    public int updateBatchProvinceId(List<TLocation> locations){
    	return orderDao.updateBatchProvinceId(locations);
    }
    
    public double getAmountForDay(TOrder order){
    	return orderDao.getAmountForDay(order);
    }
    
    public double getAmountForMonth(TOrder order){
    	return orderDao.getAmountForMonth(order);
    }
    
    public double getAmountForChannelProvinceDay(TOrder order){
    	return orderDao.getAmountForChannelProvinceDay(order);
    }
    
    public double getAmountForChannelProvinceMonth(TOrder order){
    	return orderDao.getAmountForChannelProvinceMonth(order);
    }
    
    public int getCountForMobileDay(TOrder order){
    	return orderDao.getCountForMobileDay(order);
    }
    
    public  int getCountForMobileMonth(TOrder order){
    	return orderDao.getCountForMobileMonth(order);
    }
    public double getAmountForMobileDay(TOrder order){
    	return orderDao.getAmountForMobileDay(order);
    }
    
    public double getAmountForMobileMonth(TOrder order){
    	return orderDao.getAmountForMobileMonth(order);
    }
    
    public double getAmountForMobileGlobalDay(TOrder order){
    	return orderDao.getAmountForMobileGlobalDay(order);
    }
    
    public double getAmountForMobileGlobalMonth(TOrder order){
    	return orderDao.getAmountForMobileGlobalMonth(order);
    }
    public double getAmountForPipleDay(TOrder order){
    	return orderDao.getAmountForPipleDay(order);
    }
    public double getAmountForPipleMonth(TOrder order){
    	return orderDao.getAmountForPipleMonth(order);
    }
}