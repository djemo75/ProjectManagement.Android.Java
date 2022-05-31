package uni.djem.management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.management.entities.CommentEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer>{
	CommentEntity findById(int id);
	List<CommentEntity> findAllByTaskId(int taskId);
}