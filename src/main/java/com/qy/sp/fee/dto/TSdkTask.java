package com.qy.sp.fee.dto;

import java.util.Date;

public class TSdkTask {

	private String taskId;
	private String taskName;
	private String taskDescription;
	private String version;
	private String pluginFile;
	private int taskExecute;
	private int status;
	private Date createTime;
	private Date modifyTime;
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskDescription() {
		return taskDescription;
	}
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getPluginFile() {
		return pluginFile;
	}
	public void setPluginFile(String pluginFile) {
		this.pluginFile = pluginFile;
	}
	public int getTaskExecute() {
		return taskExecute;
	}
	public void setTaskExecute(int taskExecute) {
		this.taskExecute = taskExecute;
	}
	
	
}
