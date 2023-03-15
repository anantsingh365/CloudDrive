package com.anant.CloudDrive.repository;

import com.anant.CloudDrive.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}