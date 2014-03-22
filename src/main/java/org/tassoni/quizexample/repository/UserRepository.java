package org.tassoni.quizexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tassoni.quizexample.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	public User findByUsernameAndPassword(String username, String password);
}
