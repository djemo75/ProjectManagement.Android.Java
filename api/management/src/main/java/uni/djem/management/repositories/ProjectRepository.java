package uni.djem.management.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.management.entities.ProjectEntity;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Integer>{
	ProjectEntity findById(int id);
}