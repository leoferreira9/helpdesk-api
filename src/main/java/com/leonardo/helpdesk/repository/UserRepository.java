package com.leonardo.helpdesk.repository;

import com.leonardo.helpdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {}
