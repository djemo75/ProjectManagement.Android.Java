package uni.fmi.management.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class FileEntity {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("fileName")
    @Expose
    private String fileName;

    @SerializedName("fileCode")
    @Expose
    private String fileCode;

    @SerializedName("path")
    @Expose
    private String path;

    @SerializedName("contentType")
    @Expose
    private String contentType;

    @SerializedName("createdDate")
    @Expose
    private Date createdDate;

    @SerializedName("user")
    @Expose
    private UserDetails user;

    @SerializedName("project")
    @Expose
    private ProjectEntity project;

    public FileEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileCode() { return fileCode; }

    public void setFileCode(String fileCode) { this.fileCode = fileCode; }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }

    public String getContentType() { return contentType; }

    public void setContentType(String contentType) { this.contentType = contentType; }

    public Date getCreatedDate() { return createdDate; }

    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public UserDetails getUser() { return user; }

    public void setUser(UserDetails user) { this.user = user; }

    public ProjectEntity getProject() { return project; }

    public void setProject(ProjectEntity project) { this.project = project; }
}
