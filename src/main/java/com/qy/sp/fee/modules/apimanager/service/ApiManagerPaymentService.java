package com.qy.sp.fee.modules.apimanager.service;

import java.io.File;
import java.io.FileInputStream;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.FileUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.dao.TBlobContentDao;
import com.qy.sp.fee.dao.TPipleDao;
import com.qy.sp.fee.dto.TBlobContent;
import com.qy.sp.fee.dto.TPiple;

import net.sf.json.JSONObject;

@Service
public class ApiManagerPaymentService {

	@Resource
	private TBlobContentDao tBlobContentDao;
	@Resource
	private TPipleDao tPipleDao;
	
	public JSONObject savePayment(JSONObject jsonObject){
		JSONObject result = new JSONObject();
		try {
			String filePath = jsonObject.optString("filePath");
			String fileName = jsonObject.optString("fileName");
			String pipleId = jsonObject.optString("pipleId");
			String version = jsonObject.optString("version");
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			if(piple != null){
				byte[] fileByte = null;
				fileByte = FileUtils.InputStreamToByte(new FileInputStream(new File(filePath)));
				TBlobContent contractBC = new TBlobContent();
				 String fileId = KeyHelper.createKey();
				 contractBC.setFileId(fileId);
				 contractBC.setFilename(fileName);
				 contractBC.setFileContent(fileByte);
				 tBlobContentDao.insert(contractBC);
				 piple.setPluginFile(fileId);
				 piple.setPluginVersion(version);
				 tPipleDao.updateByPrimaryKeySelective(piple);
				 result.put("pipleId", pipleId);
				 result.put("fileId", fileId);
				 result.put("status", "1");
			}
		} catch (Exception e) {
			e.printStackTrace();
			 result.put("status", "0");
		} 
		return result;
	}
}
