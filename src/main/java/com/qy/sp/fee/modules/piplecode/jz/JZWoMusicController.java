package com.qy.sp.fee.modules.piplecode.jz;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class JZWoMusicController {

	@Resource
	private JZWoMusicService jzWoMusicService;
	@RequestMapping(value = "/jzwomusic/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String mobile,String momsg,String spnumber,String linkid,String flag){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("mobile", mobile);
			bodyObject.put("momsg", momsg);
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("linkid", linkid);
			bodyObject.put("flag",flag);
			result = jzWoMusicService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
