package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.PipleOrderIdParam;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TOrder;

@Component @MyBatisRepository
public interface OrderDao {
    int deleteByPrimaryKey(String orderId);

    int insert(TOrder record);

    int insertSelective(TOrder record);

    TOrder selectByPrimaryKey(String orderId);
    
    TOrder selectByPipleKey(PipleOrderIdParam key);
    
    TOrder selectByPipleOrderId(String pipleOrderId);

    int updateByPrimaryKeySelective(TOrder record);

    int updateByPrimaryKey(TOrder record);
    
    List<String> getUnmatchProvinceMobiles(@Param("num") int num);
    
    int updateBatchProvinceId(List<TLocation> locations);
    
    double getAmountForDay(TOrder order);
    
    double getAmountForMonth(TOrder order);
    
    double getAmountForChannelProvinceDay(TOrder order);
    
    double getAmountForChannelProvinceMonth(TOrder order);
    
    int getCountForMobileDay(TOrder order);
    
    int getCountForMobileMonth(TOrder order);
    
    double getAmountForMobileDay(TOrder order);
    
    double getAmountForMobileMonth(TOrder order);
    
    double getAmountForMobileGlobalDay(TOrder order);
    
    double getAmountForMobileGlobalMonth(TOrder order);
    
    double getAmountForPipleDay(TOrder order);
    
    double getAmountForPipleMonth(TOrder order);
}