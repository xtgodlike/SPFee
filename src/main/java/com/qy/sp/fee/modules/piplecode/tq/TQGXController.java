package com.qy.sp.fee.modules.piplecode.tq;

import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.modules.piplecode.dxtv.DXTVideoService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Controller
@RequestMapping(value = "/piple")
public class TQGXController 	{

	@Resource
	private TQGXService tqgxService;


	@RequestMapping(value = "/jsgx/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(HttpServletResponse response,String params, String order_string, String orderResult){
		String resultMsg = "error";
		try{
			if(StringUtil.isEmpty(params) && StringUtil.isEmpty(order_string) && StringUtil.isEmpty(orderResult)){
				return "required parameter all empty";
			}
			JSONObject resultJson = tqgxService.processPay(params,order_string,orderResult);
			PrintWriter out = response.getWriter();
			out.print(resultJson);
			resultMsg = resultJson.toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}

	@RequestMapping(value = "/tqgx/getCode" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getCode(String mobile){
		String resultMsg = "error";
		try{
			resultMsg = tqgxService.getVerifyCode(mobile);
			return resultMsg;
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}

	@RequestMapping(value = "/tqgx/submitCode" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String submitCode(String orderId,String mobile,String vCode){
		String resultMsg = "error";
		try{
			resultMsg = tqgxService.submitVerifyCode(orderId,mobile,vCode);
			return resultMsg;
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultMsg;
	}
}
