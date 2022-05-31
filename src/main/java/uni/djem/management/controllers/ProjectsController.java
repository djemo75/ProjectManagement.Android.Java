package uni.djem.management.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uni.djem.management.RequestDtos.CreateProjectRequest;
import uni.djem.management.RequestDtos.EditProjectRequest;
import uni.djem.management.ResponseDtos.MessageResponse;
import uni.djem.management.entities.ProjectEntity;
import uni.djem.management.entities.ProjectParticipantEntity;
import uni.djem.management.entities.UserEntity;
import uni.djem.management.repositories.ProjectParticipantRepository;
import uni.djem.management.repositories.ProjectRepository;
import uni.djem.management.repositories.UserRepository;

@RequestMapping(path="/projects")
@RestController
public class ProjectsController {
	private ProjectRepository projectRepository;
	private ProjectParticipantRepository projectParticipantRepository;
	private UserRepository userRepository;
	
	public ProjectsController(ProjectRepository projectRepository, ProjectParticipantRepository projectParticipantRepository, UserRepository userRepository) {
		this.projectRepository=projectRepository;
		this.projectParticipantRepository=projectParticipantRepository;
		this.userRepository=userRepository;
	}
	
	@GetMapping("")
	public ResponseEntity<List<ProjectEntity>> getAllProjects() {
		List<ProjectEntity> projects = projectRepository.findAll();

		return new ResponseEntity<List<ProjectEntity>>(projects, HttpStatus.OK);
	}
	
	@GetMapping("/")
	public ResponseEntity<ProjectEntity> getProject(@RequestParam int id) {
		ProjectEntity project = projectRepository.findById(id);
		
		if(project == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}

		return new ResponseEntity<ProjectEntity>(project, HttpStatus.OK);
	}
	
	@GetMapping("/participated")
	public ResponseEntity<List<ProjectEntity>> getParticipatedProjectsByUserId(@RequestParam int userId) {
		List<ProjectParticipantEntity> participatedProjects = projectParticipantRepository.findAllByUserIdOrderByCreatedDateDesc(userId);
		
		List<ProjectEntity> projects = new ArrayList<ProjectEntity>();
		participatedProjects.forEach((currentProject) -> {
			ProjectEntity project = projectRepository.findById(currentProject.getProject().getId());
			projects.add(project);
		});

		return new ResponseEntity<List<ProjectEntity>>(projects, HttpStatus.OK);
	}
	
	@GetMapping("/all-users")
	public ResponseEntity<List<UserEntity>> getAllUsers() {
		List<UserEntity> users = userRepository.findAll();
		return new ResponseEntity<List<UserEntity>>(users, HttpStatus.OK);
	}
	
	@GetMapping("/participated-users")
	public ResponseEntity<List<UserEntity>> getProjectUsersByProjectId(@RequestParam int projectId) {
		ProjectEntity project = projectRepository.findById(projectId);
		if(project == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}
		
		List<ProjectParticipantEntity> participatedUsers = projectParticipantRepository.findAllByProjectIdOrderByCreatedDateDesc(projectId);
		
		List<UserEntity> users = new ArrayList<UserEntity>();
		participatedUsers.forEach((currentParticipant) -> {
			UserEntity user = userRepository.findById(currentParticipant.getUser().getId());
			users.add(user);
		});

		return new ResponseEntity<List<UserEntity>>(users, HttpStatus.OK);
	}
	
	@PostMapping("/")
	public ResponseEntity<ProjectEntity> createProject(@RequestBody CreateProjectRequest projectRequest, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		if(projectRequest.getTitle()=="" || projectRequest.getContent()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide title and content!");
		}
		
		ProjectEntity project = new ProjectEntity();
		project.setTitle(projectRequest.getTitle());
		project.setContent(projectRequest.getContent());
		project.setCreatedDate(new Date());
		project.setAuthor(user);

		projectRepository.save(project);
		
		ProjectParticipantEntity projectParticipant = new ProjectParticipantEntity();
		projectParticipant.setUser(user);
		projectParticipant.setProject(project);
		projectParticipant.setCreatedDate(new Date());
		
		projectParticipantRepository.save(projectParticipant); // Add user to project
		
		return new ResponseEntity<ProjectEntity>(project, HttpStatus.OK);
	}
	
	@PostMapping("/add-user")
	public ResponseEntity<ProjectParticipantEntity> addUserToProject(@RequestParam int userId, @RequestParam int projectId, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		ProjectEntity project = projectRepository.findById(projectId);
		
		if(project == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}
		
		if(project.getAuthor().getId() != user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not author of the project!");
		}
		
		UserEntity userForAdding = userRepository.findById(userId);
		if(userForAdding == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found!");
		}
		
		ProjectParticipantEntity existingParticipant = projectParticipantRepository.findByUserIdAndProjectId(userId, projectId);
		if(existingParticipant != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is already added to the project!");

		}
		
		ProjectParticipantEntity participant = new ProjectParticipantEntity();
		participant.setProject(project);
		participant.setUser(userForAdding);
		participant.setCreatedDate(new Date());
		
		
		projectParticipantRepository.saveAndFlush(participant);
		return new ResponseEntity<ProjectParticipantEntity>(participant, HttpStatus.OK);
	}
	
	@PutMapping("")
	public ResponseEntity<MessageResponse> editProject(@RequestParam int id, @RequestBody EditProjectRequest projectRequest, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		ProjectEntity project = projectRepository.findById(id);
		
		if(project==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with this id not found!");
		}
		
		if(project.getAuthor().getId()!=user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't edit other people's project!");
		}
		
		if(projectRequest.getTitle()=="" || projectRequest.getContent()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide title and content!");
		}
		
		project.setTitle(projectRequest.getTitle());
		project.setContent(projectRequest.getContent());
		
		projectRepository.save(project);
		
		MessageResponse response = new MessageResponse("You have successfully edited the project");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
	
	
	@DeleteMapping("/remove-user")
	public ResponseEntity<MessageResponse> removeUserFromProject(@RequestParam int userId, @RequestParam int projectId, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		ProjectEntity project = projectRepository.findById(projectId);
		
		if(project == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}
		
		if(project.getAuthor().getId() != user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not author of the project!");
		}
		
		UserEntity userForRemoving = userRepository.findById(userId);
		if(userForRemoving == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found!");
		}
		
		if(project.getAuthor().getId() == userForRemoving.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't remove self from the project!");
		}
		
		ProjectParticipantEntity existingParticipant = projectParticipantRepository.findByUserIdAndProjectId(userId, projectId);
		if(existingParticipant == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not added to the project!");

		}
		
		projectParticipantRepository.delete(existingParticipant);
		MessageResponse message = new MessageResponse("Removed user successfully");
		return new ResponseEntity<MessageResponse>(message, HttpStatus.OK);
	}

	@Transactional
	@DeleteMapping("")
	public ResponseEntity<MessageResponse> deleteProject(@RequestParam int id, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		ProjectEntity project = projectRepository.findById(id);
		
		if(project==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with this id not found!");
		}
		
		if(project.getAuthor().getId()!=user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete other people's project!");
		}
		
		projectParticipantRepository.deleteByProjectId(project.getId());
		projectRepository.delete(project);
		
		MessageResponse response = new MessageResponse("You have successfully deleted the project");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
}