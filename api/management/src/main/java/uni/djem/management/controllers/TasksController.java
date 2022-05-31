package uni.djem.management.controllers;

import java.util.Date;
import java.util.List;

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

import uni.djem.management.RequestDtos.CreateTaskRequest;
import uni.djem.management.RequestDtos.EditTaskRequest;
import uni.djem.management.ResponseDtos.MessageResponse;
import uni.djem.management.entities.ProjectEntity;
import uni.djem.management.entities.TaskEntity;
import uni.djem.management.entities.UserEntity;
import uni.djem.management.repositories.ProjectRepository;
import uni.djem.management.repositories.TaskRepository;
import uni.djem.management.repositories.UserRepository;

@RequestMapping(path="/tasks")
@RestController
public class TasksController {
	private ProjectRepository projectRepository;
	private TaskRepository taskRepository;
	private UserRepository userRepository;
	
	public TasksController(ProjectRepository projectRepository, TaskRepository taskRepository, UserRepository userRepository) {
		this.projectRepository=projectRepository;
		this.taskRepository=taskRepository;
		this.userRepository=userRepository;
	}
	
	@GetMapping("")
	public ResponseEntity<List<TaskEntity>> getAllTasks() {
		List<TaskEntity> tasks = taskRepository.findAll();

		return new ResponseEntity<List<TaskEntity>>(tasks, HttpStatus.OK);
	}
	
	@GetMapping("/")
	public ResponseEntity<TaskEntity> getTask(@RequestParam int id) {
		TaskEntity task = taskRepository.findById(id);
		
		if(task == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task is not found!");
		}

		return new ResponseEntity<TaskEntity>(task, HttpStatus.OK);
	}
	
	@GetMapping("/project")
	public ResponseEntity<List<TaskEntity>> getTasksByProjectId(@RequestParam int projectId) {
		ProjectEntity project = projectRepository.findById(projectId);
		
		if(project == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}
		
		List<TaskEntity> tasks = taskRepository.findAllByProjectId(projectId);

		return new ResponseEntity<List<TaskEntity>>(tasks, HttpStatus.OK);
	}
	
	@GetMapping("/assigned")
	public ResponseEntity<List<TaskEntity>> getAssignedTasks(HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		List<TaskEntity> tasks = taskRepository.findAllByAssignedUserId(user.getId());

		return new ResponseEntity<List<TaskEntity>>(tasks, HttpStatus.OK);
	}
	
	@PostMapping("/")
	public ResponseEntity<TaskEntity> createTask(@RequestBody CreateTaskRequest taskRequest, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		if(taskRequest.getTitle()=="" || taskRequest.getContent()==""
				|| taskRequest.getStatus()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide title, content and status!");
		}
		
		ProjectEntity project = projectRepository.findById(taskRequest.getProjectId());
		
		if(project==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}
		
		UserEntity assigedUser = userRepository.findById(taskRequest.getAssignedUserId());
		
		if(assigedUser==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found!");
		}
		
		TaskEntity task = new TaskEntity();
		task.setTitle(taskRequest.getTitle());
		task.setContent(taskRequest.getContent());
		task.setStatus(taskRequest.getStatus());
		task.setProject(project);
		task.setAssignedUser(assigedUser);
		task.setCreatedDate(new Date());
		
		taskRepository.saveAndFlush(task);
		return new ResponseEntity<TaskEntity>(task, HttpStatus.OK);
	}
	
	@PutMapping("")
	public ResponseEntity<MessageResponse> editTask(@RequestParam int id, @RequestBody EditTaskRequest taskRequest, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		TaskEntity task = taskRepository.findById(id);
		
		if(task==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with this id not found!");
		}
		
		UserEntity assigedUser = userRepository.findById(taskRequest.getAssignedUserId());
		
		if(assigedUser==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found!");
		}
		
		task.setTitle(taskRequest.getTitle());
		task.setContent(taskRequest.getContent());
		task.setStatus(taskRequest.getStatus());
		task.setAssignedUser(assigedUser);
		
		taskRepository.save(task);
		
		MessageResponse response = new MessageResponse("You have successfully edited the task");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
	
	@Transactional
	@DeleteMapping("")
	public ResponseEntity<MessageResponse> deleteTask(@RequestParam int id, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		TaskEntity task = taskRepository.findById(id);
		
		if(task == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task is not found!");
		}
		
		taskRepository.delete(task);
		MessageResponse message = new MessageResponse("You have successfully deleted the task");
		return new ResponseEntity<MessageResponse>(message, HttpStatus.OK);
	}
}