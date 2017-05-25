package com.qy.sp.fee.modules.piplecode.common;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.dao.TPipleDao;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.modules.piplecode.base.ChannelManager;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/piple")
public class CommonChannelController {
	@Resource
	private TPipleDao tPipleDao;
	private static Logger logger = LoggerFactory.getLogger(CommonChannelController.class);
	@RequestMapping(value = "/common/{pipleId}/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(@PathVariable String pipleId,HttpServletRequest request){
		String result = "error";
		JSONObject requestObject = new JSONObject();
		try{
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			if(piple != null){
				ChannelService service = ChannelManager.getInstance().getChannelService(pipleId);
				if(service != null){
					if(GlobalConst.DataType.JSON.equals(service.getPaySuccessHttpDataType())){
						
						Map requestParams = request.getParameterMap();
						for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
							String name = (String) iter.next();
							String[] values = (String[]) requestParams.get(name);
							String valueStr = "";
							for (int i = 0; i < values.length; i++) {
								valueStr = (i == values.length - 1) ? valueStr + values[i]
										: valueStr + values[i] + ",";
							}
							requestObject.put(name, valueStr);
						}
						result = service.processPaySuccess(requestObject);
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		logger.info(requestObject.toString()+","+result);
		return result;
	}
}
