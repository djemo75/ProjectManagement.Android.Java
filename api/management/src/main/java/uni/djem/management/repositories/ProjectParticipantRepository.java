package uni.djem.management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.management.entities.ProjectEntity;
import uni.djem.management.entities.ProjectParticipantEntity;

@Repository
public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipantEntity, Integer>{
	ProjectParticipantEntity findByUserIdAndProjectId(int userId, int projectId);
	List<ProjectParticipantEntity> findAllByProjectIdOrderByCreatedDateDesc(int projectId);
	List<ProjectParticipantEntity> findAllByUserIdOrderByCreatedDateDesc(int userId);
	void deleteByProjectId(int projectId);
	void deleteByUserId(int userId);
}