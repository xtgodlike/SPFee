package com.qy.sp.fee.modules.piplecode.base;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.dao.TOrderDao;
import com.qy.sp.fee.dao.TProductDao;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TProduct;

import net.sf.json.JSONObject;

@Service
public class ChannelOtherService {

	@Resource
	protected TOrderDao tOrderDao;
	@Resource
	protected TProductDao tProductDao;
	public JSONObject synOrders(){
		JSONObject resultObject = new JSONObject();
		String orderIds[] = new String[]{"14606443965753751569034","14606386440436206399258","14606318631654255663586","14606283481076619480109","14605124399933902039751"};
		for(int i=0 ;i < orderIds.length ;i++){
			String orderId = orderIds[i];
			TOrder order = tOrderDao.selectByPrimaryKey(orderId);
			if(order != null){
				String url = "http://114.215.140.102:8080/interfaces/sms/tengwang?mobile=%s&orderId=%s&amount=%s&orderStatus=%s&imsi=%s&productCode=%s";
				TProduct product = tProductDao.selectByPrimaryKey(order.getProductId());
				url = String.format(url, order.getMobile(),order.getOrderId(),order.getAmount().doubleValue(),order.getOrderStatus(),order.getImsi(),product.getProductCode());
				try {
					String result = HttpClientUtils.doGet(url, HttpClientUtils.UTF8);
					resultObject.put(orderId, result);
				} catch (Exception e) {
					e.printStackTrace();
					resultObject.put(orderId, "异常");
				}
			}else{
				resultObject.put(orderId, "不存在");
			}
		}
		return resultObject;
	}
}
