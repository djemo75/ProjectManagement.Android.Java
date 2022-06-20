package uni.djem.management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.management.entities.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer>{
	FileEntity findById(int id);
	List<FileEntity> findAllByProjectId(int projectId);
	List<FileEntity> findAllByUserId(int userId);
}