package com.qy.sp.fee.modules.apisdk.service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TBlobContentDao;
import com.qy.sp.fee.dao.TSdkTaskDao;
import com.qy.sp.fee.dto.TBlobContent;
import com.qy.sp.fee.dto.TSdkTask;
import com.qy.sp.fee.dto.TSdkTaskQueryKey;

import net.sf.json.JSONObject;

@Service
public class ApiSdkTaskService {
	public static final int STATUS_EXECUTE = 1;
	public static final int STATUS_UNEXECUTE = 2;
	@Resource
	private TSdkTaskDao  tSdkTaskDao;
	@Resource
	private TBlobContentDao tBlobContentDao;
	public JSONObject queryTasks(JSONObject requestJSonObject){
		String phoneId = requestJSonObject.optString("imei");
		String appId = requestJSonObject.optString("appId");
		String channelId = requestJSonObject.optString("channelId");
		String provinceId = requestJSonObject.optString("provinceId");
		String stepId = requestJSonObject.optString("stepId");
		String taskId = requestJSonObject.optString("taskId");
		JSONObject resultObj = new JSONObject();
		List<JSONObject> data = null;
		if(StringUtil.isNotEmptyString(taskId)){
			data = new ArrayList<JSONObject>();
			TSdkTask tempTask = tSdkTaskDao.selectByPrimaryKey(taskId);
			if(tempTask!= null && tempTask.getStatus() == 1){
				JSONObject taskObject = new JSONObject();
				taskObject.put("taskId", tempTask.getTaskId());
				taskObject.put("taskName",tempTask.getTaskName());
				taskObject.put("taskVersion",tempTask.getVersion());
				taskObject.put("taskDescription", tempTask.getTaskDescription());
				data.add(taskObject);
			}
			
		}else{
			data =  getTasks(phoneId, appId, channelId, provinceId,stepId);
		}
		resultObj.put("data", data);
		return resultObj;
	}
	
	private List<JSONObject> getTasks(String phoneId,String appId,String channelId,String provinceId,String stepId){
		Map<String,TSdkTask> taskMap = new HashMap<String,TSdkTask>();
		if(StringUtil.isNotEmptyString(phoneId)){
			TSdkTaskQueryKey key = new TSdkTaskQueryKey();
			key.setPhoneId(phoneId);
			key.setStepId(stepId);
			List<TSdkTask> tasks = tSdkTaskDao.selectTasksByTaskQueryKey(key);
			for(TSdkTask t : tasks){
				if(!taskMap.containsKey(t.getTaskId())){
					taskMap.put(t.getTaskId(),t);
				}
			}
		}
		if(StringUtil.isNotEmptyString(channelId) || StringUtil.isNotEmptyString(appId) || StringUtil.isNotEmptyString(provinceId)){
			Map<String,TSdkTask> crossMap = new HashMap<String,TSdkTask>();
			TSdkTaskQueryKey key = new TSdkTaskQueryKey();
			if(StringUtil.isNotEmptyString(channelId)){
				key.setChannelId(channelId);
			}
			if(StringUtil.isNotEmptyString(appId)){
				key.setAppId(appId);
			}
			if(StringUtil.isNotEmptyString(provinceId)){
				key.setProvinceId(provinceId);
			}
			key.setStepId(stepId);
			List<TSdkTask> tasks = tSdkTaskDao.selectTasksByTaskQueryKey(key);
			for(TSdkTask t : tasks){
				TSdkTask tempTask = crossMap.get(t.getTaskId());
				if(tempTask == null){
					crossMap.put(t.getTaskId(),t);
				}else{
					if(t.getTaskExecute() == STATUS_UNEXECUTE){
						crossMap.put(t.getTaskId(),t);
					}
				}
			}
			for(TSdkTask t : crossMap.values()){
				if(!taskMap.containsKey(t.getTaskId())){
					taskMap.put(t.getTaskId(), t);
				}
			}
		}
		TSdkTaskQueryKey key = new TSdkTaskQueryKey();
		key.setStepId(stepId);
		List<TSdkTask> tasks = tSdkTaskDao.selectTasksByTaskQueryKey(key);
		for(TSdkTask t : tasks){
			if(!taskMap.containsKey(t.getTaskId())){
				taskMap.put(t.getTaskId(), t);
			}
		}
		List<JSONObject> resulTasks = new ArrayList<JSONObject>();
		for(String keyStr : taskMap.keySet()){
			TSdkTask tempTask = taskMap.get(keyStr);
			if(tempTask.getTaskExecute() == STATUS_EXECUTE){
				JSONObject taskObject = new JSONObject();
				taskObject.put("taskId", tempTask.getTaskId());
				taskObject.put("taskName",tempTask.getTaskName());
				taskObject.put("taskVersion",tempTask.getVersion());
				taskObject.put("taskDescription", tempTask.getTaskDescription());
				resulTasks.add(taskObject);
			}
		}
		return resulTasks;
	}

	public JSONObject removeTask(JSONObject requestJSonObject){
		String taskId = requestJSonObject.optString("taskeId");
		String imei = requestJSonObject.optString("imei");
		TSdkTaskQueryKey key = new TSdkTaskQueryKey();
		key.setTaskId(taskId);
		key.setPhoneId(imei);
		tSdkTaskDao.deleteTasksByTaskQueryKey(key);
		return null;
	}
	
	public void downloadTask(JSONObject requestObject, HttpServletResponse response)
			throws MalformedURLException {
		try {
			String taskId = requestObject.optString("taskId");
			TSdkTask task = tSdkTaskDao.selectByPrimaryKey(taskId);
			if(task == null){
				response.addHeader("status", "-1");
				return ;
			}
			String fileId = task.getPluginFile();
			TBlobContent content = tBlobContentDao.selectByPrimaryKey(fileId);
			if(content == null){
				response.addHeader("status", "-1");
				return ;
			}
			// 以流的形式下载文件。
			byte[] buffer = content.getFileContent();
			// 清空response
			response.reset();
			response.addHeader("Content-Disposition", "attachment;filename="+ taskId);
			response.addHeader("Content-Length", "" + buffer.length);
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
