package com.qy.sp.fee.modules.piplecode.qianya;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.modules.piplecode.panmei.PMWoReadService;

@Controller
@RequestMapping(value = "/piple")
public class QianyaWoShopController {
	private  Logger log = Logger.getLogger(QianyaWoShopController.class);		
//	@RequestMapping(value = "/woshop/sync" ,produces = {"application/json;charset=UTF-8"})
//	@ResponseBody
	@RequestMapping(value = "/woshop/sync")
	public String channelRequest(HttpServletRequest request,HttpServletResponse response){
		String rst = "error";
		try{
			BufferedReader br = request.getReader();
			String str = "";
			String bodyStr = "";
			while((str = br.readLine()) != null){
				bodyStr += str;
			}
			log.info("QianyaWoShopController reqBodyData = "+bodyStr);
			rst = HttpClientUtils.doPostp("http://api.test.vr800.com/outer/pay/uniPay", bodyStr, HttpClientUtils.UTF8);
			log.info("QianyaWoShopController rst = "+rst);
			if(!StringUtil.isEmpty(rst)){
				   response.setContentType("text/xml");  
				    response.setContentLength(rst.length());  
			        response.getOutputStream().write(rst.getBytes());  
			        response.getOutputStream().flush();  
			        response.getOutputStream().close();
			}else{
				String defMsg = "<?xml version='1.0' encoding='UTF-8'?><callbackRsp>1</callbackRsp>";
				response.setContentType("text/xml");  
			    response.setContentLength(rst.length());  
		        response.getOutputStream().write(defMsg.getBytes());  
		        response.getOutputStream().flush();  
		        response.getOutputStream().close();
			}
	        return rst;
		}
		catch(Exception e){
			e.printStackTrace();
			log.info("QianyaWoShopController errorMsg = "+e.getMessage());
			return rst;
		}
	}
}
