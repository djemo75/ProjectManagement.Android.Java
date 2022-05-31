package uni.djem.management.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "comments")
public class CommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="type", length = 50, nullable = false)
	private String type;
	
	@Column(name="content", length = 1000, nullable = true)
	private String content;
	
	@Column(name="resourcePath", length = 200, nullable = true)
	private String resourcePath;
	
	@CreatedDate
	@Column(name="created_date", nullable = false)
	private Date createdDate;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "task_Id")
	private TaskEntity task;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id")
	private UserEntity author;

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

	public UserEntity getAuthor() {
		return author;
	}

	public void setAuthor(UserEntity author) {
		this.author = author;
	}
}