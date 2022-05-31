package uni.djem.management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.management.entities.TaskEntity;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Integer>{
	TaskEntity findById(int id);
	List<TaskEntity> findAllByProjectId(int projectId);
	List<TaskEntity> findAllByAssignedUserId(int userId);
}