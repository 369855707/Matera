package com.maternity.repository;

import com.maternity.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findByWechatOpenId(String wechatOpenId);
    Boolean existsByWechatOpenId(String wechatOpenId);
    Optional<User> findByPhone(String phone);
    List<User> findByRole(User.UserRole role);
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findByPhoneContaining(String phone);
    long countByRole(User.UserRole role);
}
