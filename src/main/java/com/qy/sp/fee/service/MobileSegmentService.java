package com.qy.sp.fee.service;

import java.io.Serializable;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.THostDao;
import com.qy.sp.fee.dao.TLocationDao;
import com.qy.sp.fee.dao.TProvinceDao;
import com.qy.sp.fee.dto.THost;
import com.qy.sp.fee.dto.TLocation;
import com.qy.sp.fee.dto.TProvince;

@Service
public class MobileSegmentService {
	@Resource
	private TProvinceDao tProvinceDao;
	@Resource
	private THostDao tHostDao;
	@Resource
	protected TLocationDao tLocationDao;
	
	public int getProvinceIdByMobile(String mobile){
		int provinceId = 0;
		try{
			String segment = mobile.substring(0, 7); // 号段
			TLocation location =  tLocationDao.selectBySegment(segment);
			if(location!=null){
				provinceId = location.getProvinceId();
			}else{
				MobileInfoResult result =getProvinceRemote(mobile);
				if(result == null){
					result = getProvinceRemote2(mobile);
				}
				if(result != null){
					TLocation loc = new TLocation();
					loc.setSegment(segment);
					THost host = tHostDao.selectByName(result.getIsp());
					loc.setHostId(host.getHostId());
					TProvince province = tProvinceDao.selectByProvinceName(result.getProvince());
					loc.setProvinceId(province.getProvinceId());
					tLocationDao.insert(loc);
					provinceId = province.getProvinceId();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return provinceId;
	}


	public TLocation getLocationByMobile(String mobile){		
	try{
			String segment = mobile;
			if(mobile.length()>7)
				segment = mobile.substring(0, 7); // 号段
			TLocation location =  tLocationDao.selectBySegment(segment);
			if(location!=null){
				return location;
			}else{
				MobileInfoResult result =getProvinceRemote(mobile);
				if(result == null){
					result = getProvinceRemote2(mobile);
				}
				if(result != null && StringUtil.isNotEmptyString(result.getIsp())  && StringUtil.isNotEmptyString(result.getProvince())){
					TLocation loc = new TLocation();
					loc.setSegment(segment);
					THost host = tHostDao.selectByName(result.getIsp());
					loc.setHostId(host.getHostId());
					TProvince province = tProvinceDao.selectByProvinceName(result.getProvince());
					loc.setProvinceId(province.getProvinceId());
					tLocationDao.insert(loc);
					return loc;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public int getHostByImsi(String imsi){
		if(StringUtil.isEmpty(imsi)){
			return 0;
		}
		if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
	            return 2;// 中国移动
        } else if (imsi.startsWith("46001")) {
            return 1;// 中国联通
        } else if (imsi.startsWith("46003")) {
            return 3;// 中国电信
        }else{
        	return 0;
        }
	}
	public int getProvinceByIpProvince(String ipProvinceName){
		try{
			TProvince province = null;
			if(NumberUtil.isNumber(ipProvinceName)){
				province = tProvinceDao.selectByPrimaryKey(NumberUtil.getInteger(ipProvinceName));
			}else{
				province = tProvinceDao.selectByProvinceName(ipProvinceName);
			}
			if(province != null){
				return province.getProvinceId();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	private  MobileInfoResult getProvinceRemote2(String mobile){
		MobileInfoResult result = new MobileInfoResult();
		try {
			String provinceStr = HttpClientUtils.doGet("http://shouji.supfree.net/fish.asp?cat="+mobile, HttpClientUtils.GBK);
			if(provinceStr.contains("中国电信")){
				result.setIsp("电信");
			}else if(provinceStr.contains("中国联通")){
				result.setIsp("联通");
			}else if(provinceStr.contains("中国移动")){
				result.setIsp("移动");
			}
			int start = provinceStr.lastIndexOf("归属地：");
			if(start != -1){
				provinceStr = provinceStr.substring(start+5,start+400 );
				provinceStr = provinceStr.substring(0,provinceStr.indexOf("</p>"));
				provinceStr = provinceStr.replaceAll("[^(\\u4e00-\\u9fa5)]", "");
				int index = provinceStr.indexOf("省");
				if(index != -1){
					provinceStr = provinceStr.substring(0,index+1);
				}
				result.setProvince(provinceStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	private  MobileInfoResult getProvinceRemote(String mobile){
		MobileInfoResult result = new MobileInfoResult();
		try {
			String provinceStr = HttpClientUtils.doGet("http://www.ip138.com:8080/search.asp?action=mobile&mobile="+mobile, HttpClientUtils.GBK);
			if(provinceStr.contains("电信")){
				result.setIsp("电信");
			}else if(provinceStr.contains("联通")){
				result.setIsp("联通");
			}else if(provinceStr.contains("移动")){
				result.setIsp("移动");
			}
			int start = provinceStr.lastIndexOf("卡号归属地");
			if(start != -1){
				provinceStr = provinceStr.substring(start+5,start+400 );
				provinceStr = provinceStr.substring(0,provinceStr.indexOf("&nbsp;"));
				provinceStr = provinceStr.replaceAll("[^(\\u4e00-\\u9fa5)]", "");
				result.setProvince(provinceStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	public class MobileInfoResult implements Serializable{

		private static final long serialVersionUID = -8268181334117010503L;
		
		private String mobile;
		private String isp;
		private String province;
		private String cityname;
		public String getMobile() {
			return mobile;
		}
		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		public String getIsp() {
			return isp;
		}
		public void setIsp(String isp) {
			this.isp = isp;
		}
		public String getProvince() {
			return province;
		}
		public void setProvince(String province) {
			this.province = province;
		}
		public String getCityname() {
			return cityname;
		}
		public void setCityname(String cityname) {
			this.cityname = cityname;
		}
	}

}
