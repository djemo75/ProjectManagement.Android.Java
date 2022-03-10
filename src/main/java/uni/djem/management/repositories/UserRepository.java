package uni.djem.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.management.entities.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer>{
	UserEntity findById(int id);
	UserEntity findByUsername(String username);
	UserEntity findUserByUsernameAndPassword(String username, String password);
}