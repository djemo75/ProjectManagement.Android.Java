package uni.djem.management.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user")
public class UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="username", length = 100, nullable = false, unique = true)
	private String username;
	
	@JsonIgnore
	@Column(name="password", length = 32, nullable = false)
	private String password;
	
	@Column(name="name", length = 50, nullable = false)
	private String name;
	
	@JsonBackReference
	@OneToMany(mappedBy="user")
	private List<ProjectParticipantEntity> projects;

	public UserEntity() {}
	
	public UserEntity(String username, String password, String name) {
		this.username = username;
		this.password = password;
		this.name = name;
	}
	
	public UserEntity(String username, String name) {
		this.username = username;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProjectParticipantEntity> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectParticipantEntity> projects) {
		this.projects = projects;
	}

}
