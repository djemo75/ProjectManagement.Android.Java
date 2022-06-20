package uni.djem.management.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import uni.djem.management.ResponseDtos.FileUploadResponse;
import uni.djem.management.ResponseDtos.MessageResponse;
import uni.djem.management.entities.CommentEntity;
import uni.djem.management.entities.FileEntity;
import uni.djem.management.entities.ProjectEntity;
import uni.djem.management.entities.TaskEntity;
import uni.djem.management.entities.UserEntity;
import uni.djem.management.repositories.FileRepository;
import uni.djem.management.repositories.ProjectRepository;
import uni.djem.management.repositories.TaskRepository;
import uni.djem.management.repositories.UserRepository;
import uni.djem.management.utils.FileDownloadUtil;

import org.apache.commons.lang3.RandomStringUtils;
 
@RequestMapping(path="/files")
@RestController
public class FileController {
	private ProjectRepository projectRepository;
	private FileRepository fileRepository;
	
	public FileController(ProjectRepository projectRepository, FileRepository fileRepository) {
		this.projectRepository=projectRepository;
		this.fileRepository=fileRepository;
	}
	
	@Operation(summary = "Download a file")
	@GetMapping("/downloadFile/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {
        FileDownloadUtil downloadUtil = new FileDownloadUtil();
         
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(fileCode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
         
        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }
         
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
         
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);       
    }
	
	@Operation(summary = "Get files by project id")
	@GetMapping("/")
    public ResponseEntity<List<FileEntity>> getFilesByProjectId(@RequestParam int projectId) {
		List<FileEntity> files = fileRepository.findAllByProjectId(projectId);

		return new ResponseEntity<List<FileEntity>>(files, HttpStatus.OK);  
    }
     
	@Operation(summary = "Upload a file")
    @PostMapping("/uploadFile")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
         
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()).replace(' ', '-');
        long size = multipartFile.getSize();
         
        Path uploadPath = Paths.get("Files-Upload");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
 
        String fileCode = RandomStringUtils.randomAlphanumeric(8);
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileCode + "-" + fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {       
            throw new IOException("Could not save file: " + fileName, ioe);
        }
         
        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setSize(size);
        response.setDownloadUri("/downloadFile/" + fileCode);
        response.setPath("/Files-Upload/" + fileCode + "-" + fileName);         
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	@Operation(summary = "Upload and save a file")
    @PostMapping("/uploadAndSaveFile")
    public ResponseEntity<FileEntity> uploadAndSaveFile(HttpSession session,
    		@RequestParam("file") MultipartFile multipartFile,
    		@RequestParam("projectId") int projectId)
                    throws IOException {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		ProjectEntity project = projectRepository.findById(projectId);
		
		if(project == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project is not found!");
		}
         
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()).replace(' ', '-');
         
        Path uploadPath = Paths.get("Files-Upload");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
 
        String fileCode = RandomStringUtils.randomAlphanumeric(8);
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileCode + "-" + fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {       
            throw new IOException("Could not save file: " + fileName, ioe);
        }
        
        FileEntity file = new FileEntity();
        file.setFileName(fileName);
        file.setFileCode(fileCode);
        file.setContentType(multipartFile.getContentType());
        file.setPath("/Files-Upload/" + fileCode + "-" + fileName);   
        file.setCreatedDate(new Date());
        file.setProject(project);
        file.setUser(user);
		
		fileRepository.saveAndFlush(file);
		return new ResponseEntity<FileEntity>(file, HttpStatus.OK);
    }
	
	@Operation(summary = "Delete file")
	@DeleteMapping("")
	public ResponseEntity<MessageResponse> deleteFile(@RequestParam int id, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		FileEntity file = fileRepository.findById(id);
		
		if(file == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File is not found!");
		}
		
		if(file.getUser().getId() != user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete other files!");
		}
		
		fileRepository.delete(file);
		MessageResponse message = new MessageResponse("You have successfully deleted the file");
		return new ResponseEntity<MessageResponse>(message, HttpStatus.OK);
	}
}