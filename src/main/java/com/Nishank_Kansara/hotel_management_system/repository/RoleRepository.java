package com.Nishank_Kansara.hotel_management_system.repository;

import com.Nishank_Kansara.hotel_management_system.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String role);

    boolean existsByName(String roleName);
}
