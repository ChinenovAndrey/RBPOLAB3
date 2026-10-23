package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.example.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}