package uni.fmi.management.http.requests;

public class EditTaskRequest {
    private String title;
    private String content;
    private String status;
    private int assignedUserId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(int assignedUserId) { this.assignedUserId = assignedUserId; }
}
