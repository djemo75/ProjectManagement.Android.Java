package uni.fmi.management.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ProjectParticipantEntity {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("createdDate")
    @Expose
    private Date createdDate;

    @SerializedName("user")
    @Expose
    private UserDetails user;

    @SerializedName("project")
    @Expose
    private ProjectEntity project;

    public ProjectParticipantEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }
}
