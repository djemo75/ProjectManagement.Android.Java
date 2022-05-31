package uni.djem.management.controllers;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uni.djem.management.RequestDtos.CreateCommentRequest;
import uni.djem.management.ResponseDtos.MessageResponse;
import uni.djem.management.entities.CommentEntity;
import uni.djem.management.entities.TaskEntity;
import uni.djem.management.entities.UserEntity;
import uni.djem.management.repositories.CommentRepository;
import uni.djem.management.repositories.TaskRepository;

@RequestMapping(path="/comments")
@RestController
public class CommentController {
	private CommentRepository commentRepository;
	private TaskRepository taskRepository;
	
	public CommentController(CommentRepository commentRepository, TaskRepository taskRepository) {
		this.commentRepository=commentRepository;
		this.taskRepository=taskRepository;
	}
	
	@GetMapping("/")
	public ResponseEntity<CommentEntity> getComment(@RequestParam int id) {
		CommentEntity comment = commentRepository.findById(id);
		
		if(comment == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment is not found!");
		}

		return new ResponseEntity<CommentEntity>(comment, HttpStatus.OK);
	}
	
	@GetMapping("/task")
	public ResponseEntity<List<CommentEntity>> getCommentsByTaskId(@RequestParam int taskId) {
		TaskEntity task = taskRepository.findById(taskId);
		
		if(task == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task is not found!");
		}
		
		List<CommentEntity> comments = commentRepository.findAllByTaskId(taskId);

		return new ResponseEntity<List<CommentEntity>>(comments, HttpStatus.OK);
	}
	
	@PostMapping("/")
	public ResponseEntity<CommentEntity> createComment(@RequestBody CreateCommentRequest commentRequest, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		if(commentRequest.getType()=="text" & commentRequest.getContent() == "") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide content for comment with type text!");
		}
		
		if(commentRequest.getType()=="image" & commentRequest.getResourcePath() == "") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide resource path for comment with type image!");
		}
		
		TaskEntity task = taskRepository.findById(commentRequest.getTaskId());
		
		if(task==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task is not found!");
		}
		
		CommentEntity comment = new CommentEntity();
		comment.setType(commentRequest.getType());
		comment.setContent(commentRequest.getContent());
		comment.setResourcePath(commentRequest.getResourcePath());
		comment.setTask(task);
		comment.setAuthor(user);
		comment.setCreatedDate(new Date());
		
		commentRepository.saveAndFlush(comment);
		return new ResponseEntity<CommentEntity>(comment, HttpStatus.OK);
	}
	
	@DeleteMapping("")
	public ResponseEntity<MessageResponse> deleteComment(@RequestParam int id, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		CommentEntity comment = commentRepository.findById(id);
		
		if(comment == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment is not found!");
		}
		
		if(comment.getAuthor().getId() != user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete other comments!");
		}
		
		commentRepository.delete(comment);
		MessageResponse message = new MessageResponse("You have successfully deleted the comment");
		return new ResponseEntity<MessageResponse>(message, HttpStatus.OK);
	}
}