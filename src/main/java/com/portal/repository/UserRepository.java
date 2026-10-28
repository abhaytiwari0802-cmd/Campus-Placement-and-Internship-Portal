package com.portal.repository;

import com.portal.model.AccountStatus;
import com.portal.model.Role;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByStatus(AccountStatus status);
    long countByRole(Role role);
    long countByStatus(AccountStatus status);
}
