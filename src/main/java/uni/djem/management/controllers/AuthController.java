package uni.djem.management.controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uni.djem.management.RequestDtos.EditUserRequest;
import uni.djem.management.RequestDtos.LoginRequest;
import uni.djem.management.RequestDtos.RegisterRequest;
import uni.djem.management.ResponseDtos.MessageResponse;
import uni.djem.management.ResponseDtos.UserDetailsResponse;
import uni.djem.management.entities.UserEntity;
import uni.djem.management.repositories.UserRepository;

@RequestMapping(path = "/auth")
@RestController
public class AuthController {
	private UserRepository userRepository;
	
	public AuthController(UserRepository userRepository) {
		this.userRepository=userRepository;
	}
	
	@PostMapping(path = "/login")
	public ResponseEntity<UserDetailsResponse> login(@RequestBody LoginRequest loginRequest, HttpSession session) throws Exception {
		if(loginRequest.getUsername()=="" || loginRequest.getPassword()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You need to provide username and password!");
		}
		
		UserEntity user = userRepository.findUserByUsernameAndPassword(loginRequest.getUsername(), hashMe(loginRequest.getPassword()));
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or password is wrong!");
		}
		
		session.setAttribute("user", user);
		UserDetailsResponse response = new UserDetailsResponse(user.getId(), user.getUsername(), user.getName());
		
		return new ResponseEntity<UserDetailsResponse>(response, HttpStatus.OK);
	}
	
	@PostMapping(path = "/register")
	public ResponseEntity<MessageResponse> register(@RequestBody RegisterRequest registerRequest, HttpSession session) throws Exception {
		UserEntity user = userRepository.findByUsername(registerRequest.getUsername());
		
		if(registerRequest.getUsername()=="" || registerRequest.getPassword()=="" ||
		   registerRequest.getName()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You need to provide content for required fields!");
		}
		
		if(user!=null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Username exist!");
		}
		
		UserEntity newUser = new UserEntity(registerRequest.getUsername(),
											hashMe(registerRequest.getPassword()),
											registerRequest.getName());
		userRepository.saveAndFlush(newUser);
		MessageResponse response = new MessageResponse("You have successfully registered!");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
	
	@PostMapping(path="/logout")
	public ResponseEntity<MessageResponse> logout(HttpSession session) {
		session.invalidate();
		MessageResponse response = new MessageResponse("You have successfully logged out!");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
	
	@GetMapping(path = "/profile")
	public ResponseEntity<UserDetailsResponse> profile(HttpSession session) {
		UserEntity userSession = (UserEntity) session.getAttribute("user");
		
		if(userSession==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		UserEntity user = userRepository.findByUsername(userSession.getUsername());
		
		UserDetailsResponse response = new UserDetailsResponse(user.getId(), user.getUsername(), user.getName());
		
		return new ResponseEntity<UserDetailsResponse>(response, HttpStatus.OK);
	}
	
	@PutMapping("/edit-profile")
	public ResponseEntity<MessageResponse> editUser(@RequestBody EditUserRequest userRequest, HttpSession session) {
		UserEntity userSession = (UserEntity)session.getAttribute("user");
		
		if(userSession==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		UserEntity user = userRepository.findByUsername(userSession.getUsername());
		
		if(userRequest.getName()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide name!");
		}
		
		user.setName(userRequest.getName());
		
		userRepository.save(user);
		
		MessageResponse response = new MessageResponse("You have successfully edited the profile");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
	
	private String hashMe(String password) {		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			md.update(password.getBytes());
			
			byte[] arr = md.digest();
			
			StringBuilder hash = new StringBuilder();
			
			for(int i = 0; i < arr.length; i++) {
				hash.append((char)arr[i]);
			}
			
			return hash.toString();
			
		} catch (NoSuchAlgorithmException e) {			
			e.printStackTrace();
		}		
				
		return null;
	}
}
