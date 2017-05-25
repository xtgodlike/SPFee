package com.qy.sp.fee.modules.apimanager.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.FileUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.dao.TBlobContentDao;
import com.qy.sp.fee.dto.TBlobContent;

@Service
public class ApiManagerTaskService {

	@Resource
	private TBlobContentDao tBlobContentDao;
	
	
	public String saveTask(){
		try {
			byte[] fileByte = null;
//			fileByte = FileUtils.InputStreamToByte(new FileInputStream(new File("D:\\project\\android\\payment\\source\\SDK\\payment\\Qianya_SDK\\build\\target\\task\\advertisement.zip")));
			fileByte = FileUtils.InputStreamToByte(new FileInputStream(new File("D:\\project\\android\\payment\\source\\SDK\\payment\\Qianya_SDK\\build\\target\\task\\example.zip")));
			TBlobContent contractBC = new TBlobContent();
			 String fileId = KeyHelper.createKey();
			 contractBC.setFileId(fileId);
//			 contractBC.setFilename("taskExample");
			 contractBC.setFilename("example");
			 contractBC.setFileContent(fileByte);
			 tBlobContentDao.insert(contractBC);
			 return fileId;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
