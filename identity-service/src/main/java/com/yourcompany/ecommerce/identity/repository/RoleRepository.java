package com.yourcompany.ecommerce.identity.repository;

import com.yourcompany.ecommerce.identity.model.ERole;
import com.yourcompany.ecommerce.identity.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}