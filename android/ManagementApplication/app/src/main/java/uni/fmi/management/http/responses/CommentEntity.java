package uni.fmi.management.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CommentEntity {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("content")
    @Expose
    private String content;

    @SerializedName("resourcePath")
    @Expose
    private String resourcePath;

    @SerializedName("createdDate")
    @Expose
    private Date createdDate;

    @SerializedName("task")
    @Expose
    private TaskEntity task;

    @SerializedName("author")
    @Expose
    private UserDetails author;

    public CommentEntity() {
    }

    public CommentEntity(int id, String type, String content, String resourcePath, TaskEntity task, UserDetails author) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.resourcePath = resourcePath;
        this.task = task;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }

    public UserDetails getAuthor() {
        return author;
    }

    public void setAuthor(UserDetails author) {
        this.author = author;
    }
}
