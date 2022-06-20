package uni.fmi.management.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class TaskEntity {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("content")
    @Expose
    private String content;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("createdDate")
    @Expose
    private Date createdDate;

    @SerializedName("project")
    @Expose
    private ProjectEntity project;

    @SerializedName("assignedUser")
    @Expose
    private UserDetails assignedUser;

    @SerializedName("deadline")
    @Expose
    private Date deadline;

    public TaskEntity() {
    }

    public TaskEntity(int id, String title, String content, String status, ProjectEntity project, UserDetails assignedUser) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.project = project;
        this.assignedUser = assignedUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public UserDetails getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(UserDetails assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Date getDeadline() { return deadline; }

    public void setDeadline(Date deadline) { this.deadline = deadline; }
}
