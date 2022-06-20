package uni.djem.management.RequestDtos;

import java.util.Date;

public class CreateTaskRequest {
	private String title;
	private String content;
	private String status;
	private int projectId;
	private int assignedUserId;
	private Date deadline;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public int getAssignedUserId() {
		return assignedUserId;
	}
	public void setAssignedUserId(int assignedUserId) {
		this.assignedUserId = assignedUserId;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
}